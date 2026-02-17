package com.sijanstu.ncm_listener.webhook.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = ValidWebhookPayloadValidator.class)
@Documented
public @interface ValidWebhookPayload {
    String message() default "Either order_id or order_ids must be provided";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

