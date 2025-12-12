import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.hyperliquid.sdk.HyperliquidClient;
import io.github.hyperliquid.sdk.apis.Exchange;
import io.github.hyperliquid.sdk.apis.Info;
import io.github.hyperliquid.sdk.model.info.L2Book;
import io.github.hyperliquid.sdk.model.order.Order;
import io.github.hyperliquid.sdk.model.order.OrderRequest;
import io.github.hyperliquid.sdk.utils.HypeError;
import io.github.hyperliquid.sdk.utils.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuickStartExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuickStartExample.class);

    public static void main(String[] args) {
        //1. Recommended: Use API Wallet for better security
        // API Wallet: Sub-wallet authorized by main wallet, with limited permissions, main private key not exposed
        String primaryWalletAddress = "";  // Primary wallet address
        String apiWalletPrivateKey = "";   // API wallet private key

        // 2. Build the client for the testnet
        HyperliquidClient client = HyperliquidClient.builder()
                .testNetUrl() // Use the testnet environment
                .addApiWallet(primaryWalletAddress, apiWalletPrivateKey)
                .build();

        Info info = client.getInfo();
        Exchange exchange = client.getExchange(); // Get the exchange instance for the added wallet

        // 3. Query market data: Get the L2 book for "ETH"
        try {
            LOGGER.info("Querying L2 book for ETH...");
            L2Book l2Book = info.l2Book("ETH");
            // Print the top 3 levels of bids and asks
            LOGGER.info("Successfully retrieved L2 book for {}:", l2Book.getCoin());
            l2Book.getLevels().get(0).subList(0, 3).forEach(level ->
                    LOGGER.info("  Ask - Price: {}, Size: {}", level.getPx(), level.getSz())
            );
            l2Book.getLevels().get(1).subList(0, 3).forEach(level ->
                    LOGGER.info("  Bid - Price: {}, Size: {}", level.getPx(), level.getSz())
            );
        } catch (HypeError e) {
            LOGGER.error("Failed to query L2 book.  Message: {}", e.getMessage());
        }

        // 4. Execute a trade: Create a limit buy order for ETH
        try {
            LOGGER.info("Placing a limit buy order for ETH...");
            // Create a limit buy order for 0.01 ETH at a price of $1500
            // This order will be canceled if not filled immediately (IOC)
            OrderRequest orderRequest = OrderRequest.builder()
                    .perp("ETH") // Perpetual contract for ETH
                    .buy("0.01") // Buying 0.01 ETH
                    .limitPrice("1500") // Limit price for the order
                    .gtc() // Good Till Cancel (GTC) order
                    .build();

            Order order = exchange.order(orderRequest);
            LOGGER.info("Order placed successfully. Response: {}", JSONUtil.writeValueAsString(order));

        } catch (HypeError | JsonProcessingException e) {
            // Example of handling a specific error, e.g., insufficient margin
            LOGGER.error("Order placement failed. Message: {}", e.getMessage(), e);
        }
    }
}