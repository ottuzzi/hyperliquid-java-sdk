package io.github.hyperliquid.sdk.model.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Locale;

/**
 * 逐笔成交订阅。
 * <p>
 * 订阅指定币种的每一笔实时成交数据，包括价格、数量、方向和时间。
 * </p>
 */
public class TradesSubscription extends Subscription {
    
    @JsonProperty("type")
    private final String type = "trades";
    
    @JsonProperty("coin")
    private String coin;
    
    /**
     * 构造逐笔成交订阅（无参构造，用于 Jackson 反序列化）。
     */
    public TradesSubscription() {
    }
    
    /**
     * 构造逐笔成交订阅。
     *
     * @param coin 币种名称（如 "BTC"、"ETH"）或资产 ID
     */
    public TradesSubscription(String coin) {
        this.coin = coin;
    }
    
    /**
     * 静态工厂方法：创建逐笔成交订阅。
     *
     * @param coin 币种名称（如 "BTC"、"ETH"）
     * @return TradesSubscription 实例
     */
    public static TradesSubscription of(String coin) {
        return new TradesSubscription(coin);
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
