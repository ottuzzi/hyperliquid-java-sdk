package io.github.hyperliquid.sdk.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.hyperliquid.sdk.utils.HypeError;
import io.github.hyperliquid.sdk.utils.JSONUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

/**
 * API 客户端基类，封装 HTTP 请求与错误处理。
 */
public class API {

    private static final Logger log = LoggerFactory.getLogger(API.class);

    protected final String baseUrl;

    protected final OkHttpClient client;

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");


    /**
     * 构造 API 客户端。
     *
     * @param baseUrl API 根地址（例如 https://api.hyperliquid.xyz）
     * @param timeout 请求超时时间（秒）
     */
    public API(String baseUrl, int timeout) {
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
        this.client = new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(timeout))
                .connectTimeout(Duration.ofSeconds(timeout))
                .readTimeout(Duration.ofSeconds(timeout))
                .writeTimeout(Duration.ofSeconds(timeout))
                .build();
    }

    public API(String baseUrl, OkHttpClient client) {
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
        this.client = client;
    }

    /**
     * 发送 POST 请求到指定路径，并返回 JSON 结果。
     *
     * @param path    相对路径，例如 "/info"、"/exchange"
     * @param payload JSON 可序列化对象（会被 ObjectMapper 序列化）
     * @return 返回的 JSON 节点
     * @throws HypeError.ClientHypeError 当响应为 4xx
     * @throws HypeError.ServerHypeError 当响应为 5xx
     */
    public JsonNode post(String path, Object payload) {
        String url = baseUrl + path;
        String json = "";
        try {
            json = JSONUtil.writeValueAsString(payload);

            RequestBody body = RequestBody.create(json, JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Accept", "application/json")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                // 统一处理响应体，避免重复读取
                String responseBody = response.body() != null ? response.body().string() : "{}";

                if (!response.isSuccessful()) {
                    int code = response.code();
                    String errorMsg = String.format("HTTP %d: %s", code, responseBody);

                    if (code >= 400 && code < 500) {
                        throw new HypeError.ClientHypeError(code, errorMsg);
                    } else {
                        throw new HypeError.ServerHypeError(code, errorMsg);
                    }
                }
                log.debug("POST: {} ", url);
                log.debug("Request: {}", json);
                log.debug("Response: {}", responseBody);

                return JSONUtil.readTree(responseBody);
            }
        } catch (IOException e) {
            log.error("Network error for POST: {} Request: {}", path, json, e);
            throw new HypeError("Network error for POST " + path + ": " + e.getMessage(), e);
        }
    }
}
