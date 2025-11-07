package io.github.hyperliquid.sdk.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.hyperliquid.sdk.model.info.UpdateLeverage;
import io.github.hyperliquid.sdk.model.order.OrderRequest;
import io.github.hyperliquid.sdk.model.order.OrderWire;
import io.github.hyperliquid.sdk.utils.Constants;
import io.github.hyperliquid.sdk.utils.JSONUtil;
import io.github.hyperliquid.sdk.utils.Signing;
import org.web3j.crypto.Credentials;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * ExchangeClient 客户端，负责下单、撤单、转账等 L1/L2 操作。
 * 当前版本实现核心下单与批量下单，其他 L1 操作将在后续补充。
 */
public class ExchangeClient {

    private final Credentials wallet;

    private final HypeHttpClient hypeHttpClient;

    private final Function<String, Integer> nameToAssetFunction;


    /**
     * 构造 Exchange 客户端。
     *
     * @param hypeHttpClient HTTP客户端实例
     * @param wallet         用户钱包凭证（包含私钥与地址）
     */
    public ExchangeClient(HypeHttpClient hypeHttpClient, Credentials wallet, Function<String, Integer> nameToAssetFunction) {
        this.hypeHttpClient = hypeHttpClient;
        this.wallet = wallet;
        this.nameToAssetFunction = nameToAssetFunction;
    }

    /**
     * 单笔下单。
     *
     * @param req 下单请求（包含币种名、方向、数量、价格、类型、reduceOnly、cloid）
     * @return 交易接口响应 JSON
     */
    public JsonNode order(OrderRequest req) {
        int assetId = nameToAssetFunction.apply(req.getCoin());
        OrderWire wire = Signing.orderRequestToOrderWire(assetId, req);
        Map<String, Object> action = Signing.orderWiresToOrderAction(List.of(wire));

        return postAction(action, null, null);
    }

    /**
     * 批量下单。
     *
     * @param requests 下单请求列表
     * @return 交易接口响应
     */
    public JsonNode bulkOrders(List<OrderRequest> requests) {
        List<OrderWire> wires = new ArrayList<>();
        for (OrderRequest r : requests) {
            int assetId = nameToAssetFunction.apply(r.getCoin());
            wires.add(Signing.orderRequestToOrderWire(assetId, r));
        }
        Map<String, Object> action = Signing.orderWiresToOrderAction(wires);
        return postAction(action, null, null);
    }

    /**
     * 根据 OID 撤单。
     *
     * @param coinName 币种名
     * @param oid      订单 OID
     * @return 响应 JSON
     */
    public JsonNode cancel(String coinName, int oid) {
        int assetId = nameToAssetFunction.apply(coinName);
        Map<String, Object> cancel = new LinkedHashMap<>();
        cancel.put("coin", assetId);
        cancel.put("oid", oid);
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("type", "cancel");
        action.put("cancels", List.of(cancel));
        return postAction(action, null, null);
    }

    /**
     * 根据 Cloid 撤单。
     *
     * @param coinName 币种名
     * @param cloid    客户端订单 ID
     * @return 响应 JSON
     */
    public JsonNode cancelByCloid(String coinName, String cloid) {
        int assetId = nameToAssetFunction.apply(coinName);
        Map<String, Object> cancel = new LinkedHashMap<>();
        cancel.put("coin", assetId);
        cancel.put("cloid", cloid);
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("type", "cancelByCloid");
        action.put("cancels", List.of(cancel));
        return postAction(action, null, null);
    }

    /**
     * 修改订单（通过 OID）。
     *
     * @param coinName 币种名
     * @param oid      原订单 OID
     * @param newReq   新订单请求（价格/数量/类型等）
     * @return 响应 JSON
     */
    public JsonNode modifyOrder(String coinName, int oid, OrderRequest newReq) {
        int assetId = nameToAssetFunction.apply(coinName);
        OrderWire wire = Signing.orderRequestToOrderWire(assetId, newReq);
        Map<String, Object> modify = new LinkedHashMap<>();
        modify.put("coin", assetId);
        modify.put("oid", oid);
        modify.put("order", wire);
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("type", "modifyOrder");
        action.put("modifies", List.of(modify));
        return postAction(action, null, null);
    }

    /**
     * 更改杠杆
     *
     * @param coinName 币种名
     * @param crossed  是否全仓
     * @param leverage 杠杆倍数
     * @return 响应 JSON
     *
     */
    public UpdateLeverage updateLeverage(String coinName, boolean crossed, int leverage) {
        int assetId = nameToAssetFunction.apply(coinName);
        Map<String, Object> actions = new LinkedHashMap<>() {{
            this.put("type", "updateLeverage");
            this.put("asset", assetId);
            this.put("isCross", crossed);
            this.put("leverage", leverage);
        }};
        return JSONUtil.convertValue(postAction(actions, null, null), UpdateLeverage.class);
    }

    /**
     * 统一 L1 动作发送封装（签名并 POST 到 /exchange）。
     *
     * @param action       L1 动作（Map）
     * @param vaultAddress 可选 vault 地址
     * @param expiresAfter 可选过期时间
     * @return JSON 响应
     */
    public JsonNode postAction(Map<String, Object> action, String vaultAddress, Long expiresAfter) {
        // 时间戳作为 nonce（毫秒），与 Python get_timestamp_ms 一致
        long nonce = Signing.getTimestampMs();

        // 特殊动作无需 vaultAddress（与 Python _post_action 一致）
        Object typeObj = action.get("type");
        String type = typeObj == null ? "" : typeObj.toString();
        String effectiveVault = ("usdClassTransfer".equals(type) || "sendAsset".equals(type)) ? null : vaultAddress;

        // 计算 actionHash -> 构造 typed data -> 标准 EIP-712 签名
        Map<String, Object> signature = Signing.signL1Action(
                wallet,
                action,
                effectiveVault,
                nonce,
                expiresAfter,
                isMainnet());

        // 构造请求体，字段与 Python _post_action 保持一致
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("action", action);
        payload.put("nonce", nonce);
        payload.put("signature", signature);
        payload.put("vaultAddress", effectiveVault);
        payload.put("expiresAfter", expiresAfter);

        return hypeHttpClient.post("/exchange", payload);
    }

    /**
     * 判断当前客户端是否连接主网。
     *
     * @return 如果是主网返回 true，否则返回 false
     */
    private boolean isMainnet() {
        return Constants.MAINNET_API_URL.equals(hypeHttpClient.getBaseUrl());
    }
}
