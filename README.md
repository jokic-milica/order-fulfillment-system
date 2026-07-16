# Order Fulfillment System

A small event-driven demo system composed of Java Spring Boot microservices that communicate through Apache Kafka. The current implementation focuses on reliability patterns such as idempotency, retry handling, and the outbox pattern.

---

## Tech Stack

| Component | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.1 |
| Message Broker | Apache Kafka |
| Persistence | MongoDB |
| Containerization | Docker + Docker Compose |
| API Documentation | Springdoc OpenAPI (Swagger UI) |
| Build Tool | Maven (multi-module) |

---

## Architecture

```text
Client
  │
  ▼
POST /orders
  │
  ▼
┌─────────────────┐        topic: orders         ┌──────────────────────┐
│   Order Service │ ──────────────────────────► │  Inventory Service   │
│   (port 8080)   │                              │   (port 8081)        │
│                 │        topic: order-results  │                      │
│                 │ ◄────────────────────────── │                      │
└─────────────────┘                              └──────────────────────┘
  │
  ▼
GET /orders/{orderId}/status
```

### Main Flow

1. The client sends a request to the Order Service.
2. The Order Service validates the payload and publishes an `OrderEvent` to the `orders` Kafka topic.
3. The Inventory Service consumes the event, evaluates stock availability, and produces an `OrderResult`.
4. The Inventory Service persists the result through an outbox mechanism and publishes it asynchronously.
5. The Order Service consumes the result and updates the order state for the client to query.

---

## Key Features

### Order Service

- Accepts `POST /orders` and publishes order events to Kafka.
- Exposes `GET /orders/{orderId}/status` for status lookup.
- Uses an idempotency guard to prevent duplicate order submission handling.
- Consumes `order-results` and updates the order outcome.

### Inventory Service

- Consumes `orders` from Kafka.
- Persists inventory state in MongoDB.
- Stores outbox entries in MongoDB before publishing results.
- Uses an outbox scheduling mechanism to publish pending results reliably.
- Detects duplicate events and reuses the existing result for the same order ID.
- Supports retry handling with exponential backoff and dead-letter handling.
- Publishes `OrderResult` with `PROCESSED`, `REJECTED`, or `FAILED` status.
- Exposes `GET /inventory` to inspect stock levels.

### Common Module

Shared library containing:
- `OrderEvent` - event published by the Order Service
- `OrderResult` - result emitted by the Inventory Service
- `OrderStatus` - enum (`PENDING`, `PROCESSED`, `REJECTED`, `FAILED`)
- `KafkaTopics` - topic constants
- Shared Kafka configuration

---

## Kafka Infrastructure

| Topic | Purpose |
|---|---|
| `orders` | Order events from Order Service |
| `order-results` | Processing results from Inventory Service |
| `orders-dlt` | Dead-letter topic for failed events |

- `orderId` is used as the partition key to preserve ordering per order.
- Producer idempotence is enabled (`enable.idempotence=true`, `acks=all`).
- JSON serialization/deserialization is handled through Spring Kafka.

---

## Reliability Patterns

The project demonstrates several distributed-systems design choices:

- **Outbox pattern**: the inventory service records an outbound event in MongoDB before publishing it to Kafka.
- **Idempotency**: duplicate events reuse the same processing result for an order instead of re-reserving inventory.
- **Manual acknowledgements**: Kafka consumption uses manual acknowledgement to avoid committing offsets too early.
- **Retry and DLT handling**: failures go through retry logic and can be routed to the dead-letter topic.

---

## How to Run

### Prerequisites

- Docker
- Docker Compose

### Start the system

```bash
docker-compose up --build
```

### Service URLs

| Service | URL |
|---|---|
| Order Service Swagger | http://localhost:8080/swagger-ui.html |
| Inventory Service Swagger | http://localhost:8081/swagger-ui.html |
| Order Service Health | http://localhost:8080/actuator/health |
| Inventory Service Health | http://localhost:8081/actuator/health |

### Stop the system

```bash
docker-compose down
```

---

## Testing

### Swagger UI

- Order Service: http://localhost:8080/swagger-ui.html
- Inventory Service: http://localhost:8081/swagger-ui.html

### Postman Collection

Import [postman/order-fulfillment-system.json](postman/order-fulfillment-system.json) into Postman.

### HTTP Test File

Open [http/api-tests.http](http/api-tests.http) in IntelliJ IDEA and run the requests individually.

---

## Initial Inventory

The system starts with the following stock levels:

| Item | Initial Stock |
|---|---|
| `item-1` | 100 |
| `item-2` | 50 |
| `item-3` | 0 |

---

## Notes

This demo is intentionally focused on the event-driven architecture and reliability patterns rather than full production-hardening. In a production environment, you would typically add stronger observability, authentication, and more durable state management beyond the current demo scope.
