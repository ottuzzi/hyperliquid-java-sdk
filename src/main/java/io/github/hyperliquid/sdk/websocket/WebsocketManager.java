package io.github.hyperliquid.sdk.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.hyperliquid.sdk.model.subscription.Subscription;
import io.github.hyperliquid.sdk.utils.JSONUtil;
import okhttp3.*;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * WebSocket 管理器。
 * 管理连接、订阅、心跳与消息分发。
 */
public class WebsocketManager {

    /**
     * 日志记录器
     */
    private static final Logger LOG = Logger.getLogger(WebsocketManager.class.getName());

    /**
     * 原始 API 根地址（http/https），用于网络可用性探测
     */
    private final String baseUrl;
    /**
     * WebSocket 连接地址（从 baseUrl 推导）
     */
    private final String wsUrl;
    /**
     * 网络探测地址（可选，设置后覆盖 baseUrl 进行探测）
     */
    private String probeUrl;
    /**
     * 是否禁用网络探测（禁用后视为始终可用）
     */
    private boolean probeDisabled = false;
    /**
     * WebSocket 主客户端（用于建立与管理 WS 连接）
     */
    private final OkHttpClient client;
    /**
     * 当前 WebSocket 连接实例
     */
    private WebSocket webSocket;
    /**
     * 是否已停止管理器（停止后不再进行重连）
     */
    private volatile boolean stopped = false;
    /**
     * 当前连接是否已建立
     */
    private volatile boolean connected = false;

    /**
     * 已尝试的重连次数
     */
    private int reconnectAttempts = 0;
    /**
     * 当前重连延迟毫秒数（指数退避）初始 1s（可配置）
     */
    private long backoffMs = 1_000L;
    /**
     * 初始重连延迟毫秒（连接成功后会重置为该值）
     */
    private long initialBackoffMs = backoffMs;
    /**
     * 内部最大退避上限毫秒（固定 30s）
     */
    private final long maxBackoffMs = 30_000L;
    /**
     * 外部配置的最大退避上限毫秒（不超过内部上限）
     */
    private long configMaxBackoffMs = maxBackoffMs;
    /**
     * 计划中的重连任务引用
     */
    private volatile ScheduledFuture<?> reconnectFuture;


    /**
     * 当前网络探测状态（true 表示可用）
     */
    private volatile boolean networkAvailable = true;
    /**
     * 网络状态检查的间隔秒数（默认 5 秒）
     */
    private int networkCheckIntervalSeconds = 5;
    /**
     * 网络监控任务引用（断线时周期探测）
     */
    private volatile ScheduledFuture<?> networkMonitorFuture;
    /**
     * 网络探测用轻量 HTTP 客户端（短超时）
     */
    private final OkHttpClient networkClient;

