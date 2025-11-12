package io.github.hyperliquid.sdk.model.info;

import lombok.Data;

/** 前端未成交订单实体封装（携带触发/止盈止损等额外信息） */
@Data
public class FrontendOpenOrder {
    /** 币种（如 "BTC" 或 Spot 索引 "@107"） */
    private String coin;
    /** 是否为仓位止盈止损单 */
    private Boolean isPositionTpsl;
    /** 是否为触发单 */
    private Boolean isTrigger;
    /** 限价（字符串） */
    private String limitPx;
    /** 订单 ID */
    private Long oid;
    /** 订单类型描述 */
    private String orderType;
    /** 原始下单数量（字符串） */
    private String origSz;
    /** 是否仅减仓 */
    private Boolean reduceOnly;
    /** 方向（A/B 或 Buy/Sell） */
    private String side;
    /** 当前剩余数量（字符串） */
    private String sz;
    /** 创建时间戳（毫秒） */
    private Long timestamp;
    /** 触发条件（上穿/下穿等） */
    private String triggerCondition;
    /** 触发价格（字符串） */
    private String triggerPx;
}
