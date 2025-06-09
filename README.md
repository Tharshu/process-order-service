# coffee-shop-order-service

## Overview
This project is a technical assessment for building a coffee shop order service. It provides RESTful APIs to manage orders, including creating, updating, and retrieving orders.

## Features
- Create new coffee orders
- Update existing orders
- Retrieve order details
- Swagger UI for API documentation and testing

## Technology Stack
- Java / Spring Boot
- REST API
- Swagger for API documentation

## Getting Started

### Prerequisites
- Java 17
- Maven
- Docker

### Running the Application
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd process-order-service

2. Build the project:
   ```bash
   mvn clean install -DskipTests

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

4. Access the Swagger UI:
   Open your web browser and navigate to URL_ADDRESS:8383/order-service/swagger-ui/index.html/
   e.g. http://localhost:8383/order-service/swagger-ui/index.html/

## API Documentation
The API documentation is available at http://localhost:8383/order-service/swagger-ui/index.html/

## Testing
The project includes unit tests.



