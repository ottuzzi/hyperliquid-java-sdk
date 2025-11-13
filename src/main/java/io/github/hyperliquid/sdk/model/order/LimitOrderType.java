package io.github.hyperliquid.sdk.model.order;

import lombok.Data;

/**
 * 限价单类型（TIF 策略）：
 * - Gtc：直到取消前有效；
 * - Alo：仅添加流动性（Post Only）；
 * - Ioc：立即成交或取消。
 */
@Data
public class LimitOrderType {

    private final Tif tif;

    /**
     * 构造限价单类型。
     */
    public LimitOrderType(Tif tif) {
        if (tif == null) {
            throw new IllegalArgumentException("tif cannot be null");
        }
        this.tif = tif;
    }

}
