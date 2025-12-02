import com.fasterxml.jackson.databind.JsonNode;
import io.github.hyperliquid.sdk.HyperliquidClient;
import io.github.hyperliquid.sdk.apis.Info;
import io.github.hyperliquid.sdk.model.subscription.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket 类型安全订阅示例
 * <p>
 * 演示功能：
 * 1. 使用类型安全的 Subscription 实体类替代 JsonNode
 * 2. 订阅多种类型的行情数据（L2订单簿、逐笔成交、K线、BBO、中间价）
 * 3. 享受编译期类型检查和更好的代码可读性
 * </p>
 */
public class ExampleWebsocketTypeSafe {

    public static void main(String[] args) throws InterruptedException {
        // ==================== 1. 初始化客户端 ====================
        HyperliquidClient client = HyperliquidClient.builder()
                .testNetUrl()  // 使用测试网
                .build();

        Info info = client.getInfo();

        System.out.println("=== Hyperliquid WebSocket 类型安全订阅示例 ===\n");

        // ==================== 2. 订阅 BTC L2 订单簿 ====================
        System.out.println("--- 订阅 BTC L2 订单簿 ---");
        L2BookSubscription btcL2Book = L2BookSubscription.of("BTC");

        info.subscribe(btcL2Book, msg -> {
            JsonNode data = msg.get("data");
            if (data != null && data.has("levels")) {
                JsonNode levels = data.get("levels");
                if (levels.isArray() && levels.size() >= 2) {
                    JsonNode bids = levels.get(0);
                    JsonNode asks = levels.get(1);
                    if (bids.isArray() && !bids.isEmpty() && asks.isArray() && !asks.isEmpty()) {
                        String bestBid = bids.get(0).get("px").asText();
                        String bestAsk = asks.get(0).get("px").asText();
                        System.out.printf("[L2订单簿] BTC 买一: %s, 卖一: %s%n", bestBid, bestAsk);
                    }
                }
            }
        });

        // ==================== 3. 订阅 ETH 逐笔成交 ====================
        System.out.println("--- 订阅 ETH 逐笔成交 ---");
        TradesSubscription ethTrades = TradesSubscription.of("ETH");

        info.subscribe(ethTrades, msg -> {
            JsonNode data = msg.get("data");
            if (data != null && data.isArray()) {
                for (JsonNode trade : data) {
                    String coin = trade.path("coin").asText();
                    String price = trade.path("px").asText();
                    String size = trade.path("sz").asText();
                    String side = trade.path("side").asText();
                    System.out.printf("[逐笔成交] %s %s - 价格: %s, 数量: %s%n",
                            coin, side.equals("A") ? "卖出" : "买入", price, size);
                }
            }
        });

        // ==================== 4. 订阅 BTC 1分钟 K线 ====================
        System.out.println("--- 订阅 BTC 1分钟 K线 ---");
        CandleSubscription btcCandle = CandleSubscription.of("BTC", "1m");

        info.subscribe(btcCandle, msg -> {
            JsonNode data = msg.get("data");
            if (data != null) {
                String open = data.path("o").asText();
                String high = data.path("h").asText();
                String low = data.path("l").asText();
                String close = data.path("c").asText();
                String volume = data.path("v").asText();
                System.out.printf("[K线] BTC 1m - 开: %s, 高: %s, 低: %s, 收: %s, 量: %s%n",
                        open, high, low, close, volume);
            }
        });

        // ==================== 5. 订阅 ETH 最佳买卖价 ====================
        System.out.println("--- 订阅 ETH 最佳买卖价 ---");
        BboSubscription ethBbo = BboSubscription.of("ETH");

        info.subscribe(ethBbo, msg -> {
            JsonNode data = msg.get("data");
            if (data != null) {
                String bidPrice = data.path("bid").path("px").asText();
                String bidSize = data.path("bid").path("sz").asText();
                String askPrice = data.path("ask").path("px").asText();
                String askSize = data.path("ask").path("sz").asText();
                System.out.printf("[BBO] ETH 买一: %s@%s, 卖一: %s@%s%n",
                        bidPrice, bidSize, askPrice, askSize);
            }
        });

        // ==================== 6. 订阅所有币种中间价 ====================
        System.out.println("--- 订阅所有币种中间价 ---");
        AllMidsSubscription allMids = AllMidsSubscription.create();

        info.subscribe(allMids, msg -> {
            JsonNode data = msg.get("data");
            if (data != null && data.has("mids")) {
                JsonNode mids = data.get("mids");
                if (mids != null && mids.isObject()) {
                    // 只打印前3个币种的中间价（避免输出过多）
                    int count = 0;
                    var iter = mids.fields();
                    System.out.print("[中间价] ");
                    while (iter.hasNext() && count < 3) {
                        var entry = iter.next();
                        System.out.printf("%s: %s  ", entry.getKey(), entry.getValue().asText());
                        count++;
                    }
                    System.out.println("...");
                }
            }
        });

        // ==================== 7. 保持运行并接收消息 ====================
        System.out.println("\n正在接收实时行情数据，运行 60 秒后自动退出...\n");

        // 使用 CountDownLatch 等待 60 秒
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(60, TimeUnit.SECONDS);

        // ==================== 8. 优雅关闭 WebSocket ====================
        System.out.println("\n正在关闭 WebSocket 连接...");
        info.closeWs();
        System.out.println("WebSocket 已关闭，程序退出。");
    }
}
