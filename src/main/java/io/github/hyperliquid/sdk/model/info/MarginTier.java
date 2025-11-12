package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 保证金层级（仓位下限与最大杠杆） */
@Data
@NoArgsConstructor
public class MarginTier {
    
    /** 仓位规模下限（字符串） */
    @JsonProperty("lowerBound")
    private String lowerBound;

    /** 对应最大杠杆倍数 */
    @JsonProperty("maxLeverage")
    private Integer maxLeverage;
}
