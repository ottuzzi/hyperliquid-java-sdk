package io.github.hyperliquid.sdk.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.github.hyperliquid.sdk.model.CandleInterval;
import io.github.hyperliquid.sdk.model.info.*;
import io.github.hyperliquid.sdk.utils.HypeError;
import io.github.hyperliquid.sdk.utils.JSONUtil;
import io.github.hyperliquid.sdk.websocket.WebsocketManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Info 客户端，提供行情、订单簿、用户状态等查询。
 */
public class InfoClient {

    private final boolean skipWs;
    private WebsocketManager wsManager;
    private final HypeHttpClient hypeHttpClient;

    // 元数据缓存与映射
    private final Map<String, Integer> nameToCoin = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<Integer, Integer> coinToAsset = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<Integer, Integer> assetToSzDecimals = new java.util.concurrent.ConcurrentHashMap<>();

    // 缓存 TTL 与命中率统计
    private volatile long cacheTtlMs = 10 * 60 * 1000L; // 默认 10 分钟
    private volatile long metaLastRefreshMs = 0L;
    private volatile long spotMetaLastRefreshMs = 0L;
    private volatile long nameToCoinHits = 0L;
    private volatile long nameToCoinMisses = 0L;
    private volatile long coinToAssetHits = 0L;
    private volatile long coinToAssetMisses = 0L;


    /**
     * 构造 InfoClient 客户端。
     *
     * @param baseUrl        API 根地址
     * @param hypeHttpClient HTTP客户端实例
     * @param skipWs         是否跳过创建 WebSocket 连接（用于测试）
     */
    public InfoClient(String baseUrl, HypeHttpClient hypeHttpClient, boolean skipWs) {
        this.hypeHttpClient = hypeHttpClient;
        this.skipWs = skipWs;
        if (!skipWs) {
            this.wsManager = new WebsocketManager(baseUrl);
        }
        // 初始化：尝试刷新 perp 与 spot 元数据，填充映射
        try {
            refreshPerpMeta();
            refreshSpotMeta();
        } catch (HypeError e) {
            // 初始化失败不影响基本功能，可延迟到调用时再请求
        }
    }

    /**
     * 设置缓存 TTL（毫秒）
     */
    public void setCacheTtlMs(long ttlMs) {
        this.cacheTtlMs = Math.max(1_000L, ttlMs);
    }

    /**
     * 获取缓存命中率统计与刷新时间信息
     */
    public Map<String, Long> getCacheStats() {
        Map<String, Long> m = new LinkedHashMap<>();
        m.put("nameToCoinHits", nameToCoinHits);
        m.put("nameToCoinMisses", nameToCoinMisses);
        m.put("coinToAssetHits", coinToAssetHits);
        m.put("coinToAssetMisses", coinToAssetMisses);
        m.put("metaLastRefreshMs", metaLastRefreshMs);
        m.put("spotMetaLastRefreshMs", spotMetaLastRefreshMs);
        m.put("cacheTtlMs", cacheTtlMs);
        return m;
    }

    /**
     * 显式刷新全部元数据缓存（线程安全）
     */
    public synchronized void refreshMetadata() throws HypeError {
        nameToCoin.clear();
        coinToAsset.clear();
        assetToSzDecimals.clear();
        refreshPerpMeta();
        refreshSpotMeta();
    }

