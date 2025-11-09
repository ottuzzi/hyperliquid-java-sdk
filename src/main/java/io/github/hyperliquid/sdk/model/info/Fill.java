package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户成交实体封装。
 *
 * <p>说明：官方返回字段在不同接口与版本可能存在差异，同时为 SDK 使用者保留完整信息。</p>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Fill {

    /**
     * 成交时间戳（毫秒）
     * -- GETTER --
     *  获取时间戳（毫秒）
     * -- SETTER --
     *  设置时间戳（毫秒）
     */
    private Long time;
    /**
     * 币种 ID（perp/spot，若服务端返回为整数 ID）
     * -- GETTER --
     *  获取币种 ID（若返回为整数）
     * -- SETTER --
     *  设置币种 ID（仅当需要强制写入整数时使用）
     */
    private Integer coin;
    /**
     * 币种名称（若服务端返回为字符串名称，如 "AVAX"）
     * -- GETTER --
     *  获取币种名称（若返回为字符串时）
     * -- SETTER --
     *  设置币种名称（仅当需要强制写入字符串时使用）
     */
    private String coinName;
    /**
     * 买入为 true，卖出为 false
     * -- GETTER --
     *  是否买入
     * -- SETTER --
     *  设置是否买入
     */
    private Boolean isBuy;
    /**
     * 成交数量
     * -- GETTER --
     *  获取成交数量
     * -- SETTER --
     *  设置成交数量
     */
    private Double size;
    /**
     * 成交价格
     * -- GETTER --
     *  获取成交价格
     * -- SETTER --
     *  设置成交价格
     */
    private Double price;

    /**
     * 额外字段容器（用于兼容不同返回结构）
     * -- GETTER --
     *  获取额外字段映射
     * -- SETTER --
     *  设置额外字段映射
     */
    private Map<String, Object> extensions = new HashMap<>();

    public Fill() {
    }

    /**
     * Builder 构建器，便于友好创建。
     */
    public static class Builder {
        private final Fill f = new Fill();

        public Builder time(Long t) {
            f.time = t;
            return this;
        }

        public Builder coin(Integer c) {
            f.coin = c;
            return this;
        }

        public Builder isBuy(Boolean b) {
            f.isBuy = b;
            return this;
        }

        public Builder size(Double s) {
            f.size = s;
            return this;
        }

        public Builder price(Double p) {
            f.price = p;
            return this;
        }

        public Builder putExtra(String k, Object v) {
            f.extensions.put(k, v);
            return this;
        }

        public Fill build() {
            return f;
        }
    }

    @JsonAnySetter
    public void put(String key, Object value) {
        extensions.put(key, value);
    }

    /**
     * 兼容性 coin 映射：同时支持整数 ID 与字符串名称。
     *
     * @param value 服务端返回的 coin 字段值（可能为数字或字符串）
     */
    @JsonSetter("coin")
    public void setCoinFlexible(Object value) {
        if (value == null) {
            this.coin = null;
            this.coinName = null;
            return;
        }
        if (value instanceof Number) {
            this.coin = ((Number) value).intValue();
            this.coinName = null;
        } else {
            // 兼容字符串与其他类型
            this.coin = null;
            this.coinName = String.valueOf(value);
        }
    }

    @JsonAnyGetter
    public Map<String, Object> any() {
        return extensions;
    }
}
