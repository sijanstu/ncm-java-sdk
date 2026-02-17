# NCM Java SDK

Lightweight Java SDK that provides an NCM webhook listener and event-publishing integration for vendor applications. This repository includes the SDK module (`ncm-listener`) and an example consumer application (`vendor-service`).

Supported: Java 17, Spring Boot 3.x

Quick links
- Build & run the example consumer (`vendor-service`)
- Webhook endpoint and payload examples
- Configuration keys

---

## Quick start


Build and run the example consumer application (`vendor-service`) from the repository root:

```bash
# Build the vendor-service module
mvn -f vendor-service/pom.xml clean package

# Run the application
java -jar vendor-service/target/vendor-service-1.0-SNAPSHOT.jar
```

You can also run in place:

```bash
mvn -f vendor-service/pom.xml spring-boot:run
```

Default listen address: http://localhost:8080

---

## Webhook API

Endpoints

- **Standard**: `POST /api/webhooks/ncm`
- **With Secret**: `POST /api/webhooks/ncm?secret=your-secret-key`

Behavior
- Returns 202 Accepted when the webhook is accepted for asynchronous processing.
- Returns 403 Forbidden if a secret is configured but missing or incorrect in the query string.
- Returns 503 Service Unavailable if the listener is disabled via configuration.
- Validation errors (missing order id(s) or invalid JSON) result in 400 Bad Request.

Payload (examples)

Single order:

```json
{
  "order_id": "ORD-001",
  "status": "pickup_completed",
  "event": "PICKUP_COMPLETED",
  "timestamp": "2026-02-17T10:00:00Z"
}
```

Batch:

```json
{
  "order_ids": ["ORD-001","ORD-002"],
  "status": "in_transit",
  "event": "SENT_FOR_DELIVERY",
  "timestamp": "2026-02-17T10:00:00Z"
}
```

Either `order_id` or `order_ids` must be present.

Example curl

**1. Standard request (no secret configured):**

```bash
curl -X POST "http://localhost:8080/api/webhooks/ncm" \
  -H "Content-Type: application/json" \
  -d '{"order_id":"ORD-001","status":"picked_up","event":"PICKUP_COMPLETED","timestamp":"2026-02-17T10:00:00Z"}'
```

**2. Protected request (with secret configured):**

```bash
curl -X POST "http://localhost:8080/api/webhooks/ncm?secret=your-secret-key" \
  -H "Content-Type: application/json" \
  -d '{"order_id":"ORD-001","status":"picked_up","event":"PICKUP_COMPLETED","timestamp":"2026-02-17T10:00:00Z"}'
```

---

## Configuration

Application-level properties (set in `vendor-service/src/main/resources/application.yaml` or env):

- `ncm.listener.enabled` (boolean, default true)
- `ncm.listener.webhook.endpoint` (string, default `/api/webhooks/ncm`)
- `ncm.listener.webhook.secret` (string, optional) — Secret for ?secret= params
- `ncm.listener.webhook.max-order-ids` (integer — maps to property `maxOrderIdsPerRequest`)
- `ncm.listener.events.publish-events` (boolean)
- `ncm.listener.logging.log-all-webhooks` (boolean)

For `@Async` processing tune Spring task executor in application config using `spring.task.execution.*` (the application owns thread-pool config).

---

## Enable and use the library

1. Add dependency (already present): `com.sijanstu.ncm_listener:ncm-listener:1.0-SNAPSHOT`.
2. Enable it from your Spring Boot application class:

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableNcmListener
public class VendorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VendorServiceApplication.class, args);
    }
}
```

The `@EnableNcmListener` annotation triggers component scan for the library webhooks when `ncm.listener.enabled=true`.

---

## Troubleshooting (quick)

- 202 Accepted: webhook accepted and processed asynchronously.
- 400 Bad Request: payload validation failed (missing order id(s) or malformed JSON).
- 403 Forbidden: secret mismatch or missing query parameter.
- 503 Service Unavailable: listener disabled (`ncm.listener.enabled=false`).
- Check logs for `com.sijanstu.ncm_listener` for processing details.

---

## Tests & build

Run module build and tests from repository root:

```bash
mvn clean verify
```

To build only the library module:

```bash
mvn -f ncm-listener/pom.xml clean package
```

---

## License

## 🤝 Support

For issues, questions, or contributions:
1. Check [Troubleshooting](#troubleshooting)
2. Review logs with DEBUG level
3. Verify webhook payload format
4. Check application configuration

---

**Version:** 1.0-SNAPSHOT  
**Java:** 17  
**Spring Boot:** 3.4.1  
**Last Updated:** 2026-02-17
