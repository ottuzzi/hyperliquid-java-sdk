package io.github.hyperliquid.sdk.model.info;

import lombok.Data;

import java.util.List;

@Data
public class OrderStatus {

    private String status;

    private Order order;

    @Data
    public static class Order {
        private OrderDetail order;
        private Long statusTimestamp;

        @Data
        public static class OrderDetail {
            private String coin;
            private String side;
            private String limitPx;
            private String sz;
            private Long oid;
            private Long timestamp;
            private String triggerCondition;
            private Boolean isTrigger;
            private String triggerPx;
            private List<String> children;
            private Boolean isPositionTpsl;
            private Boolean reduceOnly;
            private String orderType;
            private String origSz;
            private String tif;
            private String cloid;
        }
    }
}
