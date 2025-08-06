# Use official OpenJDK 17 image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy Maven files first for better caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Create uploads directory
RUN mkdir -p /tmp/uploads/property-photos

# Expose port
EXPOSE $PORT

# Run the application
CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} -Dspring.profiles.active=render -jar target/seller-funnel-1.0-SNAPSHOT.jar"]