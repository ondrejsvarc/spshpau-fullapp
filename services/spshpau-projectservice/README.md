# Project Service (projectservice)

## Description

The Project Service is a Spring Boot microservice designed to manage projects, tasks, milestones, budgets, and files. It is part of the larger SPSHPAU project ecosystem and integrates with other services like a User Service, Chat Service, Config Server, and Eureka for service discovery. The service is secured using OAuth2/JWT.

## Features

* **Project Management**:
    * Create, retrieve, update, and delete projects.
    * List projects owned by a user or where a user is a collaborator.
    * Manage project collaborators (add/remove users who are connections of the project owner).
* **Task Management**:
    * Create, retrieve, update, and delete tasks within a specific project.
    * Assign and unassign users (who are project members) to tasks.
    * Paginated listing of tasks for a project.
* **Milestone Management**:
    * Create, retrieve, update, and delete milestones for a project.
    * Paginated listing of milestones.
* **Budget Management**:
    * Define and manage a budget for each project (total amount, currency).
    * Track expenses against the project budget.
    * View remaining budget.
* **File Management**:
    * Upload files (MP3, WAV, PDF up to 50MB) associated with a project to AWS S3.
    * Download project files via pre-signed S3 URLs.
    * List project files (latest versions).
    * View all versions of a specific file.
    * Delete project files (removes from S3 and database).
* **User Handling**:
    * Maintains a local, simplified representation of users (`SimpleUser`) involved in projects.
    * Interacts with an external User Service via Feign client (`UserClient`) to fetch user connection details for adding collaborators.
* **Security**:
    * Endpoints are secured using OAuth2 and JWT Bearer tokens.
    * Role-based access control can be inferred from the JWT.
* **Microservice Architecture**:
    * Registers with Eureka for service discovery.
    * Pulls configuration from a Spring Cloud Config Server.

## Technologies Used

* **Backend**:
    * Java 17
    * Spring Boot 3.4.5 (or as per your `pom.xml`)
    * Spring MVC (for REST APIs)
    * Spring Data JPA (with Hibernate)
    * Spring Security (OAuth2 Resource Server, JWT)
    * Spring Cloud:
        * Config Client
        * Netflix Eureka Client
        * OpenFeign
    * PostgreSQL (Database)
    * AWS SDK for Java v2 (for S3 integration)
* **Build & Dependency Management**:
    * Apache Maven
* **Utilities**:
    * Lombok
* **Testing**:
    * JUnit 5
    * Mockito
* **Containerization**:
    * Docker

## Prerequisites

Before running this service, ensure you have the following set up and running:

* **Java Development Kit (JDK)**: Version 17 or higher.
* **Apache Maven**: Version 3.6.x or higher.
* **PostgreSQL Database**: An accessible instance with a database created for this service.
* **Spring Cloud Config Server**: Running and configured to serve the `projectservice` application's properties. (Default expected at `http://localhost:8888`)
* **Spring Cloud Netflix Eureka Server**: Running for service registration and discovery.
* **OAuth2 Identity Provider (IdP)**: Such as Keycloak, configured to issue JWTs. The service needs to be configured with the IdP's issuer URI.
* **User Service**: The dependent `userservice` (as defined in `UserClient`) must be running and accessible for operations like adding collaborators.
* **AWS S3 Bucket**: A configured S3 bucket with appropriate permissions if you intend to use the file upload/download features.
* **Docker**: (Optional) If you plan to run the service using Docker.

## Configuration

The service is configured primarily through `application.yml` (or `application.properties`) which is expected to be largely supplied by the Spring Cloud Config Server.

Key configuration properties include:

* **Application Name**:
    ```yaml
    spring:
      application:
        name: projectservice
    ```
* **Config Server Import**:
    ```yaml
    spring:
      config:
        import: optional:configserver:http://localhost:8888
    ```
