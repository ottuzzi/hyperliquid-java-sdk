package io.github.hyperliquid.sdk.model.info;

import java.util.List;

/**
 * 获取用户的代币余额
 */

public class SpotClearinghouseState {

    private List<Balance> balances;

    public static class Balance {
        
        private String coin;
        private Integer token;
        private String hold;
        private String total;
        private String entryNtl;

        public String getCoin() {
            return coin;
        }

        public void setCoin(String coin) {
            this.coin = coin;
        }

        public Integer getToken() {
            return token;
        }

        public void setToken(Integer token) {
            this.token = token;
        }

        public String getHold() {
            return hold;
        }

        public void setHold(String hold) {
            this.hold = hold;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public String getEntryNtl() {
            return entryNtl;
        }

        public void setEntryNtl(String entryNtl) {
            this.entryNtl = entryNtl;
        }
    }

    public List<Balance> getBalances() {
        return balances;
    }

    public void setBalances(List<Balance> balances) {
        this.balances = balances;
    }
}
