package io.github.hyperliquid.sdk.utils;

import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统一错误模型，携带错误码、消息与上下文。
 */
public final class ApiError {

    @Getter
    private final String code;
    @Getter
    private final String message;
    @Getter
    private final Integer statusCode;

    private final Map<String, Object> context;

    public ApiError(String code, String message) {
        this(code, message, null, Collections.emptyMap());
    }

    public ApiError(String code, String message, Integer statusCode) {
        this(code, message, statusCode, Collections.emptyMap());
    }

    public ApiError(String code, String message, Integer statusCode, Map<String, Object> context) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
        this.context = context == null ? Collections.emptyMap() : new LinkedHashMap<>(context);
    }

    public Map<String, Object> getContext() {
        return Collections.unmodifiableMap(context);
    }
}

