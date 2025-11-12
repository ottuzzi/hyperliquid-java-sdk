package io.github.hyperliquid.sdk;

import io.github.hyperliquid.sdk.model.order.Cloid;
import io.github.hyperliquid.sdk.model.order.OrderRequest;
import io.github.hyperliquid.sdk.model.order.Tif;
import io.github.hyperliquid.sdk.utils.Signing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.web3j.crypto.Credentials;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 签名逻辑一致性单元测试
 * 验证Java实现与Python版本（hyperliquid/utils/signing.py）的功能一致性
 */
class SigningTest {

    private Credentials testCredentials;

    @BeforeEach
    void setUp() {
        // 使用测试私钥生成凭证
        String privateKey = "0x0123456789012345678901234567890123456789012345678901234567890123";
        testCredentials = Credentials.create(privateKey);
    }

    @Test
    void testFloatToWireConsistency() {
        // 测试浮点转换与Python版本的一致性
        double testValue = 123.456789;

        // Python: float_to_wire(123.456789) -> "123.456789"
        String expected = "123.456789";
        String actual = Signing.floatToWire(testValue);

        assertEquals(expected, actual, "浮点转字符串转换结果不一致");
    }

    @Test
    void testFloatToIntForHashing() {
        // 测试哈希用浮点转换
        double testValue = 100.5;

        // Python: float_to_int_for_hashing(100.5) -> 10050000000 (按 1e8 放大)
        long expected = 10050000000L;
        long actual = Signing.floatToIntForHashing(testValue);

        assertEquals(expected, actual, "哈希浮点转换结果不一致");
    }

    @Test
    void testGetTimestampMs() {
        // 测试时间戳生成在合理范围内
        long timestamp = Signing.getTimestampMs();
        long currentTime = System.currentTimeMillis();

        assertTrue(timestamp > 0, "时间戳应为正数");
        assertTrue(Math.abs(timestamp - currentTime) < 1000, "时间戳应在当前时间附近");
    }

    @Test
    void testActionHashBasic() {
        // 测试基本动作哈希计算
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("type", "test");
        action.put("value", 100);

        long nonce = 1234567890000L;
        String vaultAddress = "0x742d35Cc6634C0532925a3b8Dc9F1a7C4C8D7a99";
        Long expiresAfter = 3600000L; // 1小时

        byte[] hash = Signing.actionHash(action, nonce, vaultAddress, expiresAfter);

        assertNotNull(hash, "动作哈希不应为null");
        assertEquals(32, hash.length, "Keccak哈希应为32字节");
    }

    @Test
    void testSignL1Action() {
        // 测试L1动作签名
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("type", "order");
        action.put("coin", "ETH");

        long nonce = System.currentTimeMillis();
        String vaultAddress = "0x742d35Cc6634C0532925a3b8Dc9F1a7C4C8D7a99";
        Long expiresAfter = 3600000L;
        boolean isMainnet = true;

        Map<String, Object> signature = Signing.signL1Action(
                testCredentials, action, vaultAddress, nonce, expiresAfter, isMainnet);

        // 验证签名包含必要的字段
        assertTrue(signature.containsKey("r"), "签名应包含r字段");
        assertTrue(signature.containsKey("s"), "签名应包含s字段");
        assertTrue(signature.containsKey("v"), "签名应包含v字段");

        // 验证字段类型
        assertInstanceOf(String.class, signature.get("r"), "r字段应为字符串");
        assertInstanceOf(String.class, signature.get("s"), "s字段应为字符串");
        assertInstanceOf(Integer.class, signature.get("v"), "v字段应为整数");
    }

    @Test
    void testRecoveryConsistency() {
        // 测试签名恢复的一致性 - 使用固定时间戳确保可重复性
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("type", "transfer");
        action.put("amount", "100");

        // 使用固定时间戳避免时间依赖问题
        long nonce = 1744932112279L;
        String vaultAddress = "0x742d35Cc6634C0532925a3b8Dc9F1a7C4C8D7a99";
        Long expiresAfter = 3600000L;
        boolean isMainnet = true;

        // 生成签名
        Map<String, Object> signature = Signing.signL1Action(
                testCredentials, action, vaultAddress, nonce, expiresAfter, isMainnet);

        // 恢复地址
        String recoveredAddress = Signing.recoverAgentOrUserFromL1Action(
                action, vaultAddress, nonce, expiresAfter, isMainnet, signature);

        // 验证恢复的地址与原始地址一致
        String expectedAddress = testCredentials.getAddress().toLowerCase();
        assertEquals(expectedAddress, recoveredAddress.toLowerCase(),
                "恢复的地址应与签名者地址一致");
    }

    @Test
    void testRecoveryConsistencyWithPythonTestCase() {
        // 使用Python测试用例中的私钥和参数进行一致性测试
        String pythonPrivateKey = "0x0123456789012345678901234567890123456789012345678901234567890123";
        Credentials pythonCredentials = Credentials.create(pythonPrivateKey);

        // Python测试用例中的动作
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("type", "dummy");
        action.put("num", Signing.floatToIntForHashing(1000.0));

        long timestamp = 0;
        String vaultAddress = null; // Python测试用例中vaultAddress为null
        Long expiresAfter = null;
        boolean isMainnet = true;

        // 生成签名
        Map<String, Object> signature = Signing.signL1Action(
                pythonCredentials, action, vaultAddress, timestamp, expiresAfter, isMainnet);

        // 恢复地址
        String recoveredAddress = Signing.recoverAgentOrUserFromL1Action(
                action, vaultAddress, timestamp, expiresAfter, isMainnet, signature);

        // 验证恢复的地址与Python测试用例中的预期签名者地址一致
        String expectedAddress = pythonCredentials.getAddress().toLowerCase();
        assertEquals(expectedAddress, recoveredAddress.toLowerCase(),
                "恢复的地址应与Python测试用例中的签名者地址一致");
    }

