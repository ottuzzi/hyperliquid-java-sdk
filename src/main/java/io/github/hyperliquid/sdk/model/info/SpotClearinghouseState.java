package io.github.hyperliquid.sdk.model.info;

import lombok.Data;

import java.util.List;

/**
 * 获取用户的代币余额
 */

@Data
public class SpotClearinghouseState {
    private List<Balance> balances;

    @Data
    public static class Balance {
        private String coin;
        private Integer token;
        private String hold;
        private String total;
        private String entryNtl;
    }
}
