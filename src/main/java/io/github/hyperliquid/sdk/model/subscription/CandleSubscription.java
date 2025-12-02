package io.github.hyperliquid.sdk.model.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Locale;

/**
 * K 线订阅。
 * <p>
 * 订阅指定币种和时间周期的 K 线数据，包括开高低收和成交量。
 * </p>
 */
public class CandleSubscription extends Subscription {
    
    @JsonProperty("type")
    private final String type = "candle";
    
    @JsonProperty("coin")
    private String coin;
    
    @JsonProperty("interval")
    private String interval;
    
    /**
     * 构造 K 线订阅（无参构造，用于 Jackson 反序列化）。
     */
    public CandleSubscription() {
    }
    
    /**
     * 构造 K 线订阅。
     *
     * @param coin     币种名称（如 "BTC"、"ETH"）或资产 ID
     * @param interval 时间周期（如 "1m"、"5m"、"15m"、"1h"、"1d"）
     */
    public CandleSubscription(String coin, String interval) {
        this.coin = coin;
        this.interval = interval;
    }
    
    /**
     * 静态工厂方法：创建 K 线订阅。
     *
     * @param coin     币种名称（如 "BTC"、"ETH"）
     * @param interval 时间周期（如 "1m"、"5m"、"15m"、"1h"、"1d"）
     * @return CandleSubscription 实例
     */
    public static CandleSubscription of(String coin, String interval) {
        return new CandleSubscription(coin, interval);
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
    
    public String getInterval() {
        return interval;
    }
    
    public void setInterval(String interval) {
        this.interval = interval;
    }
    
    @Override
    public String toIdentifier() {
        if (coin == null || interval == null) {
            return type;
        }
        String coinKey = coin.toLowerCase(Locale.ROOT);
        return type + ":" + coinKey + "," + interval;
    }
}
