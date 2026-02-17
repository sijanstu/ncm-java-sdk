package com.sijanstu.ncm_listener.webhook.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable NCM Listener module.
 * This annotation should be placed on a Spring Boot application class to enable the NCM Listener functionality.
 * When enabled, it will scan for webhook handlers in the specified package and activate them based on the configuration property.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ComponentScan(basePackages = "com.sijanstu.autowhats.webhook")
@ConditionalOnProperty(name = "ncm.listener.enabled", havingValue = "true", matchIfMissing = true)
public @interface EnableNcmListener {
}
