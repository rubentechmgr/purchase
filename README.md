# Purchase Service

A Spring Boot application for managing purchases and currency conversion.

## Technology Stack

- Java 17+
- Spring Boot
- Spring Web MVC
- Spring Validation
- Spring Data JPA (H2 for local/dev)
- Maven
- Lombok
- Jackson (JSON serialization)
- JUnit 5 & Mockito (testing)

## Architecture

- **Controller Layer:** Exposes REST API endpoints (`PurchaseController`)
- **Service Layer:** Business logic (`PurchaseService`)
- **DTOs:** Data transfer objects for requests and responses
- **Validation:** Custom and standard validation for input data
- **Persistence:** JPA entities and repositories (H2)
- **Exception Handling:** Centralized with `RestExceptionHandler`

## Building the Project

To build the project, run:
mvn clean install

## Running Locally

To start the application locally:
mvn spring-boot:run

The service will be available at http://localhost:8080

## Running Tests

To execute all unit tests:
mvn test

## Building Docker Image

To build a Docker image for the application, run:
mvn spring-boot:build-image

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
