package com.sijanstu.ncm_listener.webhook.event;

import com.sijanstu.ncm_listener.webhook.domain.WebhookEventType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

/**
 * Domain event published when NCM webhook is received.
 * Vendors listen to this event to handle order status changes.
 */
@Getter
public class NcmOrderStatusChangedEvent extends ApplicationEvent {

    private final String orderId;
    private final String status;
    private final WebhookEventType eventType;
    private final Instant eventTimestamp;

    /**
     * Create event from webhook data
     *
     * @param source         Event source
     * @param orderId        Order ID from webhook
     * @param status         Order status from webhook
     * @param eventType      NCM event type
     * @param eventTimestamp Event timestamp from NCM
     */
    public NcmOrderStatusChangedEvent(
            Object source,
            String orderId,
            String status,
            WebhookEventType eventType,
            Instant eventTimestamp) {
        super(source);
        this.orderId = orderId;
        this.status = status;
        this.eventType = eventType;
        this.eventTimestamp = eventTimestamp;
    }
}


