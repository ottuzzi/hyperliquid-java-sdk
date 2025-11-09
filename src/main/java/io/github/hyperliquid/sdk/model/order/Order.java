package io.github.hyperliquid.sdk.model.order;

import lombok.Data;

import java.util.List;

@Data
public class Order {
    private String status;
    private Response response;

    @lombok.Data
    public static class Resting {
        private long oid;
    }

    @lombok.Data
    public static class Statuses {
        private Resting resting;
        private Filled filled;
        private String error;
    }


    @lombok.Data
    public static class Filled {
        private String totalSz;
        private String avgPx;
        private Long oid;
        private String cloid;
    }

    @lombok.Data
    public static class Data {
        private List<Statuses> statuses;
    }

    @lombok.Data
    public static class Response {
        private String type;
        private Data data;
    }
}