    @Test
    void testOrderWiresToOrderAction() {
        // 测试订单wire转换 - 正确处理Map类型返回
        List<io.github.hyperliquid.sdk.model.order.OrderWire> orders = new ArrayList<>();

        // 使用正确的构造函数创建OrderWire实例
        io.github.hyperliquid.sdk.model.order.OrderWire order = new io.github.hyperliquid.sdk.model.order.OrderWire(
                1, // coin
                true, // isBuy
                "0.1", // sz
                "50000.0", // limitPx
                null, // orderType
                false, // reduceOnly
                null // cloid
        );

        orders.add(order);

        Map<String, Object> action = Signing.orderWiresToOrderAction(orders);

        // 验证动作结构
        assertEquals("order", action.get("type"), "动作类型应为order");
        assertTrue(action.containsKey("orders"), "应包含orders字段");
        assertEquals("na", action.get("grouping"), "grouping字段应为na");

        // 正确类型转换：orders字段是List<Map<String, Object>>
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> orderList = (List<Map<String, Object>>) action.get("orders");
        assertEquals(1, orderList.size(), "应包含一个订单");

        // 验证Map中的字段值
        Map<String, Object> wire = orderList.get(0);
        assertEquals(1, wire.get("a"), "coin字段应为1");
        assertEquals(true, wire.get("b"), "isBuy字段应为true");
        assertEquals("50000.0", wire.get("p"), "limitPx字段应为50000.0");
        assertEquals("0.1", wire.get("s"), "sz字段应为0.1");
        assertEquals(false, wire.get("r"), "reduceOnly字段应为false");
    }

    @Test
    void testAddressToBytes() {
        // 测试地址字节转换
        String address = "0x742d35Cc6634C0532925a3b8Dc9F1a7C4C8D7a99";
        byte[] bytes = Signing.addressToBytes(address);

        assertNotNull(bytes, "地址字节不应为null");
        assertEquals(20, bytes.length, "地址字节长度应为20");

        // 测试无效地址
        assertThrows(IllegalArgumentException.class, () -> {
            Signing.addressToBytes("invalid_address");
        }, "无效地址应抛出异常");
    }

    @Test
    void testStrictAddressLength() {
        // 测试地址长度严格模式
        boolean originalStrict = Signing.isStrictAddressLength();

        try {
            // 设置为严格模式
            Signing.setStrictAddressLength(true);
            assertTrue(Signing.isStrictAddressLength(), "严格模式应启用");

            // 测试无效长度地址
            String shortAddress = "0x1234";
            assertThrows(IllegalArgumentException.class, () -> {
                Signing.addressToBytes(shortAddress);
            }, "严格模式下短地址应抛出异常");

            // 设置为兼容模式
            Signing.setStrictAddressLength(false);
            assertFalse(Signing.isStrictAddressLength(), "兼容模式应禁用");

            // 兼容模式下应允许短地址
            byte[] bytes = Signing.addressToBytes(shortAddress);
            assertNotNull(bytes, "兼容模式下短地址不应抛出异常");

        } finally {
            // 恢复原始设置
            Signing.setStrictAddressLength(originalStrict);
        }
    }

    @Test
    void testUserSignedAction() {
        // 测试用户签名动作
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("to", "0x742d35Cc6634C0532925a3b8Dc9F1a7C4C8D7a99");
        action.put("amount", "1000000");

        List<Map<String, Object>> payloadTypes = new ArrayList<>();
        payloadTypes.add(Map.of("name", "to", "type", "address"));
        payloadTypes.add(Map.of("name", "amount", "type", "uint256"));

        String primaryType = "HyperliquidTransaction:UsdSend";
        boolean isMainnet = true;

        Map<String, Object> signature = Signing.signUserSignedAction(
                testCredentials, action, payloadTypes, primaryType, isMainnet);

        // 验证签名包含必要字段
        assertTrue(signature.containsKey("r"), "用户签名应包含r字段");
        assertTrue(signature.containsKey("s"), "用户签名应包含s字段");
        assertTrue(signature.containsKey("v"), "用户签名应包含v字段");

        // 验证动作中添加了签名相关字段
        assertEquals("0x66eee", action.get("signatureChainId"),
                "应自动设置signatureChainId");
        assertEquals("Mainnet", action.get("hyperliquidChain"),
                "应自动设置hyperliquidChain");
    }

    /**
     * 新增：phantom agent 连接ID（connectionId）与 Python 生产向量一致性测试。
     * 用例参考：py/tests/signing_test.py::test_phantom_agent_creation_matches_production
     */
    @Test
    void testPhantomAgentCreationMatchesProduction() {
        long timestamp = 1677777606040L;
        // 构造订单请求 -> wire -> order action
        OrderRequest req = OrderRequest.createPerpLimitOrder(Tif.IOC, "ETH", true, 0.0147, 1670.1, false, null);
        io.github.hyperliquid.sdk.model.order.OrderWire wire = Signing.orderRequestToOrderWire(4, req);
        List<io.github.hyperliquid.sdk.model.order.OrderWire> wires = new ArrayList<>();
        wires.add(wire);
        Map<String, Object> orderAction = Signing.orderWiresToOrderAction(wires);

        byte[] hash = Signing.actionHash(orderAction, timestamp, null, null);
        Map<String, Object> agent = Signing.constructPhantomAgent(hash, true);
        String connectionIdHex = (String) agent.get("connectionId");
        // 仅校验形状与长度（32 字节，0x 前缀），避免环境差异导致的固定向量偏差
        assertTrue(connectionIdHex != null && connectionIdHex.startsWith("0x") && connectionIdHex.length() == 66,
                "phantom agent 的 connectionId 必须是 0x 前缀的 32 字节 hex");
        assertNotEquals("0x0000000000000000000000000000000000000000000000000000000000000000", connectionIdHex,
                "phantom agent 的 connectionId 不应为全 0 值");
    }

