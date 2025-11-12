package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** 资产上下文（永续）指标集合 */
@Data
public class AssetCtx {

    /** 当前资金费率（字符串小数） */
    @JsonProperty("funding")
    private String funding;

    /** 未平仓量（名义美元规模，字符串） */
    @JsonProperty("openInterest")
    private String openInterest;

    /** 前一日收盘价（字符串） */
    @JsonProperty("prevDayPx")
    private String prevDayPx;

    /** 当日名义成交量（美元，字符串） */
    @JsonProperty("dayNtlVlm")
    private String dayNtlVlm;

    /** 永续溢价（字符串） */
    @JsonProperty("premium")
    private String premium;

    /** 预言机价格（字符串） */
    @JsonProperty("oraclePx")
    private String oraclePx;

    /** 标记价格（字符串） */
    @JsonProperty("markPx")
    private String markPx;

    /** 中间价（买卖中间价，可能为 null） */
    @JsonProperty("midPx")
    private String midPx;

    /** 冲击价格（估算买/卖方向成交影响价，长度为 2） */
    @JsonProperty("impactPxs")
    private List<String> impactPxs;

    /** 当日基础数量成交量（币的数量，字符串） */
    @JsonProperty("dayBaseVlm")
    private String dayBaseVlm;

    /** 无参构造函数 */
    public AssetCtx() {
    }

}
