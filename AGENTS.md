# Repository Guidelines

## Project Structure & Module Organization
- Backend (Spring Boot, Java 17): root `src/main/java` with Thymeleaf views in `src/main/resources/templates/**`. Tests live in `src/test/java/**`.
- Containerized backend module: `backend/` mirrors the Spring Boot app for Docker Compose.
- Frontend (React + Vite + Express): `frontend/` with app code in `frontend/src/**` and API server in `frontend/server/**`.
- Infrastructure: `docker-compose.yml`, `Dockerfile`, and environment configs (`application-*.properties`, `.env`).

## Build, Test, and Development Commands
- Backend (local): `./mvnw clean verify` (build + tests), `./mvnw spring-boot:run` (run dev), `java -jar target/seller-funnel-1.0-SNAPSHOT.jar` (run built jar).
- Frontend: `npm --prefix frontend run dev` (Vite + Express dev), `npm --prefix frontend run build` (client + server bundle), `npm --prefix frontend start` (serve production build), `npm --prefix frontend run check` (type-check), `npm --prefix frontend run db:push` (apply Drizzle schema).
- Docker (full stack): `docker-compose up -d` (Postgres, Redis, backend, frontend). Frontend expects `VITE_API_BASE_URL` (defaults to `http://localhost:8080`).

## Coding Style & Naming Conventions
- Java: 4-space indent, classes `PascalCase`, methods/fields `camelCase`, packages `lower.case`. Prefer constructor injection; annotate layers with `@Controller`, `@Service`, `@Repository`.
- Templates: Thymeleaf files in `templates/**` use kebab-case (e.g., `buyer-form.html`).
- TypeScript/React: files kebab-case (e.g., `sales-chart.tsx`), components `PascalCase`, hooks in `frontend/src/hooks`.

## Testing Guidelines
- Frameworks: JUnit 5 + Spring Boot Test. Run `./mvnw test` or `./mvnw verify` to execute tests and produce JaCoCo at `target/site/jacoco/index.html`.
- Naming: mirror package paths; end with `*Test.java` or `*Tests.java`. Add focused service/controller tests when changing behavior.
- Frontend: no test runner configured; add Vitest/react-testing-library as needed for UI changes.

## Commit & Pull Request Guidelines
- Commits: Use concise, imperative messages. Prefer Conventional Commits seen in history (e.g., `fix(thymeleaf): …`, `chore: …`, `test: …`). Group related changes only.
- PRs: include purpose, linked issues, steps to verify, and screenshots/GIFs for UI. Ensure backend tests pass and frontend builds (`./mvnw verify`, `npm --prefix frontend run build`).

## Security & Configuration
- Never commit secrets. Use `.env` locally and Spring profiles (`application-postgres.properties`, `application-render.properties`).
- For local dev, ensure Postgres is running (via Docker Compose) and set `VITE_API_BASE_URL` for the frontend.
