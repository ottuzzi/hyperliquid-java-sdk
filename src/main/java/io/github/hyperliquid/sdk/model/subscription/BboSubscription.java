package io.github.hyperliquid.sdk.model.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Locale;

/**
 * 最佳买卖价（BBO）订阅。
 * <p>
 * 订阅指定币种的最佳买一价和卖一价及其数量，数据更精简、更新更快。
 * </p>
 */
public class BboSubscription extends Subscription {
    
    @JsonProperty("type")
    private final String type = "bbo";
    
    @JsonProperty("coin")
    private String coin;
    
    /**
     * 构造 BBO 订阅（无参构造，用于 Jackson 反序列化）。
     */
    public BboSubscription() {
    }
    
    /**
     * 构造 BBO 订阅。
     *
     * @param coin 币种名称（如 "BTC"、"ETH"）或资产 ID
     */
    public BboSubscription(String coin) {
        this.coin = coin;
    }
    
    /**
     * 静态工厂方法：创建 BBO 订阅。
     *
     * @param coin 币种名称（如 "BTC"、"ETH"）
     * @return BboSubscription 实例
     */
    public static BboSubscription of(String coin) {
        return new BboSubscription(coin);
    }
    
    @Override
    public String getType() {
        return type;
    }
    
    public String getCoin() {
        return coin;
    }
    
    public void setCoin(String coin) {
        this.coin = coin;
    }
    
    @Override
    public String toIdentifier() {
        if (coin == null) {
            return type;
        }
        String coinKey = coin.toLowerCase(Locale.ROOT);
        return type + ":" + coinKey;
    }
}
