package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * openOrders 返回的未成交订单实体。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenOrder {

    /**
     * 币种名称或 Spot 索引（如 "BTC"、"@107"）
     * -- GETTER --
     *  获取币种（如 "BTC" 或 "@107"）
     * -- SETTER --
     *  设置币种（如 "BTC" 或 "@107"）
     */
    private String coin;
    /**
     * 限价，字符串形式，例如 "29792.0"
     * -- GETTER --
     *  获取限价（字符串）
     * -- SETTER --
     *  设置限价（字符串）
     */
    private String limitPx;
    /**
     * 订单 ID
     * -- GETTER --
     *  获取订单 ID
     * -- SETTER --
     *  设置订单 ID
     */
    private Long oid;
    /**
     * 方向字符串（例如 "A"/"B"、或 "Buy"/"Sell" 等，多端可能不同），保持原样
     * -- GETTER --
     *  获取方向字符串
     * -- SETTER --
     *  设置方向字符串
     */
    private String side;
    /**
     * 订单数量，字符串形式
     * -- GETTER --
     *  获取订单数量（字符串）
     * -- SETTER --
     *  设置订单数量（字符串）
     */
    private String sz;
    /**
     * 创建时间戳（毫秒）
     * -- GETTER --
     *  获取时间戳（毫秒）
     * -- SETTER --
     *  设置时间戳（毫秒）
     */
    private Long timestamp;
}

