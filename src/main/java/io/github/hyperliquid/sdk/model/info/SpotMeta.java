package io.github.hyperliquid.sdk.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SpotMeta {

    private List<Universe> universe;

    private List<Token> tokens;

    public static class Universe {

        private List<Integer> tokens;
        private String name;
        private int index;
        private boolean isCanonical;

        public List<Integer> getTokens() {
            return tokens;
        }

        public void setTokens(List<Integer> tokens) {
            this.tokens = tokens;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public boolean isCanonical() {
            return isCanonical;
        }

        public void setCanonical(boolean canonical) {
            isCanonical = canonical;
        }

        @Override
        public String toString() {
            return "Universe{" +
                    "tokens=" + tokens +
                    ", name='" + name + '\'' +
                    ", index=" + index +
                    ", isCanonical=" + isCanonical +
                    '}';
        }
    }

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

        public static class EvmContract {
            private String address;
            @JsonProperty("evm_extra_wei_decimals")
            private int evmExtraWeiDecimals;

            public String getAddress() {
                return address;
            }

            public void setAddress(String address) {
                this.address = address;
            }

            public int getEvmExtraWeiDecimals() {
                return evmExtraWeiDecimals;
            }

            public void setEvmExtraWeiDecimals(int evmExtraWeiDecimals) {
                this.evmExtraWeiDecimals = evmExtraWeiDecimals;
            }

            @Override
            public String toString() {
                return "EvmContract{" +
                        "address='" + address + '\'' +
                        ", evmExtraWeiDecimals=" + evmExtraWeiDecimals +
                        '}';
            }
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getSzDecimals() {
            return szDecimals;
        }

        public void setSzDecimals(Integer szDecimals) {
            this.szDecimals = szDecimals;
        }

        public Integer getWeiDecimals() {
            return weiDecimals;
        }

        public void setWeiDecimals(Integer weiDecimals) {
            this.weiDecimals = weiDecimals;
        }

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }

        public String getTokenId() {
            return tokenId;
        }

        public void setTokenId(String tokenId) {
            this.tokenId = tokenId;
        }

        public Boolean getCanonical() {
            return isCanonical;
        }

        public void setCanonical(Boolean canonical) {
            isCanonical = canonical;
        }

        public EvmContract getEvmContract() {
            return evmContract;
        }

        public void setEvmContract(EvmContract evmContract) {
            this.evmContract = evmContract;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getDeployerTradingFeeShare() {
            return deployerTradingFeeShare;
        }

        public void setDeployerTradingFeeShare(String deployerTradingFeeShare) {
            this.deployerTradingFeeShare = deployerTradingFeeShare;
        }

        @Override
        public String toString() {
            return "Token{" +
                    "name='" + name + '\'' +
                    ", szDecimals=" + szDecimals +
                    ", weiDecimals=" + weiDecimals +
                    ", index=" + index +
                    ", tokenId='" + tokenId + '\'' +
                    ", isCanonical=" + isCanonical +
                    ", evmContract='" + evmContract + '\'' +
                    ", fullName='" + fullName + '\'' +
                    ", deployerTradingFeeShare='" + deployerTradingFeeShare + '\'' +
                    '}';
        }
    }

    public List<Universe> getUniverse() {
        return universe;
    }

    public void setUniverse(List<Universe> universe) {
        this.universe = universe;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;
    }

    @Override
    public String toString() {
        return "SpotMeta{" +
                "universe=" + universe +
                ", tokens=" + tokens +
                '}';
    }
}
