package io.github.hyperliquid.sdk.model.order;

import lombok.Data;

import java.util.List;

/** 下单响应封装（包含 resting/filled/error 状态） */
@Data
public class Order {
    /** 顶层状态（如 "ok"/"error"） */
    private String status;
    /** 响应体，包含类型与数据 */
    private Response response;

    @lombok.Data
    public static class Resting {
        /** 挂单订单 ID */
        private long oid;
    }

    @lombok.Data
    public static class Statuses {
        /** 未成交挂单信息 */
        private Resting resting;
        /** 已成交信息 */
        private Filled filled;
        /** 错误描述（若有） */
        private String error;
    }


    @lombok.Data
    public static class Filled {
        /** 成交总数量（字符串） */
        private String totalSz;
        /** 成交均价（字符串） */
        private String avgPx;
        /** 订单 ID */
        private Long oid;
        /** 客户端订单 ID */
        private String cloid;
    }

    @lombok.Data
    public static class Data {
        /** 各订单状态列表 */
        private List<Statuses> statuses;
    }

    @lombok.Data
    public static class Response {
        /** 响应类型（如 "order"） */
        private String type;
        /** 订单状态数据 */
        private Data data;
    }
}
