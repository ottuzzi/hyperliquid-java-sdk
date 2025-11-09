package io.github.hyperliquid.sdk.model.info;

import lombok.Data;

/**
 * Query user rate limits
 **/
@Data
public class UserRateLimit {
    public String cumVlm;
    public Long nRequestsUsed;
    public Long nRequestsCap;
}
