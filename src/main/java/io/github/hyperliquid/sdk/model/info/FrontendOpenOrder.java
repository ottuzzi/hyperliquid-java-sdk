package io.github.hyperliquid.sdk.model.info;

import lombok.Data;

/**
 * Retrieve a user's open orders with additional frontend info
 * 前端未成交订单实体封装。
 */
@Data
public class FrontendOpenOrder {
    private String coin;
    private Boolean isPositionTpsl;
    private Boolean isTrigger;
    private String limitPx;
    private Long oid;
    private String orderType;
    private String origSz;
    private Boolean reduceOnly;
    private String side;
    private String sz;
    private Long timestamp;
    private String triggerCondition;
    private String triggerPx;
}
