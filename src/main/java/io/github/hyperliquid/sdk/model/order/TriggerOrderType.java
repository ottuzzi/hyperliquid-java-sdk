package io.github.hyperliquid.sdk.model.order;

import lombok.Getter;

/**
 * 触发单类型（止盈/止损/触发价与是否市价）。
 */
public class TriggerOrderType {
    private final Double triggerPx;
    private final Boolean isMarket;
    private final TpslType tpsl;

    /**
     * 止盈/止损类型枚举
     */
    @Getter
    public enum TpslType {
        TP("tp"), // 止盈/上穿触发
        SL("sl"); // 止损/下穿触发

        private final String value;

        TpslType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    /**
     * 构造触发单类型。
     *
     * @param triggerPx 触发价格
     * @param isMarket  是否市价
     * @param tpsl      止盈/止损类型
     */
    public TriggerOrderType(double triggerPx, boolean isMarket, TpslType tpsl) {
        if (tpsl == null) {
            throw new IllegalArgumentException("tpsl cannot be null");
        }
        this.triggerPx = triggerPx;
        this.isMarket = isMarket;
        this.tpsl = tpsl;
    }

    public double getTriggerPx() {
        return triggerPx;
    }

    public boolean isMarket() {
        return isMarket;
    }

    /**
     * 获取止盈/止损类型字符串值。
     *
     * @return "tp" 或 "sl"
     */
    public String getTpsl() {
        return tpsl.getValue();
    }

    /**
     * 获取止盈/止损枚举类型。
     *
     * @return TpslType 枚举
     */
    public TpslType getTpslEnum() {
        return tpsl;
    }
}