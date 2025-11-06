import io.github.hyperliquid.sdk.ExchangeManager;
import io.github.hyperliquid.sdk.info.Info;
import io.github.hyperliquid.sdk.exchange.Exchange;
import io.github.hyperliquid.sdk.model.order.*;
import io.github.hyperliquid.sdk.utils.Constants;

/**
 * QuickExchangeManagerDemoCN
 * 中文快速示例：初始化 ExchangeManager，在测试网打印中间价并按需演示下单。
 * 使用方法：
 * 1) 构建与依赖复制：
 *    mvn -q -DskipTests=true clean package
 *    mvn -q dependency:copy-dependencies
 * 2) 编译：
 *    javac -cp "target/classes;target/dependency/*" examples\QuickExchangeManagerDemoCN.java
 * 3) 运行（无 HL_PK 将跳过真实下单）：
 *    java -cp ".;examples;target/classes;target/dependency/*" QuickExchangeManagerDemoCN
 */
public class QuickExchangeManagerDemoCN {
    /**
     * 主入口：
     * - 如设置了环境变量 HL_PK（你的私钥），将进行真实下单演示；
     * - 未设置 HL_PK 时，仅打印中间价并跳过真实下单。
     */
    public static void main(String[] args) {
        // 读取环境变量 HL_PK（若未设置则使用占位符）
        String pk = System.getenv("HL_PK");

        // 初始化 ExchangeManager（连接测试网，跳过 WebSocket）
        ExchangeManager manager = ExchangeManager.builder()
                .baseUrl(Constants.TESTNET_API_URL)
                .timeout(10)
                .skipWs(true)
                .addPrivateKey(pk == null || pk.isBlank() ? "0x0000000000000000000000000000000000000000000000000000000000000000" : pk)
                .build();

        // 获取 Info 并打印 BTC 中间价
        Info info = manager.getInfo();
        System.out.println("BTC 中间价: " + info.allMids().getOrDefault("BTC", "N/A"));

        // 如提供私钥则进行真实下单演示
        if (pk != null && !pk.isBlank()) {
            Exchange ex = manager.getSingleExchange();
            OrderType type = new OrderType(new LimitOrderType("Gtc"), null);
            OrderRequest req = new OrderRequest("ETH", true, 0.01, 2500.0, type, false, null);
            System.out.println(ex.order(req).toPrettyString());
        } else {
            System.out.println("未提供私钥，跳过真实下单示例。");
        }
    }
}

