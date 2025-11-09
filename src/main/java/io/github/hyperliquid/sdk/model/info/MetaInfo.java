package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MetaInfo {
    @JsonProperty("universe")
    private List<UniverseElement> universe;

    @JsonProperty("marginTables")
    private List<MarginTableEntry> marginTables;

    @JsonProperty("collateralToken")
    private Integer collateralToken;
}
