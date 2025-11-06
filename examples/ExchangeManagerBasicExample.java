import io.github.hyperliquid.sdk.ExchangeManager;
import io.github.hyperliquid.sdk.exchange.Exchange;
import io.github.hyperliquid.sdk.info.Info;
import io.github.hyperliquid.sdk.model.order.LimitOrderType;
import io.github.hyperliquid.sdk.model.order.OrderRequest;
import io.github.hyperliquid.sdk.model.order.OrderType;
import io.github.hyperliquid.sdk.utils.Constants;

import java.util.Map;

/**
 * ExchangeManager 基础示例：演示如何初始化并进行基础查询与占位下单。
 *
 * 说明：
 * - 默认连接测试网（Constants.TESTNET_API_URL）。
 * - 若未设置 HL_PK 环境变量，则跳过真实下单，仅演示 Info 查询。
 */
public class ExchangeManagerBasicExample {
    /**
     * 程序入口：初始化 ExchangeManager 并进行基础演示。
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        // 可通过环境变量提供私钥（测试网），例如：$env:HL_PK="0x..."
        String pk = System.getenv("HL_PK");

        ExchangeManager manager = ExchangeManager.builder()
                .baseUrl(Constants.TESTNET_API_URL)
                .timeout(10)
                .skipWs(true)
                .build();

        Info info = manager.getInfo();
        Map<String, String> mids = info.allMids();
        System.out.println("[Info] 币种数量: " + mids.size());
        System.out.println("[Info] BTC 中间价: " + mids.getOrDefault("BTC", "N/A"));

        if (pk != null && !pk.isBlank()) {
            // 当提供了私钥时，创建包含该私钥的 ExchangeManager 并进行占位下单演示
            manager = ExchangeManager.builder()
                    .baseUrl(Constants.TESTNET_API_URL)
                    .timeout(10)
                    .skipWs(true)
                    .addPrivateKey(pk)
                    .build();

            Exchange ex = manager.getSingleExchange();
            OrderType type = new OrderType(new LimitOrderType("Gtc"), null);
            OrderRequest req = new OrderRequest("ETH", true, 0.01, 2500.0, type, false, null);
            System.out.println("[Exchange] 下单响应: " + ex.order(req).toPrettyString());
        } else {
            System.out.println("[ExchangeManager] 未提供 HL_PK，跳过真实下单示例。");
        }
    }
}