* **Database Connection**: (Typically provided by Config Server)
    ```yaml
    # Example - actual values from Config Server
    spring:
      datasource:
        url: jdbc:postgresql://localhost:5432/projectdb
        username: user
        password: password
        driver-class-name: org.postgresql.Driver
      jpa:
        hibernate:
          ddl-auto: update # or validate, none for production
        show-sql: true
    ```
* **Eureka Client**: (Typically provided by Config Server)
    ```yaml
    # Example - actual values from Config Server
    eureka:
      client:
        serviceUrl:
          defaultZone: http://localhost:8761/eureka/
      instance:
        preferIpAddress: true
    ```
* **OAuth2 Resource Server (Keycloak example)**: (Typically provided by Config Server)
    ```yaml
    # Example - actual values from Config Server
    spring:
      security:
        oauth2:
          resourceserver:
            jwt:
              issuer-uri: http://localhost:8080/realms/your-realm
              # jwk-set-uri: ${spring.security.oauth2.resourceserver.jwt.issuer-uri}/protocol/openid-connect/certs
    ```
* **JWT Authentication Converter**: (Typically provided by Config Server)
    ```yaml
    # Example - actual values from Config Server
    jwt:
      auth:
        converter:
          resource-id: your-client-id # Client ID in Keycloak
          principle-attribute: sub # or preferred_username
    ```
* **AWS S3 Configuration**: (Typically provided by Config Server, environment variables, or IAM roles)
    ```yaml
    # Example - actual values from Config Server or environment
    aws:
      region: your-aws-region
      credentials: # Prefer IAM roles or environment variables for production
        access-key-id: YOUR_AWS_ACCESS_KEY_ID
        secret-access-key: YOUR_AWS_SECRET_ACCESS_KEY
      s3:
        bucket-name: your-s3-bucket-name-for-project-files
        presigned-url-duration-minutes: 15
    ```
* **UserClient URL Configuration**: (Typically provided by Config Server)
    ```yaml
    # Example - actual value from Config Server
    application:
      config:
        userclienturl: http://localhost:8090 # Assuming userservice runs on 8090
    ```

Ensure your Config Server is properly set up with a configuration file for `projectservice` (e.g., `projectservice.yml` or `projectservice-default.yml`).

## Building the Service

1.  **Clone the repository** (if applicable).
2.  **Navigate to the project root directory** (where `pom.xml` is located).
3.  **Build the project using Maven**:
    ```bash
    mvn clean package
    ```
    This will compile the code, run tests (unless skipped), and package the application into a JAR file located in the `target/` directory (e.g., `target/projectservice-0.0.1-SNAPSHOT.jar`).

## Running the Service

### Locally

1.  **Ensure all prerequisites are met and running** (Database, Config Server, Eureka, IdP, User Service, S3 setup).
2.  **Run the application**:
    ```bash
    java -jar target/projectservice-0.1.1-ALPHA.jar
    ```
    You might need to provide Spring Boot properties via command-line arguments or environment variables if they are not fully managed by the Config Server for your local setup (e.g., `-Dspring.profiles.active=local`).

### Using Docker

1.  **Ensure Docker is installed and running.**
2.  **Build the Docker image** (from the project root directory where the `Dockerfile` is located):
    ```bash
    docker build -t projectservice:latest .
    ```
    (Replace `projectservice:latest` with your desired image name and tag).
3.  **Run the Docker container**:
    ```bash
    docker run -p 8092:8092 --name projectservice-container projectservice:latest
    ```
    * `-p 8092:8092`: Maps port 8092 on your host to port 8092 in the container (as specified by `EXPOSE 8092` in the Dockerfile and assuming your Spring Boot app runs on this port, which is often configured via `server.port` property).
    * You might need to pass environment variables for configuration if not using a Config Server accessible from within Docker, or use Docker networking to connect to other services. For example:
        ```bash
        docker run -p 8092:8092 \
          -e SPRING_CONFIG_IMPORT="optional:configserver:http://your-config-server-host:8888" \
          -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE="http://your-eureka-host:8761/eureka/" \
          # Add other necessary environment variables for DB, S3, OAuth etc.
          --name projectservice-container projectservice:latest
        ```

