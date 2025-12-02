import com.fasterxml.jackson.databind.JsonNode;
import io.github.hyperliquid.sdk.HyperliquidClient;
import io.github.hyperliquid.sdk.apis.Info;
import io.github.hyperliquid.sdk.model.subscription.TradesSubscription;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket 同时订阅多个币种的逐笔成交示例
 * <p>
 * 演示功能：
 * 1. 同时订阅 BTC 和 ETH 的逐笔成交数据
 * 2. 每个币种使用独立的回调函数处理消息
 * 3. 展示 Hyperliquid 官方支持的多订阅能力
 * </p>
 */
public class ExampleWebsocketMultiTrades {

    public static void main(String[] args) throws InterruptedException {
        // ==================== 1. 初始化客户端 ====================
        HyperliquidClient client = HyperliquidClient.builder()
                .testNetUrl()  // 使用测试网
                .build();

        Info info = client.getInfo();

        System.out.println("=== Hyperliquid WebSocket 多币种逐笔成交订阅示例 ===\n");

        // ==================== 2. 订阅 BTC 逐笔成交（使用类型安全的实体类）====================
        System.out.println("--- 订阅 BTC 逐笔成交 ---");
        TradesSubscription btcTrades = TradesSubscription.of("BTC");

        info.subscribe(btcTrades, msg -> {
            // 解析 BTC 成交数据
            JsonNode data = msg.get("data");
            if (data != null && data.isArray()) {
                for (JsonNode trade : data) {
                    String coin = trade.path("coin").asText();
                    String price = trade.path("px").asText();
                    String size = trade.path("sz").asText();
                    String side = trade.path("side").asText();
                    long time = trade.path("time").asLong();
                    
                    System.out.printf("[BTC 成交] %s %s - 价格: %s, 数量: %s, 时间: %d%n",
                            coin, side.equals("A") ? "卖出" : "买入", price, size, time);
                }
            }
        });

        // ==================== 3. 订阅 ETH 逐笔成交（使用类型安全的实体类）====================
        System.out.println("--- 订阅 ETH 逐笔成交 ---");
        TradesSubscription ethTrades = TradesSubscription.of("ETH");

        info.subscribe(ethTrades, msg -> {
            // 解析 ETH 成交数据
            JsonNode data = msg.get("data");
            if (data != null && data.isArray()) {
                for (JsonNode trade : data) {
                    String coin = trade.path("coin").asText();
                    String price = trade.path("px").asText();
                    String size = trade.path("sz").asText();
                    String side = trade.path("side").asText();
                    long time = trade.path("time").asLong();
                    
                    System.out.printf("[ETH 成交] %s %s - 价格: %s, 数量: %s, 时间: %d%n",
                            coin, side.equals("A") ? "卖出" : "买入", price, size, time);
                }
            }
        });

        // ==================== 4. 可选：订阅更多币种 ====================
        // 你可以继续添加更多币种的订阅，例如：
        /*
        TradesSubscription solTrades = TradesSubscription.of("SOL");
        info.subscribe(solTrades, msg -> {
            // 处理 SOL 成交数据
        });
        */

        // ==================== 5. 保持运行并接收消息 ====================
        System.out.println("\n正在接收 BTC 和 ETH 的实时成交数据，运行 60 秒后自动退出...\n");

        // 使用 CountDownLatch 等待 60 秒
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(60, TimeUnit.SECONDS);

        // ==================== 6. 优雅关闭 WebSocket ====================
        System.out.println("\n正在关闭 WebSocket 连接...");
        info.closeWs();
        System.out.println("WebSocket 已关闭，程序退出。");
    }
}
