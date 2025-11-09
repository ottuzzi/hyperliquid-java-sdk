package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MarginTier {
    
    @JsonProperty("lowerBound")
    private String lowerBound;

    @JsonProperty("maxLeverage")
    private Integer maxLeverage;
}
