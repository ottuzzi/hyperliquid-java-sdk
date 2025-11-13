package io.github.hyperliquid.sdk.model.order;

import lombok.Getter;

/**
 * 触发单类型：承载触发价、是否按市价执行、止盈/止损类型。
 * 与 Python `TriggerOrderType` 对齐（triggerPx/isMarket/tpsl）。
 */
public class TriggerOrderType {
    /** 触发价格（浮点，最终会规范化为字符串） */
    private final Double triggerPx;
    /** 触发后是否以市价执行（true=市价触发；false=限价触发） */
    private final Boolean isMarket;
    /** 止盈/止损类型 */
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

    /** 获取触发价格 */
    public double getTriggerPx() {
        return triggerPx;
    }

    /** 是否以市价触发执行 */
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
