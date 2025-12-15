# SPSHPAU Frontend

This is the frontend application for SPSHPAU, a platform designed for musicians and producers to connect, collaborate, and manage music projects. The application is built with React and Vite, utilizing Material UI for the user interface and Keycloak for authentication.

## Features

* **Authentication**: Secure login and session management using Keycloak.
* **User Profiles**:
    * View and manage user account information (location, basic details).
    * Create and manage separate Artist and Producer profiles with details like bio, experience level, availability, genres, and skills.
    * View other users' profiles.
* **User Interaction**:
    * Search for users with advanced filtering (genres, skills, profile types, experience, availability).
    * View potential collaborator matches.
    * Manage connections (send, accept, reject, remove).
    * Block and unblock users.
* **Project Management**:
    * Create, view, and delete projects.
    * Manage project collaborators.
    * Manage project tasks (create, edit, delete, assign).
    * Manage project milestones (create, edit, delete).
    * File management (upload, download, versioning, delete).
    * Budget management (create, view, update, manage expenses).
* **Real-time Chat**:
    * Direct messaging with connections.
    * WebSocket integration using StompJS and SockJS for message delivery and presence updates.
    * Unread message counts and message status (sent, delivered, read).
* **Responsive UI**: Designed with Material UI for a consistent experience across devices.

## Tech Stack

* **Framework/Library**: React 19
* **Build Tool**: Vite
* **UI Library**: Material UI (MUI) v7
* **Routing**: React Router DOM v7
* **Authentication**: Keycloak JS Adapter (`keycloak-js`, `@react-keycloak/web`)
* **State Management**: React Context API (primarily via `UserContext`)
* **Date Management**: `date-fns`, `@mui/x-date-pickers`
* **Real-time Communication**: StompJS, SockJS
* **Linting**: ESLint

## Environment Variables

The application uses Vite for handling environment variables. Create a `.env` file in the root of the project and add the following variables as needed:

* `VITE_KEYCLOAK_URL`: URL of your Keycloak server (e.g., `http://localhost:8080`)
* `VITE_KEYCLOAK_REALM`: Keycloak realm name (e.g., `SPSHPAU`)
* `VITE_KEYCLOAK_CLIENT_ID`: Keycloak client ID for this frontend (e.g., `spshpau-rest-api`)
* `VITE_API_GATEWAY_URL`: Base URL for the backend API gateway (e.g., `http://localhost:8081/api/v1`)
* `VITE_CHAT_SERVICE_WS_URL`: WebSocket URL for the chat service (e.g., `http://localhost:8091/ws`)

Example `.env` file:
```env
VITE_KEYCLOAK_URL=http://localhost:8080
VITE_KEYCLOAK_REALM=SPSHPAU
VITE_KEYCLOAK_CLIENT_ID=spshpau-rest-api
VITE_API_GATEWAY_URL=http://localhost:8081/api/v1
VITE_CHAT_SERVICE_WS_URL=http://localhost:8091/ws
