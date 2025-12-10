package io.github.hyperliquid.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.hyperliquid.sdk.model.order.Cloid;
import io.github.hyperliquid.sdk.model.order.Order;
import io.github.hyperliquid.sdk.model.order.OrderRequest;
import io.github.hyperliquid.sdk.model.order.Tif;
import io.github.hyperliquid.sdk.utils.JSONUtil;
import org.junit.jupiter.api.Test;

/**
 * 订单下单与平仓逻辑综合测试。
 */
public class OrderTest {

    /**
     * 私钥（测试网）
     */
    String privateKey = "your_private_key_here";

    /**
     * 统一客户端（测试网 + 启用调试日志）
     */
    HyperliquidClient client = HyperliquidClient.builder()
            .testNetUrl()
            .addPrivateKey(privateKey)
            .build();

    /**
     * 市价下单
     **/
    @Test
    public void testMarketOrder() throws JsonProcessingException {
        OrderRequest req = OrderRequest.Open.market("ETH", true, "0.02");
        Order order = client.getExchange().order(req);
        System.out.println(JSONUtil.writeValueAsString(order));
    }

    /**
     * 市价平仓
     **/
    @Test
    public void testMarketCloseOrder() {
        OrderRequest req = OrderRequest.Close.market("ETH", "0.01", Cloid.auto());
        Order order = client.getExchange().order(req);
        System.out.println(order);
    }

    /**
     * 全部市价平仓
     **/
    @Test
    public void testMarketCloseAllOrder() {
        Order order = client.getExchange().closePositionMarket("ETH");
        System.out.println(order);
    }

    /**
     * 限价下单
     **/
    @Test
    public void testLimitOrder() {
        OrderRequest req = OrderRequest.Open.limit(Tif.GTC, "ETH", true, "0.01", "1800.0");
        Order order = client.getExchange().order(req);
        System.out.println(order);
    }

    /**
     * 限价平仓
     **/
    @Test
    public void testLimitCloseOrder() {
        OrderRequest req = OrderRequest.Close.limit("ETH", "0.01", "2000.0", Cloid.auto());
        Order order = client.getExchange().order(req);
        System.out.println(order);
    }

    /**
     * 全部限价平仓
     **/
    @Test
    public void testLimitCloseAllOrder() {
        Order order = client.getExchange().closePositionLimit(Tif.GTC, "ETH", "4000.0", Cloid.auto());
        System.out.println(order);
    }

    @Test
    public void testCancel() {
        JsonNode node = client.getExchange().cancel("ETH", 42988070692L);
        System.out.println(node.toPrettyString());
    }


    @Test
    public void testMarketOrderALL() {
        OrderRequest req = OrderRequest.Open.market("ETH", true, "0.01");
        Order order = client.getExchange().order(req);
        System.out.println(order);
        Order closeOrder = client.getExchange().closePositionMarket("ETH");
        System.out.println(closeOrder);
    }

    @Test
    public void testTriggerOrderALL() {
        // 使用 breakoutAbove 替代已删除的 trigger 方法
        OrderRequest req = OrderRequest.Open.breakoutAbove("ETH", "0.01", "4000.0");
        Order order = client.getExchange().order(req);
        System.out.println(order);
    }

    /**
     * 测试止损平仓
     */
    @Test
    public void testStopLoss() throws JsonProcessingException {
        OrderRequest req = OrderRequest.Close.stopLoss("ETH", true, "0.01", "3500.0");
        Order order = client.getExchange().order(req);
        System.out.println(JSONUtil.writeValueAsString(order));
    }

    /**
     * 测试止盈平仓
     */
    @Test
    public void testTakeProfit() throws JsonProcessingException {
        OrderRequest req = OrderRequest.Close.takeProfit("ETH", true, "0.01", "3000.0");
        Order order = client.getExchange().order(req);
        System.out.println(JSONUtil.writeValueAsString(order));
    }
}

 
