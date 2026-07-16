# Spring Boot Payment Action Service

A containerized, production-ready Spring Boot microservice for payment actions (Initiation, Processing, and Refunds) backed by MongoDB. Designed for seamless local development with Docker Compose and automated CI/CD using GitHub Actions.

---

## Technical Stack
* **Java 21** (OpenJDK Temurin)
* **Spring Boot 3.3.1** (Web, Validation, Data MongoDB)
* **Lombok**
* **MongoDB 6.0**
* **Docker & Docker Compose**
* **GitHub Actions** (CI/CD Pipeline pushing to GHCR)

---

## Getting Started

### Prerequisites
* **WSL Ubuntu** or Linux environment
* **Docker** & **Docker Compose** installed
* **Maven** (optional, for running local non-containerized builds)

### 1. Run via Docker Compose
To build and start both the Spring Boot service and MongoDB:
```bash
docker compose up --build -d
```
Check running container status:
```bash
docker compose ps
```

---

## REST API Endpoints

### 1. Initiate Payment
* **URL**: `POST /api/payments`
* **Content-Type**: `application/json`
* **Request Body**:
```json
{
  "amount": 150.50,
  "currency": "USD",
  "paymentMethod": "CREDIT_CARD",
  "customerEmail": "buyer@example.com"
}
```
* **Response (Status 201 Created)**:
```json
{
  "transactionId": "PAY-8A7B3C4D",
  "amount": 150.50,
  "currency": "USD",
  "status": "PENDING",
  "paymentMethod": "CREDIT_CARD",
  "customerEmail": "buyer@example.com",
  "createdAt": "2026-07-16T11:20:00Z",
  "updatedAt": "2026-07-16T11:20:00Z"
}
```

### 2. Process Payment (Mock Gateway)
Processes the payment and updates the status to `SUCCESS` (or `FAILED`).
* *Note: Passing an amount of exactly `99.99` simulates a deterministic payment failure. Any other amount will succeed.*
* **URL**: `POST /api/payments/{transactionId}/process`
* **Response (Status 200 OK)**:
```json
{
  "transactionId": "PAY-8A7B3C4D",
  "amount": 150.50,
  "currency": "USD",
  "status": "SUCCESS",
  "paymentMethod": "CREDIT_CARD",
  "customerEmail": "buyer@example.com",
  "createdAt": "2026-07-16T11:20:00Z",
  "updatedAt": "2026-07-16T11:22:00Z"
}
```

### 3. Refund Payment
Refunds a successful payment transaction.
* **URL**: `POST /api/payments/{transactionId}/refund`
* **Response (Status 200 OK)**:
```json
{
  "transactionId": "PAY-8A7B3C4D",
  "amount": 150.50,
  "currency": "USD",
  "status": "REFUNDED",
  "paymentMethod": "CREDIT_CARD",
  "customerEmail": "buyer@example.com",
  "createdAt": "2026-07-16T11:20:00Z",
  "updatedAt": "2026-07-16T11:23:00Z"
}
```

### 4. Fetch Payment Details
* **URL**: `GET /api/payments/{transactionId}`
* **Response (Status 200 OK)**:
```json
{
  "transactionId": "PAY-8A7B3C4D",
  "amount": 150.50,
  "currency": "USD",
  "status": "SUCCESS",
  "paymentMethod": "CREDIT_CARD",
  "customerEmail": "buyer@example.com",
  "createdAt": "2026-07-16T11:20:00Z",
  "updatedAt": "2026-07-16T11:22:00Z"
}
```

---

## Running Unit Tests
To run Java unit tests locally using Maven:
```bash
mvn clean test
```

---

## CI/CD Pipeline (GitHub Actions)
The workflow file is located in `.github/workflows/maven.yml`. On every push to the `main` branch, the pipeline will:
1. Spin up a JDK 21 runner.
2. Build the project using Maven and run unit tests.
3. Build the Docker container.
4. Log into the GitHub Container Registry (GHCR) and push the tagged image to `ghcr.io/<your-username>/payment-action`.

---

## Clean Up (Docker Compose Teardown)
To stop the services and remove the volumes:
```bash
docker compose down -v
```
