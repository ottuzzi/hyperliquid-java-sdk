package io.github.hyperliquid.sdk.model.info;

import lombok.Data;

/** 更新杠杆操作返回 */
@Data
public class UpdateLeverage {
    /** 顶层状态（如 "ok"/"error"） */
    private String status;
    /** 响应体（类型等） */
    private Response response;

    @Data
    public static class Response {
        /** 响应类型描述 */
        private String type;
    }
}
