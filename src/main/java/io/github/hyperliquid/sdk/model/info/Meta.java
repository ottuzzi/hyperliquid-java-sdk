package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/** 市场元数据（永续） */
@Data
public class Meta {

    /** 支持交易的资产集合 */
    @JsonProperty("universe")
    private List<Universe> universe;

    /** 抵押品 Token 的整数 ID */
    @JsonProperty("collateralToken")
    private Integer collateralToken;

    /** 保证金表集合（服务端原始结构） */
    @JsonProperty("marginTables")
    private List<List<Object>> marginTables;

    /** 资产元素 */
    @Data
    public static class Universe {
        /** 数量精度（小数位） */
        @JsonProperty("szDecimals")
        private Integer szDecimals;

        /** 资产名称（如 "BTC"） */
        @JsonProperty("name")
        private String name;

        /** 该资产的最大杠杆 */
        @JsonProperty("maxLeverage")
        private Integer maxLeverage;

        /** 对应的保证金表 ID */
        @JsonProperty("marginTableId")
        private Integer marginTableId;
    }
}
