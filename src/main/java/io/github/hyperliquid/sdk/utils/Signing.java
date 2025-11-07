package io.github.hyperliquid.sdk.utils;

import io.github.hyperliquid.sdk.model.order.OrderRequest;
import io.github.hyperliquid.sdk.model.order.OrderType;
import io.github.hyperliquid.sdk.model.order.OrderWire;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.util.*;

/**
 * 签名与转换工具（覆盖核心功能：浮点转换、订单 wire 转换、动作哈希、EIP-712 签名）。
 * <p>
 * 设计目标：
 * - 统一使用 API 层共享的 ObjectMapper，避免多处配置不一致与重复实例化；
 * - 在 addressToBytes 方法中实现严格的地址长度校验（默认 20 字节），并可通过开关启用兼容性降级策略；
 * - 为关键方法补充详细文档，包括 @return 与 @throws 注释，便于 IDE 友好提示与二次开发。
 */
public final class Signing {

    /**
     * 地址长度严格模式开关：
     * - true（默认）：addressToBytes 会严格要求地址为 20 字节（40 个十六进制字符），否则抛出
     * IllegalArgumentException；
     * - false：对非 20 字节输入执行兼容性降级（>20 截取末尾 20 字节；<20 左侧补零）。
     * <p>
     * 可通过系统属性 hyperliquid.address.strict 控制默认值，例如：
     * -Dhyperliquid.address.strict=true/false
     */
    private static volatile boolean STRICT_ADDRESS_LENGTH = Boolean
            .parseBoolean(System.getProperty("hyperliquid.address.strict", "true"));

    private Signing() {
    }

    /**
     * 将浮点数转换为字符串表示（适配后端接口要求）。
     * 规则：去除科学计数法，尽量保留原精度，不添加多余的尾随 0。
     *
     * @param value 浮点数值
     * @return 字符串表示
     */
    public static String floatToWire(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        // 统一为普通字符串格式，避免科学计数法
        String s = bd.stripTrailingZeros().toPlainString();
        // 去除可能的 "+0" 情况
        if (s.equals("-0")) {
            s = "0";
        }
        return s;
    }

    /**
     * 将浮点数转换为用于哈希的整数。
     * 规则：按 1e9 放大并取整。
     *
     * @param value 浮点
     * @return 放大后的整数
     */
    public static long floatToIntForHashing(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        BigDecimal scaled = bd.multiply(BigDecimal.valueOf(1_000_000_000L));
        return scaled.longValue();
    }

    /**
     * USD 精度转换：按 1e6 放大并取整，用于 USD 类转账签名。
     *
     * @param value 金额
     * @return 放大后的整数
     */
    public static long floatToUsdInt(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        BigDecimal scaled = bd.multiply(BigDecimal.valueOf(1_000_000L));
        return scaled.longValue();
    }

    /**
     * 通用整数转换：按 1e8 放大并取整。
     *
     * @param value 浮点
     * @return 放大后的整数
     */
    public static long floatToInt(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        BigDecimal scaled = bd.multiply(BigDecimal.valueOf(100_000_000L));
        return scaled.longValue();
    }

    /**
     * 获取当前毫秒时间戳。
     *
     * @return 毫秒时间戳
     */
    public static long getTimestampMs() {
        return System.currentTimeMillis();
    }

    /**
     * 将订单类型转换为 wire 结构（Map）。
     *
     * @param orderType 订单类型
     * @return Map 结构用于序列化
     */
    public static Object orderTypeToWire(OrderType orderType) {
        if (orderType == null)
            return null;
        Map<String, Object> out = new LinkedHashMap<>();
        orderType.getLimit().ifPresent(limit -> {
            Map<String, Object> limitObj = new LinkedHashMap<>();
            limitObj.put("tif", limit.getTif());
            out.put("limit", limitObj);
        });
        orderType.getTrigger().ifPresent(trigger -> {
            Map<String, Object> trigObj = new LinkedHashMap<>();
            trigObj.put("triggerPx", floatToWire(trigger.getTriggerPx()));
            trigObj.put("isMarket", trigger.isMarket());
            trigObj.put("tpsl", trigger.getTpsl());
            out.put("trigger", trigObj);
        });
        return out.isEmpty() ? null : out;
    }