    /**
     * 活跃订阅集合，按标识符分组存储并去重
     */
    private final Map<String, List<ActiveSubscription>> subscriptions = new ConcurrentHashMap<>();
    /**
     * 标识符缓存（优化字符串拼接性能）
     */
    private final Map<String, String> identifierCache = new ConcurrentHashMap<>();
    /**
     * 定时任务调度器（用于心跳、重连与网络监控）
     */
    private final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);

    /**
     * 连接状态监听器：用于通知连接、断开、重连与网络状态变化。
     */
    public interface ConnectionListener {
        /**
         * 正在建立连接（包含重连过程）
         */
        void onConnecting(String url);

        /**
         * 连接已建立
         */
        void onConnected(String url);

        /**
         * 连接断开（code/reason/cause 可能为空）
         */
        void onDisconnected(String url, int code, String reason, Throwable cause);

        /**
         * 进入重连：attempt 为本次尝试序号（从 1 开始），nextDelayMs 为下一次尝试的延迟毫秒数
         */
        void onReconnecting(String url, int attempt, long nextDelayMs);

        /**
         * 重连失败：超过最大次数
         */
        void onReconnectFailed(String url, int attempted, Throwable lastError);

        /**
         * 网络不可用
         */
        void onNetworkUnavailable(String url);

        /**
         * 网络恢复可用
         */
        void onNetworkAvailable(String url);
    }

    /**
     * 连接监听器集合（线程安全）
     */
    private final List<ConnectionListener> connectionListeners = Collections.synchronizedList(new ArrayList<>());

    /**
     * 回调接口
     */
    public interface MessageCallback {
        void onMessage(JsonNode msg);
    }

    /**
     * 回调异常监听接口。
     * 当用户回调抛出异常时，框架会捕获并通知此监听器。
     */
    public interface CallbackErrorListener {
        /**
         * 用户回调抛出异常时触发。
         *
         * @param url        当前 WebSocket URL
         * @param identifier 订阅标识符（由 subscriptionToIdentifier/wsMsgToIdentifier 生成）
         * @param message    导致异常的消息（原始 JSON）
         * @param error      异常对象
         */
        void onCallbackError(String url, String identifier, JsonNode message, Throwable error);
    }

    /**
     * 活跃订阅记录
     */
    public static class ActiveSubscription {
        public final JsonNode subscription;
        public final MessageCallback callback;

        public ActiveSubscription(JsonNode s, MessageCallback c) {
            this.subscription = s;
            this.callback = c;
        }
    }

    /**
     * 回调异常监听器集合（线程安全）
     */
    private final List<CallbackErrorListener> callbackErrorListeners = Collections.synchronizedList(new ArrayList<>());

    /**
     * 构造 WebSocket 管理器。
     *
     * @param baseUrl API 根地址（http/https），会自动转换为 ws/wss
     */
    public WebsocketManager(String baseUrl) {
        this.baseUrl = baseUrl;
        String scheme = baseUrl.startsWith("https") ? "wss" : "ws";
        String tail = baseUrl.replaceFirst("https?", "");
        this.wsUrl = scheme + tail + "/ws";
        this.probeUrl = null;
        this.client = new OkHttpClient.Builder()
                .pingInterval(Duration.ofSeconds(20))
                .readTimeout(Duration.ofSeconds(0)) // WebSocket 不设 readTimeout
                .build();
        // 用于网络连通性检查的轻量客户端（短超时）
        this.networkClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(3))
                .readTimeout(Duration.ofSeconds(3))
                .callTimeout(Duration.ofSeconds(5))
                .build();
        connect();
        startPing();
    }

    /**
     * 建立（或重建）WebSocket 连接。
     * 会在 onOpen 中自动重新发送所有订阅。
     */
    private void connect() {
        notifyConnecting();
        Request request = new Request.Builder().url(wsUrl).build();
        this.webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                connected = true;
                reconnectAttempts = 0;
                backoffMs = initialBackoffMs;
                stopNetworkMonitor();
                notifyConnected();
                // 重新订阅
                for (List<ActiveSubscription> list : subscriptions.values()) {
                    for (ActiveSubscription sub : list) {
                        sendSubscribe(sub.subscription);
                    }
                }
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                try {
                    JsonNode msg = JSONUtil.readTree(text);
                    String identifier = wsMsgToIdentifier(msg);
                    if (identifier != null && subscriptions.containsKey(identifier)) {
                        for (ActiveSubscription sub : subscriptions.get(identifier)) {
                            try {
                                sub.callback.onMessage(msg);
                            } catch (Exception cbEx) {
                                // 记录日志并触发异常监听器，不影响其他订阅回调
                                LOG.log(Level.WARNING, "WebSocket 回调异常，identifier=" + identifier, cbEx);
                                notifyCallbackError(identifier, msg, cbEx);
                            }
                        }
                    }
                } catch (IOException e) {
                    // ignore parsing error
                }
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
                onMessage(webSocket, bytes.utf8());
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, Response response) {
                connected = false;
                notifyDisconnected(-1, String.valueOf(t), t);
                if (!stopped) {
                    scheduleReconnect(t, null, null);
                }
            }

            @Override
            public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                webSocket.close(code, reason);
            }

            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                connected = false;
                notifyDisconnected(code, reason, null);
                if (!stopped) {
                    scheduleReconnect(null, code, reason);
                }
            }
        });
    }

    private void startPing() {
        scheduler.scheduleAtFixedRate(this::sendPing, 20, 20, TimeUnit.SECONDS);
    }

    /**
     * 发送 ping 消息（内部方法，由定时器自动调用）
     */
    private void sendPing() {
        if (webSocket != null && connected) {
            Map<String, Object> payload = Map.of("method", "ping");
            try {
                webSocket.send(JSONUtil.writeValueAsString(payload));
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 停止并关闭连接
     */
    public void stop() {
        stopped = true;

        // 先取消所有计划任务
        cancelTask(reconnectFuture);
        cancelTask(networkMonitorFuture);

        // 关闭 WebSocket 连接
        if (webSocket != null) {
            try {
                webSocket.close(1000, "stop");
            } catch (Exception ignored) {
            }
            webSocket = null;
        }

        // 优雅关闭调度器
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // 关闭 OkHttpClient 资源（释放连接池和线程池）
        try {
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
        } catch (Exception ignored) {
        }

        try {
            networkClient.dispatcher().executorService().shutdown();
            networkClient.connectionPool().evictAll();
        } catch (Exception ignored) {
        }
    }

    /**
     * 安排一次重连尝试（带指数退避，无限重试直到成功）。
     * 初始 1s，最大 30s，无限重试；同时启动网络监控，当网络恢复时将立即触发一次重连。
     */
    private synchronized void scheduleReconnect(Throwable cause, Integer code, String reason) {
        if (stopped)
            return;
        // 保守地关闭旧连接资源
        if (webSocket != null) {
            try {
                webSocket.close(1001, "reconnect");
            } catch (Exception ignored) {
            }
            webSocket = null;
        }

        long nextDelay = backoffMs + (long) (Math.random() * 250L); // 少量抖动
        notifyReconnecting(reconnectAttempts + 1, nextDelay);

        cancelTask(reconnectFuture);
        reconnectFuture = scheduler.schedule(() -> {
            if (!stopped) {
                connect();
            }
        }, nextDelay, TimeUnit.MILLISECONDS);

        reconnectAttempts++;
        // 退避增长受两层上限约束：内部上限与外部配置上限
        backoffMs = Math.min(Math.min(maxBackoffMs, configMaxBackoffMs), backoffMs * 2);

        startNetworkMonitor();
    }

    /**
     * 启动网络状态监控（仅在断线时运行）
     */
    private synchronized void startNetworkMonitor() {
        if (networkMonitorFuture != null && !networkMonitorFuture.isCancelled())
            return;
        networkMonitorFuture = scheduler.scheduleWithFixedDelay(() -> {
            boolean ok = isNetworkAvailable();
            if (ok) {
                if (!networkAvailable) {
                    networkAvailable = true;
                    notifyNetworkAvailable();
                }
                // 网络可用且当前未连接：尝试快速重连（重置退避与次数）
                if (!connected && !stopped) {
                    backoffMs = initialBackoffMs;
                    // 网络恢复后重置计数器，允许重新开始重连尝试
                    reconnectAttempts = 0;
                    cancelTask(reconnectFuture);
                    notifyReconnecting(1, 0);
                    reconnectFuture = scheduler.schedule(this::connect, 0, TimeUnit.MILLISECONDS);
                }
            } else {
                if (networkAvailable) {
                    networkAvailable = false;
                    notifyNetworkUnavailable();
                }
            }
        }, 0, networkCheckIntervalSeconds, TimeUnit.SECONDS);
    }

    /**
     * 停止网络状态监控
     */
    private synchronized void stopNetworkMonitor() {
        if (networkMonitorFuture != null) {
            networkMonitorFuture.cancel(false);
            networkMonitorFuture = null;
        }
        networkAvailable = true; // 停止监控时默认视为可用
    }

    /**
     * 增强的网络可用性探测：HEAD 请求 baseUrl，允许 2xx/3xx，支持重试
     */
    private boolean isNetworkAvailable() {
        if (probeDisabled) {
            return true;
        }
        String url = probeUrl != null ? probeUrl : baseUrl;
        int maxRetries = 2;
        long retryDelayMs = 100;

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                Request req = new Request.Builder().url(url).head().build();
                try (Response resp = networkClient.newCall(req).execute()) {
                    if (resp.code() < 400) {
                        return true;
                    }
                }
            } catch (Exception e) {
                // 最后一次尝试失败才返回 false
                if (attempt == maxRetries - 1) {
                    LOG.log(Level.FINE, "网络探测失败，已重试 " + maxRetries + " 次", e);
                    return false;
                }
                // 非最后一次尝试，等待后重试
                try {
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }

    public void setNetworkProbeUrl(String url) {
        this.probeUrl = url;
    }

    public void setNetworkProbeDisabled(boolean disabled) {
        this.probeDisabled = disabled;
    }

    /**
     * 添加连接状态监听器
     */
    public void addConnectionListener(ConnectionListener l) {
        if (l != null)
            connectionListeners.add(l);
    }

    /**
     * 移除连接状态监听器
     */
    public void removeConnectionListener(ConnectionListener l) {
        if (l != null)
            connectionListeners.remove(l);
    }

    /**
     * 添加回调异常监听器
     */
    public void addCallbackErrorListener(CallbackErrorListener l) {
        if (l != null)
            callbackErrorListeners.add(l);
    }

    /**
     * 移除回调异常监听器
     */
    public void removeCallbackErrorListener(CallbackErrorListener l) {
        if (l != null)
            callbackErrorListeners.remove(l);
    }

    /**
     * 设置网络监控检查间隔秒数（默认 5）
     */
    public void setNetworkCheckIntervalSeconds(int seconds) {
        this.networkCheckIntervalSeconds = Math.max(1, seconds);
    }

    /**
     * 设置重连指数退避参数。
     *
     * @param initialMs 初始重连延迟毫秒（建议 500ms~2000ms）
     * @param maxMs     最大重连延迟毫秒（建议不超过 30000ms）
     */
    public void setReconnectBackoffMs(long initialMs, long maxMs) {
        long init = Math.max(100, initialMs);
        long max = Math.max(init, maxMs);
        this.initialBackoffMs = init;
        this.backoffMs = init;
        this.configMaxBackoffMs = Math.min(maxBackoffMs, max);
    }

    /**
     * 安全取消定时任务
     */
    private void cancelTask(ScheduledFuture<?> future) {
        if (future != null && !future.isCancelled()) {
            future.cancel(false);
        }
    }

    /**
     * 通用监听器通知方法（防御式，单个监听异常不影响其它监听）
     */
    private <T> void notifyListeners(List<T> listeners, java.util.function.Consumer<T> action) {
        synchronized (listeners) {
            for (T listener : listeners) {
                try {
                    action.accept(listener);
                } catch (Exception ignored) {
                }
            }
        }
    }

    // 监听器通知封装（使用通用方法减少重复代码）
    private void notifyConnecting() {
        notifyListeners(connectionListeners, l -> l.onConnecting(wsUrl));
    }

    private void notifyConnected() {
        notifyListeners(connectionListeners, l -> l.onConnected(wsUrl));
    }

    private void notifyDisconnected(int code, String reason, Throwable cause) {
        notifyListeners(connectionListeners, l -> l.onDisconnected(wsUrl, code, reason, cause));
    }

    private void notifyReconnecting(int attempt, long nextDelayMs) {
        notifyListeners(connectionListeners, l -> l.onReconnecting(wsUrl, attempt, nextDelayMs));
    }

    private void notifyReconnectFailed(int attempted, Throwable lastError) {
        notifyListeners(connectionListeners, l -> l.onReconnectFailed(wsUrl, attempted, lastError));
    }

    private void notifyNetworkUnavailable() {
        notifyListeners(connectionListeners, l -> l.onNetworkUnavailable(wsUrl));
    }

    private void notifyNetworkAvailable() {
        notifyListeners(connectionListeners, l -> l.onNetworkAvailable(wsUrl));
    }

    /**
     * 通知：用户回调异常
     */
    private void notifyCallbackError(String identifier, JsonNode msg, Throwable error) {
        notifyListeners(callbackErrorListeners, l -> l.onCallbackError(wsUrl, identifier, msg, error));
    }

    /**
     * 订阅消息（类型安全版本，使用 Subscription 实体类）。
     *
     * @param subscription 订阅对象（Subscription 实体类）
     * @param callback     回调
     */
    public void subscribe(Subscription subscription, MessageCallback callback) {
        if (stopped) {
            throw new IllegalStateException("WebsocketManager has been stopped, cannot subscribe");
        }
        if (subscription == null) {
            throw new IllegalArgumentException("subscription cannot be null");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }

        // 将 Subscription 对象转换为 JsonNode
        JsonNode jsonNode = JSONUtil.convertValue(subscription, JsonNode.class);
        subscribe(jsonNode, callback);
    }

    /**
     * 订阅消息（兼容版本，使用 JsonNode）。
     *
     * @param subscription 订阅对象
     * @param callback     回调
     */
    public void subscribe(JsonNode subscription, MessageCallback callback) {
        if (stopped) {
            throw new IllegalStateException("WebsocketManager has been stopped, cannot subscribe");
        }
        if (subscription == null) {
            throw new IllegalArgumentException("subscription cannot be null");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }

        String identifier = subscriptionToIdentifier(subscription);
        List<ActiveSubscription> list = subscriptions.computeIfAbsent(identifier, k -> new CopyOnWriteArrayList<>());
        for (ActiveSubscription s : list) {
            if (s.subscription.equals(subscription)) {
                return;
            }
        }
        list.add(new ActiveSubscription(subscription, callback));
        sendSubscribe(subscription);
    }

    private void sendSubscribe(JsonNode subscription) {
        if (webSocket == null || !connected) {
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("method", "subscribe");
        payload.put("subscription", subscription);
        try {
            webSocket.send(JSONUtil.writeValueAsString(payload));
        } catch (Exception ignored) {
        }
    }

    /**
     * 取消订阅（类型安全版本，使用 Subscription 实体类）。
     *
     * @param subscription 订阅对象（Subscription 实体类）
     */
    public void unsubscribe(Subscription subscription) {
        if (subscription == null) {
            throw new IllegalArgumentException("subscription cannot be null");
        }

        // 将 Subscription 对象转换为 JsonNode
        JsonNode jsonNode = JSONUtil.convertValue(subscription, JsonNode.class);
        unsubscribe(jsonNode);
    }

    /**
     * 取消订阅（兼容版本，使用 JsonNode）。
     *
     * @param subscription 订阅对象
     */
    public void unsubscribe(JsonNode subscription) {
        if (subscription == null) {
            throw new IllegalArgumentException("subscription cannot be null");
        }

        String identifier = subscriptionToIdentifier(subscription);
        List<ActiveSubscription> list = subscriptions.get(identifier);
        if (list != null) {
            list.removeIf(s -> s.subscription.equals(subscription));
            if (list.isEmpty())
                subscriptions.remove(identifier);
        }

        if (webSocket == null || !connected) {
            return;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("method", "unsubscribe");
        payload.put("subscription", subscription);
        try {
            webSocket.send(JSONUtil.writeValueAsString(payload));
        } catch (Exception ignored) {
        }
    }

    /**
     * 将订阅对象转换为标识符（用于多订阅去重与路由）。
     * <p>
     * 该方法为包级可见，主要供内部使用和单元测试。
     * 频道标识符约定：
     * - l2Book:{coin}，trades:{coin}，bbo:{coin}，candle:{coin},{interval}
     * - userEvents，orderUpdates，allMids
     * -
     * userFills:{user}，userFundings:{user}，userNonFundingLedgerUpdates:{user}，webData2:{user}
     * - activeAssetCtx:{coin}，activeAssetData:{coin},{user}
     * - coin 可为字符串或整数；字符串统一转换为小写。
     * </p>
     */
    String subscriptionToIdentifier(JsonNode subscription) {
        if (subscription == null || !subscription.has("type"))
            return "unknown";
        String type = subscription.get("type").asText();
        switch (type) {
            case "allMids":
            case "userEvents":
            case "orderUpdates":
                return type;
            case "l2Book": {
                JsonNode coinNode = subscription.get("coin");
                String coinKey = extractCoinIdentifier(coinNode);
                return buildCachedIdentifier(type, coinKey);
            }
            case "trades":
            case "bbo":
            case "activeAssetCtx": {
                JsonNode coinNode = subscription.get("coin");
                String coinKey = extractCoinIdentifier(coinNode);
                return buildCachedIdentifier(type, coinKey);
            }
            case "candle": {
                JsonNode coinNode = subscription.get("coin");
                JsonNode iNode = subscription.get("interval");
                String coinKey = extractCoinIdentifier(coinNode);
                String interval = iNode == null ? null : iNode.asText();
                if (coinKey != null && interval != null)
                    return buildCachedIdentifier(type, coinKey + "," + interval);
                return type;
            }
            case "userFills":
            case "userFundings":
            case "userNonFundingLedgerUpdates":
            case "webData2": {
                JsonNode userNode = subscription.get("user");
                String user = userNode == null ? null : userNode.asText().toLowerCase(Locale.ROOT);
                return buildCachedIdentifier(type, user);
            }
            case "activeAssetData": {
                JsonNode coinNode = subscription.get("coin");
                JsonNode userNode = subscription.get("user");
                String coinKey = extractCoinIdentifier(coinNode);
                String user = userNode == null ? null : userNode.asText().toLowerCase(Locale.ROOT);
                if (coinKey != null && user != null)
                    return buildCachedIdentifier(type, coinKey + "," + user);
                return type;
            }
            default:
                return type;
        }
    }

    /**
     * 提取 Coin 标识符（封装重复逻辑）
     */
    private String extractCoinIdentifier(JsonNode coinNode) {
        if (coinNode == null) return null;
        return coinNode.isNumber()
                ? String.valueOf(coinNode.asInt())
                : coinNode.asText().toLowerCase(Locale.ROOT);
    }

    /**
     * 构建带缓存的标识符（优化字符串拼接性能）
     */
    private String buildCachedIdentifier(String type, String suffix) {
        if (suffix == null) return type;

        String cacheKey = type + "|" + suffix;
        return identifierCache.computeIfAbsent(cacheKey, k -> type + ":" + suffix);
    }

    /**
     * 从消息中提取标识符，与 subscriptionToIdentifier 对应。
     * <p>
     * 该方法为包级可见，主要供内部使用和单元测试。
     * 兼容两种消息格式：channel 为字符串或对象（{type: ..., ...}）。
     * </p>
     */
    String wsMsgToIdentifier(JsonNode msg) {
        if (msg == null || !msg.has("channel"))
            return null;
        JsonNode channelNode = msg.get("channel");
        String type = null;
        if (channelNode.isTextual()) {
            type = channelNode.asText();
        } else if (channelNode.isObject() && channelNode.has("type")) {
            type = channelNode.get("type").asText();
        }
        if (type == null)
            return null;
        switch (type) {
            case "pong":
            case "allMids":
            case "userEvents":
            case "orderUpdates":
                return type;
            case "l2Book": {
                JsonNode coinNode = msg.path("data").path("coin");
                String coinKey = extractCoinIdentifier(coinNode);
                return buildCachedIdentifier(type, coinKey);
            }
            case "trades": {
                JsonNode trades = msg.get("data");
                String coinKey = null;
                if (trades != null && trades.isArray() && !trades.isEmpty()) {
                    JsonNode first = trades.get(0);
                    JsonNode coinNode = first.get("coin");
                    if (coinNode != null) {
                        coinKey = extractCoinIdentifier(coinNode);
                    }
                }
                return buildCachedIdentifier(type, coinKey);
            }
            case "candle": {
                JsonNode data = msg.get("data");
                if (data != null) {
                    String s = data.path("s").asText(null);
                    String i = data.path("i").asText(null);
                    if (s != null && i != null)
                        return buildCachedIdentifier(type, s.toLowerCase(Locale.ROOT) + "," + i);
                }
                return type;
            }
            case "bbo": {
                JsonNode coinNode = msg.path("data").path("coin");
                String coinKey = extractCoinIdentifier(coinNode);
                return buildCachedIdentifier(type, coinKey);
            }
            case "userFills":
            case "userFundings":
            case "userNonFundingLedgerUpdates":
            case "webData2": {
                JsonNode userNode = msg.path("data").path("user");
                String user = (userNode != null && userNode.isTextual()) ? userNode.asText().toLowerCase(Locale.ROOT)
                        : null;
                return buildCachedIdentifier(type, user);
            }
            case "activeAssetCtx":
            case "activeSpotAssetCtx": {
                JsonNode coinNode = msg.path("data").path("coin");
                String coinKey = extractCoinIdentifier(coinNode);
                return type.equals("activeSpotAssetCtx")
                        ? buildCachedIdentifier("activeAssetCtx", coinKey != null ? coinKey : "unknown")
                        : buildCachedIdentifier(type, coinKey);
            }
            case "activeAssetData": {
                JsonNode data = msg.get("data");
                if (data != null) {
                    JsonNode coinNode = data.get("coin");
                    JsonNode userNode = data.get("user");
                    String coinKey = extractCoinIdentifier(coinNode);
                    String user = (userNode != null && userNode.isTextual())
                            ? userNode.asText().toLowerCase(Locale.ROOT)
                            : null;
                    if (coinKey != null && user != null)
                        return buildCachedIdentifier(type, coinKey + "," + user);
                }
                return type;
            }
            default:
                return type;
        }
    }
}
