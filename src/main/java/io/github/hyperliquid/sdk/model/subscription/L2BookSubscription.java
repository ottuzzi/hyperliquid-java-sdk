package io.github.hyperliquid.sdk.model.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Locale;

/**
 * L2 订单簿订阅。
 * <p>
 * 订阅指定币种的完整订单簿深度数据，包括买卖盘价格和数量。
 * </p>
 */
public class L2BookSubscription extends Subscription {
    
    @JsonProperty("type")
    private final String type = "l2Book";
    
    @JsonProperty("coin")
    private String coin;
    
    /**
     * 构造 L2 订单簿订阅（无参构造，用于 Jackson 反序列化）。
     */
    public L2BookSubscription() {
    }
    
    /**
     * 构造 L2 订单簿订阅。
     *
     * @param coin 币种名称（如 "BTC"、"ETH"）或资产 ID
     */
    public L2BookSubscription(String coin) {
        this.coin = coin;
    }
    
    /**
     * 静态工厂方法：创建 L2 订单簿订阅。
     *
     * @param coin 币种名称（如 "BTC"、"ETH"）
     * @return L2BookSubscription 实例
     */
    public static L2BookSubscription of(String coin) {
        return new L2BookSubscription(coin);
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
