package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/** 现货市场元数据（资产与 Token 信息） */
@Data
public class SpotMeta {
    /** 现货资产（聚合 Token）集合 */
    private List<Universe> universe;
    /** 现货 Token 列表 */
    private List<Token> tokens;

    @Data
    public static class Universe {
        /** 该现货资产包含的 Token ID 列表 */
        private List<Integer> tokens;
        /** 资产简称（如 "BTC"） */
        private String name;
        /** 资产索引（整数） */
        private int index;
        /** 是否为规范主资产 */
        private boolean isCanonical;
    }

    @Data
    public static class Token {
        /** Token 名称（如 "WETH"） */
        private String name;
        /** 交易数量精度 */
        private Integer szDecimals;
        /** Wei 精度（EVM 代币最小单位精度） */
        private Integer weiDecimals;
        /** Token 索引（整数） */
        private Integer index;
        /** Token 唯一 ID（字符串） */
        private String tokenId;
        /** 是否为规范主 Token */
        private Boolean isCanonical;
        /** EVM 合约信息（可能为 null） */
        private EvmContract evmContract;
        /** Token 全名（可能为 null） */
        private String fullName;
        /** 部署者交易手续费分成比例（字符串，可能为 null） */
        private String deployerTradingFeeShare;

        @Data
        public static class EvmContract {
            /** 合约地址 */
            private String address;
            @JsonProperty("evm_extra_wei_decimals")
            private int evmExtraWeiDecimals; /** 额外 Wei 精度（合约特性） */
        }
    }
}
