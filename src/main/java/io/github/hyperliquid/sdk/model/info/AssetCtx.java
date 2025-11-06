package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

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

    public String getFunding() {
        return funding;
    }

    public void setFunding(String funding) {
        this.funding = funding;
    }

    public String getOpenInterest() {
        return openInterest;
    }

    public void setOpenInterest(String openInterest) {
        this.openInterest = openInterest;
    }

    public String getPrevDayPx() {
        return prevDayPx;
    }

    public void setPrevDayPx(String prevDayPx) {
        this.prevDayPx = prevDayPx;
    }

    public String getDayNtlVlm() {
        return dayNtlVlm;
    }

    public void setDayNtlVlm(String dayNtlVlm) {
        this.dayNtlVlm = dayNtlVlm;
    }

    public String getPremium() {
        return premium;
    }

    public void setPremium(String premium) {
        this.premium = premium;
    }

    public String getOraclePx() {
        return oraclePx;
    }

    public void setOraclePx(String oraclePx) {
        this.oraclePx = oraclePx;
    }

    public String getMarkPx() {
        return markPx;
    }

    public void setMarkPx(String markPx) {
        this.markPx = markPx;
    }

    public String getMidPx() {
        return midPx;
    }

    public void setMidPx(String midPx) {
        this.midPx = midPx;
    }

    public List<String> getImpactPxs() {
        return impactPxs;
    }

    public void setImpactPxs(List<String> impactPxs) {
        this.impactPxs = impactPxs;
    }

    public String getDayBaseVlm() {
        return dayBaseVlm;
    }

    public void setDayBaseVlm(String dayBaseVlm) {
        this.dayBaseVlm = dayBaseVlm;
    }
}
