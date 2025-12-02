import com.fasterxml.jackson.databind.JsonNode;
import io.github.hyperliquid.sdk.HyperliquidClient;
import io.github.hyperliquid.sdk.apis.Info;
import io.github.hyperliquid.sdk.utils.JSONUtil;
import io.github.hyperliquid.sdk.websocket.WebsocketManager;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket 订阅 BTC 行情示例
 * <p>
 * 演示功能：
 * 1. 订阅 BTC L2 订单簿（实时盘口数据）
 * 2. 订阅 BTC 实时成交（Trades）
 * 3. 订阅 BTC K线数据（1分钟周期）
 * 4. 添加连接状态监听器
 * 5. 优雅关闭 WebSocket 连接
 * </p>
 */
public class ExampleWebsocketBTC {

    public static void main(String[] args) throws InterruptedException {
        // ==================== 1. 初始化客户端 ====================
        // 注意：WebSocket 订阅不需要私钥，只订阅公开行情数据
        HyperliquidClient client = HyperliquidClient.builder()
                .testNetUrl()  // 使用测试网
                .build();

        Info info = client.getInfo();

        System.out.println("=== Hyperliquid WebSocket 订阅 BTC 行情示例 ===\n");

        // ==================== 2. 添加连接状态监听器（可选）====================
        // 注意：连接状态监听器是【完全可选的】，不添加也能正常使用 WebSocket
        // 添加监听器仅用于观察连接状态，方便调试和日志记录
        info.addConnectionListener(new WebsocketManager.ConnectionListener() {
            @Override
            public void onConnecting(String url) {
                System.out.println("[连接中] WebSocket 正在连接: " + url);
            }

            @Override
            public void onConnected(String url) {
                System.out.println("[已连接] WebSocket 连接成功: " + url);
            }

            @Override
            public void onDisconnected(String url, int code, String reason, Throwable cause) {
                System.out.println("[已断开] WebSocket 断开连接 - 代码: " + code + ", 原因: " + reason);
                if (cause != null) {
                    System.out.println("[错误] 断开原因: " + cause.getMessage());
                }
            }

            @Override
            public void onReconnecting(String url, int attempt, long nextDelayMs) {
                System.out.println("[重连中] 第 " + attempt + " 次重连，延迟 " + nextDelayMs + " ms");
            }

            @Override
            public void onReconnectFailed(String url, int attempted, Throwable lastError) {
                System.out.println("[重连失败] 已尝试 " + attempted + " 次重连");
            }

            @Override
            public void onNetworkUnavailable(String url) {
                System.out.println("[网络不可用] 网络探测失败");
            }

            @Override
            public void onNetworkAvailable(String url) {
                System.out.println("[网络可用] 网络已恢复");
            }
        });

        // ==================== 3. 订阅 BTC L2 订单簿 ====================
        // WebSocket 连接在构造时已自动建立，直接订阅即可
        System.out.println("\n--- 订阅 BTC L2 订单簿 ---");
        JsonNode l2BookSub = JSONUtil.convertValue(
                Map.of("type", "l2Book", "coin", "BTC"),
                JsonNode.class
        );

        info.subscribe(l2BookSub, msg -> {
            // 解析订单簿数据
            JsonNode data = msg.get("data");
            if (data != null && data.has("levels")) {
                JsonNode levels = data.get("levels");
                if (levels.isArray() && levels.size() >= 2) {
                    JsonNode bids = levels.get(0); // 买盘
                    JsonNode asks = levels.get(1); // 卖盘
                    if (bids.isArray() && !bids.isEmpty() && asks.isArray() && !asks.isEmpty()) {
                        String bestBid = bids.get(0).get("px").asText();
                        String bestAsk = asks.get(0).get("px").asText();
                        System.out.printf("[L2订单簿] BTC 买一: %s, 卖一: %s%n", bestBid, bestAsk);
                    }
                }
            }
        });

        // ==================== 4. 订阅 BTC 实时成交 ====================
        System.out.println("--- 订阅 BTC 实时成交 ---");
        JsonNode tradesSub = JSONUtil.convertValue(
                Map.of("type", "trades", "coin", "BTC"),
                JsonNode.class
        );

        info.subscribe(tradesSub, msg -> {
            // 解析成交数据
            JsonNode data = msg.get("data");
            if (data != null && data.isArray()) {
                for (JsonNode trade : data) {
                    String price = trade.path("px").asText();
                    String size = trade.path("sz").asText();
                    String side = trade.path("side").asText();
                    long time = trade.path("time").asLong();
                    System.out.printf("[实时成交] BTC %s 成交 - 价格: %s, 数量: %s, 时间: %d%n",
                            side, price, size, time);
                }
            }
        });

        // ==================== 5. 订阅 BTC 1分钟 K线 ====================
        System.out.println("--- 订阅 BTC 1分钟 K线 ---");
        JsonNode candleSub = JSONUtil.convertValue(
                Map.of("type", "candle", "coin", "BTC", "interval", "1m"),
                JsonNode.class
        );

        info.subscribe(candleSub, msg -> {
            // 解析 K线数据
            JsonNode data = msg.get("data");
            if (data != null) {
                String open = data.path("o").asText();
                String high = data.path("h").asText();
                String low = data.path("l").asText();
                String close = data.path("c").asText();
                String volume = data.path("v").asText();
                long timestamp = data.path("t").asLong();
                System.out.printf("[K线数据] BTC 1分钟 - 开: %s, 高: %s, 低: %s, 收: %s, 量: %s, 时间: %d%n",
                        open, high, low, close, volume, timestamp);
            }
        });

        // ==================== 6. 订阅 BTC 最佳买卖价 (BBO) ====================
        System.out.println("--- 订阅 BTC 最佳买卖价 ---");
        JsonNode bboSub = JSONUtil.convertValue(
                Map.of("type", "bbo", "coin", "BTC"),
                JsonNode.class
        );

        info.subscribe(bboSub, msg -> {
            JsonNode data = msg.get("data");
            if (data != null) {
                String bidPrice = data.path("bid").path("px").asText();
                String bidSize = data.path("bid").path("sz").asText();
                String askPrice = data.path("ask").path("px").asText();
                String askSize = data.path("ask").path("sz").asText();
                System.out.printf("[BBO] BTC 买一: %s@%s, 卖一: %s@%s%n",
                        bidPrice, bidSize, askPrice, askSize);
            }
        });

        // ==================== 7. 保持运行并接收消息 ====================
        System.out.println("\n正在接收 BTC 实时行情数据，运行 60 秒后自动退出...\n");

        // 使用 CountDownLatch 等待 60 秒
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(60, TimeUnit.SECONDS);

        // ==================== 8. 优雅关闭 WebSocket ====================
        System.out.println("\n正在关闭 WebSocket 连接...");
        info.closeWs();
        System.out.println("WebSocket 已关闭，程序退出。");
    }
}
