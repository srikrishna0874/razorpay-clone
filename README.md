# Razorpay Spring Boot Application

A Spring Boot application that models a payment service platform with merchant onboarding, API key management, order creation, and payment initiation.

## Table of Contents

- [Project Overview](#project-overview)
- [Tech Stack](#tech-stack)
- [Modules](#modules)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Available APIs](#available-apis)
- [Sample Request Bodies](#sample-request-bodies)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Testing](#testing)
- [Notes](#notes)
- [License](#license)

## Project Overview

This repository contains a Java Spring Boot application designed around a simplified payment platform. It includes:

- Merchant signup and onboarding
- Merchant API key creation and management
- Order creation and lifecycle tracking
- Payment initialization and result tracking
- PostgreSQL-backed persistence with JPA
- OpenAPI/Swagger documentation for REST endpoints

## Tech Stack

- Java 25
- Spring Boot 4.0.6
- Spring Data JPA
- Spring Web / Spring MVC
- Springdoc OpenAPI UI
- PostgreSQL
- MapStruct
- Lombok

## Modules

- `common` - shared entities, enums, exceptions, validation, and utility code
- `merchant` - merchant registration, API key lifecycle, authentication interfaces
- `payment` - order creation, payment processing, DTOs, controllers, services
- `vault` - secure storage entities and vault-related persistence
- `operations` - operational domain entities and workflows

## Getting Started

### Prerequisites

- Java 25 installed
- Maven 3.x installed
- PostgreSQL running

### Build

From the project root:

```bash
./mvnw clean package
```

On Windows:

```powershell
.\mvnw.cmd clean package
```

### Run

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

## Configuration

Application configuration is located in `src/main/resources/application.yaml`.

Default database configuration values:

- `DB_URL=jdbc:postgresql://localhost:5432/razorpaydb`
- `DB_USER=postgres`
- `DB_PASSWORD=root`

The application uses the following JPA properties:

- `spring.jpa.hibernate.ddl-auto: update`
- `spring.jpa.show-sql: true`
- `hibernate.dialect: org.hibernate.dialect.PostgreSQLDialect`

### Environment Variables

Set environment variables to override the defaults:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/razorpaydb
export DB_USER=postgres
export DB_PASSWORD=root
```

On Windows PowerShell:

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/razorpaydb'
$env:DB_USER = 'postgres'
$env:DB_PASSWORD = 'root'
```

## Available APIs

### Merchant Authentication

- `POST /v1/auth/signup`
  - Creates a new merchant account.
  - Request body: `MerchantSignupRequest`
  - Response: `MerchantResponse`

### API Key Management

- `POST /v1/merchants/{merchantId}/api-keys`
  - Create a new API key for a merchant.
  - Request body: `CreateApiKeyRequest`
  - Response: `ApiKeyCreateResponse`

- `GET /v1/merchants/{merchantId}/api-keys`
  - List all API keys for a merchant.
  - Response: `List<ApiKeyResponse>`

- `DELETE /v1/merchants/{merchantId}/api-keys/{keyId}`
  - Revoke an API key.
  - Response: `204 No Content`

- `POST /v1/merchants/{merchantId}/api-keys/{keyId}/rotate`
  - Rotate an existing API key and return a new secret.
  - Response: `ApiKeyCreateResponse`

### Order Management

- `POST /v1/orders`
  - Create a new order for payment processing.
  - Request body: `CreateOrderRequest`
  - Response: `OrderResponse`

### Payment Processing

- `POST /v1/payments`
  - Initiate a payment against an order.
  - Request body: `PaymentInitRequestDto`
  - Response: `PaymentResponse`

## Sample Request Bodies

### Merchant Signup

```json
{
  "name": "Acme Corp",
  "email": "merchant@example.com",
  "password": "password123",
  "businessName": "Acme Payments",
  "businessType": "RETAIL"
}
```

### Create API Key

```json
{
  "environment": "PRODUCTION"
}
```

### Create Order

```json
{
  "money": {
    "amount": 1000,
    "currency": "INR"
  },
  "receipt": "order_12345",
  "notes": {
    "customer_phone": "+911234567890"
  },
  "expiresAt": "2026-07-30T12:00:00"
}
```

### Initiate Payment

```json
{
  "orderId": "00000000-0000-0000-0000-000000000000",
  "paymentMethod": "CARD",
  "methodDetails": {
    "card_number": "4111111111111111",
    "expiry_month": "12",
    "expiry_year": "2030"
  }
}
```

## API Documentation

When the application is running, access the Swagger UI at:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/swagger-ui/index.html`

## Project Structure

- `src/main/java/com/springboot/razorpay`
  - `common` - shared entities, enums, exceptions, utilities
  - `merchant` - controllers, DTOs, entities, mappers, repositories, services
  - `payment` - controllers, DTOs, entities, mappers, repositories, services
  - `vault` - vault-related entities and persistence
  - `operations` - operational data and workflows
- `src/main/resources/application.yaml` - application configuration

## Testing

Run tests with:

```bash
./mvnw test
```

On Windows:

```powershell
.\mvnw.cmd test
```

## Notes

- The application currently uses a hard-coded merchant ID in the `OrderController` and `PaymentController` as a temporary placeholder.
- Lombok is configured as an optional dependency and is excluded from the packaged artifact.
- MapStruct is enabled through annotation processing in Maven.
- JPA schema management is configured with `hibernate.ddl-auto: update`.

## License

Add a license section if needed.
