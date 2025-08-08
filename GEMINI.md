# Project Overview

This is a comprehensive Spring Boot web application designed as a lead generation and management system for real estate professionals. The application serves as a funnel for connecting buyers and sellers, allowing users to capture leads, manage marketing campaigns, and bird-dog deals effectively.

**Main Technologies:**

*   **Backend:** Java, Spring Boot
*   **Frontend:** Thymeleaf
*   **Database:** H2 (development), PostgreSQL (production)
*   **Build:** Maven

**Architecture:**

The application follows a standard Model-View-Controller (MVC) architecture:

*   **`src/main/java/com/realestate/sellerfunnel/controller`**: Contains the web controllers that handle incoming HTTP requests.
*   **`src/main/java/com/realestate/sellerfunnel/service`**: Contains the business logic of the application.
*   **`src/main/java/com/realestate/sellerfunnel/repository`**: Contains the data access layer, using Spring Data JPA to interact with the database.
*   **`src/main/java/com/realestate/sellerfunnel/model`**: Contains the JPA entity classes.
*   **`src/main/resources/templates`**: Contains the Thymeleaf templates for the user interface.

# Building and Running

**Prerequisites:**

*   Java 21 or higher
*   Maven 3.6+
*   Docker (optional, for PostgreSQL)

**Running the Application:**

*   **Development Mode (H2 Database):**
    ```bash
    mvn spring-boot:run
    ```
*   **Production Mode (PostgreSQL):**
    ```bash
    docker-compose up -d
    mvn spring-boot:run -Dspring.profiles.active=postgres
    ```

**Testing the Application:**

```bash
mvn test
```

# Development Conventions

*   **Code Style:** Follows standard Spring Boot conventions.
*   **Testing:**
    *   Unit tests for the service layer using JUnit 5.
    *   Integration tests for the web layer using MockMvc.
    *   Database tests for the repository layer using `@DataJpaTest`.
*   **Branching:** Features are developed in branches named `feature/<feature-name>`.
*   **Commits:** Commit messages should be clear and descriptive.
