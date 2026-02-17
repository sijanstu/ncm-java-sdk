# Vendor Service - NCM Webhook Listener

[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A production-ready **Spring Boot 3.4.1** microservice that receives NCM (Order Management) webhooks and processes order events through an event-driven architecture. Built with asynchronous processing, comprehensive logging, and extensible event handlers.

## 📋 Table of Contents

- [Quick Start](#quick-start)
- [Architecture](#architecture)
- [Webhook API](#webhook-api)
- [Event Types](#event-types)
- [Configuration](#configuration)
- [Building & Deployment](#building--deployment)
- [Usage Examples](#usage-examples)
- [Event Processing](#event-processing)
- [Extending](#extending)
- [Troubleshooting](#troubleshooting)
- [Best Practices](#best-practices)

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8.1+

### Build
```bash
cd vendor-service
mvn clean package
```

### Run
```bash
java -jar target/vendor-service-1.0-SNAPSHOT.jar
```

Service starts on **http://localhost:8080**

### Test Webhook
```bash
curl -X POST http://localhost:8080/api/webhooks/ncm \
  -H "Content-Type: application/json" \
  -d '{
    "order_id": "NCM-ORD-2026-001",
    "status": "pickup_completed",
    "event": "PICKUP_COMPLETED",
    "timestamp": "2026-02-17T17:32:00Z"
  }'
```

## 🏗️ Architecture

### Integrated Design
Vendor Service **includes** the NCM Listener library as a component. It's a **single unified service**, not separate microservices.

```
┌─────────────────────────────────────────────────────┐
│          Vendor Service (Single JAR)                │
├─────────────────────────────────────────────────────┤
│                                                      │
│  ┌──────────────────────────────────────────────┐  │
│  │ NCM Listener Library (Integrated)            │  │
│  ├──────────────────────────────────────────────┤  │
│  │ • NcmWebhookController                       │  │
│  │   └─ POST /api/webhooks/ncm                 │  │
│  │                                              │  │
│  │ • WebhookProcessingService                   │  │
│  │   └─ Validates & publishes events (@Async) │  │
│  │                                              │  │
│  │ • NcmOrderStatusChangedEvent                 │  │
│  │   └─ Spring ApplicationEvent                 │  │
│  └──────────────────────────────────────────────┘  │
│                      ↓                               │
│  ┌──────────────────────────────────────────────┐  │
│  │ Vendor Service Logic                         │  │
│  ├──────────────────────────────────────────────┤  │
│  │ • NcmOrderStatusListener                     │  │
│  │   └─ @EventListener                          │  │
│  │   └─ Routes events to handlers               │  │
│  │                                              │  │
│  │ • Event Handlers                             │  │
│  │   ├─ handlePickupCompleted()                │  │
│  │   ├─ handleSentForDelivery()                │  │
│  │   ├─ handleOrderDispatched()                │  │
│  │   ├─ handleOrderArrived()                   │  │
│  │   └─ handleDeliveryCompleted()              │  │
│  │                                              │  │
│  │ • Custom Business Logic                      │  │
│  │   └─ Database, notifications, etc.           │  │
│  └──────────────────────────────────────────────┘  │
│                                                      │
└─────────────────────────────────────────────────────┘
         Port: 8080
```

### Event Flow

```
External NCM System
         ↓
    HTTP Webhook
         ↓
POST /api/webhooks/ncm (port 8080)
         ↓
NcmWebhookController (from library)
         ↓
WebhookProcessingService (@Async)
         ↓
Validates & Parses Webhook
         ↓
NcmOrderStatusChangedEvent (Spring ApplicationEvent)
         ↓
ApplicationEventPublisher
         ↓
NcmOrderStatusListener (@EventListener)
         ↓
Route to Handler Method (switch on eventType)
         ↓
Custom Business Logic
```

## 📡 Webhook API

### Endpoint
```
POST /api/webhooks/ncm
Host: localhost:8080
Content-Type: application/json
```

### Request Payload

**Single Order:**
```json
{
  "order_id": "NCM-ORD-2026-001",
  "status": "pickup_completed",
  "event": "PICKUP_COMPLETED",
  "timestamp": "2026-02-17T17:32:00Z"
}
```

**Multiple Orders:**
```json
{
  "order_ids": ["ORD-123", "ORD-124", "ORD-125"],
  "status": "sent_for_delivery",
  "event": "SENT_FOR_DELIVERY",
  "timestamp": "2026-02-17T17:32:00Z"
}
```

### Response
```
HTTP 200 OK
(Processing happens asynchronously)
```

### Payload Schema

| Field | Type | Required | Format | Description |
|-------|------|----------|--------|-------------|
| `order_id` | string | ✓* | Any | Single order ID |
| `order_ids` | string[] | ✓* | Any | Multiple order IDs |
| `status` | string | ✓ | lowercase | Current order status |
| `event` | string | ✓ | UPPERCASE | Event type (see Event Types) |
| `timestamp` | string | ✓ | ISO-8601 | Event timestamp |

*Either `order_id` OR `order_ids` must be provided (not both)

### Error Handling
- ✅ **200 OK** - Webhook accepted for processing
- ❌ **400 Bad Request** - Invalid JSON or missing required fields
- ❌ **500 Server Error** - Processing failure (logged)

## 🎯 Event Types

The service supports five order lifecycle events:

| Event Type | Code | Handler | Description |
|-----------|------|---------|-------------|
| **Pickup Completed** | `PICKUP_COMPLETED` | `handlePickupCompleted()` | Order picked up from sender |
| **Sent for Delivery** | `SENT_FOR_DELIVERY` | `handleSentForDelivery()` | Order handed to delivery partner |
| **Order Dispatched** | `ORDER_DISPATCHED` | `handleOrderDispatched()` | Order out for delivery |
| **Order Arrived** | `ORDER_ARRIVED` | `handleOrderArrived()` | Order arrived at destination |
| **Delivery Completed** | `DELIVERY_COMPLETED` | `handleDeliveryCompleted()` | Order successfully delivered |

### Example Events

```bash
# Event 1: Pickup Completed
curl -X POST http://localhost:8080/api/webhooks/ncm \
  -H "Content-Type: application/json" \
  -d '{
    "order_id": "ORD-001",
    "status": "picked_up",
    "event": "PICKUP_COMPLETED",
    "timestamp": "2026-02-17T10:00:00Z"
  }'

# Event 2: Sent for Delivery
curl -X POST http://localhost:8080/api/webhooks/ncm \
  -H "Content-Type: application/json" \
  -d '{
    "order_id": "ORD-001",
    "status": "in_transit",
    "event": "SENT_FOR_DELIVERY",
    "timestamp": "2026-02-17T12:00:00Z"
  }'

# Event 3: Delivery Completed
curl -X POST http://localhost:8080/api/webhooks/ncm \
  -H "Content-Type: application/json" \
  -d '{
    "order_id": "ORD-001",
    "status": "delivered",
    "event": "DELIVERY_COMPLETED",
    "timestamp": "2026-02-17T15:00:00Z"
  }'
```

## ⚙️ Configuration

### Application Properties (application.yaml)

```yaml
spring:
  application:
    name: vendor-service

  # Async Task Execution (for @Async processing)
  task:
    execution:
      pool:
        core-size: 2        # Min threads
        max-size: 5         # Max threads
        queue-capacity: 100 # Queue size
    scheduling:
      pool:
        size: 2             # Scheduler threads

server:
  port: 8080                # Listen port
  compression:
    enabled: true           # Enable gzip

logging:
  level:
    com.sijanstu.vendor_service: DEBUG
    com.sijanstu.ncm_listener: DEBUG
  pattern:
    console: "%d{HH:mm:ss} [%thread] %logger{36} - %msg%n"

# NCM Listener Configuration
ncm:
  listener:
    enabled: true
    webhook:
      endpoint: /api/webhooks/ncm
      max-order-ids: 100
    events:
      publish-events: true
    logging:
      log-all-webhooks: true
```

### Environment Variables (Optional)
```bash
SERVER_PORT=8080
LOGGING_LEVEL_COM_SIJANSTU=DEBUG
NCM_LISTENER_ENABLED=true
```

## 🔨 Building & Deployment

### Build Steps

```bash
# Clean build
mvn clean package

# Build with tests
mvn clean verify

# Build specific module
mvn clean package -DskipTests
```

### Output
```
target/vendor-service-1.0-SNAPSHOT.jar (52MB fat JAR)
```

### Docker Deployment (Optional)

```dockerfile
FROM openjdk:17-slim
COPY target/vendor-service-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
docker build -t vendor-service:latest .
docker run -p 8080:8080 vendor-service:latest
```

### Kubernetes Deployment (Optional)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: vendor-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: vendor-service
  template:
    metadata:
      labels:
        app: vendor-service
    spec:
      containers:
      - name: vendor-service
        image: vendor-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: SERVER_PORT
          value: "8080"
```

## 💡 Usage Examples

### Example 1: Single Order Pickup

```bash
#!/bin/bash

ORDER_ID="NCM-2026-00123"
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

curl -X POST http://localhost:8080/api/webhooks/ncm \
  -H "Content-Type: application/json" \
  -d "{
    \"order_id\": \"$ORDER_ID\",
    \"status\": \"picked_up\",
    \"event\": \"PICKUP_COMPLETED\",
    \"timestamp\": \"$TIMESTAMP\"
  }"
```

### Example 2: Batch Order Update

```bash
#!/bin/bash

TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

curl -X POST http://localhost:8080/api/webhooks/ncm \
  -H "Content-Type: application/json" \
  -d "{
    \"order_ids\": [\"ORD-001\", \"ORD-002\", \"ORD-003\"],
    \"status\": \"in_transit\",
    \"event\": \"SENT_FOR_DELIVERY\",
    \"timestamp\": \"$TIMESTAMP\"
  }"
```

### Example 3: Using Python

```python
import requests
import json
from datetime import datetime

webhook_url = "http://localhost:8080/api/webhooks/ncm"

payload = {
    "order_id": "ORD-12345",
    "status": "delivered",
    "event": "DELIVERY_COMPLETED",
    "timestamp": datetime.utcnow().isoformat() + "Z"
}

response = requests.post(
    webhook_url,
    json=payload,
    headers={"Content-Type": "application/json"}
)

print(f"Status: {response.status_code}")
print(f"Response: {response.text}")
```

### Example 4: Using Node.js/TypeScript

```typescript
const axios = require('axios');

const sendWebhook = async (orderId: string, eventType: string) => {
  try {
    const response = await axios.post(
      'http://localhost:8080/api/webhooks/ncm',
      {
        order_id: orderId,
        status: eventType.toLowerCase(),
        event: eventType,
        timestamp: new Date().toISOString()
      },
      {
        headers: { 'Content-Type': 'application/json' }
      }
    );
    console.log('Webhook sent:', response.status);
  } catch (error) {
    console.error('Webhook failed:', error.message);
  }
};

sendWebhook('ORD-12345', 'PICKUP_COMPLETED');
```

## 🔄 Event Processing

### Processing Flow

```
1. Webhook Received
   └─ Validated by NcmWebhookController

2. Order IDs Extracted
   └─ Single (order_id) or multiple (order_ids)

3. Event Type Parsed
   └─ Matched against WebhookEventType enum

4. Event Published (@Async)
   └─ NcmOrderStatusChangedEvent per order

5. Event Routed
   └─ NcmOrderStatusListener receives event

6. Handler Invoked
   └─ Based on eventType (switch statement)

7. Custom Logic Executed
   └─ Your business implementation
```

### Processing Characteristics

| Aspect | Value |
|--------|-------|
| **Webhook Acceptance** | Synchronous (immediate 200 OK) |
| **Processing** | Asynchronous (@Async) |
| **Thread Pool** | 2-5 threads |
| **Queue Size** | 100 events |
| **Error Handling** | Logged, non-blocking |
| **Scalability** | Configurable thread pool |

## 🎨 Extending

### Add Custom Event Handler

Edit `NcmOrderStatusListener.java`:

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class NcmOrderStatusListener {

    private final OrderRepository orderRepository;      // Inject your service
    private final NotificationService notificationService;

    @EventListener
    public void handleNcmOrderStatusChanged(NcmOrderStatusChangedEvent event) {
        log.debug("Processing order: {}", event.getOrderId());
        processOrderEvent(event);
    }

    private void handlePickupCompleted(NcmOrderStatusChangedEvent event) {
        log.debug("Pickup completed - Order ID: {}", event.getOrderId());
        
        // Your custom logic
        Order order = orderRepository.findByOrderId(event.getOrderId());
        order.setStatus("PICKED_UP");
        orderRepository.save(order);
        
        // Send notification
        notificationService.sendPickupConfirmation(order);
    }

    private void handleDeliveryCompleted(NcmOrderStatusChangedEvent event) {
        log.debug("Delivery completed - Order ID: {}", event.getOrderId());
        
        // Your custom logic
        Order order = orderRepository.findByOrderId(event.getOrderId());
        order.setStatus("DELIVERED");
        order.setDeliveredAt(LocalDateTime.now());
        orderRepository.save(order);
        
        // Send delivery confirmation
        notificationService.sendDeliveryConfirmation(order);
    }
}
```

### Add Database Persistence

Add dependency to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

### Add Email Notifications

```java
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    public void sendDeliveryConfirmation(Order order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(order.getCustomerEmail());
        message.setSubject("Order Delivered: " + order.getOrderId());
        message.setText("Your order has been delivered successfully!");
        mailSender.send(message);
    }
}
```

### Add Monitoring/Metrics

```java
@Service
@RequiredArgsConstructor
public class NcmOrderStatusListener {

    private final MeterRegistry meterRegistry;

    @EventListener
    public void handleNcmOrderStatusChanged(NcmOrderStatusChangedEvent event) {
        meterRegistry.counter("ncm.events.received", 
            "event_type", event.getEventType().toString()).increment();
        
        processOrderEvent(event);
        
        meterRegistry.counter("ncm.events.processed", 
            "event_type", event.getEventType().toString()).increment();
    }
}
```

## 🐛 Troubleshooting

### Issue: "No valid order IDs found in webhook"

**Cause:** Webhook payload missing `order_id` or `order_ids` field

**Solution:**
```bash
# ❌ WRONG (no order ID)
curl -X POST http://localhost:8080/api/webhooks/ncm \
  -H "Content-Type: application/json" \
  -d '{"status": "pickup_completed"}'

# ✅ CORRECT (with order_id)
curl -X POST http://localhost:8080/api/webhooks/ncm \
  -H "Content-Type: application/json" \
  -d '{
    "order_id": "ORD-123",
    "status": "pickup_completed",
    "event": "PICKUP_COMPLETED",
    "timestamp": "2026-02-17T17:32:00Z"
  }'
```

### Issue: "Unknown event type"

**Cause:** Event field not in uppercase or not a valid type

**Solution:**
```bash
# ❌ WRONG (lowercase or invalid)
"event": "pickup_completed"

# ✅ CORRECT (valid types)
"event": "PICKUP_COMPLETED"      # ✓
"event": "SENT_FOR_DELIVERY"     # ✓
"event": "ORDER_DISPATCHED"      # ✓
"event": "ORDER_ARRIVED"         # ✓
"event": "DELIVERY_COMPLETED"    # ✓
```

### Issue: Events not being processed

**Check:**
1. Service running: `curl http://localhost:8080/health` (if available)
2. Logs: Look for "Webhook processing" messages
3. Port: Verify port 8080 is accessible
4. Payload: Ensure valid JSON

**Debug Logging:**
```yaml
logging:
  level:
    com.sijanstu: DEBUG
    org.springframework: DEBUG
```

### Issue: OutOfMemory or thread pool exhausted

**Solution:** Adjust `application.yaml`:
```yaml
spring.task.execution:
  pool:
    max-size: 20              # Increase from 5
    queue-capacity: 500       # Increase from 100
```

### Issue: Performance degradation

**Monitor:**
- Thread pool utilization: `curl http://localhost:8081/actuator/metrics`
- Queue depth
- Processing time

**Optimize:**
- Increase thread pool size
- Optimize event handler logic
- Add database indexing
- Use caching

## 📊 Best Practices

### 1. Webhook Security
```java
@PostMapping("/api/webhooks/ncm")
public void handleWebhook(
    @RequestBody NcmWebhookPayloadDto payload,
    @RequestHeader(name = "X-Signature", required = true) String signature) {
    
    // Verify signature
    if (!verifySignature(payload, signature)) {
        throw new SecurityException("Invalid signature");
    }
    
    webhookProcessingService.processWebhookAsync(payload);
}
```

### 2. Idempotency
```java
private void handlePickupCompleted(NcmOrderStatusChangedEvent event) {
    Order order = orderRepository.findByOrderId(event.getOrderId());
    
    // Only process if not already processed
    if (order.getStatus().equals("PICKED_UP")) {
        log.debug("Order already processed: {}", event.getOrderId());
        return;
    }
    
    order.setStatus("PICKED_UP");
    orderRepository.save(order);
}
```

### 3. Monitoring & Alerting
```java
meterRegistry.timer("ncm.event.processing.duration",
    "event_type", event.getEventType().toString())
    .record(() -> processOrderEvent(event));
```

### 4. Logging
```java
log.info("Order {} transitioned to {}", 
    event.getOrderId(), event.getEventType());
```

### 5. Error Recovery
```java
@EventListener
public void handleNcmOrderStatusChanged(NcmOrderStatusChangedEvent event) {
    try {
        processOrderEvent(event);
    } catch (Exception e) {
        log.error("Failed to process order {}: {}", 
            event.getOrderId(), e.getMessage(), e);
        // Store for retry/manual intervention
    }
}
```

## 📚 Project Structure

```
vendor-service/
├── src/main/
│   ├── java/com/sijanstu/vendor_service/
│   │   ├── VendorServiceApplication.java
│   │   └── ncm/
│   │       └── listener/
│   │           └── NcmOrderStatusListener.java
│   └── resources/
│       └── application.yaml
├── pom.xml
└── README.md
```

## 🔗 Related Services

- **NCM Listener Library** (`com.sijanstu.ncm_listener:ncm-listener`)
  - Provides webhook endpoint and event publishing
  - Integrated as library dependency

## 📝 License

MIT License - See LICENSE file

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
