package io.github.hyperliquid.sdk.model.info;

import lombok.Data;

/**
 * Retrieve a user's fills
 * 用户最近成交
 **/
@Data
public class UserFill {

    private String coin;
    private String px;
    private String sz;
    private String side;
    private Long time;
    private String startPosition;
    private String dir;
    private String closedPnl;
    private String hash;
    private Long oid;
    private Boolean crossed;
    private String fee;
    private Long tid;
    private String feeToken;
    private String twapId;
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
