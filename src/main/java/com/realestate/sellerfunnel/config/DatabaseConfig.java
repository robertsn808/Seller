package com.realestate.sellerfunnel.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
@Profile("render")
public class DatabaseConfig {

    // Spring Boot 2.4+ automatically converts DATABASE_URL to proper JDBC format
    // Just need to ensure the environment variable is available
    
    @Bean
    @ConditionalOnProperty("DATABASE_URL")
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        System.out.println("=== DATABASE DEBUG INFO ===");
        System.out.println("DATABASE_URL: " + databaseUrl);
        System.out.println("Active profiles: " + String.join(",", System.getProperty("spring.profiles.active", "none")));
        
        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            try {
                URI dbUri = new URI(databaseUrl);
                
                String host = dbUri.getHost();
                int port = dbUri.getPort() != -1 ? dbUri.getPort() : 5432;
                String database = dbUri.getPath().substring(1);
                String[] userInfo = dbUri.getUserInfo().split(":");
                String username = userInfo[0];
                String password = userInfo.length > 1 ? userInfo[1] : "";
                
                String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
                
                System.out.println("Converted JDBC URL: " + jdbcUrl);
                System.out.println("Username: " + username);
                System.out.println("=== END DEBUG INFO ===");
                
                return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();
            } catch (Exception e) {
                System.err.println("Failed to parse DATABASE_URL: " + e.getMessage());
                throw new RuntimeException("Failed to parse DATABASE_URL", e);
            }
        }
        
        throw new RuntimeException("DATABASE_URL not found or invalid format: " + databaseUrl);
    }
}