    /**
     * 刷新 perp 元数据，并更新映射与刷新时间
     */
    private void refreshPerpMeta() throws HypeError {
        JsonNode meta = meta();
        if (meta != null && meta.has("universe") && meta.get("universe").isArray()) {
            JsonNode universe = meta.get("universe");
            for (int assetId = 0; assetId < universe.size(); assetId++) {
                JsonNode a = universe.get(assetId);
                String nm = a.has("name") ? a.get("name").asText() : ("ASSET_" + assetId);
                // perp 资产使用枚举下标
                nameToCoin.putIfAbsent(nm, assetId);
                coinToAsset.putIfAbsent(assetId, assetId);
                int szDec = a.has("szDecimals") ? a.get("szDecimals").asInt() : 2;
                assetToSzDecimals.putIfAbsent(assetId, szDec);
            }
        }
        if (meta != null && meta.has("assets") && meta.get("assets").isArray()) {
            JsonNode assets = meta.get("assets");
            for (int i = 0; i < assets.size(); i++) {
                JsonNode a = assets.get(i);
                int assetId = a.has("id") ? a.get("id").asInt() : i;
                int coinId = a.has("coin") ? a.get("coin").asInt() : i;
                coinToAsset.putIfAbsent(coinId, assetId);
                int szDec = a.has("szDecimals") ? a.get("szDecimals").asInt() : 2;
                assetToSzDecimals.putIfAbsent(assetId, szDec);
            }
        }
        metaLastRefreshMs = System.currentTimeMillis();
    }

    /**
     * 刷新 spot 元数据，并更新映射与刷新时间
     */
    private void refreshSpotMeta() throws HypeError {
        JsonNode spot = spotMeta();
        if (spot != null && spot.has("universe") && spot.get("universe").isArray()) {
            JsonNode universe = spot.get("universe");
            for (int i = 0; i < universe.size(); i++) {
                JsonNode asset = universe.get(i);
                String name = asset.has("name") ? asset.get("name").asText() : ("SPOT_" + i);
                int index = asset.has("index") ? asset.get("index").asInt() : i;
                int assetId = index + 10000; // Spot 资产偏移量

                // name -> coin 的映射：优先使用返回中的 coin 字段，否则使用 index 作为占位
                if (asset.has("coin")) {
                    nameToCoin.putIfAbsent(name, asset.get("coin").asInt());
                } else {
                    nameToCoin.putIfAbsent(name, index);
                }

                // coin -> asset 映射（Spot 使用 index + 10000）
                coinToAsset.putIfAbsent(index, assetId);

                // 计算 szDecimals（取 base token 的 szDecimals）
                if (spot.has("tokens") && asset.has("tokens") && asset.get("tokens").isArray()
                        && !asset.get("tokens").isEmpty()) {
                    int baseTokenIndex = asset.get("tokens").get(0).asInt();
                    JsonNode tokens = spot.get("tokens");
                    if (tokens.isArray() && baseTokenIndex >= 0 && baseTokenIndex < tokens.size()) {
                        JsonNode baseInfo = tokens.get(baseTokenIndex);
                        int szDec = baseInfo.has("szDecimals") ? baseInfo.get("szDecimals").asInt() : 2;
                        assetToSzDecimals.putIfAbsent(assetId, szDec);
                    }
                }
            }
        }
        spotMetaLastRefreshMs = System.currentTimeMillis();
    }

    /**
     * TTL 自动刷新检查（必要时调用 refresh）
     */
    private void ensureFreshCaches() {
        long now = System.currentTimeMillis();
        if (now - metaLastRefreshMs > cacheTtlMs) {
            try {
                refreshPerpMeta();
            } catch (HypeError ignored) {
            }
        }
        if (now - spotMetaLastRefreshMs > cacheTtlMs) {
            try {
                refreshSpotMeta();
            } catch (HypeError ignored) {
            }
        }
    }

    public JsonNode postInfo(Object payload) {
        return hypeHttpClient.post("/info", payload);
    }

