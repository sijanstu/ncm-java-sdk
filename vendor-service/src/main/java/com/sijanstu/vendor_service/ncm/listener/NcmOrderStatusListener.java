package com.sijanstu.vendor_service.ncm.listener;

import com.sijanstu.ncm_listener.webhook.domain.WebhookEventType;
import com.sijanstu.ncm_listener.webhook.event.NcmOrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NcmOrderStatusListener {

    @EventListener
    public void handleNcmOrderStatusChanged(NcmOrderStatusChangedEvent event) {
        processOrderEvent(event);
    }

    private void processOrderEvent(NcmOrderStatusChangedEvent event) {
        WebhookEventType eventType = event.getEventType();

        if (eventType == null) {
            log.debug("Event type is null, skipping processing");
            return;
        }

        switch (eventType) {
            case PICKUP_COMPLETED:
                handlePickupCompleted(event);
                break;
            case SENT_FOR_DELIVERY:
                handleSentForDelivery(event);
                break;
            case ORDER_DISPATCHED:
                handleOrderDispatched(event);
                break;
            case ORDER_ARRIVED:
                handleOrderArrived(event);
                break;
            case DELIVERY_COMPLETED:
                handleDeliveryCompleted(event);
                break;
        }
    }

    private void handlePickupCompleted(NcmOrderStatusChangedEvent event) {
        log.debug("Pickup completed - Order ID: {}", event.getOrderId());
    }

    private void handleSentForDelivery(NcmOrderStatusChangedEvent event) {
        log.debug("Sent for delivery - Order ID: {}", event.getOrderId());
    }

    private void handleOrderDispatched(NcmOrderStatusChangedEvent event) {
        log.debug("Order dispatched - Order ID: {}", event.getOrderId());
    }

    private void handleOrderArrived(NcmOrderStatusChangedEvent event) {
        log.debug("Order arrived - Order ID: {}", event.getOrderId());
    }

    private void handleDeliveryCompleted(NcmOrderStatusChangedEvent event) {
        log.debug("Delivery completed - Order ID: {}", event.getOrderId());
    }
}

