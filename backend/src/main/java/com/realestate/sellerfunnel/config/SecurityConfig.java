package com.realestate.sellerfunnel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(authz -> authz
                // API endpoints - mostly public for now
                .requestMatchers("/api/buyer", "/api/seller", "/api/photos/**", "/api/dashboard-stats").permitAll()
                .requestMatchers("/api/buyers", "/api/sellers", "/api/clients").authenticated()
                // Admin endpoints
                .requestMatchers("/admin/**").authenticated()
                // Property management
                .requestMatchers("/property/**").permitAll()
                // Public endpoints (forms and static resources)
                .requestMatchers("/", "/buyer", "/buyer/**", "/seller", "/seller/**", 
                               "/photos/**", "/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/admin", false)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/logged-out")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins (React dev server)
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:5173", 
            "https://*.vercel.app",
            "https://*.netlify.app"
        ));
        
        // Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Allow all headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Allow credentials
        configuration.setAllowCredentials(true);
        
        // Set max age for preflight requests
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        return source;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
            .username(adminUsername)
            .password(passwordEncoder().encode(adminPassword))
            .roles("ADMIN")
            .build();

        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}