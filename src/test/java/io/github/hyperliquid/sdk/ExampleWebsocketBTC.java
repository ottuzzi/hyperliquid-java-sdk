package io.github.hyperliquid.sdk;

import io.github.hyperliquid.sdk.apis.Info;
import io.github.hyperliquid.sdk.model.subscription.TradesSubscription;

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
        // 注意：WebSocket 订阅不需要私钥，只订阅公开行情数据
        HyperliquidClient client = HyperliquidClient.builder()
                //.testNetUrl()  // 使用测试网
                .build();
        Info info = client.getInfo();
        // ==================== 4. 订阅 BTC 实时成交 ====================
        // 订阅 BTC 逐笔成交
        TradesSubscription btcTrades = TradesSubscription.of("BTC");
        info.subscribe(btcTrades, msg -> {
            System.out.println("BTC 成交: " + msg);
        });

        // 订阅 ETH 逐笔成交
        TradesSubscription ethTrades = TradesSubscription.of("ETH");
        info.subscribe(ethTrades, msg -> {
            System.out.println("ETH 成交: " + msg);
        });
    }
}
