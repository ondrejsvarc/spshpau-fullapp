# Discovery Server (Eureka) with Config Server Integration

## Description

This service is a Spring Cloud Netflix Eureka Server, responsible for service registration and discovery within a microservices architecture. It is configured to fetch its operational settings from a Spring Cloud Config Server. Other microservices register with this Eureka server and can discover each other through it, facilitating dynamic scaling and resilience.

## Technologies Used

* **Backend**:
    * Java 17
    * Spring Boot (version as per `pom.xml`)
    * Spring Cloud Netflix Eureka Server
    * Spring Cloud Config Client
* **Build & Dependency Management**:
    * Apache Maven
* **Containerization**:
    * Docker

## Prerequisites

* **Java Development Kit (JDK)**: Version 17 or higher.
* **Apache Maven**: Version 3.6.x or higher.
* **Spring Cloud Config Server**: A running instance, configured to serve this Discovery Server's properties. (Default expected at `http://localhost:8888`)
* **Docker**: Required if you plan to build and run the service using Docker.

## Configuration

This Discovery Server is a Spring Cloud Eureka Server that expects to retrieve its configuration from a Spring Cloud Config Server. The `application.yml` (using `spring.config.import`) in your Discovery Server project should be configured to connect to the Config Server.

**Example `application.yml` for Config Server import:**
```yaml
spring:
  application:
    name: discovery-server # Must match the name used in Config Server properties
  config:
    import: "optional:configserver:http://localhost:8888" # Adjust if your Config Server is elsewhere