    /**
     * 将下单请求转换为 wire 结构（OrderWire），其中 coin 需为整数资产 ID，
     * sz/limitPx 转为字符串，orderType 转为 Map。
     *
     * @param coinId 整数资产 ID
     * @param req    下单请求
     * @return OrderWire
     */
    public static OrderWire orderRequestToOrderWire(int coinId, OrderRequest req) {
        String szStr = floatToWire(req.getSz());
        String pxStr = req.getLimitPx() == null ? null : floatToWire(req.getLimitPx());
        Object orderTypeWire = orderTypeToWire(req.getOrderType());
        return new OrderWire(coinId, req.isBuy(), szStr, pxStr, orderTypeWire, req.isReduceOnly(), req.getCloid());
    }

    /**
     * 计算 L1 动作的哈希。
     * 结构：msgpack(action) + 8字节nonce + vaultAddress标志与地址 + expiresAfter标志与值 ->
     * keccak256。
     *
     * @param action       动作对象（Map/POJO）
     * @param nonce        随机数/时间戳
     * @param vaultAddress 可选 vault 地址（0x 前缀地址 或 null）
     * @param expiresAfter 可选过期时间（毫秒）
     * @return 32字节哈希
     */
    public static byte[] actionHash(Object action, long nonce, String vaultAddress, Long expiresAfter) {
        // 完全对齐 Python 的 action_hash 序列化与拼接规则：
        // 1) 对 action 进行 MessagePack Map 编码（保持插入顺序）；
        // 2) 直接追加 nonce 的 8 字节大端原始字节；
        // 3) vaultAddress：null -> 追加单字节 0x00；非 null -> 追加 0x01 后紧接 20 字节地址；
        // 4) expiresAfter：仅当非 null 时，追加单字节 0x00 + 8 字节大端原始字节；
        try {
            byte[] actionMsgpack = packAsMsgpack(action);

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream(actionMsgpack.length + 64);
            // 写入 msgpack 编码的 action
            baos.write(actionMsgpack);

            // 追加 nonce 8 字节（大端）
            byte[] nonceBytes = java.nio.ByteBuffer.allocate(8).putLong(nonce).array();
            baos.write(nonceBytes);

            // 追加 vaultAddress 标记及地址
            if (vaultAddress == null) {
                baos.write(new byte[]{0x00});
            } else {
                baos.write(new byte[]{0x01});
                byte[] addrBytes = addressToBytes(vaultAddress);
                if (addrBytes.length != 20) {
                    throw new IllegalArgumentException("vaultAddress must be 20 bytes");
                }
                baos.write(addrBytes);
            }

            // 追加 expiresAfter（仅当非 null）
            if (expiresAfter != null) {
                baos.write(new byte[]{0x00});
                byte[] expBytes = java.nio.ByteBuffer.allocate(8).putLong(expiresAfter).array();
                baos.write(expBytes);
            }

            byte[] preimage = baos.toByteArray();
            return Hash.sha3(preimage);
        } catch (Exception e) {
            throw new HypeError("Failed to compute action hash: " + e.getMessage());
        }
    }

    /**
     * 将任意 Java 对象以 Python msgpack.packb 的等价方式编码。
     * 支持 Map（建议使用 LinkedHashMap 保持插入顺序）、List、String、数字、布尔和 null。
     */
    private static byte[] packAsMsgpack(Object obj) throws java.io.IOException {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        writeMsgpack(packer, obj);
        packer.close();
        return packer.toByteArray();
    }

