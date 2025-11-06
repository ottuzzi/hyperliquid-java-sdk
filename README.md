**Languages:** [中文](README.zh-CN.md)

# Hyperliquid Java SDK

A Software Development Kit (SDK) for the Hyperliquid decentralized exchange.

## Project Overview

- Info client: market data, order books, user state
- Exchange client: order placement, bulk orders, cancel/modify
- ExchangeManager: high-level module to manage multiple wallet credentials and unified Info access, making trading flows
  simpler
- WebSocket manager: connection lifecycle, heartbeat, resubscription, reconnect with backoff

## Installation

Prerequisites:

- Java 21 or later
- Maven 3.8+

Build from source:

```
mvn -q -DskipTests=true clean package
mvn -q dependency:copy-dependencies
```

Use as a dependency (Maven):

If you are integrating this SDK into your own project, add the following dependency to your root-level pom.xml:

```xml

<dependency>
    <groupId>io.github.heiye115</groupId>
    <artifactId>hyperliquid-java-sdk</artifactId>
    <version>0.2.0</version>
</dependency>
```

Notes:

- Ensure your project uses Java 21+.
- If you previously built from source, you can remove local module references and rely on the Maven artifact directly.

## Usage

This README only retains ExchangeManager-related examples.

### Quick ExchangeManager demo (core)

```java
import io.github.hyperliquid.sdk.ExchangeManager;
import io.github.hyperliquid.sdk.info.Info;
import io.github.hyperliquid.sdk.exchange.Exchange;
import io.github.hyperliquid.sdk.model.order.*;
import io.github.hyperliquid.sdk.utils.Constants;

/**
 * Demo: Initialize ExchangeManager and place a sample order on testnet.
 * - Set environment variable HL_PK to your private key for a real order demo.
 * - Without HL_PK, the demo prints mids and skips order placement.
 */
public class QuickExchangeManagerDemo {
    public static void main(String[] args) {
        String pk = System.getenv("HL_PK");

        ExchangeManager manager = ExchangeManager.builder()
                .baseUrl(Constants.TESTNET_API_URL)
                .timeout(10)
                .skipWs(true)
                .addPrivateKey(pk == null || pk.isBlank() ? "0x0000000000000000000000000000000000000000000000000000000000000000" : pk)
                .build();

        Info info = manager.getInfo();
        System.out.println("BTC mid: " + info.allMids().getOrDefault("BTC", "N/A"));

        if (pk != null && !pk.isBlank()) {
            Exchange ex = manager.getSingleExchange();
            OrderType type = new OrderType(new LimitOrderType("Gtc"), null);
            OrderRequest req = new OrderRequest("ETH", true, 0.01, 2500.0, type, false, null);
            System.out.println(ex.order(req).toPrettyString());
        } else {
            System.out.println("No private key provided; skipping order placement.");
        }
    }
}
```

Minimum version: ExchangeManager requires SDK version >= 0.2.0.

### ExchangeManager examples under examples/

- ExchangeManagerBasicExample.java — initialize the manager and perform a safe order demo
- ExchangeManagerScenarioExample.java — common trading flows: order, bulk order, cancel/modify
- ExchangeManagerErrorHandlingExample.java — handle missing/invalid keys and manager errors

Compile examples (Windows PowerShell):

```
mvn -q -DskipTests=true clean package
mvn -q dependency:copy-dependencies
javac -cp target/classes;target/dependency/* examples/ExchangeManagerBasicExample.java examples/ExchangeManagerScenarioExample.java examples/ExchangeManagerErrorHandlingExample.java
```

Run demos (without private key, demos skip real orders):

```
java -cp .;examples;target/classes;target/dependency/* ExchangeManagerBasicExample
java -cp .;examples;target/classes;target/dependency/* ExchangeManagerErrorHandlingExample
```

## Verification Checklist

- Old examples have been removed from this README (only ExchangeManager demos remain)
- Retained examples compile and run with the commands above
- Document structure is clear and navigation works (English/Chinese links)
- No residual references to old examples

## License

Apache License 2.0. See `LICENSE` at the project root.

Copyright notice:

- Copyright (c) 2025 Hyperliquid Java SDK contributors
- Distributed under Apache 2.0 on an "AS IS" basis without warranties or conditions of any kind

