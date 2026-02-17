package com.sijanstu.ncm_listener.webhook.controller;

import com.sijanstu.ncm_listener.webhook.config.NcmListenerProperties;
import com.sijanstu.ncm_listener.webhook.dto.NcmWebhookPayloadDto;
import com.sijanstu.ncm_listener.webhook.service.WebhookProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint for NCM webhooks.
 * Receives webhook payloads and delegates to processing service which emits events.
 */
@Slf4j
@RestController
@RequestMapping("${ncm.listener.webhook.endpoint:/api/webhooks/ncm}")
@RequiredArgsConstructor
public class NcmWebhookController {

    private final WebhookProcessingService webhookProcessingService;
    private final NcmListenerProperties properties;

    /**
     * Handle incoming NCM webhook
     *
     * @param payload Webhook payload
     */
    @PostMapping
    public void handleWebhook(
            @RequestBody NcmWebhookPayloadDto payload) {

        if (properties.isEnabled() && payload != null) {
            try {
                webhookProcessingService.processWebhookAsync(payload);
                log.debug("Webhook accepted for processing");
            } catch (Exception e) {
                log.warn("Failed to submit webhook for async processing: {}", e.getMessage());
            }
        }
    }
}
