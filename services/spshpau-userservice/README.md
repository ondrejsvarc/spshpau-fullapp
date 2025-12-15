# User Service (userservice)

## Description

The User Service is a Spring Boot microservice designed to manage users, their profiles (artist and producer), skills, genres, and interactions within the SPSHPAU application ecosystem. It handles user data synchronization from an identity provider (like Keycloak), profile management, user connections, blocking, and user matching functionalities. This service integrates with a Config Server for centralized configuration and Eureka for service discovery, and is secured using OAuth2/JWT.

## Features

* **User Management**:
    * Synchronize user data (ID, username, email, names) from an OAuth2/JWT token (e.g., Keycloak).
    * Retrieve detailed user information by ID or username.
    * Update user location.
    * Activate or deactivate user accounts (admin functionality).
    * Search for active users based on various criteria (name, genres, skills, profile types).
    * Find matching users based on profile compatibility, genre overlap, and availability.
* **Profile Management**:
    * **Artist Profiles**: Create, retrieve, update (full and partial) artist profiles including bio, availability, experience level, associated genres, and skills.
    * **Producer Profiles**: Create, retrieve, update (full and partial) producer profiles including bio, availability, experience level, and associated genres.
* **Genre Management**:
    * Create, retrieve (paginated), and delete musical genres (admin functionality for creation/deletion).
    * Link genres to artist and producer profiles.
* **Skill Management**:
    * Create, retrieve (paginated), and delete skills (admin functionality for creation/deletion).
    * Link skills to artist profiles.
* **User Interactions**:
    * **Connections**: Send, accept, reject, and remove connection requests between users. List current connections, pending incoming, and pending outgoing requests.
    * **Blocking**: Allow users to block and unblock other users. List blocked users.
    * Check interaction status between two users (e.g., connected, blocked, pending).
* **Caching**:
    * User match results are cached using Caffeine to improve performance.
* **Security**:
    * Endpoints are secured using OAuth2 and JWT Bearer tokens.
    * Role-based access control derived from JWT claims via `JwtAuthConverter`.
* **Microservice Architecture**:
    * Registers with Eureka for service discovery.
    * Pulls configuration from a Spring Cloud Config Server.

## Technologies Used

* **Backend**:
    * Java 17
    * Spring Boot 3.4.5 (Parent POM Version)
    * Spring MVC (for REST APIs)
    * Spring Data JPA (with Hibernate)
    * Spring Security (OAuth2 Resource Server, JWT)
    * Spring Cache (with Caffeine)
    * Spring Cloud:
        * Config Client
        * Netflix Eureka Client
    * PostgreSQL (Database)
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
* **Spring Cloud Config Server**: Running and configured to serve the `userservice` application's properties. (Default expected at `http://localhost:8888`)
* **Spring Cloud Netflix Eureka Server**: Running for service registration and discovery.
* **OAuth2 Identity Provider (IdP)**: Such as Keycloak, configured to issue JWTs. The service needs to be configured with the IdP's issuer URI.
* **Docker**: (Optional) If you plan to run the service using Docker.

## Configuration

The service is configured primarily through `application.yml` (or `application.properties`) which is expected to be largely supplied by the Spring Cloud Config Server.

Key configuration properties include:

* **Application Name**:
    ```yaml
    spring:
      application:
        name: userservice
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
        url: jdbc:postgresql://localhost:5432/your_userservice_db
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
        preferIpAddress: true # Or configure hostname
    ```
* **OAuth2 Resource Server (Keycloak example)**: (Typically provided by Config Server)
    ```yaml
    # Example - actual values from Config Server
    spring:
      security:
        oauth2:
          resourceserver:
            jwt:
              issuer-uri: http://localhost:8080/realms/your-realm # Replace with your IdP's issuer URI
              # jwk-set-uri: ${spring.security.oauth2.resourceserver.jwt.issuer-uri}/protocol/openid-connect/certs (often derived)
    ```
