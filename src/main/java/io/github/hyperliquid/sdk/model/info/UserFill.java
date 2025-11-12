package io.github.hyperliquid.sdk.model.info;

import lombok.Data;

/**
 * Retrieve a user's fills
 * 用户最近成交
 **/
@Data
public class UserFill {

    /** 币种（如 "BTC" 或 Spot 索引 "@107"） */
    private String coin;
    /** 成交价格（字符串） */
    private String px;
    /** 成交数量（字符串） */
    private String sz;
    /** 方向（A/B 或 Buy/Sell） */
    private String side;
    /** 成交时间戳（毫秒） */
    private Long time;
    /** 成交时起始仓位大小（字符串） */
    private String startPosition;
    /** 方向描述（如 open/close 等） */
    private String dir;
    /** 已关闭盈亏（字符串） */
    private String closedPnl;
    /** 成交哈希 */
    private String hash;
    /** 订单 ID */
    private Long oid;
    /** 是否为穿越成交（crossed） */
    private Boolean crossed;
    /** 手续费（字符串） */
    private String fee;
    /** 成交序号（tid） */
    private Long tid;
    /** 手续费代币标识 */
    private String feeToken;
    /** TWAP 策略 ID（若为切片成交） */
    private String twapId;
    /** Builder 费用（字符串，若适用） */
    private String builderFee;


    // 实用方法 - 判断是否为现货交易
    public boolean isSpotTrade() {
        return coin != null && coin.startsWith("@");
    }

    // 实用方法 - 判断是否为永续合约交易
    public boolean isPerpTrade() {
        return coin != null && !coin.startsWith("@");
    }

    // 实用方法 - 获取资产ID（如果是现货交易）
    public String getAssetId() {
        if (isSpotTrade() && coin != null) {
            return coin.substring(1); // 去掉"@"符号
        }
        return coin;
    }
}
