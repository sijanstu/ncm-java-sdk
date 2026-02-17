package com.sijanstu.ncm_listener.webhook.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * NCM webhook event types
 */
@Getter
@AllArgsConstructor
public enum WebhookEventType {
    PICKUP_COMPLETED("pickup_completed"),
    SENT_FOR_DELIVERY("sent_for_delivery"),
    ORDER_DISPATCHED("order_dispatched"),
    ORDER_ARRIVED("order_arrived"),
    DELIVERY_COMPLETED("delivery_completed");

    private final String eventCode;

    private static final Map<String, WebhookEventType> LOOKUP = new HashMap<>();

    static {
        for (WebhookEventType t : values()) {
            LOOKUP.put(t.eventCode.toLowerCase(), t);
        }
    }

    /**
     * Parse event code to WebhookEventType.
     * Returns null for unknown codes (doesn't throw).
     *
     * @param code Event code from webhook
     * @return WebhookEventType or null if not found
     */
    public static WebhookEventType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        return LOOKUP.get(code.trim().toLowerCase());
    }
}
