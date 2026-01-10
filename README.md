# Purchase Service

A Spring Boot application for managing purchases and currency conversion.

## Technology Stack

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Validation
- Spring Data JPA (PostgreSQL)
- PostgreSQL
- Maven
- Lombok
- Jackson (JSON serialization)
- JUnit 5 & Mockito (testing)

## Architecture

- **Controller Layer:** Exposes REST API endpoints (`PurchaseController`)
- **Service Layer:** Business logic (`PurchaseService`)
- **DTOs:** Data transfer objects for requests and responses
- **Validation:** Custom and standard validation for input data
- **Persistence:** JPA entities and repositories (PostgreSQL)
- **Exception Handling:** Centralized with `RestExceptionHandler`

## Prerequisites

- Java 21
- Maven (or use the provided Maven Wrapper: `./mvnw` or `mvnw.cmd`)
- Docker & Docker Compose

## Building the Project with Docker

1. Build the application JAR:
   mvn clean package

2. Build and start the containers:
   docker compose up --build

The service will be available at http://localhost:8080

To stop and remove containers:
docker compose down


## PurchaseController API

## 1. Create Purchase

You can use Postman or curl to test the API.
Example: Create a Purchase
Request:

Method: POST
URL: http://localhost:8080/purchases
Body (JSON):
    {
    "amountUsd": 100.0,
    "transactionDate": "2025-01-25",
    "description": "Sample purchase"
    }

Response: Returns the ID of the created purchase.



## 2. Get Purchase with Currency Conversion
Endpoint: GET /purchases/{id}?currency={targetCurrency}
Path Variable: id - Purchase ID
Query Param: currency - Target currency code (e.g., EUR)


Request:
Method: GET
URL: http://localhost:8080/purchases/1?currency=EUR

Response: Returns purchase details with the amount converted to the requested currency.

   Response: Returns purchase details with the amount converted to the requested currency.
   {
   "amountUsd": 100.00,
   "convertedAmount": 96.10,
   "description": "Sample purchase",
   "exchangeRate": 0.961000,
   "id": 1,
   "targetCurrency": "EUR",
   "transactionDate": "2025-01-25"
   }

## Security Considerations

Security was intentionally left out of this service to keep the example focused and easy to run locally.
In a real production environment, the REST APIs should be secured. This would typically include:

- Authentication and authorization (for example, using Spring Security with JWT or OAuth2)
- Restricting access to endpoints based on user roles or permissions
- Enforcing HTTPS/TLS
- Proper CORS configuration and request validation
- Basic protections such as rate limiting and request filtering

These are standard practices for protecting RESTful APIs and would be added before deploying this service to production.