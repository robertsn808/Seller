# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Real Estate Connect is a Spring Boot web application that serves as a funnel for connecting real estate buyers and sellers. The application allows buyers to submit their "buy box" requirements and sellers to list their properties, enabling you to bird-dog deals by matching qualified leads.

## Development Commands

### Running with H2 (Development)
- **Start the application**: `mvn spring-boot:run`
- **Access H2 console**: http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:testdb)

### Running with PostgreSQL (Production)
- **Start PostgreSQL**: `docker-compose up -d`
- **Start with PostgreSQL**: `mvn spring-boot:run -Dspring.profiles.active=postgres`
- **Stop PostgreSQL**: `docker-compose down`

### General Commands
- **Build the project**: `mvn clean package`
- **Run tests**: `mvn test`
- **Access the application**: http://localhost:8080

### Project Structure
- `src/main/java/com/realestate/sellerfunnel/` - Main application package
  - `SellerFunnelApplication.java` - Spring Boot main class
  - `model/` - Entity classes (Buyer, Seller)
  - `repository/` - JPA repositories
  - `controller/` - Web controllers
- `src/main/resources/` - Configuration and templates
  - `application.properties` - Spring Boot configuration
  - `templates/` - Thymeleaf HTML templates
- `pom.xml` - Maven configuration with Spring Boot dependencies

## Key Features

### Buyer Lead Collection
- Comprehensive buyer form capturing budget, preferences, and requirements
- Fields include: budget range, preferred areas, bedrooms/bathrooms, property type, financing needs, timeframe
- **NEW**: Creative financing options (seller financing, lease-to-own, etc.)
- Validation ensures data quality

### Seller Lead Collection  
- Detailed property listing form for sellers
- Fields include: property details, address, price, condition, selling reason, timeframe
- **NEW**: Property photo upload support (up to 10 photos, 10MB each)
- **NEW**: Creative financing options (owner financing, rent-to-own, etc.)
- Support for repair details and property condition

### Admin Dashboard
- View all buyer and seller leads at `/admin`
- Sort by submission date (most recent first)
- Contact information readily accessible
- **NEW**: Property photo thumbnails in seller listings
- **NEW**: Creative financing indicators for both buyers and sellers
- Lead statistics and overview

## Database Configuration

### Development (H2)
The application uses H2 in-memory database by default for development. Data is recreated on each restart.

### Production (PostgreSQL)
For persistent data storage, use PostgreSQL:
1. Start PostgreSQL: `docker-compose up -d`
2. Run application: `mvn spring-boot:run -Dspring.profiles.active=postgres`
3. Database: `seller_funnel` with user `postgres` and password `password`

### File Storage
- Property photos are stored in `uploads/property-photos/` directory
- Access photos via `/photos/{fileName}` endpoint
- Supports JPG, PNG, GIF, WebP formats

## Architecture Notes

This is a Spring MVC web application using Thymeleaf for templating. The architecture follows standard Spring Boot patterns with clearly separated layers (Controller → Service → Repository → Entity). Form validation uses Bean Validation annotations, and the H2 console is available for development database access.