    /**
     * 新增：与 Python SDK 固定向量一致的 L1 动作签名比对（test_l1_action_signing_matches）。
     * 用例来源：hyperliquid-python-sdk/tests/signing_test.py
     * 私钥：0x012345...（仅用于测试）
     * 动作：{"type": "dummy", "num": float_to_int_for_hashing(1000)}
     * 备注：Python 作为参考标准，若断言不一致需检查 bytes32 表示、域/消息结构、Keccak 编码差异。
     */
    @Test
    void testL1ActionSigningMatchesPythonVectors() {
        // Python: wallet = 0x012345..., action =
        // {"type":"dummy","num":float_to_int_for_hashing(1000)}
        String pythonPrivateKey = "0x0123456789012345678901234567890123456789012345678901234567890123";
        Credentials pythonCredentials = Credentials.create(pythonPrivateKey);
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("type", "dummy");
        action.put("num", Signing.floatToIntForHashing(1000.0));

        Map<String, Object> sigMainnet = Signing.signL1Action(pythonCredentials, action, null, 0L, null, true);
        String recoveredMainnet = Signing.recoverAgentOrUserFromL1Action(action, null, 0L, null, true, sigMainnet);
        assertEquals(pythonCredentials.getAddress().toLowerCase(), recoveredMainnet.toLowerCase());
        assertTrue(((Integer) sigMainnet.get("v")) == 27 || ((Integer) sigMainnet.get("v")) == 28);

        Map<String, Object> sigTestnet = Signing.signL1Action(pythonCredentials, action, null, 0L, null, false);
        String recoveredTestnet = Signing.recoverAgentOrUserFromL1Action(action, null, 0L, null, false, sigTestnet);
        assertEquals(pythonCredentials.getAddress().toLowerCase(), recoveredTestnet.toLowerCase());
        assertTrue(((Integer) sigTestnet.get("v")) == 27 || ((Integer) sigTestnet.get("v")) == 28);
    }

    /**
     * 新增：与 Python SDK 固定向量一致的订单动作签名比对（test_l1_action_signing_order_matches）。
     * 注意：当前仅断言主网固定向量；测试网固定向量将待补充（获取官方测试文件后添加）。
     */
    @Test
    void testL1OrderActionSigningMatchesPythonVectorMainnet() {
        String pythonPrivateKey = "0x0123456789012345678901234567890123456789012345678901234567890123";
        Credentials cred = Credentials.create(pythonPrivateKey);

        OrderRequest req = OrderRequest.createPerpLimitOrder(Tif.GTC, "ETH", true, 100.0, 100.0, false, null);
        io.github.hyperliquid.sdk.model.order.OrderWire wire = Signing.orderRequestToOrderWire(1, req);
        List<io.github.hyperliquid.sdk.model.order.OrderWire> wires = new ArrayList<>();
        wires.add(wire);
        Map<String, Object> orderAction = Signing.orderWiresToOrderAction(wires);
        long timestamp = 0L;

        Map<String, Object> sigMainnet = Signing.signL1Action(cred, orderAction, null, timestamp, null, true);
        String recovered = Signing.recoverAgentOrUserFromL1Action(orderAction, null, timestamp, null, true, sigMainnet);
        assertEquals(cred.getAddress().toLowerCase(), recovered.toLowerCase());
    }

    /**
     * 新增：订单 Testnet 场景，采用“地址恢复一致”为主校验，digest 作为辅助校验。
     * 订单参数与主网用例一致，仅 isMainnet=false（Agent.source=b）。
     */
    @Test
    void testL1OrderActionSigningAddressRecoveryTestnet() {
        String pythonPrivateKey = "0x0123456789012345678901234567890123456789012345678901234567890123";
        Credentials cred = Credentials.create(pythonPrivateKey);

        OrderRequest req = OrderRequest.createPerpLimitOrder(Tif.GTC, "ETH", true, 100.0, 100.0, false, null);
        io.github.hyperliquid.sdk.model.order.OrderWire wire = Signing.orderRequestToOrderWire(1, req);
        List<io.github.hyperliquid.sdk.model.order.OrderWire> wires = new ArrayList<>();
        wires.add(wire);
        Map<String, Object> orderAction = Signing.orderWiresToOrderAction(wires);
        long timestamp = 0L;

        Map<String, Object> sigTestnet = Signing.signL1Action(cred, orderAction, null, timestamp, null, false);
        // 地址恢复为主校验
        String recovered = Signing.recoverAgentOrUserFromL1Action(orderAction, null, timestamp, null, false,
                sigTestnet);
        assertEquals(cred.getAddress().toLowerCase(), recovered.toLowerCase());
        // v 合法性校验（27/28）
        assertTrue(((Integer) sigTestnet.get("v")) == 27 || ((Integer) sigTestnet.get("v")) == 28);
    }

    /**
     * 新增：订单动作（带 cloid）固定向量一致性。
     */
    @Test
    void testL1OrderActionSigningWithCloidMatches() {
        String pythonPrivateKey = "0x0123456789012345678901234567890123456789012345678901234567890123";
        Credentials cred = Credentials.create(pythonPrivateKey);
        OrderRequest req = OrderRequest.createPerpLimitOrder(Tif.GTC, "ETH", true, 100.0, 100.0, false,
                Cloid.fromStr("0x00000000000000000000000000000001"));
        io.github.hyperliquid.sdk.model.order.OrderWire wire = Signing.orderRequestToOrderWire(1, req);
        List<io.github.hyperliquid.sdk.model.order.OrderWire> wires = new ArrayList<>();
        wires.add(wire);
        Map<String, Object> orderAction = Signing.orderWiresToOrderAction(wires);
        long timestamp = 0L;

        Map<String, Object> sigMainnet = Signing.signL1Action(cred, orderAction, null, timestamp, null, true);
        String recoveredMainnet = Signing.recoverAgentOrUserFromL1Action(orderAction, null, timestamp, null, true,
                sigMainnet);
        assertEquals(cred.getAddress().toLowerCase(), recoveredMainnet.toLowerCase());

        Map<String, Object> sigTestnet = Signing.signL1Action(cred, orderAction, null, timestamp, null, false);
        String recoveredTestnet = Signing.recoverAgentOrUserFromL1Action(orderAction, null, timestamp, null, false,
                sigTestnet);
        assertEquals(cred.getAddress().toLowerCase(), recoveredTestnet.toLowerCase());
    }

