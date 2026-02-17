package com.sijanstu.ncm_listener.webhook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sijanstu.ncm_listener.webhook.validation.ValidWebhookPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * NCM webhook payload.
 * Assumes valid data from NCM (validation errors are not returned).
 */
@ValidWebhookPayload
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NcmWebhookPayloadDto {

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("order_ids")
    private String[] orderIds;

    private String status;

    private String event;

    private Instant timestamp;
}
