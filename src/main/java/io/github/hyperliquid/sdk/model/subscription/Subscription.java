package io.github.hyperliquid.sdk.model.subscription;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * WebSocket 订阅基类。
 * <p>
 * 所有具体的订阅类型都继承此类，提供类型安全的订阅参数封装。
 * </p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Subscription {
    
    /**
     * 获取订阅类型（由子类实现）。
     *
     * @return 订阅类型字符串
     */
    public abstract String getType();
    
    /**
     * 生成唯一标识符（用于订阅去重与消息路由）。
     * <p>
     * 默认实现返回订阅类型，子类可根据需要覆盖此方法。
     * </p>
     *
     * @return 唯一标识符字符串
     */
    public String toIdentifier() {
        return getType();
    }
}
