# Hotel Booking Microservices System

A full-stack hotel booking application built using a microservices architecture. The system consists of independent Spring Boot services, an Angular frontend, Spring Cloud Gateway, PostgreSQL databases, Apache Kafka for asynchronous event-driven communication, and an optional AI assistant powered by Spring AI and Ollama.

## Architecture

### Services

| Service           | Responsibility                                          |
| ----------------- | ------------------------------------------------------- |
| API Gateway       | Single entry point for frontend requests                |
| User Service      | Authentication, authorization, and user management      |
| Booking Service   | Booking management and business rules                   |
| Audit Service     | Consumes audit events and stores audit logs             |
| Apache Kafka      | Event streaming platform for asynchronous communication |
| AI Chat Service   | AI assistant with access to system data                 |
| Frontend          | Angular web application                                 |
| PostgreSQL        | Persistent data storage                                 |
| Ollama (Optional) | Local LLM runtime for AI features                       |

---

## System Architecture

```text
                                  ┌─────────────────┐
                                  │     Browser     │
                                  └────────┬────────┘
                                           │
                                           ▼
                             ┌──────────────────────────┐
                             │ Angular Frontend (Nginx) │
                             │   http://localhost:3000  │
                             └────────────┬─────────────┘
                                          │
                                          ▼
                           ┌──────────────────────────────┐
                           │         API Gateway          │
                           │     Spring Cloud Gateway     │
                           └─┬────────────┬─────────────┬─┘
                             │            │             │
                             ▼            ▼             ▼
                ┌───────────────┐ ┌───────────────┐ ┌───────────────┐
                │ User Service  │ │Booking Service│ │AI Chat Service│
                └───────┬───┬───┘ └────┬───┬──────┘ └───────┬───────┘
                        │   │          │   │                │
                        ▼   │          │   ▼                ▼
               ┌──────────────┐   ┌────────────┐   ┌────────────────┐
               │userservice_db│   │ booking_db │   │  Ollama (LLM)  │
               └──────────────┘   └────────────┘   └────────────────┘
                            │         │
                            └────┬────┘
                                 ▼
                        ┌────────────────┐
                        │  Apache Kafka  │
                        │  audit-events  │
                        └───────┬────────┘
                                ▼
                        ┌────────────────┐
                        │ Audit Service  │
                        └───────┬────────┘
                                ▼
                        ┌────────────────┐
                        │    audit_db    │
                        └────────────────┘
```

### Request Flow

```text
Browser
   ↓
Frontend (Angular)
   ↓
API Gateway
   ├── User Service
   ├── Booking Service
   └── AI Chat Service
```

Client requests are routed through the API Gateway using REST APIs.

Internal service-to-service communication uses synchronous REST APIs via Spring Cloud OpenFeign when an immediate response is required. Business events (such as user registration, user updates, and booking creation) are published asynchronously to Apache Kafka, where the Audit Service consumes them and persists audit logs.


---

## Service Responsibilities

### User Service

* JWT authentication
* User management
* Role-based access control
* Admin and User roles

### Booking Service

* Room management
* Booking creation
* Booking cancellation
* Availability checks
* Booking validation rules

### Audit Service

* Consumes audit events from Apache Kafka
* Stores immutable audit logs
* Tracks important business events
* Idempotent event processing
* Passive service without business logic

Examples:

* User registered
* User updated
* User deleted
* Booking created
* Booking cancelled

### AI Chat Service

* Answers questions about users, rooms, and bookings
* Aggregates information from the User Service, Booking Service, and Audit Service
* Read-only access to business data
* Optional feature

---

## Architecture Principles

* Separation of concerns
* Independent services
* Clear service boundaries
* API Gateway pattern
* REST for synchronous client requests
* Event-driven communication using Apache Kafka
* AI service is read-only
* Audit service is passive

---

## Frontend

Built with Angular and Angular Material.

### Features

* User authentication
* Room browsing
* Booking management
* Admin dashboard
* User administration
* AI chat interface (optional)
* Route guards
* Role-based UI
* JWT authentication

---

## Backend Features

### Authentication

* JWT-based authentication
* Stateless security
* Role-based authorization

### Booking Rules

* No bookings in the past
* Check-out date must be after check-in date
* Maximum booking period validation
* Availability checking

### Audit Logging

* Booking events
* User actions
* System activity tracking

### Event Streaming

* Apache Kafka messaging
* Event-driven architecture
* Asynchronous audit processing
* Kafka producers and consumers
* Consumer groups
* Idempotent event handling

---

## Database Design

The system uses a single PostgreSQL container with multiple databases:

* userservice_db
* booking_db
* audit_db

Each service owns its own database and is responsible for its own data.

---

## Technology Stack

### Frontend

* Angular
* Angular Material
* TypeScript

### Backend

* Spring Boot
* Spring Security
* Spring Data JPA
* Spring Cloud Gateway
* Spring Cloud OpenFeign
* Spring for Apache Kafka
* JWT Authentication
* Spring AI

### Database

* PostgreSQL

### Messaging

* Apache Kafka

### AI

* Ollama
* Qwen3

### DevOps

* Docker
* Docker Compose
* Maven
* GitHub

---

## Getting Started

### Build Backend Services

Run the following command inside each Spring Boot service directory:

```bash
./mvnw clean package
```

Services:

* user-service
* booking-service
* audit-service
* ai-chat-service
* api-gateway

### Start the Application
#### Without AI

```bash
docker compose up --build
```
This starts:

* PostgreSQL
* Apache Kafka
* Kafka UI
* User Service
* Booking Service
* Audit Service
* API Gateway
* Frontend

#### With AI

```bash
docker compose --profile ai up --build
```

Starts everything above plus:

* Ollama
* AI Chat Service

The AI model is downloaded automatically during startup.

---

## Application URLs

| Component   | URL                   |
| ----------- | --------------------- |
| Frontend    | http://localhost:3000 |
| API Gateway | http://localhost:8080 |
| Kafka UI    | http://localhost:8085 |

---

## Default Credentials

### Administrator

Username:

```text
admin
```

Password:

```text
admin123
```

### Sample Users

All users loaded from data.sql:

Password:

```text
password
```

---

## Key Concepts Demonstrated

* Microservices architecture
* API Gateway pattern (Spring Cloud Gateway)
* Database-per-service design
* RESTful APIs
* Synchronous service-to-service communication with Spring Cloud OpenFeign
* Event-driven architecture with Apache Kafka
* Asynchronous messaging
* Kafka producers and consumers
* Consumer groups
* Idempotent event processing
* JWT authentication and authorization
* Role-based access control (RBAC)
* Docker containerization
* Optional AI integration (Spring AI + Ollama)


