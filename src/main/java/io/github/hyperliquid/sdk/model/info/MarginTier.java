package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MarginTier {
    
    @JsonProperty("lowerBound")
    private String lowerBound;

    @JsonProperty("maxLeverage")
    private Integer maxLeverage;

    // 构造函数、Getter和Setter
    public MarginTier() {
    }

    public String getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(String lowerBound) {
        this.lowerBound = lowerBound;
    }

    public Integer getMaxLeverage() {
        return maxLeverage;
    }

    public void setMaxLeverage(Integer maxLeverage) {
        this.maxLeverage = maxLeverage;
    }
}
