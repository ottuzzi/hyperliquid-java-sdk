package io.github.hyperliquid.sdk.model.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Order Updates Subscription
 */
public class OrderUpdatesSubscription extends Subscription {

    @JsonProperty("type")
    private final String type = "orderUpdates";

    /**
     * wallet address
     **/
    @JsonProperty("user")
    private String user;

    public OrderUpdatesSubscription(String user) {
        this.user = user;
    }

    public static OrderUpdatesSubscription of(String user) {
        return new OrderUpdatesSubscription(user);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String toIdentifier() {
        if (user == null) {
            return type;
        }
        return type + ":" + user;
    }
}
