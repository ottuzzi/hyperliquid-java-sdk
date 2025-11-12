package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 资产元素（名称、精度、杠杆与保证金表绑定） */
@Data
@NoArgsConstructor
public class UniverseElement {
    
    /** 数量精度（小数位） */
    @JsonProperty("szDecimals")
    private Integer szDecimals;

    /** 资产名称（如 "BTC"） */
    @JsonProperty("name")
    private String name;

    /** 最大杠杆倍数 */
    @JsonProperty("maxLeverage")
    private Integer maxLeverage;

    /** 绑定的保证金表 ID */
    @JsonProperty("marginTableId")
    private Integer marginTableId;
}
