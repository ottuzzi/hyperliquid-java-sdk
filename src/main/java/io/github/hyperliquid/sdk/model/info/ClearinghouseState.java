package io.github.hyperliquid.sdk.model.info;

import lombok.*;

import java.util.List;

/**
 * 永续清算所状态封装。
 */
@Data
public class ClearinghouseState {
    private List<AssetPositions> assetPositions;
    private String crossMaintenanceMarginUsed;
    private CrossMarginSummary crossMarginSummary;
    private MarginSummary marginSummary;
    private Long time;
    private String withdrawable;

    @Data
    public static class CumFunding {
        private String allTime;
        private String sinceChange;
        private String sinceOpen;
    }


    @Data
    public static class Leverage {
        private String rawUsd;
        private String type;
        private int value;
    }

    @Data
    public static class Position {
        private String coin;
        private CumFunding cumFunding;
        private String entryPx;
        private Leverage leverage;
        private String liquidationPx;
        private String marginUsed;
        private int maxLeverage;
        private String positionValue;
        private String returnOnEquity;
        private String szi;
        private String unrealizedPnl;
    }

    @Data
    public static class AssetPositions {
        private Position position;
        private String type;
    }

    @Data
    public static class CrossMarginSummary {
        private String accountValue;
        private String totalMarginUsed;
        private String totalNtlPos;
        private String totalRawUsd;
    }

    @Data
    public static class MarginSummary {
        private String accountValue;
        private String totalMarginUsed;
        private String totalNtlPos;
        private String totalRawUsd;
    }
}
