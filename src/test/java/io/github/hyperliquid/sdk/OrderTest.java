package io.github.hyperliquid.sdk;

import io.github.hyperliquid.sdk.apis.Exchange;
import io.github.hyperliquid.sdk.model.order.Order;
import io.github.hyperliquid.sdk.model.order.OrderRequest;
import io.github.hyperliquid.sdk.model.order.Tif;
import io.github.hyperliquid.sdk.model.order.TriggerOrderType;
import io.github.hyperliquid.sdk.utils.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * 订单下单与平仓逻辑综合测试。
 */
public class OrderTest {

    /**
     * 私钥（测试网）
     */
    String privateKey = "your_testnet_private_key_here";

    /**
     * 统一客户端（测试网 + 启用调试日志）
     */
    HyperliquidClient client = HyperliquidClient.builder()
            .baseUrl(Constants.TESTNET_API_URL)
            .addPrivateKey(privateKey)
            .enableDebugLogs()
            .build();

    /**
     * 获取币种当前中间价。
     * <p>
     * 设计考虑：
     * - 限价/触发价选择需要参考当前行情中位数，避免写死；
     * - 返回 double 以便后续进行百分比偏移。
     */
    private double mid(String symbol) {
        Map<String, String> mids = client.getInfo().allMids();
        String px = mids.get(symbol);
        Assertions.assertNotNull(px, "mid price not found for " + symbol);
        return Double.parseDouble(px);
    }

    /**
     * 市价下单：做多与做空各 0.01。
     */
    @Test
    public void testMarketOrders() {
        String symbol = "ETH";
        Exchange ex = client.getSingleExchange();

        OrderRequest longReq = OrderRequest.createDefaultPerpMarketOrder(symbol, true, 0.01);
        Order longOrder = ex.order(longReq);
        Assertions.assertNotNull(longOrder);
        Assertions.assertNotNull(longOrder.getResponse());

        OrderRequest shortReq = OrderRequest.createDefaultPerpMarketOrder(symbol, false, 0.01);
        Order shortOrder = ex.order(shortReq);
        Assertions.assertNotNull(shortOrder);
        Assertions.assertNotNull(shortOrder.getResponse());
    }

    /**
     * 限价下单：做多与做空各 0.01。
     * 策略：
     * - 买单限价取 `mid * 0.99`；卖单限价取 `mid * 1.01`；
     * - 使用 `Tif.GTC` 以便保留委托；
     */
    @Test
    public void testLimitOrders() {
        String symbol = "ETH";
        double m = mid(symbol);
        double buyPx = m * 0.99;
        double sellPx = m * 1.01;

        Exchange ex = client.getSingleExchange();

        OrderRequest longReq = OrderRequest.createPerpLimitOrder(Tif.GTC, symbol, true, 0.01, buyPx, false, null);
        Order longOrder = ex.order(longReq);
        Assertions.assertNotNull(longOrder);
        Assertions.assertNotNull(longOrder.getResponse());

        OrderRequest shortReq = OrderRequest.createPerpLimitOrder(Tif.GTC, symbol, false, 0.01, sellPx, false, null);
        Order shortOrder = ex.order(shortReq);
        Assertions.assertNotNull(shortOrder);
        Assertions.assertNotNull(shortOrder.getResponse());
    }

    /**
     * 触发价止盈/止损（TP/SL）：为仓位下发两类触发单。
     * 步骤：
     * - 预先开多 0.01；
     * - 创建 SL（触发后市价平仓）：`triggerPx = mid * 0.98`；
     * - 创建 TP（触发后市价平仓）：`triggerPx = mid * 1.02`；
     * - 两单均设置 `reduceOnly = true` 以保证仅减仓。
     */
    @Test
    public void testTriggerTpSl() throws Exception {
        String symbol = "ETH";
        Exchange ex = client.getSingleExchange();
        double m = mid(symbol);

        OrderRequest openReq = OrderRequest.createDefaultPerpMarketOrder(symbol, true, 0.01);
        Order openOrder = ex.order(openReq);
        Assertions.assertNotNull(openOrder);

        double slTrigger = m * 0.98;
        double tpTrigger = m * 1.02;

        OrderRequest slReq = OrderRequest.createPerpTriggerOrder(symbol, false, 0.01, null, slTrigger,
                true, TriggerOrderType.TpslType.SL, true, null);
        Order slOrder = ex.order(slReq);
        Assertions.assertNotNull(slOrder);
        Assertions.assertNotNull(slOrder.getResponse());

        OrderRequest tpReq = OrderRequest.createPerpTriggerOrder(symbol, false, 0.01, null, tpTrigger,
                true, TriggerOrderType.TpslType.TP, true, null);
        Order tpOrder = ex.order(tpReq);
        Assertions.assertNotNull(tpOrder);
        Assertions.assertNotNull(tpOrder.getResponse());
    }

    /**
     * 市价平仓：自动推断方向与数量，完整平掉当前仓位。
     */
    @Test
    public void testClosePositionMarket() {
        String symbol = "ETH";
        Exchange ex = client.getSingleExchange();

        // 先开仓 0.01
        OrderRequest openReq = OrderRequest.createDefaultPerpMarketOrder(symbol, true, 0.01);
        Order openOrder = ex.order(openReq);
        Assertions.assertNotNull(openOrder);

        // 自动推断并平仓
        OrderRequest closeReq = OrderRequest.closePositionAtMarket(symbol);
        Order closeOrder = ex.order(closeReq);
        Assertions.assertNotNull(closeOrder);
        Assertions.assertNotNull(closeOrder.getResponse());
    }

    /**
     * 限价平仓：以 `reduceOnly=true` 的限价单完成部分平仓 0.01。
     * 说明：
     * - 开多 0.01；随后以卖出限价单（`reduceOnly=true`）平掉 0.01；
     * - 使用 `GTC` 保留委托；价格设为 `mid * 1.01`，更保守以便在测试网保持挂单。
     */
    @Test
    public void testClosePositionLimit() {
        String symbol = "ETH";
        Exchange ex = client.getSingleExchange();
        double m = mid(symbol);

        OrderRequest openReq = OrderRequest.createDefaultPerpMarketOrder(symbol, true, 0.01);
        Order openOrder = ex.order(openReq);
        Assertions.assertNotNull(openOrder);

        double sellPx = m * 1.01;
        OrderRequest closeReq = OrderRequest.createPerpLimitOrder(Tif.GTC, symbol, false, 0.01, sellPx,
                true, null);
        Order closeOrder = ex.order(closeReq);
        Assertions.assertNotNull(closeOrder);
        Assertions.assertNotNull(closeOrder.getResponse());
    }
}
