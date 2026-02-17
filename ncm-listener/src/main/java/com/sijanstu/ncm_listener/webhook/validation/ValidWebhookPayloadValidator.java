package com.sijanstu.ncm_listener.webhook.validation;

import com.sijanstu.ncm_listener.webhook.dto.NcmWebhookPayloadDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidWebhookPayloadValidator implements ConstraintValidator<ValidWebhookPayload, NcmWebhookPayloadDto> {

    @Override
    public boolean isValid(NcmWebhookPayloadDto value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        boolean hasSingle = value.getOrderId() != null && !value.getOrderId().trim().isEmpty();
        boolean hasMultiple = value.getOrderIds() != null && value.getOrderIds().length > 0;
        return hasSingle || hasMultiple;
    }
}

