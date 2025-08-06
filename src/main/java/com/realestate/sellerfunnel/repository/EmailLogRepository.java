package com.realestate.sellerfunnel.repository;

import com.realestate.sellerfunnel.model.EmailLog;
import com.realestate.sellerfunnel.model.EmailCampaign;
import com.realestate.sellerfunnel.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    
    // Find by campaign
    List<EmailLog> findByEmailCampaignOrderByCreatedAtDesc(EmailCampaign emailCampaign);
    
    // Find by client
    List<EmailLog> findByClientOrderByCreatedAtDesc(Client client);
    
    // Find by status
    List<EmailLog> findByStatusOrderByCreatedAtDesc(String status);
    
    // Find by recipient email
    List<EmailLog> findByRecipientEmailOrderByCreatedAtDesc(String recipientEmail);
    
    // Find successful emails
    @Query("SELECT el FROM EmailLog el WHERE el.status IN ('SENT', 'DELIVERED', 'OPENED', 'CLICKED') ORDER BY el.createdAt DESC")
    List<EmailLog> findSuccessfulEmails();
    
    // Find failed emails
    @Query("SELECT el FROM EmailLog el WHERE el.status IN ('BOUNCED', 'FAILED') ORDER BY el.createdAt DESC")
    List<EmailLog> findFailedEmails();
    
    // Find engaged emails (opened or clicked)
    @Query("SELECT el FROM EmailLog el WHERE el.status IN ('OPENED', 'CLICKED') ORDER BY el.createdAt DESC")
    List<EmailLog> findEngagedEmails();
    
    // Find emails sent in date range
    @Query("SELECT el FROM EmailLog el WHERE el.sentDate BETWEEN :startDate AND :endDate ORDER BY el.sentDate DESC")
    List<EmailLog> findBySentDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find emails by campaign and status
    List<EmailLog> findByEmailCampaignAndStatusOrderByCreatedAtDesc(EmailCampaign emailCampaign, String status);
    
    // Find emails by client and status
    List<EmailLog> findByClientAndStatusOrderByCreatedAtDesc(Client client, String status);
    
    // Count emails by status for a campaign
    @Query("SELECT el.status, COUNT(el) FROM EmailLog el WHERE el.emailCampaign = :campaign GROUP BY el.status")
    List<Object[]> countByStatusForCampaign(@Param("campaign") EmailCampaign campaign);
    
    // Count emails by status for a client
    @Query("SELECT el.status, COUNT(el) FROM EmailLog el WHERE el.client = :client GROUP BY el.status")
    List<Object[]> countByStatusForClient(@Param("client") Client client);
    
    // Find emails that bounced
    List<EmailLog> findByStatusAndBounceReasonIsNotNullOrderByBouncedDateDesc(String status);
    
    // Find emails by tracking ID
    Optional<EmailLog> findByTrackingId(String trackingId);
    
    // Find recent emails for a client
    @Query("SELECT el FROM EmailLog el WHERE el.client = :client ORDER BY el.createdAt DESC LIMIT :limit")
    List<EmailLog> findRecentEmailsForClient(@Param("client") Client client, @Param("limit") int limit);
    
    // Find emails that need follow-up (sent but not opened)
    @Query("SELECT el FROM EmailLog el WHERE el.status = 'SENT' AND el.sentDate <= :date ORDER BY el.sentDate ASC")
    List<EmailLog> findEmailsNeedingFollowUp(@Param("date") LocalDateTime date);
    
    // Find emails by sender email
    List<EmailLog> findBySenderEmailOrderByCreatedAtDesc(String senderEmail);
    
    // Find emails by sender name
    List<EmailLog> findBySenderNameContainingIgnoreCaseOrderByCreatedAtDesc(String senderName);
    
    // Count total emails sent
    @Query("SELECT COUNT(el) FROM EmailLog el WHERE el.status IN ('SENT', 'DELIVERED', 'OPENED', 'CLICKED')")
    Long countSuccessfulEmails();
    
    // Count total emails failed
    @Query("SELECT COUNT(el) FROM EmailLog el WHERE el.status IN ('BOUNCED', 'FAILED')")
    Long countFailedEmails();
    
    // Count total emails opened
    @Query("SELECT COUNT(el) FROM EmailLog el WHERE el.status IN ('OPENED', 'CLICKED')")
    Long countOpenedEmails();
    
    // Find emails by multiple criteria
    @Query("SELECT el FROM EmailLog el WHERE " +
           "(:campaign IS NULL OR el.emailCampaign = :campaign) AND " +
           "(:client IS NULL OR el.client = :client) AND " +
           "(:status IS NULL OR el.status = :status) AND " +
           "(:senderEmail IS NULL OR el.senderEmail = :senderEmail) " +
           "ORDER BY el.createdAt DESC")
    List<EmailLog> findByFilters(@Param("campaign") EmailCampaign campaign,
                                @Param("client") Client client,
                                @Param("status") String status,
                                @Param("senderEmail") String senderEmail);
    
    // Find emails with bounce reasons
    @Query("SELECT el FROM EmailLog el WHERE el.bounceReason IS NOT NULL AND el.bounceReason != '' ORDER BY el.bouncedDate DESC")
    List<EmailLog> findEmailsWithBounceReasons();
    
    // Find emails by subject line containing
    List<EmailLog> findBySubjectLineContainingIgnoreCaseOrderByCreatedAtDesc(String subjectLine);
    
    // Find emails sent today
    @Query("SELECT el FROM EmailLog el WHERE DATE(el.sentDate) = DATE(:today) ORDER BY el.sentDate DESC")
    List<EmailLog> findEmailsSentToday(@Param("today") LocalDateTime today);
    
    // Find emails sent this week
    @Query("SELECT el FROM EmailLog el WHERE el.sentDate >= :weekStart ORDER BY el.sentDate DESC")
    List<EmailLog> findEmailsSentThisWeek(@Param("weekStart") LocalDateTime weekStart);
    
    // Find emails sent this month
    @Query("SELECT el FROM EmailLog el WHERE el.sentDate >= :monthStart ORDER BY el.sentDate DESC")
    List<EmailLog> findEmailsSentThisMonth(@Param("monthStart") LocalDateTime monthStart);
} 