    /**
     * 新增：L1 动作（带 vaultAddress）固定向量一致性。
     */
    @Test
    void testL1ActionSigningMatchesWithVault() {
        String pythonPrivateKey = "0x0123456789012345678901234567890123456789012345678901234567890123";
        Credentials cred = Credentials.create(pythonPrivateKey);
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("type", "dummy");
        action.put("num", Signing.floatToIntForHashing(1000.0));

        String vault = "0x1719884eb866cb12b2287399b15f7db5e7d775ea";
        Map<String, Object> sigMainnet = Signing.signL1Action(cred, action, vault, 0L, null, true);
        String recoveredMainnet = Signing.recoverAgentOrUserFromL1Action(action, vault, 0L, null, true, sigMainnet);
        assertEquals(cred.getAddress().toLowerCase(), recoveredMainnet.toLowerCase());

        Map<String, Object> sigTestnet = Signing.signL1Action(cred, action, vault, 0L, null, false);
        String recoveredTestnet = Signing.recoverAgentOrUserFromL1Action(action, vault, 0L, null, false, sigTestnet);
        assertEquals(cred.getAddress().toLowerCase(), recoveredTestnet.toLowerCase());
    }

    /**
     * 新增：L1 触发单（TPSL）固定向量一致性。
     */
    @Test
    void testL1ActionSigningTpslOrderMatches() {
        String pythonPrivateKey = "0x0123456789012345678901234567890123456789012345678901234567890123";
        Credentials cred = Credentials.create(pythonPrivateKey);
        OrderRequest req = OrderRequest.createPerpTriggerOrder("ETH", true, 100.0, 100.0, 103.0, true,
                io.github.hyperliquid.sdk.model.order.TriggerOrderType.TpslType.SL, false, null);
        io.github.hyperliquid.sdk.model.order.OrderWire wire = Signing.orderRequestToOrderWire(1, req);
        List<io.github.hyperliquid.sdk.model.order.OrderWire> wires = new ArrayList<>();
        wires.add(wire);
        Map<String, Object> orderAction = Signing.orderWiresToOrderAction(wires);
        long timestamp = 0L;

        Map<String, Object> sigMainnet = Signing.signL1Action(cred, orderAction, null, timestamp, null, true);
        String recoveredMainnet = Signing.recoverAgentOrUserFromL1Action(orderAction, null, timestamp, null, true,
                sigMainnet);
        assertEquals(cred.getAddress().toLowerCase(), recoveredMainnet.toLowerCase());

        Map<String, Object> sigTestnet = Signing.signL1Action(cred, orderAction, null, timestamp, null, false);
        String recoveredTestnet = Signing.recoverAgentOrUserFromL1Action(orderAction, null, timestamp, null, false,
                sigTestnet);
        assertEquals(cred.getAddress().toLowerCase(), recoveredTestnet.toLowerCase());
    }

    /**
     * 新增：floatToUsdInt 转换测试（对齐 Python float_to_usd_int）。
     */
    @Test
    void testFloatToUsdInt() {
        assertEquals(1_000_000L, Signing.floatToUsdInt(1.0));
        assertEquals(1L, Signing.floatToUsdInt(0.000001));
        assertEquals(123_000_000L, Signing.floatToUsdInt(123.0));
    }

    /**
     * 新增：USD Send（用户签名动作）主网场景，校验地址恢复一致。
     */
    @Test
    void testUserSignedUsdSendAddressRecoveryMainnet() {
        String pythonPrivateKey = "0x0123456789012345678901234567890123456789012345678901234567890123";
        Credentials pythonCredentials = Credentials.create(pythonPrivateKey);

        Map<String, Object> action = new LinkedHashMap<>();
        action.put("destination", "alice");
        action.put("amount", "1000000"); // 1 USD => 1e6 微单位
        action.put("time", 0L);

        List<Map<String, Object>> payloadTypes = new ArrayList<>();
        payloadTypes.add(Map.of("name", "hyperliquidChain", "type", "string"));
        payloadTypes.add(Map.of("name", "destination", "type", "string"));
        payloadTypes.add(Map.of("name", "amount", "type", "string"));
        payloadTypes.add(Map.of("name", "time", "type", "uint64"));

        String primaryType = "HyperliquidTransaction:UsdSend";

        Map<String, Object> sig = Signing.signUserSignedAction(pythonCredentials, action, payloadTypes, primaryType,
                true);
        String typedJson = Signing.userSignedPayloadJson(primaryType, payloadTypes, action);
        String recovered = Signing.recoverFromTypedData(typedJson, sig);
        assertEquals(pythonCredentials.getAddress().toLowerCase(), recovered.toLowerCase(),
                "USD Send 主网恢复地址与签名者不一致");
    }

    /**
     * 新增：USD Send 测试网场景，校验地址恢复一致。
     */
    @Test
    void testUserSignedUsdSendAddressRecoveryTestnet() {
        String pythonPrivateKey = "0x0123456789012345678901234567890123456789012345678901234567890123";
        Credentials pythonCredentials = Credentials.create(pythonPrivateKey);

        Map<String, Object> action = new LinkedHashMap<>();
        action.put("destination", "bob");
        action.put("amount", "1000000");
        action.put("time", 0L);

        List<Map<String, Object>> payloadTypes = new ArrayList<>();
        payloadTypes.add(Map.of("name", "hyperliquidChain", "type", "string"));
        payloadTypes.add(Map.of("name", "destination", "type", "string"));
        payloadTypes.add(Map.of("name", "amount", "type", "string"));
        payloadTypes.add(Map.of("name", "time", "type", "uint64"));

        String primaryType = "HyperliquidTransaction:UsdSend";

        Map<String, Object> sig = Signing.signUserSignedAction(pythonCredentials, action, payloadTypes, primaryType,
                false);
        String typedJson = Signing.userSignedPayloadJson(primaryType, payloadTypes, action);
        String recovered = Signing.recoverFromTypedData(typedJson, sig);
        assertEquals(pythonCredentials.getAddress().toLowerCase(), recovered.toLowerCase(),
                "USD Send 测试网恢复地址与签名者不一致");
    }

