package com.realestate.sellerfunnel.repository;

import com.realestate.sellerfunnel.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    // Find by email
    Optional<Client> findByEmail(String email);
    
    // Find active clients
    List<Client> findByIsActiveTrueOrderByCreatedAtDesc();
    
    // Find by client type
    List<Client> findByClientTypeOrderByCreatedAtDesc(String clientType);
    
    // Find by lead source
    List<Client> findByLeadSourceOrderByCreatedAtDesc(String leadSource);
    
    // Find clients who have opted in for emails
    List<Client> findByEmailOptedInTrueAndIsActiveTrueOrderByCreatedAtDesc();
    
    // Find clients by type who have opted in for emails
    List<Client> findByClientTypeAndEmailOptedInTrueAndIsActiveTrueOrderByCreatedAtDesc(String clientType);
    
    // Find clients by multiple types who have opted in for emails
    @Query("SELECT c FROM Client c WHERE c.clientType IN :clientTypes AND c.emailOptedIn = true AND c.isActive = true ORDER BY c.createdAt DESC")
    List<Client> findByClientTypesAndEmailOptedIn(@Param("clientTypes") List<String> clientTypes);
    
    // Find clients by lead source who have opted in for emails
    List<Client> findByLeadSourceAndEmailOptedInTrueAndIsActiveTrueOrderByCreatedAtDesc(String leadSource);
    
    // Find clients who haven't been contacted recently
    @Query("SELECT c FROM Client c WHERE (c.lastContactDate IS NULL OR c.lastContactDate <= :date) AND c.emailOptedIn = true AND c.isActive = true ORDER BY c.lastContactDate ASC NULLS FIRST")
    List<Client> findClientsForFollowUp(@Param("date") LocalDateTime date);
    
    // Find clients by city
    List<Client> findByCityOrderByCreatedAtDesc(String city);
    
    // Find clients by state
    List<Client> findByStateOrderByCreatedAtDesc(String state);
    
    // Find clients by company name
    List<Client> findByCompanyNameContainingIgnoreCaseOrderByCreatedAtDesc(String companyName);
    
    // Find clients by name (first or last name)
    @Query("SELECT c FROM Client c WHERE LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY c.createdAt DESC")
    List<Client> findByNameContainingIgnoreCase(@Param("name") String name);
    
    // Count clients by type
    @Query("SELECT c.clientType, COUNT(c) FROM Client c WHERE c.isActive = true GROUP BY c.clientType")
    List<Object[]> countByClientType();
    
    // Count clients by lead source
    @Query("SELECT c.leadSource, COUNT(c) FROM Client c WHERE c.isActive = true GROUP BY c.leadSource")
    List<Object[]> countByLeadSource();
    
    // Find clients created in date range
    @Query("SELECT c FROM Client c WHERE c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.createdAt DESC")
    List<Client> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find clients by multiple criteria
    @Query("SELECT c FROM Client c WHERE " +
           "(:clientType IS NULL OR c.clientType = :clientType) AND " +
           "(:leadSource IS NULL OR c.leadSource = :leadSource) AND " +
           "(:city IS NULL OR c.city = :city) AND " +
           "(:state IS NULL OR c.state = :state) AND " +
           "c.emailOptedIn = true AND c.isActive = true " +
           "ORDER BY c.createdAt DESC")
    List<Client> findByFilters(@Param("clientType") String clientType, 
                              @Param("leadSource") String leadSource,
                              @Param("city") String city,
                              @Param("state") String state);
    
    // Find duplicate emails
    @Query("SELECT c.email, COUNT(c) FROM Client c GROUP BY c.email HAVING COUNT(c) > 1")
    List<Object[]> findDuplicateEmails();
    
    // Find clients with no recent contact
    @Query("SELECT c FROM Client c WHERE c.lastContactDate IS NULL OR c.lastContactDate <= :date ORDER BY c.lastContactDate ASC NULLS FIRST")
    List<Client> findClientsNeedingContact(@Param("date") LocalDateTime date);
} 