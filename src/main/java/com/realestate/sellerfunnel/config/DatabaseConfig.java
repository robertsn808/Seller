package com.realestate.sellerfunnel.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
@Profile("render")
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        System.out.println("DATABASE_URL from environment: " + databaseUrl);
        
        if (databaseUrl != null && !databaseUrl.trim().isEmpty()) {
            try {
                // Handle both postgresql:// and jdbc:postgresql:// formats
                if (databaseUrl.startsWith("postgresql://")) {
                    URI dbUri = new URI(databaseUrl);
                    
                    String host = dbUri.getHost();
                    int port = dbUri.getPort() != -1 ? dbUri.getPort() : 5432;
                    String database = dbUri.getPath().substring(1); // Remove leading slash
                    String[] userInfo = dbUri.getUserInfo().split(":");
                    String username = userInfo[0];
                    String password = userInfo.length > 1 ? userInfo[1] : "";
                    
                    String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
                    
                    System.out.println("Converted JDBC URL: " + jdbcUrl);
                    System.out.println("Username: " + username);
                    
                    return DataSourceBuilder.create()
                        .url(jdbcUrl)
                        .username(username)
                        .password(password)
                        .driverClassName("org.postgresql.Driver")
                        .build();
                } else if (databaseUrl.startsWith("jdbc:postgresql://")) {
                    // Already in JDBC format
                    return DataSourceBuilder.create()
                        .url(databaseUrl)
                        .driverClassName("org.postgresql.Driver")
                        .build();
                }
            } catch (Exception e) {
                System.err.println("Failed to parse DATABASE_URL: " + databaseUrl);
                e.printStackTrace();
                throw new RuntimeException("Failed to parse DATABASE_URL: " + databaseUrl, e);
            }
        }
        
        System.err.println("DATABASE_URL environment variable not found or empty. Available env vars:");
        System.getenv().entrySet().forEach(entry -> {
            if (entry.getKey().contains("DATABASE") || entry.getKey().contains("POSTGRES")) {
                System.err.println("  " + entry.getKey() + "=" + entry.getValue());
            }
        });
        
        throw new RuntimeException("DATABASE_URL environment variable not found or invalid: " + databaseUrl);
    }
}