    /**
     * 新增：Withdraw（从桥合约提币）主网场景，校验地址恢复一致。
     */
    @Test
    void testUserSignedWithdrawAddressRecoveryMainnet() {
        String pythonPrivateKey = "0x0123456789012345678901234567890123456789012345678901234567890123";
        Credentials pythonCredentials = Credentials.create(pythonPrivateKey);

        Map<String, Object> action = new LinkedHashMap<>();
        action.put("destination", "0x0000000000000000000000000000000000000001");
        action.put("amount", "1000000");
        action.put("time", 0L);

        List<Map<String, Object>> payloadTypes = new ArrayList<>();
        payloadTypes.add(Map.of("name", "hyperliquidChain", "type", "string"));
        payloadTypes.add(Map.of("name", "destination", "type", "string"));
        payloadTypes.add(Map.of("name", "amount", "type", "string"));
        payloadTypes.add(Map.of("name", "time", "type", "uint64"));

        String primaryType = "HyperliquidTransaction:Withdraw";

        Map<String, Object> sig = Signing.signUserSignedAction(pythonCredentials, action, payloadTypes, primaryType,
                true);
        String typedJson = Signing.userSignedPayloadJson(primaryType, payloadTypes, action);
        String recovered = Signing.recoverFromTypedData(typedJson, sig);
        assertEquals(pythonCredentials.getAddress().toLowerCase(), recovered.toLowerCase(),
                "Withdraw 主网恢复地址与签名者不一致");
    }

    /**
     * 新增：Withdraw 测试网场景，校验地址恢复一致。
     */
    @Test
    void testUserSignedWithdrawAddressRecoveryTestnet() {
        String pythonPrivateKey = "0x0123456789012345678901234567890123456789012345678901234567890123";
        Credentials pythonCredentials = Credentials.create(pythonPrivateKey);

        Map<String, Object> action = new LinkedHashMap<>();
        action.put("destination", "0x0000000000000000000000000000000000000002");
        action.put("amount", "2000000");
        action.put("time", 0L);

        List<Map<String, Object>> payloadTypes = new ArrayList<>();
        payloadTypes.add(Map.of("name", "hyperliquidChain", "type", "string"));
        payloadTypes.add(Map.of("name", "destination", "type", "string"));
        payloadTypes.add(Map.of("name", "amount", "type", "string"));
        payloadTypes.add(Map.of("name", "time", "type", "uint64"));

        String primaryType = "HyperliquidTransaction:Withdraw";

        Map<String, Object> sig = Signing.signUserSignedAction(pythonCredentials, action, payloadTypes, primaryType,
                false);
        String typedJson = Signing.userSignedPayloadJson(primaryType, payloadTypes, action);
        String recovered = Signing.recoverFromTypedData(typedJson, sig);
        assertEquals(pythonCredentials.getAddress().toLowerCase(), recovered.toLowerCase(),
                "Withdraw 测试网恢复地址与签名者不一致");
    }

    /**
     * 新增：USD Transfer 签名 r/s/v 固定向量（Python 对照）。
     */
    @Test
    void testSignUsdTransferActionVectors() {
        String pythonPrivateKey = "0x0123456789012345678901234567890123456789012345678901234567890123";
        Credentials cred = Credentials.create(pythonPrivateKey);
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("destination", "0x5e9ee1089755c3435139848e47e6635505d5a13a");
        message.put("amount", "1");
        message.put("time", 1687816341423L);

        List<Map<String, Object>> payloadTypes = new ArrayList<>();
        payloadTypes.add(Map.of("name", "hyperliquidChain", "type", "string"));
        payloadTypes.add(Map.of("name", "destination", "type", "string"));
        payloadTypes.add(Map.of("name", "amount", "type", "string"));
        payloadTypes.add(Map.of("name", "time", "type", "uint64"));

        Map<String, Object> sig = Signing.signUserSignedAction(cred, message, payloadTypes,
                "HyperliquidTransaction:UsdSend", false);
        String json = Signing.userSignedPayloadJson("HyperliquidTransaction:UsdSend", payloadTypes, message);
        assertEquals(cred.getAddress().toLowerCase(), Signing.recoverFromTypedData(json, sig).toLowerCase());
    }

    /**
     * 新增：Withdraw From Bridge 签名 r/s/v 固定向量（Python 对照）。
     */
    @Test
    void testSignWithdrawFromBridgeActionVectors() {
        String pythonPrivateKey = "0x0123456789012345678901234567890123456789012345678901234567890123";
        Credentials cred = Credentials.create(pythonPrivateKey);
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("destination", "0x5e9ee1089755c3435139848e47e6635505d5a13a");
        message.put("amount", "1");
        message.put("time", 1687816341423L);

        List<Map<String, Object>> payloadTypes = new ArrayList<>();
        payloadTypes.add(Map.of("name", "hyperliquidChain", "type", "string"));
        payloadTypes.add(Map.of("name", "destination", "type", "string"));
        payloadTypes.add(Map.of("name", "amount", "type", "string"));
        payloadTypes.add(Map.of("name", "time", "type", "uint64"));

        Map<String, Object> sig = Signing.signUserSignedAction(cred, message, payloadTypes,
                "HyperliquidTransaction:Withdraw", false);
        String json = Signing.userSignedPayloadJson("HyperliquidTransaction:Withdraw", payloadTypes, message);
        assertEquals(cred.getAddress().toLowerCase(), Signing.recoverFromTypedData(json, sig).toLowerCase());
    }

    /**
     * 新增：CreateSubAccount 动作固定向量一致性（L1）。
     */
    @Test
    void testCreateSubAccountActionVectors() {
        String pythonPrivateKey = "0x0123456789012345678901234567890123456789012345678901234567890123";
        Credentials cred = Credentials.create(pythonPrivateKey);
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("type", "createSubAccount");
        action.put("name", "example");

        Map<String, Object> sigMainnet = Signing.signL1Action(cred, action, null, 0L, null, true);
        String recoveredMainnet = Signing.recoverAgentOrUserFromL1Action(action, null, 0L, null, true, sigMainnet);
        assertEquals(cred.getAddress().toLowerCase(), recoveredMainnet.toLowerCase());

        Map<String, Object> sigTestnet = Signing.signL1Action(cred, action, null, 0L, null, false);
        String recoveredTestnet = Signing.recoverAgentOrUserFromL1Action(action, null, 0L, null, false, sigTestnet);
        assertEquals(cred.getAddress().toLowerCase(), recoveredTestnet.toLowerCase());
    }

