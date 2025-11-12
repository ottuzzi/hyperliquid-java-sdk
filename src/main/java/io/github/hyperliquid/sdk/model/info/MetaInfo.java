package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 市场元数据（类型化） */
@Data
@NoArgsConstructor
public class MetaInfo {
    /** 支持交易的资产集合 */
    @JsonProperty("universe")
    private List<UniverseElement> universe;

    /** 保证金表集合（类型化） */
    @JsonProperty("marginTables")
    private List<MarginTableEntry> marginTables;

    /** 抵押品 Token 的整数 ID */
    @JsonProperty("collateralToken")
    private Integer collateralToken;
}
