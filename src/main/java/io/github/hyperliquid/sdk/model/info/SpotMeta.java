package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SpotMeta {
    private List<Universe> universe;
    private List<Token> tokens;

    @Data
    public static class Universe {
        private List<Integer> tokens;
        private String name;
        private int index;
        private boolean isCanonical;
    }

    @Data
    public static class Token {
        private String name;
        private Integer szDecimals;
        private Integer weiDecimals;
        private Integer index;
        private String tokenId;
        private Boolean isCanonical;
        private EvmContract evmContract;
        private String fullName;
        private String deployerTradingFeeShare;

        @Data
        public static class EvmContract {
            private String address;
            @JsonProperty("evm_extra_wei_decimals")
            private int evmExtraWeiDecimals;
        }
    }
}
