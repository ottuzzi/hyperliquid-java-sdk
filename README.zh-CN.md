**Languages:** [English](README.md)

# Hyperliquid Java SDK

Hyperliquid 去中心化交易所的软件开发工具包 (SDK)。

## 项目概述

- Info 客户端：行情、订单簿、用户状态
- Exchange 客户端：下单、批量下单、撤单/改价
- ExchangeManager 模块：统一管理多个钱包凭证并复用单个 Info 客户端，简化常见交易流程
- WebSocket 管理器：连接生命周期、心跳、自动重订阅、指数退避重连

## 安装

环境要求：

- Java 21 或更高版本
- Maven 3.8+

从源码构建：

```
mvn -q -DskipTests=true clean package
mvn -q dependency:copy-dependencies
```

## 使用

本文档仅保留与 ExchangeManager 相关的示例。

### QuickExchangeManagerDemoCN（核心示例）

```java
import io.github.hyperliquid.sdk.ExchangeManager;
import io.github.hyperliquid.sdk.info.Info;
import io.github.hyperliquid.sdk.exchange.Exchange;
import io.github.hyperliquid.sdk.model.order.*;
import io.github.hyperliquid.sdk.utils.Constants;

/**
 * 示例：初始化 ExchangeManager 并在测试网进行安全演示下单。
 * - 如设置环境变量 HL_PK（你的私钥），将进行真实下单演示。
 * - 未设置 HL_PK 时，仅打印中间价并跳过真实下单。
 */
public class QuickExchangeManagerDemoCN {
    public static void main(String[] args) {
        String pk = System.getenv("HL_PK");

        ExchangeManager manager = ExchangeManager.builder()
                .baseUrl(Constants.TESTNET_API_URL)
                .timeout(10)
                .skipWs(true)
                .addPrivateKey(pk == null || pk.isBlank() ? "0x0000000000000000000000000000000000000000000000000000000000000000" : pk)
                .build();

        Info info = manager.getInfo();
        System.out.println("BTC 中间价: " + info.allMids().getOrDefault("BTC", "N/A"));

        if (pk != null && !pk.isBlank()) {
            Exchange ex = manager.getSingleExchange();
            OrderType type = new OrderType(new LimitOrderType("Gtc"), null);
            OrderRequest req = new OrderRequest("ETH", true, 0.01, 2500.0, type, false, null);
            System.out.println(ex.order(req).toPrettyString());
        } else {
            System.out.println("未提供私钥，跳过真实下单示例。");
        }
    }
}
```

最低版本要求：使用 ExchangeManager 需要 SDK 版本 >= 0.2.0。

### examples/ 目录中的 ExchangeManager 相关示例

- ExchangeManagerBasicExample.java —— 初始化管理器并进行安全下单演示
- ExchangeManagerScenarioExample.java —— 典型交易流程：下单、批量下单、撤单/改价
- ExchangeManagerErrorHandlingExample.java —— 错误处理：缺失/非法私钥与异常示范

编译示例（Windows PowerShell）：

```
mvn -q -DskipTests=true clean package
mvn -q dependency:copy-dependencies
javac -cp target/classes;target/dependency/* examples/ExchangeManagerBasicExample.java examples/ExchangeManagerScenarioExample.java examples/ExchangeManagerErrorHandlingExample.java
```

运行演示（无私钥将跳过真实下单）：

```
java -cp .;examples;target/classes;target/dependency/* ExchangeManagerBasicExample
java -cp .;examples;target/classes;target/dependency/* ExchangeManagerErrorHandlingExample
```

## 验证清单

- 文档已移除旧示例，仅保留 ExchangeManager 相关示例
- 保留示例可按上述命令编译运行
- 文档结构清晰，中英文导航可用（README.md / README.zh-CN.md）
- 无残余旧示例引用或说明

## 许可证

Apache License 2.0。详见项目根目录 `LICENSE` 文件。

版权声明：

- Copyright (c) 2025 Hyperliquid Java SDK contributors
- 按 Apache 2.0 许可“现状”分发，不附带任何明示或暗示的担保或条件。

