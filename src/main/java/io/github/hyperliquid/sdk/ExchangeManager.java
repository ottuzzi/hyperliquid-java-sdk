package io.github.hyperliquid.sdk;

import io.github.hyperliquid.sdk.exchange.Exchange;
import io.github.hyperliquid.sdk.info.Info;
import io.github.hyperliquid.sdk.utils.Constants;
import io.github.hyperliquid.sdk.utils.HypeError;
import okhttp3.OkHttpClient;
import org.web3j.crypto.Credentials;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * ExchangeManager 统一管理客户端，负责下单、撤单、转账等 L1/L2 操作。
 * 支持多个钱包凭证的管理与切换。
 * 当前版本实现核心下单与批量下单，其他 L1 操作将在后续补充。
 */
public class ExchangeManager {


    private final Info info;

    /**
     * K:私钥 V:Exchange
     **/
    private final Map<String, Exchange> exchangeMap;

    /**
     * K:私钥 V:地址
     **/
    private final Map<String, String> privateKeyMap;


    private ExchangeManager(Info info, Map<String, Exchange> exchangeMap, Map<String, String> privateKeyMap) {
        this.info = info;
        this.exchangeMap = exchangeMap;
        this.privateKeyMap = privateKeyMap;
    }

    /**
     * 获取 Info 实例
     **/
    public Info getInfo() {
        return info;
    }

    /**
     * 获取单个Exchange , 如果有多个则返回第一个
     **/
    public Exchange getSingleExchange() {
        if (exchangeMap.isEmpty()) {
            throw new HypeError("No exchange instances available.");
        }
        return exchangeMap.values().iterator().next();
    }

    /**
     * 根据私钥获取 Exchange 实例
     **/
    public Exchange useExchange(String privateKey) {
        Exchange ex = exchangeMap.get(privateKey);
        if (ex == null) {
            throw new HypeError("No exchange instance found for the provided private key.");
        }
        return ex;
    }

    /**
     * 获取钱包地址
     **/
    public String getAddress(String privateKey) {
        String address = privateKeyMap.get(privateKey);
        if (address == null) {
            throw new HypeError("No address found for the provided private key.");
        }
        return address;
    }

    /**
     * 获取单个钱包地址 , 如果有多个则返回第一个
     **/
    public String getSingleAddress() {
        if (privateKeyMap.isEmpty()) {
            throw new HypeError("No addresses available.");
        }
        return privateKeyMap.values().iterator().next();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String baseUrl = Constants.MAINNET_API_URL;

        private int timeout = 10;

        private boolean skipWs = false;

        private final List<String> privateKeys = new ArrayList<>();

        private OkHttpClient okHttpClient = null;

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder addPrivateKey(String privateKey) {
            privateKeys.add(privateKey);
            return this;
        }

        public Builder addPrivateKeys(List<String> pks) {
            privateKeys.addAll(pks);
            return this;
        }

        public Builder skipWs(boolean skipWs) {
            this.skipWs = skipWs;
            return this;
        }

        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder okHttpClient(OkHttpClient client) {
            this.okHttpClient = client;
            return this;
        }

        private OkHttpClient getOkHttpClient() {
            return okHttpClient != null ? okHttpClient : new OkHttpClient.Builder()
                    .connectTimeout(Duration.ofSeconds(timeout))
                    .readTimeout(Duration.ofSeconds(timeout))
                    .writeTimeout(Duration.ofSeconds(timeout))
                    .build();
        }


        public ExchangeManager build() {
            OkHttpClient httpClient = getOkHttpClient();
            Info info = new Info(baseUrl, httpClient, skipWs);
            Map<String, Exchange> exchangeMap = new ConcurrentHashMap<>();
            Map<String, String> privateKeyMap = new ConcurrentHashMap<>();
            if (!privateKeys.isEmpty()) {
                for (String key : privateKeys) {
                    Credentials credentials = Credentials.create(key);
                    privateKeyMap.put(key, credentials.getAddress());
                    exchangeMap.put(key, new Exchange(baseUrl, httpClient, credentials, info));
                }
            }
            return new ExchangeManager(info, exchangeMap, privateKeyMap);
        }
    }
}
