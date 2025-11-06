package io.github.hyperliquid.sdk.model.info;

/**
 * Retrieve a user's open orders with additional frontend info
 * 前端未成交订单实体封装。
 */

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

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public Boolean getPositionTpsl() {
        return isPositionTpsl;
    }

    public void setPositionTpsl(Boolean positionTpsl) {
        isPositionTpsl = positionTpsl;
    }

    public Boolean getTrigger() {
        return isTrigger;
    }

    public void setTrigger(Boolean trigger) {
        isTrigger = trigger;
    }

    public String getLimitPx() {
        return limitPx;
    }

    public void setLimitPx(String limitPx) {
        this.limitPx = limitPx;
    }

    public Long getOid() {
        return oid;
    }

    public void setOid(Long oid) {
        this.oid = oid;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getOrigSz() {
        return origSz;
    }

    public void setOrigSz(String origSz) {
        this.origSz = origSz;
    }

    public Boolean getReduceOnly() {
        return reduceOnly;
    }

    public void setReduceOnly(Boolean reduceOnly) {
        this.reduceOnly = reduceOnly;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public String getSz() {
        return sz;
    }

    public void setSz(String sz) {
        this.sz = sz;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTriggerCondition() {
        return triggerCondition;
    }

    public void setTriggerCondition(String triggerCondition) {
        this.triggerCondition = triggerCondition;
    }

    public String getTriggerPx() {
        return triggerPx;
    }

    public void setTriggerPx(String triggerPx) {
        this.triggerPx = triggerPx;
    }

    @Override
    public String toString() {
        return "FrontendOpenOrder{" +
                "coin='" + coin + '\'' +
                ", isPositionTpsl=" + isPositionTpsl +
                ", isTrigger=" + isTrigger +
                ", limitPx='" + limitPx + '\'' +
                ", oid=" + oid +
                ", orderType='" + orderType + '\'' +
                ", origSz='" + origSz + '\'' +
                ", reduceOnly=" + reduceOnly +
                ", side='" + side + '\'' +
                ", sz='" + sz + '\'' +
                ", timestamp=" + timestamp +
                ", triggerCondition='" + triggerCondition + '\'' +
                ", triggerPx='" + triggerPx + '\'' +
                '}';
    }
}
