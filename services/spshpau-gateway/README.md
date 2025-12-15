# API Gateway (apigateway)

## Description

The API Gateway is a Spring Boot microservice designed to act as a single entry point for the SPSHPAU project ecosystem. It handles routing of requests to appropriate backend services, and integrates with a Spring Cloud Config Server for centralized configuration and a Eureka server for service discovery. It also includes Spring Boot Actuator for monitoring and management.

## Features

* **Request Routing**: Leverages Spring Cloud Gateway to route incoming API requests to downstream microservices.
* **Service Discovery**: Integrates with Netflix Eureka to dynamically discover and route to service instances.
* **Centralized Configuration**: Fetches its configuration from a Spring Cloud Config Server, allowing for dynamic configuration updates.
* **Monitoring and Management**: Includes Spring Boot Actuator for application health checks, metrics, and other operational insights.

## Technologies Used

* **Backend**:
    * Java 17
    * Spring Boot 3.4.5
    * Spring Cloud Gateway
    * Spring Cloud:
        * Config Client
        * Netflix Eureka Client
    * Spring Boot Actuator
* **Build & Dependency Management**:
    * Apache Maven
* **Testing**:
    * JUnit 5 (via `spring-boot-starter-test`)

## Prerequisites

Before running this service, ensure you have the following set up and running:

* **Java Development Kit (JDK)**: Version 17 or higher.
* **Apache Maven**: Version 3.6.x or higher.
* **Spring Cloud Config Server**: Running and configured to serve the `gateway` application's properties. (Default expected at `http://localhost:8888`)
* **Spring Cloud Netflix Eureka Server**: Running for service registration and discovery.

## Configuration

The service is configured primarily through `application.yml`, which is expected to be largely supplied by the Spring Cloud Config Server. The local `application.yml` specifies the application name and the Config Server import URI.

Key local configuration properties (`src/main/resources/application.yml`):

```yaml
spring:
  application:
    name: gateway
  config:
    import: optional:configserver:http://localhost:8888