package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MetaInfo {

    @JsonProperty("universe")
    private List<UniverseElement> universe;

    @JsonProperty("marginTables")
    private List<MarginTableEntry> marginTables;

    @JsonProperty("collateralToken")
    private Integer collateralToken;

    // 构造函数、Getter和Setter
    public MetaInfo() {
    }

    public List<UniverseElement> getUniverse() {
        return universe;
    }

    public void setUniverse(List<UniverseElement> universe) {
        this.universe = universe;
    }

    public List<MarginTableEntry> getMarginTables() {
        return marginTables;
    }

    public void setMarginTables(List<MarginTableEntry> marginTables) {
        this.marginTables = marginTables;
    }

    public Integer getCollateralToken() {
        return collateralToken;
    }

    public void setCollateralToken(Integer collateralToken) {
        this.collateralToken = collateralToken;
    }
}
