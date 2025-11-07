package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Meta {

    @JsonProperty("universe")
    private List<Universe> universe;

    @JsonProperty("collateralToken")
    private Integer collateralToken;

    @JsonProperty("marginTables")
    private List<List<Object>> marginTables;

    public static class Universe {

        @JsonProperty("szDecimals")
        private Integer szDecimals;

        @JsonProperty("name")
        private String name;

        @JsonProperty("maxLeverage")
        private Integer maxLeverage;

        @JsonProperty("marginTableId")
        private Integer marginTableId;

        // getters and setters
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

        @Override
        public String toString() {
            return "Universe{" +
                    "szDecimals=" + szDecimals +
                    ", name='" + name + '\'' +
                    ", maxLeverage=" + maxLeverage +
                    ", marginTableId=" + marginTableId +
                    '}';
        }
    }

    public List<Universe> getUniverse() {
        return universe;
    }

    public void setUniverse(List<Universe> universe) {
        this.universe = universe;
    }

    public Integer getCollateralToken() {
        return collateralToken;
    }

    public void setCollateralToken(Integer collateralToken) {
        this.collateralToken = collateralToken;
    }

    public List<List<Object>> getMarginTables() {
        return marginTables;
    }

    public void setMarginTables(List<List<Object>> marginTables) {
        this.marginTables = marginTables;
    }

    @Override
    public String toString() {
        return "Meta{" +
                "universe=" + universe +
                ", collateralToken=" + collateralToken +
                ", marginTables=" + marginTables +
                '}';
    }
}
