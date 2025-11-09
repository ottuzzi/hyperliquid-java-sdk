package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Candle（K 线）类型模型。
 */
@Data
public class Candle {

    /**
     * 结束时间戳（毫秒）
     */
    @JsonProperty("T")
    private Long endTimestamp;

    /**
     * 起始时间戳（毫秒）
     */
    @JsonProperty("t")
    private Long startTimestamp;

    /**
     * 收盘价
     */
    @JsonProperty("c")
    private String closePrice;

    /**
     * 最高价
     */
    @JsonProperty("h")
    private String highPrice;

    /**
     * 最低价
     */
    @JsonProperty("l")
    private String lowPrice;

    /**
     * 开盘价
     */
    @JsonProperty("o")
    private String openPrice;

    /**
     * 交易量
     */
    @JsonProperty("v")
    private String volume;

    /**
     * 时间间隔（如 "1m"、"15m"、"1h"、"1d" 等）
     */
    @JsonProperty("i")
    private String interval;

    /**
     * 交易对符号（如 "BTC"）
     */
    @JsonProperty("s")
    private String symbol;

    /**
     * 交易次数
     */
    @JsonProperty("n")
    private Integer tradeCount;
}
