package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class AssetCtx {

    @JsonProperty("funding")
    private String funding;

    @JsonProperty("openInterest")
    private String openInterest;

    @JsonProperty("prevDayPx")
    private String prevDayPx;

    @JsonProperty("dayNtlVlm")
    private String dayNtlVlm;

    @JsonProperty("premium")
    private String premium;

    @JsonProperty("oraclePx")
    private String oraclePx;

    @JsonProperty("markPx")
    private String markPx;

    @JsonProperty("midPx")
    private String midPx;

    @JsonProperty("impactPxs")
    private List<String> impactPxs;

    @JsonProperty("dayBaseVlm")
    private String dayBaseVlm;

    // 构造函数、Getter和Setter
    public AssetCtx() {
    }

}
