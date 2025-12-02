package io.github.hyperliquid.sdk.model.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 所有币种中间价订阅。
 * <p>
 * 订阅所有币种的中间价（买一价和卖一价的平均值），用于快速获取市场行情概览。
 * </p>
 */
public class AllMidsSubscription extends Subscription {
    
    @JsonProperty("type")
    private final String type = "allMids";
    
    /**
     * 构造所有币种中间价订阅。
     */
    public AllMidsSubscription() {
    }
    
    /**
     * 静态工厂方法：创建所有币种中间价订阅。
     *
     * @return AllMidsSubscription 实例
     */
    public static AllMidsSubscription create() {
        return new AllMidsSubscription();
    }
    
    @Override
    public String getType() {
        return type;
    }
}
