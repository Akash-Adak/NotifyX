# NotifyX — Event-Driven Notification Service

> A scalable, fault-tolerant notification system built with **Spring Boot**, **Apache Kafka**, and **PostgreSQL**.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Project Structure](#project-structure)
- [Kafka Flow](#kafka-flow)
- [API Reference](#api-reference)
- [Getting Started](#getting-started)
- [Sample Event](#sample-event)
- [Troubleshooting](#troubleshooting)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [Author](#author)

---

## Overview

NotifyX is a production-ready, event-driven notification microservice. It consumes notification events from Kafka topics, processes them asynchronously, and persists them reliably — with built-in retry logic and Dead Letter Queue (DLQ) support for maximum resilience.

Built for high-throughput systems where **scalability**, **fault tolerance**, and **clean architecture** are non-negotiable.

---

## Tech Stack

| Layer       | Technology                     |
|-------------|-------------------------------|
| Backend     | Spring Boot 3                 |
| Messaging   | Apache Kafka                  |
| Database    | PostgreSQL                    |
| ORM         | Spring Data JPA (Hibernate)   |
| Build Tool  | Maven                         |
| Java        | 17+                           |

---

## Features

- **Real-time event consumption** via Kafka listeners
- **Automatic retry mechanism** for transient failures
- **Dead Letter Queue (DLQ)** to capture and isolate unprocessable messages
- **Persistent storage** with PostgreSQL
- **Pagination & filtering** on notification queries
- **Microservice-ready** clean layered architecture

---

## Project Structure

```
notification-system/
│
├── controller/          # REST API endpoints
├── service/             # Business logic layer
├── repository/          # Spring Data JPA repositories
├── model/               # JPA entity classes
├── consumer/            # Kafka consumer listeners
├── config/              # Kafka & application configuration
└── resources/
    └── application.yml  # App configuration
```

---

## Kafka Flow

```
Producer
   │
   ▼
Kafka Topic
   │
   ▼
Consumer ──► Service ──► PostgreSQL
   │
   ▼ (on failure)
Retry Topics
   │
   ▼ (exhausted retries)
Dead Letter Queue (DLQ)
```

---

## API Reference

### Get All Notifications

```http
GET /api/notifications
```

Returns a paginated list of all notifications.

---

### Get Notifications by User

```http
GET /api/notifications/user/{userId}
```

| Parameter | Type     | Description                  |
|-----------|----------|------------------------------|
| `userId`  | `string` | **Required.** Target user ID |

---

## Getting Started

### Prerequisites

Ensure the following services are running before starting the application:

- Apache Kafka
- Zookeeper
- PostgreSQL

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/notifyx.git
cd notifyx
```

### 2. Configure the Application

Edit `src/main/resources/application.yml` with your database and Kafka credentials.

> ⚠️ **Important:** Use `Asia/Kolkata` as the timezone — `Asia/Calcutta` is deprecated and may cause connection errors.

### 3. Build the Project

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

The service will start on `http://localhost:8080` by default.

---

## Sample Event

Publish this payload to your configured Kafka topic to trigger a notification:

```json
{
  "userId": "user1",
  "type": "INFO",
  "message": "User liked your post"
}
```

---

## Troubleshooting

### Database Connection Error

**Symptom:** Application fails to connect to PostgreSQL on startup.

**Fix:** Verify the timezone setting in `application.yml`:

```yaml
# ✅ Correct
spring.datasource.url: jdbc:postgresql://localhost:5432/notifyxdb?serverTimezone=Asia/Kolkata

# ❌ Deprecated — will cause errors
spring.datasource.url: jdbc:postgresql://localhost:5432/notifyxdb?serverTimezone=Asia/Calcutta
```

---

### Kafka Not Consuming Messages

**Symptom:** Events published to Kafka are not being processed.

**Checklist:**
- Confirm Kafka is running on `localhost:9092`
- Verify the topic name matches the configuration in `application.yml`
- Check consumer group offsets using Kafka CLI tools
- Review application logs for `KafkaListenerContainerFactory` errors

---


## Contributing

Contributions are welcome! Here's how to get started:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m 'Add your feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

Please make sure your code follows the existing style and includes relevant tests.

---

## Author

**Akash Adak**
Backend & DevOps Enthusiast

---

<p align="center">If you found this project useful, consider giving it a ⭐ on GitHub!</p>