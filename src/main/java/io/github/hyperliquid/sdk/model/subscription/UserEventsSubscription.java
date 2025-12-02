package io.github.hyperliquid.sdk.model.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 用户事件订阅。
 * <p>
 * 订阅当前用户的所有交易事件，包括订单状态变化、成交通知等。
 * 该订阅不需要指定用户地址，自动使用当前登录用户。
 * </p>
 */
public class UserEventsSubscription extends Subscription {
    
    @JsonProperty("type")
    private final String type = "userEvents";
    
    /**
     * 构造用户事件订阅。
     */
    public UserEventsSubscription() {
    }
    
    /**
     * 静态工厂方法：创建用户事件订阅。
     *
     * @return UserEventsSubscription 实例
     */
    public static UserEventsSubscription create() {
        return new UserEventsSubscription();
    }
    
    @Override
    public String getType() {
        return type;
    }
}
