package com.sijanstu.ncm_listener.webhook.service;

import com.sijanstu.ncm_listener.webhook.config.NcmListenerProperties;
import com.sijanstu.ncm_listener.webhook.domain.WebhookEventType;
import com.sijanstu.ncm_listener.webhook.dto.NcmWebhookPayloadDto;
import com.sijanstu.ncm_listener.webhook.event.NcmOrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Webhook processing service - stateless and event-driven.
 * Receives webhooks, validates, and publishes Spring events for vendor service to handle.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookProcessingService {

    private final ApplicationEventPublisher eventPublisher;
    private final NcmListenerProperties properties;

    /**
     * Process webhook asynchronously.
     *
     * @param payload Webhook payload
     */
    @Async
    public void processWebhookAsync(NcmWebhookPayloadDto payload) {
        if (!properties.isEnabled()) {
            log.debug("NCM Listener is disabled, skipping processing");
            return;
        }

        try {
            processWebhook(payload);
        } catch (Exception e) {
            // Log error but don't propagate - webhook already accepted
            log.error("Error processing webhook asynchronously: {}", e.getMessage(), e);
        }
    }

    /**
     * Process webhook synchronously (internal use only).
     *
     * @param payload Webhook payload
     */
    private void processWebhook(NcmWebhookPayloadDto payload) {
        if (payload == null) {
            log.warn("Payload is null, skipping processing");
            return;
        }

        try {
            List<String> orderIds = extractOrderIds(payload);
            if (orderIds.isEmpty()) {
                log.warn("No valid order IDs found in webhook");
                return;
            }

            // Parse event type (gracefully handle unknown events)
            WebhookEventType eventType = parseEventType(payload.getEvent());
            if (eventType == null) {
                log.warn("Unknown event type: {}", payload.getEvent());
                return;
            }

            if (properties.getLogging().isLogAllWebhooks()) {
                log.info("Webhook processing - OrderIds: {}, Event: {}, Status: {}",
                        String.join(",", orderIds),
                        payload.getEvent(),
                        payload.getStatus());
            }

            // Publish event for each order
            orderIds.forEach(orderId -> publishEvent(orderId, payload.getStatus(), eventType, payload.getTimestamp()));

        } catch (Exception e) {
            log.error("Unexpected error processing webhook: {}", e.getMessage(), e);
        }
    }

    /**
     * Extract order IDs from payload.
     *
     * @param payload Webhook payload
     * @return List of order IDs
     */
    private List<String> extractOrderIds(NcmWebhookPayloadDto payload) {
        if (payload.getOrderId() != null && !payload.getOrderId().trim().isEmpty()) {
            return Collections.singletonList(payload.getOrderId());
        }

        if (payload.getOrderIds() != null && payload.getOrderIds().length > 0) {
            return Arrays.stream(payload.getOrderIds())
                    .filter(id -> id != null && !id.trim().isEmpty())
                    .limit(properties.getWebhook().getMaxOrderIdsPerRequest())
                    .toList();
        }

        return Collections.emptyList();
    }

    /**
     * Parse event type gracefully.
     *
     * @param eventCode Event code
     * @return WebhookEventType or null
     */
    private WebhookEventType parseEventType(String eventCode) {
        return WebhookEventType.fromCode(eventCode);
    }

    /**
     * Publish event for vendor service to listen.
     * Errors are logged but not thrown.
     *
     * @param orderId   Order ID
     * @param status    Order status
     * @param eventType Event type
     * @param timestamp Event timestamp
     */
    private void publishEvent(String orderId, String status, WebhookEventType eventType, Instant timestamp) {
        try {
            if (properties.getEvents().isPublishEvents()) {
                eventPublisher.publishEvent(new NcmOrderStatusChangedEvent(
                        this, orderId, status, eventType, timestamp
                ));
            }
        } catch (Exception e) {
            log.error("Error publishing event for order {}: {}", orderId, e.getMessage(), e);
        }
    }
}
