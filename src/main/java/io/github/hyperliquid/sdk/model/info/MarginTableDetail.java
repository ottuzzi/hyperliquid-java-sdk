package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MarginTableDetail {
    
    @JsonProperty("description")
    private String description;

    @JsonProperty("marginTiers")
    private List<MarginTier> marginTiers;

    public MarginTableDetail() {
    }
}
