package io.github.hyperliquid.sdk;

import io.github.hyperliquid.sdk.model.info.UpdateLeverage;
import org.junit.jupiter.api.Test;

public class SimpleTest {

    String privateKey = "your_private_key_here";
    ExchangeManager manager = ExchangeManager.builder().addPrivateKey(privateKey).enableDebugLogs().build();

    @Test
    public void updateLeverage() {
        UpdateLeverage leverage = manager.getSingleExchangeClient().updateLeverage("BTC", true, 10);
        System.out.println(leverage);
    }
}
