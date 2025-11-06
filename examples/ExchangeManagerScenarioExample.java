import com.fasterxml.jackson.databind.JsonNode;
import io.github.hyperliquid.sdk.ExchangeManager;
import io.github.hyperliquid.sdk.exchange.Exchange;
import io.github.hyperliquid.sdk.info.Info;
import io.github.hyperliquid.sdk.model.Cloid;
import io.github.hyperliquid.sdk.model.order.LimitOrderType;
import io.github.hyperliquid.sdk.model.order.OrderRequest;
import io.github.hyperliquid.sdk.model.order.OrderType;
import io.github.hyperliquid.sdk.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * ExchangeManager 典型业务场景示例：
 * - 限价下单（含 Cloid）
 * - 批量下单
 * - 按 Cloid 撤单
 * - 按 OID 撤单（演示，需替换为真实 OID）
 * - 修改订单（演示，需替换为真实 OID）
 *
 * 注意：
 * - 默认连接测试网。
 * - 需要通过环境变量 HL_PK 提供测试网私钥，否则仅输出提示并跳过真实交易动作。
 */
public class ExchangeManagerScenarioExample {
    /**
     * 程序入口：执行典型交易流程示例。
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        String pk = System.getenv("HL_PK");
        if (pk == null || pk.isBlank()) {
            System.out.println("[Scenario] 未提供 HL_PK，跳过真实交易动作。");
            return;
        }

        ExchangeManager manager = ExchangeManager.builder()
                .baseUrl(Constants.TESTNET_API_URL)
                .timeout(10)
                .skipWs(true)
                .addPrivateKey(pk)
                .build();

        Info info = manager.getInfo();
        System.out.println("[Info] BTC 中间价: " + info.allMids().getOrDefault("BTC", "N/A"));

        Exchange ex = manager.getSingleExchange();

        // 1) 限价下单（含 Cloid）：买入 0.01 ETH @ 2500.0
        String cloidRaw = "demo-" + System.currentTimeMillis();
        OrderType type = new OrderType(new LimitOrderType("Gtc"), null);
        OrderRequest singleReq = new OrderRequest("ETH", true, 0.01, 2500.0, type, false, new Cloid(cloidRaw));
        JsonNode orderResp = ex.order(singleReq);
        System.out.println("[order] resp: " + orderResp.toPrettyString());

        // 2) 批量下单：示例包含两笔订单
        List<OrderRequest> batch = new ArrayList<>();
        batch.add(new OrderRequest("ETH", false, 0.02, 2600.0, type, false, null));
        batch.add(new OrderRequest("BTC", true, 0.001, 30000.0, type, false, null));
        JsonNode bulkResp = ex.bulkOrders(batch);
        System.out.println("[bulkOrders] resp: " + bulkResp.toPrettyString());

        // 3) 按 Cloid 撤单（基于第 1 步的 cloidRaw）
        JsonNode cancelByCloidResp = ex.cancelByCloid("ETH", cloidRaw);
        System.out.println("[cancelByCloid] resp: " + cancelByCloidResp.toPrettyString());

        // 4) 按 OID 撤单（演示需要替换为真实 OID）
        int sampleOid = 123456789; // 请替换为你的真实 OID
        JsonNode cancelResp = ex.cancel("ETH", sampleOid);
        System.out.println("[cancel by oid] resp: " + cancelResp.toPrettyString());

        // 5) 修改订单（演示需要替换为真实 OID）：将价格改为 2550.0
        OrderRequest modified = new OrderRequest("ETH", true, 0.01, 2550.0, type, false, null);
        JsonNode modifyResp = ex.modifyOrder("ETH", sampleOid, modified);
        System.out.println("[modifyOrder] resp: " + modifyResp.toPrettyString());
    }
}
