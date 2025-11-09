package io.github.hyperliquid.sdk.model.info;

import lombok.Data;

@Data
public class UpdateLeverage {
    private String status;
    private Response response;

    @Data
    public static class Response {
        private String type;
    }
}