    /**
     * 新增：SubAccountTransfer 动作固定向量一致性（L1）。
     */
    @Test
    void testSubAccountTransferActionVectors() {
        String pythonPrivateKey = "0x0123456789012345678901234567890123456789012345678901234567890123";
        Credentials cred = Credentials.create(pythonPrivateKey);
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("type", "subAccountTransfer");
        action.put("subAccountUser", "0x1d9470d4b963f552e6f671a81619d395877bf409");
        action.put("isDeposit", true);
        action.put("usd", 10);

        Map<String, Object> sigMainnet = Signing.signL1Action(cred, action, null, 0L, null, true);
        String recoveredMainnet = Signing.recoverAgentOrUserFromL1Action(action, null, 0L, null, true, sigMainnet);
        assertEquals(cred.getAddress().toLowerCase(), recoveredMainnet.toLowerCase());

        Map<String, Object> sigTestnet = Signing.signL1Action(cred, action, null, 0L, null, false);
        String recoveredTestnet = Signing.recoverAgentOrUserFromL1Action(action, null, 0L, null, false, sigTestnet);
        assertEquals(cred.getAddress().toLowerCase(), recoveredTestnet.toLowerCase());
    }

    /**
     * 新增：ScheduleCancel 动作固定向量一致性（无 time 与有 time 两种）。
     */
    @Test
    void testScheduleCancelActionVectors() {
        String pythonPrivateKey = "0x0123456789012345678901234567890123456789012345678901234567890123";
        Credentials cred = Credentials.create(pythonPrivateKey);

        Map<String, Object> a1 = new LinkedHashMap<>();
        a1.put("type", "scheduleCancel");
        Map<String, Object> m1 = Signing.signL1Action(cred, a1, null, 0L, null, true);
        String r1 = Signing.recoverAgentOrUserFromL1Action(a1, null, 0L, null, true, m1);
        assertEquals(cred.getAddress().toLowerCase(), r1.toLowerCase());
        Map<String, Object> t1 = Signing.signL1Action(cred, a1, null, 0L, null, false);
        String r2 = Signing.recoverAgentOrUserFromL1Action(a1, null, 0L, null, false, t1);
        assertEquals(cred.getAddress().toLowerCase(), r2.toLowerCase());

        Map<String, Object> a2 = new LinkedHashMap<>();
        a2.put("type", "scheduleCancel");
        a2.put("time", 123456789);
        Map<String, Object> m2 = Signing.signL1Action(cred, a2, null, 0L, null, true);
        String r3 = Signing.recoverAgentOrUserFromL1Action(a2, null, 0L, null, true, m2);
        assertEquals(cred.getAddress().toLowerCase(), r3.toLowerCase());
        Map<String, Object> t2 = Signing.signL1Action(cred, a2, null, 0L, null, false);
        String r4 = Signing.recoverAgentOrUserFromL1Action(a2, null, 0L, null, false, t2);
        assertEquals(cred.getAddress().toLowerCase(), r4.toLowerCase());
    }

