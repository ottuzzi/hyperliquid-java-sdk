package io.github.hyperliquid.sdk.model.info;

import lombok.*;

import java.util.List;

/** 永续清算所状态封装（账户与仓位概览） */
@Data
public class ClearinghouseState {
    /** 各资产的仓位信息列表 */
    private List<AssetPositions> assetPositions;
    /** 交叉保证金维持保证金占用 */
    private String crossMaintenanceMarginUsed;
    /** 交叉保证金汇总 */
    private CrossMarginSummary crossMarginSummary;
    /** 单币种保证金汇总 */
    private MarginSummary marginSummary;
    /** 状态时间戳（毫秒） */
    private Long time;
    /** 可提余额（字符串） */
    private String withdrawable;

    @Data
    public static class CumFunding {
        /** 历史累计资金费率影响 */
        private String allTime;
        /** 自上次杠杆/模式变更以来累计 */
        private String sinceChange;
        /** 自开仓以来累计 */
        private String sinceOpen;
    }


    @Data
    public static class Leverage {
        /** 原始美元规模（用于计算） */
        private String rawUsd;
        /** 杠杆类型（cross/isolated） */
        private String type;
        /** 杠杆倍数值 */
        private int value;
    }

    @Data
    public static class Position {
        /** 币种名称 */
        private String coin;
        /** 累计资金费率影响 */
        private CumFunding cumFunding;
        /** 开仓均价 */
        private String entryPx;
        /** 杠杆信息 */
        private Leverage leverage;
        /** 预估强平价 */
        private String liquidationPx;
        /** 保证金占用 */
        private String marginUsed;
        /** 最大允许杠杆 */
        private int maxLeverage;
        /** 仓位名义价值 */
        private String positionValue;
        /** 账户收益率（ROE） */
        private String returnOnEquity;
        /** 仓位签名数量（正多负空，字符串） */
        private String szi;
        /** 未实现盈亏 */
        private String unrealizedPnl;
    }

    @Data
    public static class AssetPositions {
        /** 仓位详情 */
        private Position position;
        /** 类型（如 perp） */
        private String type;
    }

    @Data
    public static class CrossMarginSummary {
        /** 账户总价值 */
        private String accountValue;
        /** 总保证金占用 */
        private String totalMarginUsed;
        /** 总名义仓位 */
        private String totalNtlPos;
        /** 总原始美元规模 */
        private String totalRawUsd;
    }

    @Data
    public static class MarginSummary {
        /** 账户总价值 */
        private String accountValue;
        /** 总保证金占用 */
        private String totalMarginUsed;
        /** 总名义仓位 */
        private String totalNtlPos;
        /** 总原始美元规模 */
        private String totalRawUsd;
    }
}
