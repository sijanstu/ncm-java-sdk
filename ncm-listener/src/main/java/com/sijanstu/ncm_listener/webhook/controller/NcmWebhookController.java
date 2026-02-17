package com.sijanstu.ncm_listener.webhook.controller;

import com.sijanstu.ncm_listener.webhook.config.NcmListenerProperties;
import com.sijanstu.ncm_listener.webhook.dto.NcmWebhookPayloadDto;
import com.sijanstu.ncm_listener.webhook.service.WebhookProcessingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
@Validated
public class NcmWebhookController {

    private final WebhookProcessingService webhookProcessingService;
    private final NcmListenerProperties properties;

    /**
     * Handle incoming NCM webhook
     *
     * @param payload Webhook payload
     */
    @PostMapping
    public ResponseEntity<Void> handleWebhook(@Valid @NotNull @RequestBody NcmWebhookPayloadDto payload) {
        if (!properties.isEnabled()) {
            log.debug("NCM Listener disabled - rejecting webhook");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        try {
            webhookProcessingService.processWebhookAsync(payload);
            log.debug("Webhook accepted for processing");
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (Exception e) {
            log.warn("Failed to submit webhook for async processing: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
