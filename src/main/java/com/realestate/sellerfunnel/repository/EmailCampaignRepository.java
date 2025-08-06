package com.realestate.sellerfunnel.repository;

import com.realestate.sellerfunnel.model.EmailCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailCampaignRepository extends JpaRepository<EmailCampaign, Long> {
    
    // Find active campaigns
    List<EmailCampaign> findByIsActiveTrueOrderByCreatedAtDesc();
    
    // Find campaigns by status
    List<EmailCampaign> findByStatusOrderByCreatedAtDesc(String status);
    
    // Find scheduled campaigns ready to send
    @Query("SELECT ec FROM EmailCampaign ec WHERE ec.status = 'SCHEDULED' AND ec.scheduledDate <= :now ORDER BY ec.scheduledDate ASC")
    List<EmailCampaign> findScheduledCampaignsReadyToSend(@Param("now") LocalDateTime now);
    
    // Find campaigns by target audience
    List<EmailCampaign> findByTargetAudienceOrderByCreatedAtDesc(String targetAudience);
    
    // Find campaigns by client type filter
    List<EmailCampaign> findByClientTypeFilterOrderByCreatedAtDesc(String clientTypeFilter);
    
    // Find campaigns by lead source filter
    List<EmailCampaign> findByLeadSourceFilterOrderByCreatedAtDesc(String leadSourceFilter);
    
    // Find campaigns created in date range
    @Query("SELECT ec FROM EmailCampaign ec WHERE ec.createdAt BETWEEN :startDate AND :endDate ORDER BY ec.createdAt DESC")
    List<EmailCampaign> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find campaigns sent in date range
    @Query("SELECT ec FROM EmailCampaign ec WHERE ec.sentDate BETWEEN :startDate AND :endDate ORDER BY ec.sentDate DESC")
    List<EmailCampaign> findBySentDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find campaigns by name containing
    List<EmailCampaign> findByCampaignNameContainingIgnoreCaseOrderByCreatedAtDesc(String campaignName);
    
    // Find campaigns by sender email
    List<EmailCampaign> findBySenderEmailOrderByCreatedAtDesc(String senderEmail);
    
    // Count campaigns by status
    @Query("SELECT ec.status, COUNT(ec) FROM EmailCampaign ec GROUP BY ec.status")
    List<Object[]> countByStatus();
    
    // Count campaigns by target audience
    @Query("SELECT ec.targetAudience, COUNT(ec) FROM EmailCampaign ec GROUP BY ec.targetAudience")
    List<Object[]> countByTargetAudience();
    
    // Find campaigns with high engagement (open rate > threshold)
    @Query("SELECT ec FROM EmailCampaign ec WHERE ec.sentCount > 0 AND (ec.openedCount * 100.0 / ec.sentCount) >= :minOpenRate ORDER BY (ec.openedCount * 100.0 / ec.sentCount) DESC")
    List<EmailCampaign> findHighEngagementCampaigns(@Param("minOpenRate") double minOpenRate);
    
    // Find campaigns with low engagement (open rate < threshold)
    @Query("SELECT ec FROM EmailCampaign ec WHERE ec.sentCount > 0 AND (ec.openedCount * 100.0 / ec.sentCount) <= :maxOpenRate ORDER BY (ec.openedCount * 100.0 / ec.sentCount) ASC")
    List<EmailCampaign> findLowEngagementCampaigns(@Param("maxOpenRate") double maxOpenRate);
    
    // Find campaigns by multiple criteria
    @Query("SELECT ec FROM EmailCampaign ec WHERE " +
           "(:status IS NULL OR ec.status = :status) AND " +
           "(:targetAudience IS NULL OR ec.targetAudience = :targetAudience) AND " +
           "(:clientTypeFilter IS NULL OR ec.clientTypeFilter = :clientTypeFilter) AND " +
           "(:isActive IS NULL OR ec.isActive = :isActive) " +
           "ORDER BY ec.createdAt DESC")
    List<EmailCampaign> findByFilters(@Param("status") String status,
                                     @Param("targetAudience") String targetAudience,
                                     @Param("clientTypeFilter") String clientTypeFilter,
                                     @Param("isActive") Boolean isActive);
    
    // Find campaigns that need follow-up (sent but low engagement)
    @Query("SELECT ec FROM EmailCampaign ec WHERE ec.status = 'SENT' AND ec.sentCount > 0 AND (ec.openedCount * 100.0 / ec.sentCount) < 20 ORDER BY ec.sentDate DESC")
    List<EmailCampaign> findCampaignsNeedingFollowUp();
    
    // Find recent campaigns (last 30 days)
    @Query("SELECT ec FROM EmailCampaign ec WHERE ec.createdAt >= :startDate ORDER BY ec.createdAt DESC")
    List<EmailCampaign> findRecentCampaigns(@Param("startDate") LocalDateTime startDate);
    
    // Find campaigns by sender name
    List<EmailCampaign> findBySenderNameContainingIgnoreCaseOrderByCreatedAtDesc(String senderName);
} 