* **JWT Authentication Converter**: (Typically provided by Config Server, configured via application properties in `JwtAuthConverter.java`)
    ```yaml
    # Example - actual values from Config Server for JwtAuthConverter.java
    jwt:
      auth:
        converter:
          resource-id: your-client-id # Client ID in Keycloak that has the roles
          principle-attribute: sub # or preferred_username, based on your IdP token structure
    ```
* **Cache Configuration**: (Defined in `CacheConfig.java`)
    * Cache name for user matches: `userMatches`
    * Uses Caffeine, expires after 4 hours, max size 500.

Ensure your Config Server is properly set up with a configuration file for `userservice` (e.g., `userservice.yml` or `userservice-default.yml`).

## Building the Service

1.  **Clone the repository** (if applicable).
2.  **Navigate to the project root directory** (`spshpau-userservice`, where its `pom.xml` is located).
3.  **Build the project using Maven**:
    ```bash
    mvn clean package
    ```
    This will compile the code, run tests (unless skipped via `-DskipTests`), and package the application into a JAR file located in the `target/` directory (e.g., `target/userservice-0.0.1-SNAPSHOT.jar`).

## Running the Service

### Locally

1.  **Ensure all prerequisites are met and running** (Database, Config Server, Eureka, IdP).
2.  **Run the application**:
    ```bash
    java -jar target/userservice-0.0.1-SNAPSHOT.jar
    ```
    You might need to provide Spring Boot properties via command-line arguments or environment variables if they are not fully managed by the Config Server for your local setup (e.g., `-Dspring.profiles.active=local`). The application typically runs on port 8080 unless `server.port` is configured otherwise (e.g., to 8090 as per your Dockerfile).

### Using Docker

1.  **Ensure Docker is installed and running.**
2.  **Build the Docker image** (from the `spshpau-userservice` root directory where the `Dockerfile` is located):
    ```bash
    # If using the version from your adjusted Dockerfile
    docker build -t userservice:0.1.1-ALPHA . 
    # Or, for the version from pom.xml
    # docker build --build-arg JAR_FILE=target/userservice-0.0.1-SNAPSHOT.jar -t userservice:0.0.1-SNAPSHOT .
    ```
    (Replace `userservice:tag` with your desired image name and tag).
3.  **Run the Docker container**:
    ```bash
    # Assuming your Dockerfile EXPOSEs 8090 and your app runs on 8090
    docker run -p 8090:8090 --name userservice-container userservice:0.1.1-ALPHA 
    ```
    * `-p 8090:8090`: Maps port 8090 on your host to port 8090 in the container (as specified by `EXPOSE 8090` in your adjusted Dockerfile).
    * You might need to pass environment variables for configuration if not using a Config Server accessible from within Docker, or use Docker networking to connect to other services. For example:
        ```bash
        docker run -p 8090:8090 \
          -e SPRING_CONFIG_IMPORT="optional:configserver:http://your-config-server-host:8888" \
          -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE="http://your-eureka-host:8761/eureka/" \
          # Add other necessary environment variables for DB, OAuth etc.
          --name userservice-container userservice:0.1.1-ALPHA 
        ```

The service should be accessible at `http://localhost:8090` (or the configured port).

## API Endpoints Overview

All API endpoints require a valid JWT Bearer token in the `Authorization` header, except for `/api/v1/util/ping`.

* **Utility Endpoints**: `BASE_URL: /api/v1/util`
    * `GET /ping`: Checks service availability (public).
    * `GET /auth`: (Requires Auth) Checks token validity.
* **User Endpoints**: `BASE_URL: /api/v1/users`
    * `GET /me`: Get details of the currently authenticated user.
    * `PUT /me/sync`: Synchronize current user's data from Keycloak.
    * `PUT /me/location`: Update current user's location.
    * `GET /search/username/{username}`: Get user details by username.
    * `GET /search/id/{userId}`: Get user summary by ID.
    * `PUT /{userId}/deactivate`: (Admin) Deactivate a user.
    * `PUT /{userId}/reactivate`: (Admin) Reactivate a user.
    * `GET /search/filter`: Search/filter active users.
    * `GET /matches`: Find potential collaborators for the current user.
