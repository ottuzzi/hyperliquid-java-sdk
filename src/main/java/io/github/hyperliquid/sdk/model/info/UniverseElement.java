package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UniverseElement {
    
    @JsonProperty("szDecimals")
    private Integer szDecimals;

    @JsonProperty("name")
    private String name;

    @JsonProperty("maxLeverage")
    private Integer maxLeverage;

    @JsonProperty("marginTableId")
    private Integer marginTableId;
}