    @SuppressWarnings("unchecked")
    private static void writeMsgpack(MessageBufferPacker packer, Object obj) throws java.io.IOException {
        switch (obj) {
            case null -> {
                packer.packNil();
                return;
            }
            case Map<?, ?> ignored -> {
                Map<Object, Object> map = (Map<Object, Object>) obj;
                packer.packMapHeader(map.size());
                for (Map.Entry<Object, Object> e : map.entrySet()) {
                    // 键按字符串编码
                    packer.packString(String.valueOf(e.getKey()));
                    writeMsgpack(packer, e.getValue());
                }
                return;
            }
            case List<?> ignored -> {
                List<Object> list = (List<Object>) obj;
                packer.packArrayHeader(list.size());
                for (Object o : list) {
                    writeMsgpack(packer, o);
                }
                return;
            }
            case String s -> {
                packer.packString(s);
                return;
            }
            case Integer i -> {
                packer.packInt(i);
                return;
            }
            case Long l -> {
                packer.packLong(l);
                return;
            }
            case Double v -> {
                packer.packDouble(v);
                return;
            }
            case Float v -> {
                packer.packDouble(v.doubleValue());
                return;
            }
            case Boolean b -> {
                packer.packBoolean(b);
                return;
            }

            // 其它类型（如自定义 POJO）统一转 Map 或 String
            case OrderWire ow -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("coin", ow.coin);
                m.put("isBuy", ow.isBuy);
                m.put("sz", ow.sz);
                if (ow.limitPx != null)
                    m.put("limitPx", ow.limitPx);
                if (ow.orderType != null)
                    m.put("orderType", ow.orderType);
                m.put("reduceOnly", ow.reduceOnly);
                if (ow.cloid != null)
                    m.put("cloid", ow.cloid.getRaw());
                writeMsgpack(packer, m);
                return;
            }
            default -> {
            }
        }
        // 回退为字符串表示
        packer.packString(String.valueOf(obj));
    }

    /**
     * 地址字符串转字节数组（去除 0x 前缀）。
     * <p>
     * 行为说明：
     * - 严格模式（默认启用）：仅接受 20 字节地址（40 个十六进制字符），否则抛出 IllegalArgumentException；
     * - 兼容模式（可通过 setStrictAddressLength(false) 或 -Dhyperliquid.address.strict=false
     * 开启）：
     * 非 20 字节输入将执行降级处理：长度大于 20 截取末尾 20 字节；长度不足 20 则在左侧补零至 20 字节。
     *
     * @param address 以太坊地址，支持带或不带 0x 前缀
     * @return 地址的 20 字节表示
     * @throws IllegalArgumentException 当地址为空、非十六进制或在严格模式下长度不为 20 字节时抛出
     */
    public static byte[] addressToBytes(String address) {
        if (address == null) {
            throw new IllegalArgumentException("address must not be null");
        }
        String clean = Numeric.cleanHexPrefix(address);
        if (clean.isEmpty()) {
            throw new IllegalArgumentException("address must not be empty");
        }
        final byte[] full;
        try {
            full = Numeric.hexStringToByteArray(clean);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("address contains non-hex characters: " + address, e);
        }

        if (STRICT_ADDRESS_LENGTH) {
            if (full.length != 20) {
                throw new IllegalArgumentException(
                        "address must be exactly 20 bytes (40 hex chars), got " + full.length + " bytes");
            }
            return full;
        }

        // 兼容模式：>20 截取末尾 20 字节；<20 左侧补零
        if (full.length > 20) {
            return Arrays.copyOfRange(full, full.length - 20, full.length);
        }
        if (full.length == 20) {
            return full;
        }
        byte[] out = new byte[20];
        System.arraycopy(full, 0, out, 20 - full.length, full.length);
        return out;
    }

    /**
     * 为用户签名 L1 动作（EIP-712 Typed Data）。
     *
     * @param credentials 用户凭证（私钥）
     * @return r/s/v 十六进制签名
     * @throws HypeError 当序列化或签名过程出现异常时抛出（封装底层异常信息）
     */
    public static Map<String, Object> signTypedData(Credentials credentials, String typedDataJson) {
        // 使用标准 EIP-712 结构化数据编码与签名（与 Python eth_account.encode_typed_data 一致）。
        try {
            org.web3j.crypto.StructuredDataEncoder encoder = new org.web3j.crypto.StructuredDataEncoder(typedDataJson);
            byte[] digest = encoder.hashStructuredData();
            Sign.SignatureData sig = Sign.signMessage(digest, credentials.getEcKeyPair(), false);
            String r = Numeric.toHexString(sig.getR());
            String s = Numeric.toHexString(sig.getS());
            int vInt = new java.math.BigInteger(1, sig.getV()).intValue();
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("r", r);
            out.put("s", s);
            out.put("v", vInt);
            return out;
        } catch (Exception e) {
            throw new HypeError("Failed to sign typed data: " + e.getMessage());
        }
    }

    /**
     * 构造 Phantom Agent（与 Python construct_phantom_agent 一致）。
     */
    public static Map<String, Object> constructPhantomAgent(byte[] hash, boolean isMainnet) {
        Map<String, Object> agent = new LinkedHashMap<>();
        agent.put("source", isMainnet ? "a" : "b");
        // bytes32 以 0x 前缀十六进制字符串表达，兼容 web3j StructuredDataEncoder
        agent.put("connectionId", Numeric.toHexString(hash));
        return agent;
    }

    /**
     * 生成 EIP-712 TypedData JSON（与 Python l1_payload 一致）。
     */
    public static String l1PayloadJson(Map<String, Object> phantomAgent) {
        Map<String, Object> domain = new LinkedHashMap<>();
        domain.put("chainId", 1337);
        domain.put("name", "Exchange");
        domain.put("verifyingContract", "0x0000000000000000000000000000000000000000");
        domain.put("version", "1");

        List<Map<String, Object>> agentTypes = new ArrayList<>();
        agentTypes.add(Map.of("name", "source", "type", "string"));
        agentTypes.add(Map.of("name", "connectionId", "type", "bytes32"));

        List<Map<String, Object>> eipTypes = new ArrayList<>();
        eipTypes.add(Map.of("name", "name", "type", "string"));
        eipTypes.add(Map.of("name", "version", "type", "string"));
        eipTypes.add(Map.of("name", "chainId", "type", "uint256"));
        eipTypes.add(Map.of("name", "verifyingContract", "type", "address"));

        Map<String, Object> types = new LinkedHashMap<>();
        types.put("Agent", agentTypes);
        types.put("EIP712Domain", eipTypes);

        Map<String, Object> full = new LinkedHashMap<>();
        full.put("domain", domain);
        full.put("types", types);
        full.put("primaryType", "Agent");
        full.put("message", phantomAgent);

        com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
        try {
            return om.writeValueAsString(full);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new HypeError("Failed to build typed data json: " + e.getMessage());
        }
    }

    /**
     * 对 L1 动作进行签名（完整流程）：计算 actionHash -> 构造 phantom agent -> 生成 EIP-712 typed data
     * -> 签名。
     */
    public static Map<String, Object> signL1Action(Credentials credentials, Object action, String vaultAddress,
                                                   long nonce, Long expiresAfter, boolean isMainnet) {
        byte[] hash = actionHash(action, nonce, vaultAddress, expiresAfter);
        Map<String, Object> agent = constructPhantomAgent(hash, isMainnet);
        String typedJson = l1PayloadJson(agent);
        return signTypedData(credentials, typedJson);
    }

    /**
     * 将多个 OrderWire 转换为 L1 动作对象，用于签名与发送。
     * <p>
     * 生成的结构形如：
     * {
     * "type": "order",
     * "orders": [
     * {"coin": 1, "isBuy": true, "sz": "0.1", "limitPx": "60000", "orderType":
     * {...}, "reduceOnly": false, "cloid": "..."},
     * ...
     * ]
     * }
     *
     * @param orders 订单 wire 列表
     * @return Map 动作对象 {"type": "order", "orders": [...]}
     */
    public static Map<String, Object> orderWiresToOrderAction(List<OrderWire> orders) {
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("type", "order");
        List<Map<String, Object>> wires = new ArrayList<>();
        for (OrderWire o : orders) {
            Map<String, Object> w = new LinkedHashMap<>();
            w.put("coin", o.coin);
            w.put("isBuy", o.isBuy);
            w.put("sz", o.sz);
            if (o.limitPx != null)
                w.put("limitPx", o.limitPx);
            if (o.orderType != null)
                w.put("orderType", o.orderType);
            w.put("reduceOnly", o.reduceOnly);
            if (o.cloid != null)
                w.put("cloid", o.cloid.getRaw());
            wires.add(w);
        }
        action.put("orders", wires);
        return action;
    }

    /**
     * 设置地址长度校验严格模式。
     *
     * @param strict 是否严格要求地址为 20 字节
     */
    public static void setStrictAddressLength(boolean strict) {
        STRICT_ADDRESS_LENGTH = strict;
    }

    /**
     * 获取当前地址长度校验模式。
     *
     * @return true 表示严格模式；false 表示兼容模式
     */
    public static boolean isStrictAddressLength() {
        return STRICT_ADDRESS_LENGTH;
    }
}
