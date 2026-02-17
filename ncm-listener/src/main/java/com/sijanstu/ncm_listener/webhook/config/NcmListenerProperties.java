package com.sijanstu.ncm_listener.webhook.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for NCM Listener.
 * Allows enabling/disabling the listener, configuring webhook endpoint, and logging options.
 */
@Component
@ConfigurationProperties(prefix = "ncm.listener")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NcmListenerProperties {

    private boolean enabled = true;
    private Webhook webhook = new Webhook();
    private Events events = new Events();
    private Logging logging = new Logging();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Webhook {
        private String endpoint = "/api/webhooks/ncm";
        private int maxOrderIdsPerRequest = 100;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Events {
        private boolean publishEvents = true;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Logging {
        private boolean logAllWebhooks = true;
    }
}