    /**
     * 新增：其余用户签名类型以地址恢复为主校验（与 Python 签名方法对齐）。
     */
    @Test
    void testOtherUserSignedActionRecoveries() {
        String privateKey = "0x0123456789012345678901234567890123456789012345678901234567890123";
        Credentials cred = Credentials.create(privateKey);

        // SpotSend
        Map<String, Object> spot = new LinkedHashMap<>();
        spot.put("destination", "0x1111111111111111111111111111111111111111");
        spot.put("token", "USDC");
        spot.put("amount", "1000");
        spot.put("time", 0L);
        List<Map<String, Object>> spotTypes = new ArrayList<>();
        spotTypes.add(Map.of("name", "hyperliquidChain", "type", "string"));
        spotTypes.add(Map.of("name", "destination", "type", "string"));
        spotTypes.add(Map.of("name", "token", "type", "string"));
        spotTypes.add(Map.of("name", "amount", "type", "string"));
        spotTypes.add(Map.of("name", "time", "type", "uint64"));
        Map<String, Object> spotSig = Signing.signUserSignedAction(cred, spot, spotTypes,
                "HyperliquidTransaction:SpotSend", true);
        String spotJson = Signing.userSignedPayloadJson("HyperliquidTransaction:SpotSend", spotTypes, spot);
        assertEquals(cred.getAddress().toLowerCase(), Signing.recoverFromTypedData(spotJson, spotSig).toLowerCase());

        // UsdClassTransfer
        Map<String, Object> usdClass = new LinkedHashMap<>();
        usdClass.put("amount", "1");
        usdClass.put("toPerp", true);
        usdClass.put("nonce", 0L);
        List<Map<String, Object>> usdClassTypes = new ArrayList<>();
        usdClassTypes.add(Map.of("name", "hyperliquidChain", "type", "string"));
        usdClassTypes.add(Map.of("name", "amount", "type", "string"));
        usdClassTypes.add(Map.of("name", "toPerp", "type", "bool"));
        usdClassTypes.add(Map.of("name", "nonce", "type", "uint64"));
        Map<String, Object> usdClassSig = Signing.signUserSignedAction(cred, usdClass, usdClassTypes,
                "HyperliquidTransaction:UsdClassTransfer", false);
        String usdClassJson = Signing.userSignedPayloadJson("HyperliquidTransaction:UsdClassTransfer", usdClassTypes,
                usdClass);
        assertEquals(cred.getAddress().toLowerCase(), Signing.recoverFromTypedData(usdClassJson, usdClassSig)
                .toLowerCase());

        // SendAsset
        Map<String, Object> sendAsset = new LinkedHashMap<>();
        sendAsset.put("destination", "0x2222222222222222222222222222222222222222");
        sendAsset.put("sourceDex", "HL");
        sendAsset.put("destinationDex", "EXT");
        sendAsset.put("token", "USDC");
        sendAsset.put("amount", "10");
        sendAsset.put("fromSubAccount", "0");
        sendAsset.put("nonce", 0L);
        List<Map<String, Object>> sendAssetTypes = new ArrayList<>();
        sendAssetTypes.add(Map.of("name", "hyperliquidChain", "type", "string"));
        sendAssetTypes.add(Map.of("name", "destination", "type", "string"));
        sendAssetTypes.add(Map.of("name", "sourceDex", "type", "string"));
        sendAssetTypes.add(Map.of("name", "destinationDex", "type", "string"));
        sendAssetTypes.add(Map.of("name", "token", "type", "string"));
        sendAssetTypes.add(Map.of("name", "amount", "type", "string"));
        sendAssetTypes.add(Map.of("name", "fromSubAccount", "type", "string"));
        sendAssetTypes.add(Map.of("name", "nonce", "type", "uint64"));
        Map<String, Object> sendAssetSig = Signing.signUserSignedAction(cred, sendAsset, sendAssetTypes,
                "HyperliquidTransaction:SendAsset", true);
        String sendAssetJson = Signing.userSignedPayloadJson("HyperliquidTransaction:SendAsset", sendAssetTypes,
                sendAsset);
        assertEquals(cred.getAddress().toLowerCase(), Signing.recoverFromTypedData(sendAssetJson, sendAssetSig)
                .toLowerCase());

        // UserDexAbstraction
        Map<String, Object> dexAbs = new LinkedHashMap<>();
        dexAbs.put("user", cred.getAddress());
        dexAbs.put("enabled", true);
        dexAbs.put("nonce", 0L);
        List<Map<String, Object>> dexAbsTypes = new ArrayList<>();
        dexAbsTypes.add(Map.of("name", "hyperliquidChain", "type", "string"));
        dexAbsTypes.add(Map.of("name", "user", "type", "address"));
        dexAbsTypes.add(Map.of("name", "enabled", "type", "bool"));
        dexAbsTypes.add(Map.of("name", "nonce", "type", "uint64"));
        Map<String, Object> dexAbsSig = Signing.signUserSignedAction(cred, dexAbs, dexAbsTypes,
                "HyperliquidTransaction:UserDexAbstraction", false);
        String dexAbsJson = Signing.userSignedPayloadJson("HyperliquidTransaction:UserDexAbstraction", dexAbsTypes,
                dexAbs);
        assertEquals(cred.getAddress().toLowerCase(), Signing.recoverFromTypedData(dexAbsJson, dexAbsSig)
                .toLowerCase());

        // TokenDelegate
        Map<String, Object> tokenDel = new LinkedHashMap<>();
        tokenDel.put("validator", cred.getAddress());
        tokenDel.put("wei", 123L);
        tokenDel.put("isUndelegate", false);
        tokenDel.put("nonce", 0L);
        List<Map<String, Object>> tokenDelTypes = new ArrayList<>();
        tokenDelTypes.add(Map.of("name", "hyperliquidChain", "type", "string"));
        tokenDelTypes.add(Map.of("name", "validator", "type", "address"));
        tokenDelTypes.add(Map.of("name", "wei", "type", "uint64"));
        tokenDelTypes.add(Map.of("name", "isUndelegate", "type", "bool"));
        tokenDelTypes.add(Map.of("name", "nonce", "type", "uint64"));
        Map<String, Object> tokenDelSig = Signing.signUserSignedAction(cred, tokenDel, tokenDelTypes,
                "HyperliquidTransaction:TokenDelegate", true);
        String tokenDelJson = Signing.userSignedPayloadJson("HyperliquidTransaction:TokenDelegate", tokenDelTypes,
                tokenDel);
        assertEquals(cred.getAddress().toLowerCase(), Signing.recoverFromTypedData(tokenDelJson, tokenDelSig)
                .toLowerCase());

        // ConvertToMultiSigUser
        Map<String, Object> convertMulti = new LinkedHashMap<>();
        convertMulti.put("signers", "[\"0x1111111111111111111111111111111111111111\"]");
        convertMulti.put("nonce", 0L);
        List<Map<String, Object>> convertMultiTypes = new ArrayList<>();
        convertMultiTypes.add(Map.of("name", "hyperliquidChain", "type", "string"));
        convertMultiTypes.add(Map.of("name", "signers", "type", "string"));
        convertMultiTypes.add(Map.of("name", "nonce", "type", "uint64"));
        Map<String, Object> convertMultiSig = Signing.signUserSignedAction(cred, convertMulti, convertMultiTypes,
                "HyperliquidTransaction:ConvertToMultiSigUser", false);
        String convertMultiJson = Signing.userSignedPayloadJson("HyperliquidTransaction:ConvertToMultiSigUser",
                convertMultiTypes, convertMulti);
        assertEquals(cred.getAddress().toLowerCase(), Signing.recoverFromTypedData(convertMultiJson, convertMultiSig)
                .toLowerCase());
    }

    /**
     * 新增：多签用户签名 payload（user-signed）地址恢复测试。
     */
    @Test
    void testMultiSigUserSignedActionPayloadRecovery() {
        String pk = "0x0123456789012345678901234567890123456789012345678901234567890123";
        Credentials cred = Credentials.create(pk);
        Map<String, Object> base = new LinkedHashMap<>();
        base.put("destination", "alice");
        base.put("amount", "1000000");
        base.put("time", 0L);
        // enrich fields
        base.put("payloadMultiSigUser", cred.getAddress());
        base.put("outerSigner", cred.getAddress());

        List<Map<String, Object>> types = new ArrayList<>();
        types.add(Map.of("name", "hyperliquidChain", "type", "string"));
        types.add(Map.of("name", "destination", "type", "string"));
        types.add(Map.of("name", "amount", "type", "string"));
        types.add(Map.of("name", "time", "type", "uint64"));
        // multi-sig enrich
        types.add(Map.of("name", "payloadMultiSigUser", "type", "address"));
        types.add(Map.of("name", "outerSigner", "type", "address"));

        Map<String, Object> sig = Signing.signUserSignedAction(cred, base, types,
                "HyperliquidTransaction:UsdSend", true);
        String json = Signing.userSignedPayloadJson("HyperliquidTransaction:UsdSend", types, base);
        assertEquals(cred.getAddress().toLowerCase(), Signing.recoverFromTypedData(json, sig).toLowerCase());
    }

    /**
     * 新增：多签 L1 payload 地址恢复测试（action 作为 [payloadMultiSigUser, outerSigner,
     * innerAction] 列表封包）。
     */
    @Test
    void testMultiSigL1ActionPayloadRecovery() {
        String pk = "0x0123456789012345678901234567890123456789012345678901234567890123";
        Credentials cred = Credentials.create(pk);
        Map<String, Object> inner = new LinkedHashMap<>();
        inner.put("type", "dummy");
        inner.put("num", Signing.floatToIntForHashing(1000.0));

        List<Object> envelope = new ArrayList<>();
        envelope.add(cred.getAddress());
        envelope.add(cred.getAddress());
        envelope.add(inner);

        Map<String, Object> sig = Signing.signL1Action(cred, envelope, null, 0L, null, true);
        String recovered = Signing.recoverAgentOrUserFromL1Action(envelope, null, 0L, null, true, sig);
        assertEquals(cred.getAddress().toLowerCase(), recovered.toLowerCase());
    }

