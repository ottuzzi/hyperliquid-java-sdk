package io.github.hyperliquid.sdk.model.info;

import lombok.Data;

import java.util.List;

/** 现货清算所状态：用户代币余额列表 */

@Data
public class SpotClearinghouseState {
    /** 余额列表 */
    private List<Balance> balances;

    @Data
    public static class Balance {
        /** Token 名称或索引前缀形式（如 "@107"） */
        private String coin;
        /** Token 整数 ID */
        private Integer token;
        /** 冻结/占用数量（字符串） */
        private String hold;
        /** 总余额数量（字符串） */
        private String total;
        /** 名义美元价值（字符串） */
        private String entryNtl;
    }
}
