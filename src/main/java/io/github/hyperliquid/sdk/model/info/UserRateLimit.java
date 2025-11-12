package io.github.hyperliquid.sdk.model.info;

import lombok.Data;

/** 用户速率限制信息 */
@Data
public class UserRateLimit {
    /** 累计交易量（字符串） */
    public String cumVlm;
    /** 已使用的请求次数 */
    public Long nRequestsUsed;
    /** 请求次数上限 */
    public Long nRequestsCap;
}
