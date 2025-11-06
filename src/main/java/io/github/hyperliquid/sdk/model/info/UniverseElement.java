package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UniverseElement {
    
    @JsonProperty("szDecimals")
    private Integer szDecimals;

    @JsonProperty("name")
    private String name;

    @JsonProperty("maxLeverage")
    private Integer maxLeverage;

    @JsonProperty("marginTableId")
    private Integer marginTableId;

    // 构造函数、Getter和Setter
    public UniverseElement() {
    }

    public Integer getSzDecimals() {
        return szDecimals;
    }

    public void setSzDecimals(Integer szDecimals) {
        this.szDecimals = szDecimals;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMaxLeverage() {
        return maxLeverage;
    }

    public void setMaxLeverage(Integer maxLeverage) {
        this.maxLeverage = maxLeverage;
    }

    public Integer getMarginTableId() {
        return marginTableId;
    }

    public void setMarginTableId(Integer marginTableId) {
        this.marginTableId = marginTableId;
    }
}
