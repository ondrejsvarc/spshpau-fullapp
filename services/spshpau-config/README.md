# SPSHPAU Configuration Server (spshpau-config)

## Description

The SPSHPAU Configuration Server is a Spring Boot microservice designed to provide centralized configuration management for various services within the SPSHPAU project ecosystem. It utilizes Spring Cloud Config Server to serve configurations from a native backend, which in this case are YAML files stored within the project's classpath. This server ensures that all dependent microservices (like `chatservice`, `discoveryserver`, `gateway`, `projectservice`, and `userservice`) have a consistent and managed source for their operational parameters.

## Features

* **Centralized Configuration**: Provides a single source of truth for configuration properties for all connected microservices.
* **Native Profile Support**: Serves configuration files directly from the classpath (`src/main/resources/configurations`), simplifying deployment and management for this project structure.
* **Environment-Agnostic**: Can serve different configurations for different profiles or environments if structured accordingly (though the current setup primarily uses a default set).
* **Spring Boot Actuator**: (Implicitly included with Spring Boot) Offers endpoints for monitoring and managing the application.

## Technologies Used

* **Backend**:
    * Java 17
    * Spring Boot 3.4.5
    * Spring Cloud Config Server
* **Build & Dependency Management**:
    * Apache Maven
* **Configuration File Format**:
    * YAML

## Prerequisites

Before running this service, ensure you have the following set up:

* **Java Development Kit (JDK)**: Version 17 or higher.
* **Apache Maven**: Version 3.6.x or higher (for building from source).

## Configuration

The service is configured primarily through its `application.yml` file located in `src/main/resources/application.yml`.

Key configuration properties include:

* **Application Name**:
    ```yaml
    spring:
      application:
        name: config-server
    ```
* **Active Profile & Native Configuration**:
  The server is set to use the `native` profile, meaning it will look for configuration files locally. The `search-locations` property specifies that these files are located in the `classpath:/configurations/` directory.
    ```yaml
    spring:
      profiles:
        active: native
      cloud:
        config:
          server:
            native:
              search-locations: classpath:/configurations
    ```
* **Server Port**:
  The server is configured to run on port `8888`.
    ```yaml
    server:
      port: 8888
    ```

## Configuration Files Hosted

This Configuration Server manages and serves configuration files for the following microservices. These files are located within the `src/main/resources/configurations/` directory:

* **chatservice.yml**: Configuration for the Chat Service.
* **discoveryserver.yml**: Configuration for the Eureka Discovery Server.
* **gateway.yml**: Configuration for the API Gateway.
* **projectservice.yml**: Configuration for the Project Service.
* **userservice.yml**: Configuration for the User Service.

Each of these files contains specific properties required by the respective microservice, such as database connections, security settings (OAuth2/JWT), server ports, and integration points with other services like Eureka or other clients.

## Building the Service

1.  **Clone the repository** (if applicable).
2.  **Navigate to the project root directory** (where `pom.xml` is located).
3.  **Build the project using Maven**:
    ```bash
    mvn clean package
    ```
    This will compile the code, run tests (unless skipped), and package the application into a JAR file located in the `target/` directory (e.g., `target/config-0.1.1-ALPHA.jar` based on the pom.xml).

## Running the Service

### Locally

1.  **Ensure Prerequisites are met**: JDK 17+ is installed.
2.  **Build the project** (as described above) or use a pre-built JAR.
3.  **Run the application using Java**:
    ```bash
    java -jar target/config-0.1.1-ALPHA.jar
    ```
    The service will start and be accessible, by default, at `http://localhost:8888`. Microservices configured to use this config server (e.g., by setting `spring.config.import=optional:configserver:http://localhost:8888`) will then fetch their respective configurations.

### With Docker

```bash
# Example Docker build command
docker build -t config:0.1.1-ALPHA .

# Example Docker run command
docker run -p 8888:8888 --name configserver-container config:0.1.1-ALPHA