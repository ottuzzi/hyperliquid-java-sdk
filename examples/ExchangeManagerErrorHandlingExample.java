import io.github.hyperliquid.sdk.ExchangeManager;
import io.github.hyperliquid.sdk.exchange.Exchange;
import io.github.hyperliquid.sdk.utils.Constants;
import io.github.hyperliquid.sdk.utils.HypeError;

/**
 * ExchangeManager 错误处理示例：
 * - 无私钥构建后尝试 getSingleExchange 的异常示范
 * - 使用未知私钥调用 useExchange 的异常示范
 * - 非法私钥构建触发 web3j Credentials.create 的异常捕获
 */
public class ExchangeManagerErrorHandlingExample {
    /**
     * 程序入口：演示异常捕获与安全使用。
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        // 1) 无私钥构建，调用 getSingleExchange 将抛出 HypeError
        try {
            ExchangeManager noKeyManager = ExchangeManager.builder()
                    .baseUrl(Constants.TESTNET_API_URL)
                    .skipWs(true)
                    .timeout(10)
                    .build();
            Exchange ex = noKeyManager.getSingleExchange();
            System.out.println("[unexpected] 获取到 Exchange: " + ex);
        } catch (HypeError e) {
            System.out.println("[expected] 无私钥时 getSingleExchange 异常: " + e.getMessage());
        }

        // 2) 使用未知私钥调用 useExchange
        try {
            ExchangeManager manager = ExchangeManager.builder()
                    .baseUrl(Constants.TESTNET_API_URL)
                    .skipWs(true)
                    .timeout(10)
                    .addPrivateKey("0x0123456789012345678901234567890123456789012345678901234567890123")
                    .build();
            manager.useExchange("0xabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcd");
        } catch (HypeError e) {
            System.out.println("[expected] 未知私钥 useExchange 异常: " + e.getMessage());
        }

        // 3) 非法私钥构建：长度或格式不正确会抛出 RuntimeException/IllegalArgumentException
        try {
            ExchangeManager.builder()
                    .baseUrl(Constants.TESTNET_API_URL)
                    .skipWs(true)
                    .timeout(10)
                    .addPrivateKey("not-a-valid-private-key")
                    .build();
            System.out.println("[unexpected] 非法私钥构建成功（不应发生）");
        } catch (RuntimeException e) {
            System.out.println("[expected] 非法私钥构建异常: " + e.getMessage());
        }
    }
}