    /**
     * 查询所有中间价（allMids），类型化返回，可指定 perp dex 名称。
     *
     * @param dex perp dex 名称（可为空或空字符串）
     * @return 币种到中间价的映射
     */
    public Map<String, String> allMids(String dex) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "allMids");
        if (dex != null) {
            payload.put("dex", dex);
        }
        JsonNode node = postInfo(payload);
        return JSONUtil.convertValue(node, TypeFactory.defaultInstance().constructMapType(Map.class, String.class, String.class));
    }

    public Map<String, String> allMids() {
        return allMids(null);
    }

    /**
     * 查询 perp 元数据（meta）。
     */
    public JsonNode meta() {
        Map<String, Object> payload = Map.of("type", "meta");
        return postInfo(payload);
    }

    /**
     * 查询 perp 元数据（meta），可指定 perp dex 名称。
     *
     * @param dex perp dex 名称（可为空或空字符串）
     * @return JSON 结果
     */
    public JsonNode meta(String dex) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "meta");
        if (dex != null)
            payload.put("dex", dex);
        return postInfo(payload);
    }

    /**
     * 获取永续资产相关信息（包括标价、当前资金、未平仓合约等）
     */
    public JsonNode metaAndAssetCtxs() {
        Map<String, Object> payload = Map.of("type", "metaAndAssetCtxs");
        return postInfo(payload);
    }

    /**
     * 查询 spot 元数据（spotMeta）。
     */
    public JsonNode spotMeta() {
        Map<String, Object> payload = Map.of("type", "spotMeta");
        return postInfo(payload);
    }

    /**
     * 查询 spot 元数据与资产上下文（spotMetaAndAssetCtxs）。
     */
    public JsonNode spotMetaAndAssetCtxs() {
        Map<String, Object> payload = Map.of("type", "spotMetaAndAssetCtxs");
        return postInfo(payload);
    }


    /**
     * L2 book snapshot
     * L2 订单簿快照。
     *
     * @param coin     币种
     * @param nSigFigs Optional field to aggregate levels to nSigFigs significant figures. Valid values are 2, 3, 4, 5, and null, which means full precision
     * @param mantissa Optional field to aggregate levels. This field is only allowed if nSigFigs is 5. Accepts values of 1, 2 or 5.
     */
    public L2Book l2Book(String coin, Integer nSigFigs, Integer mantissa) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "l2Book");
        payload.put("coin", coin);
        if (nSigFigs != null) {
            payload.put("nSigFigs", nSigFigs);
        }
        if (mantissa != null) {
            payload.put("mantissa", mantissa);
        }
        return JSONUtil.convertValue(postInfo(payload), L2Book.class);
    }

    public L2Book l2Book(String coin) {
        return l2Book(coin, null, null);
    }


    /**
     * Candle snapshot
     * Only the most recent 5000 candles are available
     * <p>
     * Supported intervals: "1m", "3m", "5m", "15m", "30m", "1h", "2h", "4h", "8h", "12h", "1d", "3d", "1w", "1M"
     * K 线快照（类型化返回，支持以币种名称传参）。
     *
     * <p>
     * 某些环境下服务端可能要求请求体中 coin 字段为字符串（如 "BTC" 或 "@107"）。
     * 为提升兼容性，提供该重载方法，使用币种名称直接发起请求。
     * </p>
     *
     * @param coin      币种名称（如 "BTC"，或形如 "@107" 的内部标识）
     * @param interval  间隔字符串（如 "1m"、"15m"、"1h"、"1d" 等）
     * @param startTime 起始毫秒
     * @param endTime   结束毫秒
     * @return Candle 列表
     */
    public List<Candle> candleSnapshot(String coin, CandleInterval interval, Long startTime, Long endTime) {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("coin", coin);
        req.put("interval", interval.getCode());
        req.put("startTime", startTime);
        req.put("endTime", endTime);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "candleSnapshot");
        payload.put("req", req);
        return JSONUtil.toList(postInfo(payload), Candle.class);
    }

    /**
     * 获取最近一个间隔周期的最新 K 线 便捷方法。
     */
    public Candle candleSnapshotLatest(String coin, CandleInterval interval) {
        long endTime = System.currentTimeMillis();
        long startTime = endTime - interval.toMillis();
        List<Candle> candles = candleSnapshot(coin, interval, startTime, endTime);
        return !candles.isEmpty() ? candles.getLast() : null;
    }

    /**
     * 根据时间周期和K线数量获取
     * 注意:仅提供最新的5000支蜡烛。
     */
    public List<Candle> candleSnapshotByCount(String coin, CandleInterval interval, int count) {
        if (count <= 0) {
            throw new HypeError("count必须大于0");
        }
        long endTime = System.currentTimeMillis();
        long startTime = endTime - interval.toMillis() * count;
        List<Candle> candles = candleSnapshot(coin, interval, startTime, endTime);
        return candles.size() > count ? candles.subList(candles.size() - count, candles.size()) : candles;
    }

    /**
     * 将整数 coinId 转换为 /info 接口需要的字符串 coin 表达。
     * <p>
     * 规则：
     * - 若能在 meta.universe 中以下标找到名称，则返回该名称（用于 perp，例如 "BTC"、"ETH"）。
     * - 否则回退为 "@" + coinId（用于 spot，例如 "@107"）。
     *
     * @param coinId 整数 coin 标识
     * @return /info 请求所需的字符串 coin
     */
    private String coinIdToInfoCoinString(int coinId) {
        try {
            JsonNode m = meta();
            if (m != null && m.has("universe") && m.get("universe").isArray()) {
                JsonNode uni = m.get("universe");
                if (coinId >= 0 && coinId < uni.size()) {
                    JsonNode a = uni.get(coinId);
                    if (a != null && a.has("name")) {
                        String nm = a.get("name").asText();
                        if (nm != null && !nm.isEmpty()) {
                            return nm;
                        }
                    }
                }
            }
        } catch (HypeError e) {
            // 忽略初始化失败，走回退逻辑
        }
        // Spot 或未命中时的通用回退
        return "@" + coinId;
    }


    /**
     * 间隔字符串转毫秒工具（支持官方文档列出的间隔）。
     *
     * @param interval 间隔字符串（如 "1m"、"15m"、"1h"、"1d"、"1w"、"1M"）
     * @return 间隔毫秒（"1M" 近似按 30 天计算）
     * @throws HypeError 若间隔不受支持
     */
    private long intervalToMs(String interval) {
        if (interval == null)
            throw new HypeError("间隔字符串不能为空");
        return switch (interval) {
            case "1m" -> 60_000L;
            case "3m" -> 180_000L;
            case "5m" -> 300_000L;
            case "15m" -> 900_000L;
            case "30m" -> 1_800_000L;
            case "1h" -> 3_600_000L;
            case "2h" -> 7_200_000L;
            case "4h" -> 14_400_000L;
            case "8h" -> 28_800_000L;
            case "12h" -> 43_200_000L;
            case "1d" -> 86_400_000L;
            case "3d" -> 259_200_000L;
            case "1w" -> 604_800_000L;
            case "1M" -> 2_592_000_000L; // 30 天近似
            default -> throw new HypeError("不支持的间隔字符串：" + interval);
        };
    }

    /**
     * 用户状态查询。
     *
     * @param address 用户地址
     * @return JSON 结果
     */
    public JsonNode userState(String address) {
        Map<String, Object> payload = Map.of("type", "clearinghouseState", "user", address);
        return postInfo(payload);
    }

    /**
     * 查询用户未成交订单。
     */
    public List<OpenOrder> openOrders(String address) {
        return openOrders(address, null);
    }

    /**
     * 查询用户未成交订单（openOrders），可指定 perp dex 名称。
     */
    public List<OpenOrder> openOrders(String address, String dex) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "openOrders");
        payload.put("user", address);
        if (dex != null) {
            payload.put("dex", dex);
        }
        return JSONUtil.toList(postInfo(payload), OpenOrder.class);
    }

    /**
     * Retrieve a user's fills by time
     * Returns at most 2000 fills per response and only the 10000 most recent fills are available
     * 查询用户成交（按时间范围）。
     */
    public List<UserFill> userFillsByTime(String address, Long startTime, Long endTime, Boolean aggregateByTime) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "userFillsByTime");
        payload.put("user", address);
        payload.put("startTime", startTime);
        if (endTime != null) {
            payload.put("endTime", endTime);
        }
        if (aggregateByTime != null) {
            payload.put("aggregateByTime", aggregateByTime);
        }
        return JSONUtil.toList(postInfo(payload), UserFill.class);
    }

    public List<UserFill> userFillsByTime(String address, Long startTime) {
        return userFillsByTime(address, startTime, null, null);
    }

    public List<UserFill> userFillsByTime(String address, Long startTime, Long endTime) {
        return userFillsByTime(address, startTime, endTime, null);
    }

    public List<UserFill> userFillsByTime(String address, Long startTime, Boolean aggregateByTime) {
        return userFillsByTime(address, startTime, null, aggregateByTime);
    }


    /**
     * 查询用户费用（返现/手续费）。
     */
    public JsonNode userFees(String address) {
        Map<String, Object> payload = Map.of("type", "userFees", "user", address);
        return postInfo(payload);
    }

    /**
     * 查询资金费率历史（指定币种与时间范围）。
     */
    public JsonNode fundingHistory(int coin, long startMs, long endMs) {
        return this.fundingHistory(this.coinIdToInfoCoinString(coin), startMs, endMs);
    }

    /**
     * 查询资金费率历史（指定币种与时间范围）。
     */
    public JsonNode fundingHistory(String coin, long startMs, long endMs) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "fundingHistory");
        payload.put("coin", coin);
        payload.put("startTime", startMs);
        payload.put("endTime", endMs);
        return postInfo(payload);
    }

    /**
     * 查询用户资金费率历史（按用户与币种）。
     */
    public JsonNode userFundingHistory(String address, int coin, long startMs, long endMs) {
        return this.userFundingHistory(address, this.coinIdToInfoCoinString(coin), startMs, endMs);
    }

    /**
     * 查询用户资金费率历史（按用户与币种）。
     */
    public JsonNode userFundingHistory(String address, String coin, long startMs, long endMs) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "userFunding");
        payload.put("user", address);
        payload.put("coin", coin);
        payload.put("startTime", startMs);
        payload.put("endTime", endMs);
        return postInfo(payload);
    }

    /**
     * 用户非资金费率账本更新（不含 funding）。
     */
    public JsonNode userNonFundingLedgerUpdates(String address, long startMs, long endMs) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "userNonFundingLedgerUpdates");
        payload.put("user", address);
        payload.put("startTime", startMs);
        payload.put("endTime", endMs);
        return postInfo(payload);
    }

    /**
     * 历史订单查询。
     */
    public JsonNode historicalOrders(String address, long startMs, long endMs) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "historicalOrders");
        payload.put("user", address);
        payload.put("startTime", startMs);
        payload.put("endTime", endMs);
        return postInfo(payload);
    }

    /**
     * 用户 TWAP 切片成交查询。
     */
    public JsonNode userTwapSliceFills(String address, long startMs, long endMs) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "userTwapSliceFills");
        payload.put("user", address);
        payload.put("startTime", startMs);
        payload.put("endTime", endMs);
        return postInfo(payload);
    }


    /**
     * Retrieve a user's open orders with additional frontend info
     * 前端附加信息的未成交订单（frontendOpenOrders），可指定 perp dex 名称。
     *
     * @param address 用户地址
     * @param dex     perp dex 名称（可为空或空字符串）
     * @return JSON 数组（包含前端额外字段）
     */
    public List<FrontendOpenOrder> frontendOpenOrders(String address, String dex) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "frontendOpenOrders");
        payload.put("user", address);
        if (dex != null) {
            payload.put("dex", dex);
        }
        return JSONUtil.toList(postInfo(payload), FrontendOpenOrder.class);
    }

    public List<FrontendOpenOrder> frontendOpenOrders(String address) {
        return frontendOpenOrders(address, null);
    }


    /**
     * Retrieve a user's fills
     * 用户最近成交（最多 2000 条）。
     *
     * @param address         用户地址
     * @param aggregateByTime 是否按时间聚合（true/false）
     * @return JSON 数组
     */
    public List<UserFill> userFills(String address, Boolean aggregateByTime) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "userFills");
        payload.put("user", address);
        if (aggregateByTime != null) {
            payload.put("aggregateByTime", aggregateByTime);
        }
        return JSONUtil.toList(postInfo(payload), UserFill.class);
    }

    public List<UserFill> userFills(String address) {
        return userFills(address, null);
    }


    /**
     * 查询所有 perpetual dexs（perpDexs）。
     *
     * @return JSON 数组
     */
    public JsonNode perpDexs() {
        Map<String, Object> payload = Map.of("type", "perpDexs");
        return postInfo(payload);
    }

    /**
     * 查询所有 perpetual dexs（类型化返回）。
     *
     * <p>
     * 文档示例显示返回数组的元素可能为 null 或对象，形如：
     * [null, {"name":"test","fullName":"test dex", ...}]
     * 因此此处采用 Map<String, Object> 以保持灵活的字段接收能力。
     * </p>
     */
    public List<Map<String, Object>> perpDexsTyped() {
        JsonNode node = perpDexs();
        return JSONUtil.convertValue(node,
                TypeFactory.defaultInstance().constructCollectionType(List.class,
                        TypeFactory.defaultInstance().constructMapType(Map.class, String.class, Object.class)));
    }

    /**
     * 永续清算所状态（用户账户摘要）。
     *
     * @param address 用户地址
     * @param dex     可选 dex 名称（传 null 则不包含该字段）
     * @return JSON 对象
     */
    public JsonNode clearinghouseState(String address, String dex) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "clearinghouseState");
        payload.put("user", address);
        if (dex != null && !dex.isEmpty()) {
            payload.put("dex", dex);
        }
        return postInfo(payload);
    }

    /**
     * 永续清算所状态（类型化返回）。
     */
    public ClearinghouseState clearinghouseStateTyped(String address, String dex) {
        JsonNode node = clearinghouseState(address, dex);
        return JSONUtil.convertValue(node, ClearinghouseState.class);
    }


    /**
     * 获取用户的代币余额
     */
    public SpotClearinghouseState spotClearinghouseState(String address) {
        Map<String, Object> payload = Map.of("type", "spotClearinghouseState", "user", address);
        JsonNode node = postInfo(payload);
        return JSONUtil.convertValue(node, SpotClearinghouseState.class);
    }

    /**
     * Retrieve details for a vault
     **/
    public JsonNode vaultDetails(String vaultAddress, String user) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "vaultDetails");
        payload.put("vaultAddress", vaultAddress);
        if (user != null) {
            payload.put("user", user);
        }
        return postInfo(payload);
    }

    /**
     * Spot Deploy Auction 状态。
     *
     * @param address 用户地址（文档要求）
     * @return JSON 对象
     */
    public JsonNode spotDeployState(String address) {
        Map<String, Object> payload = Map.of("type", "spotDeployState", "user", address);
        return postInfo(payload);
    }

    /**
     * 用户组合（portfolio）。
     */
    public JsonNode portfolio(String address) {
        Map<String, Object> payload = Map.of("type", "portfolio", "user", address);
        return postInfo(payload);
    }

    /**
     * 用户仓位费率与等级（userRole）。
     */
    public JsonNode userRole(String address) {
        Map<String, Object> payload = Map.of("type", "userRole", "user", address);
        return postInfo(payload);
    }

    /**
     * Query user rate limits
     * 用户速率限制（userRateLimit）。
     */
    public UserRateLimit userRateLimit(String address) {
        Map<String, Object> payload = Map.of("type", "userRateLimit", "user", address);
        return JSONUtil.convertValue(postInfo(payload), UserRateLimit.class);
    }

    /**
     * Query order status by oid or cloid
     * 订单状态查询（orderStatus）。
     **/
    public OrderStatus orderStatus(String address, Long oid) {
        Map<String, Object> payload = Map.of("type", "orderStatus", "user", address, "oid", oid);
        return JSONUtil.convertValue(postInfo(payload), OrderStatus.class);
    }

    /**
     * 查询推荐人状态。
     */
    public JsonNode queryReferralState(String address) {
        Map<String, Object> payload = Map.of("type", "queryReferralState", "user", address);
        return postInfo(payload);
    }

    /**
     * 查询子账户列表。
     */
    public JsonNode querySubAccounts(String address) {
        Map<String, Object> payload = Map.of("type", "querySubAccounts", "user", address);
        return postInfo(payload);
    }

    /**
     * 查询用户到多签签名者映射。
     */
    public JsonNode queryUserToMultiSigSigners(String address) {
        Map<String, Object> payload = Map.of("type", "queryUserToMultiSigSigners", "user", address);
        return postInfo(payload);
    }

    /**
     * 永续部署拍卖状态。
     */
    public JsonNode queryPerpDeployAuctionStatus() {
        Map<String, Object> payload = Map.of("type", "queryPerpDeployAuctionStatus");
        return postInfo(payload);
    }

    /**
     * 现货部署拍卖状态。
     */
    public JsonNode querySpotDeployAuctionStatus() {
        Map<String, Object> payload = Map.of("type", "querySpotDeployAuctionStatus");
        return postInfo(payload);
    }

    // =====================
    // 新增：Perp 市场状态
    // =====================

    /**
     * 获取 Perp 市场状态（perpDexStatus）。
     *
     * @param dex perp dex 名称；空字符串代表第一个 perp dex
     * @return JSON 对象
     */
    public JsonNode perpDexStatus(String dex) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "perpDexStatus");
        payload.put("dex", dex == null ? "" : dex);
        return postInfo(payload);
    }

    /**
     * 获取 Perp 市场状态（类型化返回）。
     *
     * @param dex perp dex 名称；空字符串代表第一个 perp dex
     * @return 类型化模型 PerpDexStatus
     */
    public PerpDexStatus perpDexStatusTyped(String dex) {
        JsonNode node = perpDexStatus(dex);
        return JSONUtil.convertValue(node, PerpDexStatus.class);
    }

    /**
     * 查询用户 DEX 抽象状态。
     */
    public JsonNode queryUserDexAbstractionState(String address) {
        Map<String, Object> payload = Map.of("type", "queryUserDexAbstractionState", "user", address);
        return postInfo(payload);
    }

    /**
     * 用户仓库（vault）权益。
     */
    public JsonNode userVaultEquities(String address) {
        Map<String, Object> payload = Map.of("type", "userVaultEquities", "user", address);
        return postInfo(payload);
    }

    /**
     * 用户的额外代理（extraAgents）。
     */
    public JsonNode extraAgents(String address) {
        Map<String, Object> payload = Map.of("type", "extraAgents", "user", address);
        return postInfo(payload);
    }

    /**
     * 名称映射为资产 ID。
     *
     * @param name 币种名（如 "ETH"）
     * @return 资产 ID（可能抛出异常）
     */
    public int nameToAsset(String name) {
        // TTL 检查，必要时刷新缓存
        ensureFreshCaches();
        // 尝试直接从缓存读取
        Integer coin = nameToCoin.get(name);
        if (coin != null) {
            nameToCoinHits++;
        } else {
            nameToCoinMisses++;
        }
        if (coin == null) {
            // 刷新 perp 元数据
            refreshPerpMeta();
            coin = nameToCoin.get(name);
        }

        // 若仍未命中，尝试刷新 spot 元数据（spotMeta.universe）
        if (coin == null) {
            refreshSpotMeta();
            coin = nameToCoin.get(name);
        }

        if (coin == null)
            throw new HypeError("Unknown coin name: " + name);
        Integer asset = coinToAsset.get(coin);
        if (asset != null) {
            coinToAssetHits++;
        } else {
            coinToAssetMisses++;
        }
        if (asset == null) {
            // 兼容旧结构：尝试从 meta.assets 刷新
            // 使用 perp meta.assets 进行补充刷新
            refreshPerpMeta();
            asset = coinToAsset.get(coin);
        }
        if (asset == null)
            throw new HypeError("Unknown asset for coin: " + name);
        return asset;
    }

    /**
     * 订阅 WebSocket。
     *
     * @param subscription 订阅对象
     * @param callback     消息回调
     */
    public void subscribe(JsonNode subscription, WebsocketManager.MessageCallback callback) {
        if (skipWs)
            throw new HypeError("WebSocket disabled by skipWs");
        wsManager.subscribe(subscription, callback);
    }

    /**
     * 取消订阅。
     *
     * @param subscription 订阅对象
     */
    public void unsubscribe(JsonNode subscription) {
        if (skipWs)
            return;
        wsManager.unsubscribe(subscription);
    }

    /**
     * 关闭 WebSocket 连接。
     */
    public void closeWs() {
        if (wsManager != null)
            wsManager.stop();
    }

    // ============================
    // WebSocket 重连与网络监听配置代理
    // ============================

    /**
     * 添加连接状态监听器（用于接收连接、断开、重连、网络状态变化的通知）。
     *
     * @param listener 监听器实现
     */
    public void addConnectionListener(WebsocketManager.ConnectionListener listener) {
        if (skipWs)
            return;
        if (wsManager != null)
            wsManager.addConnectionListener(listener);
    }

    /**
     * 移除连接状态监听器。
     *
     * @param listener 监听器实现
     */
    public void removeConnectionListener(WebsocketManager.ConnectionListener listener) {
        if (skipWs)
            return;
        if (wsManager != null)
            wsManager.removeConnectionListener(listener);
    }

    /**
     * 设置最大重连尝试次数（默认 5）。
     *
     * @param max 最大次数（建议 5~10）
     */
    public void setMaxReconnectAttempts(int max) {
        if (skipWs)
            return;
        if (wsManager != null)
            wsManager.setMaxReconnectAttempts(max);
    }

    /**
     * 设置网络监控的检查间隔（秒）。
     *
     * @param seconds 间隔秒数（默认 5，建议 3~10）
     */
    public void setNetworkCheckIntervalSeconds(int seconds) {
        if (skipWs)
            return;
        if (wsManager != null)
            wsManager.setNetworkCheckIntervalSeconds(seconds);
    }

    /**
     * 设置重连指数退避参数。
     *
     * @param initialMs 初始重连延迟毫秒（建议 500~2000）
     * @param maxMs     最大重连延迟毫秒（建议不超过 5000~30000）
     */
    public void setReconnectBackoffMs(long initialMs, long maxMs) {
        if (skipWs)
            return;
        if (wsManager != null)
            wsManager.setReconnectBackoffMs(initialMs, maxMs);
    }

    // ============================
    // WebSocket 回调异常监听配置代理
    // ============================

    /**
     * 添加回调异常监听器
     */
    public void addCallbackErrorListener(WebsocketManager.CallbackErrorListener listener) {
        if (skipWs)
            return;
        if (wsManager != null)
            wsManager.addCallbackErrorListener(listener);
    }

    /**
     * 移除回调异常监听器
     */
    public void removeCallbackErrorListener(WebsocketManager.CallbackErrorListener listener) {
        if (skipWs)
            return;
        if (wsManager != null)
            wsManager.removeCallbackErrorListener(listener);
    }
}
