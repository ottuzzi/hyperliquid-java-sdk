package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MarginTableDetail {
    
    @JsonProperty("description")
    private String description;

    @JsonProperty("marginTiers")
    private List<MarginTier> marginTiers;

    // 构造函数、Getter和Setter
    public MarginTableDetail() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<MarginTier> getMarginTiers() {
        return marginTiers;
    }

    public void setMarginTiers(List<MarginTier> marginTiers) {
        this.marginTiers = marginTiers;
    }
}
