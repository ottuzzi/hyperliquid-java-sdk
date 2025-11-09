package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Meta {

    @JsonProperty("universe")
    private List<Universe> universe;

    @JsonProperty("collateralToken")
    private Integer collateralToken;

    @JsonProperty("marginTables")
    private List<List<Object>> marginTables;

    @Data
    public static class Universe {
        @JsonProperty("szDecimals")
        private Integer szDecimals;

        @JsonProperty("name")
        private String name;

        @JsonProperty("maxLeverage")
        private Integer maxLeverage;

        @JsonProperty("marginTableId")
        private Integer marginTableId;
    }
}