The service should be accessible at `http://localhost:8092` (or the configured port).

## API Endpoints Overview

All API endpoints require a valid JWT Bearer token in the `Authorization` header.

* **Utility Endpoints**:
    * `GET /api/v1/util/ping`: Checks service availability.
    * `GET /api/v1/util/auth`: (Requires Auth) Checks token validity.
* **Project Endpoints**: `BASE_URL: /api/v1/projects`
    * `POST /`: Create a new project.
    * `GET /{projectId}`: Get project details.
    * `GET /owned`: Get projects owned by the authenticated user.
    * `GET /collaborating`: Get projects where the authenticated user is a collaborator.
    * `GET /{projectId}/owner`: Get the owner of a project.
    * `GET /{projectId}/collaborators`: Get collaborators of a project.
    * `PUT /{projectId}`: Update project information.
    * `DELETE /{projectId}`: Delete a project.
    * `POST /{projectId}/collaborators/{collaboratorId}`: Add a collaborator.
    * `DELETE /{projectId}/collaborators/{collaboratorId}`: Remove a collaborator.
* **Project Task Endpoints**: `BASE_URL: /api/v1/projects/{projectId}/tasks`
    * `POST /`: Create a new task for the project.
    * `GET /{taskId}`: Get task details.
    * `GET /`: Get all tasks for the project (paginated).
    * `PUT /{taskId}`: Update a task.
    * `DELETE /{taskId}`: Delete a task.
    * `POST /{taskId}/assign/{assigneeUserId}`: Assign a user to a task.
    * `DELETE /{taskId}/unassign`: Remove a user from a task.
* **Project Milestone Endpoints**: `BASE_URL: /api/v1/projects/{projectId}/milestones`
    * `POST /`: Create a new milestone.
    * `GET /{milestoneId}`: Get milestone details.
    * `GET /`: Get all milestones for the project (paginated).
    * `PUT /{milestoneId}`: Update a milestone.
    * `DELETE /{milestoneId}`: Delete a milestone.
* **Project Budget & Expense Endpoints**: `BASE_URL: /api/v1/projects/{projectId}/budget`
    * `POST /`: Create the project budget.
    * `GET /`: Get project budget details.
    * `PUT /`: Update project budget.
    * `DELETE /`: Delete project budget.
    * `GET /remaining`: Get remaining budget amount.
    * `POST /expenses`: Add an expense to the budget.
    * `GET /expenses/{expenseId}`: Get specific expense details.
    * `GET /expenses`: Get all expenses for the budget (paginated).
    * `PUT /expenses/{expenseId}`: Update an expense.
    * `DELETE /expenses/{expenseId}`: Remove an expense.
* **Project File Endpoints**: `BASE_URL: /api/v1/projects/{projectId}/files`
    * `POST /`: Upload a project file (multipart/form-data).
    * `GET /`: List latest versions of all files for the project.
    * `GET /{fileId}/metadata`: Get metadata for a specific file.
    * `GET /{fileId}/download-url`: Get a pre-signed S3 download URL for a file.
    * `DELETE /{fileId}`: Delete a file (and its S3 version).
    * `GET /versions?filename={originalFilename}`: List all versions of a file by its original name.

(For detailed request/response formats, refer to the DTOs and controller implementations or API documentation if available e.g., Swagger/OpenAPI.)

## Security

The service is secured using Spring Security with OAuth2 Resource Server capabilities.
* All API endpoints (except `/api/v1/util/ping`) require a valid JWT Bearer token.
* The JWT is validated against the configured issuer URI.
* User information (like subject/ID and roles) is extracted from the JWT for authorization purposes.
* The `JwtAuthConverter` class is used to extract custom roles from the JWT based on a configured resource ID (client ID).

## Testing

To run the unit tests for the service:
```bash
mvn test