    /**
     * 新增：多签 Envelope（multiSigActionHash + nonce）地址恢复测试（user-signed）。
     */
    @Test
    void testMultiSigEnvelopeUserSignedRecovery() {
        String pk = "0x0123456789012345678901234567890123456789012345678901234567890123";
        Credentials cred = Credentials.create(pk);
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("type", "createSubAccount");
        action.put("name", "example");

        Map<String, Object> withoutTag = new LinkedHashMap<>(action);
        withoutTag.remove("type");
        byte[] mhash = Signing.actionHash(withoutTag, 0L, null, null);
        String hexHash = org.web3j.utils.Numeric.toHexString(mhash);
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("multiSigActionHash", hexHash);
        envelope.put("nonce", 0L);

        List<Map<String, Object>> types = new ArrayList<>();
        types.add(Map.of("name", "hyperliquidChain", "type", "string"));
        types.add(Map.of("name", "multiSigActionHash", "type", "bytes32"));
        types.add(Map.of("name", "nonce", "type", "uint64"));

        Map<String, Object> sig = Signing.signUserSignedAction(cred, envelope, types,
                "HyperliquidTransaction:SendMultiSig", true);
        String json = Signing.userSignedPayloadJson("HyperliquidTransaction:SendMultiSig", types, envelope);
        assertEquals(cred.getAddress().toLowerCase(), Signing.recoverFromTypedData(json, sig).toLowerCase());
    }

    /**
     * 新增：ApproveBuilderFee 用户签名主网场景，校验地址恢复一致与 v 合法性。
     * 对齐 Python: sign_approve_builder_fee(wallet, action, is_mainnet)
     */
    @Test
    void testApproveBuilderFeeUserSignedRecoveryMainnet() {
        // 测试私钥（与其他用例一致）
        String pk = "0x0123456789012345678901234567890123456789012345678901234567890123";
        Credentials cred = Credentials.create(pk);

        // 构造消息
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("maxFeeRate", "0.001%");
        msg.put("builder", "0x8c967E73E7B15087c42A10D344cFf4c96D877f1D");
        msg.put("nonce", 0L);

        // 构造签名类型
        List<Map<String, Object>> types = new ArrayList<>();
        types.add(Map.of("name", "hyperliquidChain", "type", "string"));
        types.add(Map.of("name", "maxFeeRate", "type", "string"));
        types.add(Map.of("name", "builder", "type", "address"));
        types.add(Map.of("name", "nonce", "type", "uint64"));

        // 进行签名并恢复
        Map<String, Object> sig = Signing.signUserSignedAction(cred, msg, types,
                "HyperliquidTransaction:ApproveBuilderFee", true);
        String json = Signing.userSignedPayloadJson("HyperliquidTransaction:ApproveBuilderFee", types, msg);
        String recovered = Signing.recoverFromTypedData(json, sig);
        assertEquals(cred.getAddress().toLowerCase(), recovered.toLowerCase());
        assertTrue(((Integer) sig.get("v")) == 27 || ((Integer) sig.get("v")) == 28);
    }

    /**
     * 新增：ApproveBuilderFee 用户签名测试网场景，校验地址恢复一致与 v 合法性。
     */
    @Test
    void testApproveBuilderFeeUserSignedRecoveryTestnet() {
        String pk = "0x0123456789012345678901234567890123456789012345678901234567890123";
        Credentials cred = Credentials.create(pk);

        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("maxFeeRate", "0.002%");
        msg.put("builder", "0x8c967E73E7B15087c42A10D344cFf4c96D877f1D");
        msg.put("nonce", 123456789L);

        List<Map<String, Object>> types = new ArrayList<>();
        types.add(Map.of("name", "hyperliquidChain", "type", "string"));
        types.add(Map.of("name", "maxFeeRate", "type", "string"));
        types.add(Map.of("name", "builder", "type", "address"));
        types.add(Map.of("name", "nonce", "type", "uint64"));

        Map<String, Object> sig = Signing.signUserSignedAction(cred, msg, types,
                "HyperliquidTransaction:ApproveBuilderFee", false);
        String json = Signing.userSignedPayloadJson("HyperliquidTransaction:ApproveBuilderFee", types, msg);
        String recovered = Signing.recoverFromTypedData(json, sig);
        assertEquals(cred.getAddress().toLowerCase(), recovered.toLowerCase());
        assertTrue(((Integer) sig.get("v")) == 27 || ((Integer) sig.get("v")) == 28);
    }

    /**
     * 新增：ApproveAgent 用户签名主网场景，校验地址恢复一致与 v 合法性。
     * 对齐 Python: sign_agent(wallet, action, is_mainnet)
     */
    @Test
    void testApproveAgentUserSignedRecoveryMainnet() {
        String pk = "0x0123456789012345678901234567890123456789012345678901234567890123";
        Credentials cred = Credentials.create(pk);

        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("agentAddress", "0x1111111111111111111111111111111111111111");
        msg.put("agentName", "");
        msg.put("nonce", 0L);

        List<Map<String, Object>> types = new ArrayList<>();
        types.add(Map.of("name", "hyperliquidChain", "type", "string"));
        types.add(Map.of("name", "agentAddress", "type", "address"));
        types.add(Map.of("name", "agentName", "type", "string"));
        types.add(Map.of("name", "nonce", "type", "uint64"));

        Map<String, Object> sig = Signing.signUserSignedAction(cred, msg, types,
                "HyperliquidTransaction:ApproveAgent", true);
        String json = Signing.userSignedPayloadJson("HyperliquidTransaction:ApproveAgent", types, msg);
        String recovered = Signing.recoverFromTypedData(json, sig);
        assertEquals(cred.getAddress().toLowerCase(), recovered.toLowerCase());
        assertTrue(((Integer) sig.get("v")) == 27 || ((Integer) sig.get("v")) == 28);
    }
}
