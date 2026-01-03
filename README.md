# GDS SWE Challenge - Lunch Place Decider

## Overview
This application solves the "lunch location dilemma" by allowing teams to collectively decide on a lunch spot. Users can submit restaurant choices, and the system randomly selects one. It supports multiple sessions, user validation, and automated background processing.

## Technologies
- **Java 25**: Latest LTS version for modern language features.
- **Spring Boot 4.0.1**: Application framework.
- **Spring Data JPA**: Data access layer.
- **H2 Database**: In-memory database for rapid development and testing.
- **Liquibase**: Database migration and schema management.
- **Spring Batch**: For processing bulk data (loading pre-defined users).
- **SpringDoc OpenAPI (Swagger)**: API documentation.

## Prerequisites
### For Local Development
- Java JDK 25
- Maven 3.8+

### For Containerized Run
- Docker Desktop


## Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/chinpui12/gds-swe-challenge.git
cd gds-swe-challenge
```

### 2. Build the application
```bash
mvn clean install
```

### 3. Run the application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

### 4. Run with Docker
> [!IMPORTANT]
> **Stop Local Instance**: If you have the application running locally via `mvn spring-boot:run`, you **MUST** stop it (Ctrl+C) before running with Docker to avoid port conflicts (Error `Bind for 0.0.0.0:8080 failed: port is already allocated`).

To build and run the application without installing Java/Maven locally, you can use the provided convenience scripts:

**Windows**:
```cmd
run.bat
```

**Linux/macOS**:
```bash
./run.sh
```

Alternatively, you can run directly with Docker Compose:
```bash
docker-compose up --build
```

The application will be available at `http://localhost:8080`.


## API Documentation & Verification
The application provides a comprehensive Swagger UI for exploring and testing the APIs.
- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

> [!TIP]
> You can verify the application requirements using the Swagger UI or by following the detailed steps in the [Verification Guide](VERIFICATION_GUIDE.md).


### Core Endpoints
- **Restaurant API** (`/restaurant`)
    - `POST /restaurant/submit`: Submit a restaurant choice (Header `X-Username` required).
    - `GET /restaurant/random`: Get a random restaurant for a session.
- **Session API** (`/session`)
    - `GET /session`: List all sessions.
    - `POST /session/invite`: Invite users to a session (Creator only).
    - `PATCH /session/{id}/reset`: Re-open a closed session.

## Features & Implementation Details

### 1. Restaurant Submission & Random Selection
- Users can submit restaurants linked to a specific session (or the global session by default if no session is specified).
- The randomizer selects a restaurant from the pool. Once selected, the session closes to prevent further submissions.

> [!NOTE]
> **Global Session Exception**: While custom sessions restrict the "Random Choice" request to the session creator, the **Global Session (ID: 0)** allows **any user** to trigger the random selection.


### 2. Session Management
- **Multiple Sessions**: Users with the valid privileges can start their own lunch sessions.
- **Invitation System**: Session creators can invite specific users. Only invited users can contribute to private sessions.
- **Pre-defined Users**: A list of users is loaded from `src/main/resources/data/default-users.csv` on startup using **Spring Batch**. Including specifying which users have the privilege to initiate a new session.

### 3. Data Persistence
- Uses H2 in-memory database.
- **H2 Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - **JDBC URL**: `jdbc:h2:mem:gds-swe-challenge-db`
  - **User**: `sa`
  - **Password**: (Empty)
- Schema is managed via Liquibase changelogs (`src/main/resources/db/changelog`).


## Design Considerations
- **Layered Architecture**: Controller -> Service -> Repository.
- **Validation**: Input validation using Jakarta Bean Validation (`@Valid`).
- **Error Handling**: Global exception handler (`GlobalExceptionHandler`) for consistent error responses.
- **Auditing**: Entities track `createdAt`, `updatedAt`, etc.

## Automated Testing
Run unit and integration tests with:
```bash
mvn test
```