* **Artist Profile Endpoints**: `BASE_URL: /api/v1/users/artist-profile`
    * `GET /me`: Get current user's artist profile.
    * `PUT /me/create`: Create or update current user's artist profile.
    * `PATCH /me/patch`: Partially update current user's artist profile.
    * `GET /me/genres`: Get genres for current user's artist profile.
    * `POST /me/genres/add/{genreId}`: Add a genre to current user's artist profile.
    * `DELETE /me/genres/remove/{genreId}`: Remove a genre from current user's artist profile.
    * `GET /me/skills`: Get skills for current user's artist profile.
    * `POST /me/skills/add/{skillId}`: Add a skill to current user's artist profile.
    * `DELETE /me/skills/remove/{skillId}`: Remove a skill from current user's artist profile.
    * `GET /{username}`: Get artist profile by username.
    * `GET /{username}/genres`: Get genres for an artist profile by username.
    * `GET /{username}/skills`: Get skills for an artist profile by username.
* **Producer Profile Endpoints**: `BASE_URL: /api/v1/users/producer-profile`
    * `GET /me`: Get current user's producer profile.
    * `PUT /me/create`: Create or update current user's producer profile.
    * `PATCH /me/patch`: Partially update current user's producer profile.
    * `GET /me/genres`: Get genres for current user's producer profile.
    * `POST /me/genres/add/{genreId}`: Add a genre to current user's producer profile.
    * `DELETE /me/genres/remove/{genreId}`: Remove a genre.
    * `GET /{username}`: Get producer profile by username.
    * `GET /{username}/genres`: Get genres for a producer profile by username.
* **Genre Endpoints**: `BASE_URL: /api/v1/genres`
    * `POST /add`: (Admin) Add a new genre.
    * `DELETE /delete/{genreId}`: (Admin) Delete a genre.
    * `GET /`: Get all genres (paginated).
* **Skill Endpoints**: `BASE_URL: /api/v1/skills`
    * `POST /add`: (Admin) Add a new skill.
    * `DELETE /delete/{skillId}`: (Admin) Delete a skill.
    * `GET /`: Get all skills (paginated).
* **User Interaction Endpoints**: `BASE_URL: /api/v1/interactions/me`
    * `POST /connections/request/{addresseeId}`: Send a connection request.
    * `POST /connections/accept/{requesterId}`: Accept a connection request.
    * `DELETE /connections/reject/{requesterId}`: Reject a connection request.
    * `DELETE /connections/remove/{otherUserId}`: Remove an existing connection.
    * `GET /connections`: Get current user's connections (paginated).
    * `GET /connections/all`: Get all of current user's connections.
    * `GET /connections/requests/incoming`: Get pending incoming requests.
    * `GET /connections/requests/outgoing`: Get pending outgoing requests.
    * `POST /blocks/block/{blockedId}`: Block a user.
    * `DELETE /blocks/unblock/{blockedId}`: Unblock a user.
    * `GET /blocks`: Get users blocked by the current user.
    * `GET /status/{otherUserId}`: Check interaction status with another user.

(For detailed request/response formats, refer to the DTOs and controller implementations.)

## Security

The service is secured using Spring Security with OAuth2 Resource Server capabilities.
* All API endpoints (except for the public `/api/v1/util/ping`) require a valid JWT Bearer token in the `Authorization` header.
* The JWT is validated against the configured `issuer-uri` from your Identity Provider.
* The `JwtAuthConverter.java` class is responsible for extracting authorities (roles) from the JWT, based on claims associated with a specific `resource-id` (client ID) and prefixing them with `ROLE_`. These roles can then be used with method-level security annotations (e.g., `@PreAuthorize("hasRole('client_admin')")`).

## Testing

To run the unit and integration tests for the service:
```bash
mvn test