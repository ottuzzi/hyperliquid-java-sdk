package io.github.hyperliquid.sdk.model.info;

import lombok.Data;

import java.util.List;

/** 订单状态返回封装 */
@Data
public class OrderStatus {

    /** 顶层状态（如 "ok"/"error"） */
    private String status;

    /** 订单详情与状态时间戳 */
    private Order order;

    @Data
    public static class Order {
        /** 订单详情 */
        private OrderDetail order;
        /** 状态更新时间戳（毫秒） */
        private Long statusTimestamp;

        @Data
        public static class OrderDetail {
            /** 币种名称 */
            private String coin;
            /** 方向（A/B 或 Buy/Sell） */
            private String side;
            /** 限价（字符串） */
            private String limitPx;
            /** 下单数量（字符串） */
            private String sz;
            /** 订单 ID */
            private Long oid;
            /** 创建时间戳（毫秒） */
            private Long timestamp;
            /** 触发条件描述 */
            private String triggerCondition;
            /** 是否为触发单 */
            private Boolean isTrigger;
            /** 触发价格（字符串） */
            private String triggerPx;
            /** 子订单 ID 列表（若拆分/切片） */
            private List<String> children;
            /** 是否为仓位止盈止损 */
            private Boolean isPositionTpsl;
            /** 是否仅减仓 */
            private Boolean reduceOnly;
            /** 订单类型描述 */
            private String orderType;
            /** 原始下单数量（字符串） */
            private String origSz;
            /** TIF 策略（Gtc/Alo/Ioc） */
            private String tif;
            /** 客户端订单 ID */
            private String cloid;
        }
    }
}
