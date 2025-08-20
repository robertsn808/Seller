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
    
    // Find by client status
    List<Client> findByClientStatusOrderByCreatedAtDesc(String clientStatus);
    
    // Find by client type and status
    List<Client> findByClientTypeAndClientStatusOrderByCreatedAtDesc(String clientType, String clientStatus);
    
    // Find by lead source
    List<Client> findByLeadSourceOrderByCreatedAtDesc(String leadSource);
    
    // Find clients who have opted in for emails
    List<Client> findByEmailOptedInTrueAndIsActiveTrueOrderByCreatedAtDesc();
    
    // Find clients by type who have opted in for emails
    List<Client> findByClientTypeAndEmailOptedInTrueAndIsActiveTrueOrderByCreatedAtDesc(String clientType);
    
    // Find clients by status who have opted in for emails
    List<Client> findByClientStatusAndEmailOptedInTrueAndIsActiveTrueOrderByCreatedAtDesc(String clientStatus);
    
    // Find clients by multiple types who have opted in for emails
    @Query("SELECT c FROM Client c WHERE c.clientType IN :clientTypes AND c.emailOptedIn = true AND c.isActive = true ORDER BY c.createdAt DESC")
    List<Client> findByClientTypesAndEmailOptedIn(@Param("clientTypes") List<String> clientTypes);
    
    // Find clients by multiple statuses who have opted in for emails
    @Query("SELECT c FROM Client c WHERE c.clientStatus IN :clientStatuses AND c.emailOptedIn = true AND c.isActive = true ORDER BY c.createdAt DESC")
    List<Client> findByClientStatusesAndEmailOptedIn(@Param("clientStatuses") List<String> clientStatuses);
    
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
    
    // Enhanced search by multiple criteria
    @Query("SELECT c FROM Client c WHERE " +
           "(:firstName IS NULL OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
           "(:lastName IS NULL OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
           "(:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:phone IS NULL OR c.phoneNumber LIKE CONCAT('%', :phone, '%')) AND " +
           "(:clientType IS NULL OR c.clientType = :clientType) AND " +
           "(:clientStatus IS NULL OR c.clientStatus = :clientStatus) AND " +
           "(:leadSource IS NULL OR c.leadSource = :leadSource) AND " +
           "(:city IS NULL OR LOWER(c.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:state IS NULL OR LOWER(c.state) LIKE LOWER(CONCAT('%', :state, '%'))) AND " +
           "(:companyName IS NULL OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :companyName, '%'))) AND " +
           "(:isActive IS NULL OR c.isActive = :isActive) " +
           "ORDER BY c.createdAt DESC")
    List<Client> findByAdvancedSearch(@Param("firstName") String firstName,
                                     @Param("lastName") String lastName,
                                     @Param("email") String email,
                                     @Param("phone") String phone,
                                     @Param("clientType") String clientType,
                                     @Param("clientStatus") String clientStatus,
                                     @Param("leadSource") String leadSource,
                                     @Param("city") String city,
                                     @Param("state") String state,
                                     @Param("companyName") String companyName,
                                     @Param("isActive") Boolean isActive);
    
    // Find clients with high contact counts
    @Query("SELECT c FROM Client c WHERE c.totalContactCount >= :minContacts ORDER BY c.totalContactCount DESC")
    List<Client> findByMinContactCount(@Param("minContacts") Integer minContacts);
    
    // Find clients with no contact history
    @Query("SELECT c FROM Client c WHERE c.totalContactCount = 0 OR c.totalContactCount IS NULL ORDER BY c.createdAt DESC")
    List<Client> findClientsWithNoContact();
    
    // Find clients by contact frequency
    @Query("SELECT c FROM Client c WHERE c.emailContactCount >= :minEmails OR c.phoneContactCount >= :minCalls ORDER BY c.totalContactCount DESC")
    List<Client> findByContactFrequency(@Param("minEmails") Integer minEmails, @Param("minCalls") Integer minCalls);
    
    // Count clients by type
    @Query("SELECT c.clientType, COUNT(c) FROM Client c WHERE c.isActive = true GROUP BY c.clientType")
    List<Object[]> countByClientType();
    
    // Count clients by status
    @Query("SELECT c.clientStatus, COUNT(c) FROM Client c WHERE c.isActive = true GROUP BY c.clientStatus")
    List<Object[]> countByClientStatus();
    
    // Count clients by lead source
    @Query("SELECT c.leadSource, COUNT(c) FROM Client c WHERE c.isActive = true GROUP BY c.leadSource")
    List<Object[]> countByLeadSource();
    
    // Find clients created in date range
    @Query("SELECT c FROM Client c WHERE c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.createdAt DESC")
    List<Client> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find clients by multiple criteria
    @Query("SELECT c FROM Client c WHERE " +
           "(:clientType IS NULL OR c.clientType = :clientType) AND " +
           "(:clientStatus IS NULL OR c.clientStatus = :clientStatus) AND " +
           "(:leadSource IS NULL OR c.leadSource = :leadSource) AND " +
           "(:city IS NULL OR c.city = :city) AND " +
           "(:state IS NULL OR c.state = :state) AND " +
           "c.emailOptedIn = true AND c.isActive = true " +
           "ORDER BY c.createdAt DESC")
    List<Client> findByFilters(@Param("clientType") String clientType,
                              @Param("clientStatus") String clientStatus,
                              @Param("leadSource") String leadSource,
                              @Param("city") String city,
                              @Param("state") String state);
    
    // Find duplicate emails
    @Query("SELECT c.email, COUNT(c) FROM Client c GROUP BY c.email HAVING COUNT(c) > 1")
    List<Object[]> findDuplicateEmails();
    
    // Find clients with no recent contact
    @Query("SELECT c FROM Client c WHERE c.lastContactDate IS NULL OR c.lastContactDate <= :date ORDER BY c.lastContactDate ASC NULLS FIRST")
    List<Client> findClientsNeedingContact(@Param("date") LocalDateTime date);
    
    // Find clients by contact date range
    @Query("SELECT c FROM Client c WHERE c.lastContactDate BETWEEN :startDate AND :endDate ORDER BY c.lastContactDate DESC")
    List<Client> findByContactDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find top contacted clients
    @Query("SELECT c FROM Client c WHERE c.totalContactCount > 0 ORDER BY c.totalContactCount DESC")
    List<Client> findTopContactedClients();
    
    // Find clients by email contact count
    @Query("SELECT c FROM Client c WHERE c.emailContactCount >= :minEmails ORDER BY c.emailContactCount DESC")
    List<Client> findByEmailContactCount(@Param("minEmails") Integer minEmails);
    
    // Find clients by phone contact count
    @Query("SELECT c FROM Client c WHERE c.phoneContactCount >= :minCalls ORDER BY c.phoneContactCount DESC")
    List<Client> findByPhoneContactCount(@Param("minCalls") Integer minCalls);
    
    // Count active clients
    long countByIsActiveTrue();
} 