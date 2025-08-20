# Multi-stage build for Spring Boot application
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS runtime

# Add non-root user
RUN addgroup -g 1001 -S spring && \
    adduser -S spring -u 1001

# Create upload directory
RUN mkdir -p /app/uploads && \
    chown -R spring:spring /app

USER spring:spring

WORKDIR /app

# Copy jar from builder stage
COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]