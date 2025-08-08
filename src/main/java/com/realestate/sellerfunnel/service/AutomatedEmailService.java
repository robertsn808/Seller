package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.EmailCampaign;
import com.realestate.sellerfunnel.model.Client;
import com.realestate.sellerfunnel.model.AIGeneratedContent;
import com.realestate.sellerfunnel.repository.EmailCampaignRepository;
import com.realestate.sellerfunnel.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class AutomatedEmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(AutomatedEmailService.class);
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private AIContentGenerationService aiContentGenerationService;
    
    @Autowired
    private EmailCampaignRepository emailCampaignRepository;
    
    @Autowired
    private ClientRepository clientRepository;
    
    /**
     * Schedule an email campaign with AI-generated content
     */
    public EmailCampaign scheduleAutomatedEmail(String campaignName, 
                                               String targetAudience,
                                               String clientTypeFilter,
                                               String leadSourceFilter,
                                               LocalDateTime scheduledDate,
                                               String aiPrompt,
                                               String contentType,
                                               String category,
                                               String context) {
        try {
            // Generate AI content for subject and body
            AIGeneratedContent subjectContent = aiContentGenerationService.generateContent(
                aiPrompt + " Create a compelling email subject line for " + targetAudience,
                "EMAIL_SUBJECT",
                targetAudience,
                category,
                context,
                3
            );
            
            AIGeneratedContent bodyContent = aiContentGenerationService.generateContent(
                aiPrompt + " Create an engaging email body for " + targetAudience,
                "EMAIL_BODY",
                targetAudience,
                category,
                context,
                3
            );
            
            // Create the email campaign
            EmailCampaign campaign = new EmailCampaign();
            campaign.setCampaignName(campaignName);
            campaign.setSubjectLine(subjectContent.getContent());
            campaign.setContent(bodyContent.getContent());
            campaign.setTargetAudience(targetAudience);
            campaign.setClientTypeFilter(clientTypeFilter);
            campaign.setLeadSourceFilter(leadSourceFilter);
            campaign.setIsScheduled(true);
            campaign.setScheduledDate(scheduledDate);
            campaign.setStatus("SCHEDULED");
            campaign.setNotes("AI-generated content. Subject ID: " + subjectContent.getId() + 
                            ", Body ID: " + bodyContent.getId());
            
            // Get estimated recipient count
            List<Client> targetClients = getTargetClients(targetAudience, clientTypeFilter, leadSourceFilter);
            campaign.setTotalRecipients(targetClients.size());
            
            return emailCampaignRepository.save(campaign);
            
        } catch (Exception e) {
            logger.error("Error scheduling automated email: {}", e.getMessage());
            throw new RuntimeException("Failed to schedule automated email", e);
        }
    }
    
    /**
     * Schedule a follow-up email sequence
     */
    public List<EmailCampaign> scheduleFollowUpSequence(String baseCampaignName,
                                                       String targetAudience,
                                                       String clientTypeFilter,
                                                       String leadSourceFilter,
                                                       LocalDateTime startDate,
                                                       String aiPrompt,
                                                       String contentType,
                                                       String category,
                                                       int numberOfEmails,
                                                       int daysBetweenEmails) {
        List<EmailCampaign> campaigns = new java.util.ArrayList<>();
        
        for (int i = 1; i <= numberOfEmails; i++) {
            LocalDateTime emailDate = startDate.plusDays((i - 1) * daysBetweenEmails);
            String campaignName = baseCampaignName + " - Follow-up " + i;
            String followUpPrompt = aiPrompt + " This is follow-up email " + i + " of " + numberOfEmails + 
                                  ". Make it engaging and encourage action.";
            
            EmailCampaign campaign = scheduleAutomatedEmail(
                campaignName, targetAudience, clientTypeFilter, leadSourceFilter,
                emailDate, followUpPrompt, contentType, category, "follow_up_sequence"
            );
            
            campaigns.add(campaign);
        }
        
        return campaigns;
    }
    
    /**
     * Schedule a welcome email sequence for new clients
     */
    public List<EmailCampaign> scheduleWelcomeSequence(String targetAudience,
                                                      String clientTypeFilter,
                                                      String leadSourceFilter,
                                                      String aiPrompt,
                                                      String category) {
        LocalDateTime startDate = LocalDateTime.now().plusHours(1); // Start in 1 hour
        
        return scheduleFollowUpSequence(
            "Welcome Series",
            targetAudience,
            clientTypeFilter,
            leadSourceFilter,
            startDate,
            aiPrompt + " Create a welcome email for new clients",
            "WELCOME_EMAIL",
            category,
            3, // 3 welcome emails
            2  // 2 days apart
        );
    }
    
    /**
     * Schedule a re-engagement campaign for inactive clients
     */
    public EmailCampaign scheduleReEngagementCampaign(String targetAudience,
                                                     String clientTypeFilter,
                                                     String leadSourceFilter,
                                                     LocalDateTime scheduledDate,
                                                     String aiPrompt,
                                                     String category) {
        return scheduleAutomatedEmail(
            "Re-engagement Campaign",
            targetAudience,
            clientTypeFilter,
            leadSourceFilter,
            scheduledDate,
            aiPrompt + " Create a re-engagement email for inactive clients. Make it compelling and offer value.",
            "RE_ENGAGEMENT_EMAIL",
            category,
            "inactive_clients"
        );
    }
    
    /**
     * Schedule a seasonal campaign
     */
    public EmailCampaign scheduleSeasonalCampaign(String season,
                                                 String targetAudience,
                                                 String clientTypeFilter,
                                                 String leadSourceFilter,
                                                 LocalDateTime scheduledDate,
                                                 String aiPrompt,
                                                 String category) {
        return scheduleAutomatedEmail(
            season + " Campaign",
            targetAudience,
            clientTypeFilter,
            leadSourceFilter,
            scheduledDate,
            aiPrompt + " Create a seasonal email for " + season + ". Make it relevant and timely.",
            "SEASONAL_EMAIL",
            category,
            season.toLowerCase()
        );
    }
    
    /**
     * Scheduled task to process automated emails
     * Runs every minute to check for scheduled emails
     */
    @Scheduled(fixedRate = 60000) // Every minute
    public void processScheduledEmails() {
        try {
            List<EmailCampaign> scheduledCampaigns = emailCampaignRepository
                .findByStatusOrderByCreatedAtDesc("SCHEDULED");
            // Filter for campaigns that should be sent now
            scheduledCampaigns = scheduledCampaigns.stream()
                .filter(c -> c.getScheduledDate() != null && c.getScheduledDate().isBefore(LocalDateTime.now()))
                .collect(java.util.stream.Collectors.toList());
            
            for (EmailCampaign campaign : scheduledCampaigns) {
                processScheduledCampaign(campaign);
            }
        } catch (Exception e) {
            logger.error("Error processing scheduled emails: {}", e.getMessage());
        }
    }
    
    /**
     * Process a single scheduled campaign
     */
    private void processScheduledCampaign(EmailCampaign campaign) {
        try {
            logger.info("Processing scheduled campaign: {}", campaign.getCampaignName());
            
            // Update status to sending
            campaign.setStatus("SENDING");
            emailCampaignRepository.save(campaign);
            
            // Get target clients
            List<Client> targetClients = getTargetClients(
                campaign.getTargetAudience(),
                campaign.getClientTypeFilter(),
                campaign.getLeadSourceFilter()
            );
            
            // Send the campaign
            EmailService.EmailCampaignResult result = emailService.sendEmailCampaign(campaign, targetClients);
            
            // Update campaign with results
            campaign.setStatus("SENT");
            campaign.setSentDate(LocalDateTime.now());
            campaign.setSentCount(result.getSentCount());
            campaign.setTotalRecipients(targetClients.size());
            
            emailCampaignRepository.save(campaign);
            
            logger.info("Campaign {} sent successfully. Sent: {}, Failed: {}, Skipped: {}", 
                campaign.getCampaignName(), result.getSentCount(), result.getFailedCount(), result.getSkippedCount());
            
        } catch (Exception e) {
            logger.error("Error processing campaign {}: {}", campaign.getCampaignName(), e.getMessage());
            
            // Update campaign status to failed
            campaign.setStatus("FAILED");
            campaign.setNotes("Failed to send: " + e.getMessage());
            emailCampaignRepository.save(campaign);
        }
    }
    
    /**
     * Get target clients based on filters
     */
    public List<Client> getTargetClients(String targetAudience, String clientTypeFilter, String leadSourceFilter) {
        List<Client> clients = new java.util.ArrayList<>();
        
        // Get all active clients
        List<Client> allClients = clientRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        
        for (Client client : allClients) {
            // Check if client matches target audience
            if (targetAudience != null && !targetAudience.isEmpty()) {
                if (!targetAudience.equals("ALL") && !targetAudience.equals(client.getClientType())) {
                    continue;
                }
            }
            
            // Check client type filter
            if (clientTypeFilter != null && !clientTypeFilter.isEmpty()) {
                if (!clientTypeFilter.equals(client.getClientType())) {
                    continue;
                }
            }
            
            // Check lead source filter
            if (leadSourceFilter != null && !leadSourceFilter.isEmpty()) {
                if (!leadSourceFilter.equals(client.getLeadSource())) {
                    continue;
                }
            }
            
            // Check if client has opted in for emails
            if (client.getEmailOptedIn() != null && client.getEmailOptedIn()) {
                clients.add(client);
            }
        }
        
        return clients;
    }
    
    /**
     * Get campaign statistics
     */
    public Map<String, Object> getCampaignStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<EmailCampaign> allCampaigns = emailCampaignRepository.findAll();
        
        stats.put("totalCampaigns", allCampaigns.size());
        stats.put("scheduledCampaigns", allCampaigns.stream()
            .filter(c -> "SCHEDULED".equals(c.getStatus())).count());
        stats.put("sentCampaigns", allCampaigns.stream()
            .filter(c -> "SENT".equals(c.getStatus())).count());
        stats.put("failedCampaigns", allCampaigns.stream()
            .filter(c -> "FAILED".equals(c.getStatus())).count());
        
        // Calculate average open rate
        double avgOpenRate = allCampaigns.stream()
            .filter(c -> c.getSentCount() != null && c.getSentCount() > 0)
            .mapToDouble(EmailCampaign::getOpenRate)
            .average()
            .orElse(0.0);
        
        stats.put("averageOpenRate", avgOpenRate);
        
        return stats;
    }
    
    /**
     * Cancel a scheduled campaign
     */
    public boolean cancelScheduledCampaign(Long campaignId) {
        try {
            EmailCampaign campaign = emailCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
            
            if ("SCHEDULED".equals(campaign.getStatus())) {
                campaign.setStatus("CANCELLED");
                emailCampaignRepository.save(campaign);
                logger.info("Campaign {} cancelled successfully", campaign.getCampaignName());
                return true;
            } else {
                logger.warn("Cannot cancel campaign {} - status is {}", campaign.getCampaignName(), campaign.getStatus());
                return false;
            }
        } catch (Exception e) {
            logger.error("Error cancelling campaign {}: {}", campaignId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Reschedule a campaign
     */
    public boolean rescheduleCampaign(Long campaignId, LocalDateTime newScheduledDate) {
        try {
            EmailCampaign campaign = emailCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
            
            if ("SCHEDULED".equals(campaign.getStatus()) || "CANCELLED".equals(campaign.getStatus())) {
                campaign.setScheduledDate(newScheduledDate);
                campaign.setStatus("SCHEDULED");
                emailCampaignRepository.save(campaign);
                logger.info("Campaign {} rescheduled to {}", campaign.getCampaignName(), newScheduledDate);
                return true;
            } else {
                logger.warn("Cannot reschedule campaign {} - status is {}", campaign.getCampaignName(), campaign.getStatus());
                return false;
            }
        } catch (Exception e) {
            logger.error("Error rescheduling campaign {}: {}", campaignId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Send a single email to all opted-in clients with AI-generated content
     */
    public EmailCampaign sendBulkEmail(String campaignName,
                                      String aiPrompt,
                                      String contentType,
                                      String category,
                                      String context) {
        try {
            // Generate AI content for subject and body
            AIGeneratedContent subjectContent = aiContentGenerationService.generateContent(
                aiPrompt + " Create a compelling email subject line for a bulk email campaign",
                "EMAIL_SUBJECT",
                "ALL",
                category,
                context,
                3
            );
            
            AIGeneratedContent bodyContent = aiContentGenerationService.generateContent(
                aiPrompt + " Create an engaging email body for a bulk email campaign to all clients",
                "EMAIL_BODY",
                "ALL",
                category,
                context,
                3
            );
            
            // Get all opted-in clients
            List<Client> targetClients = getTargetClients("ALL", null, null);
            
            // Create the email campaign
            EmailCampaign campaign = new EmailCampaign();
            campaign.setCampaignName(campaignName);
            campaign.setSubjectLine(subjectContent.getContent());
            campaign.setContent(bodyContent.getContent());
            campaign.setTargetAudience("ALL");
            campaign.setIsScheduled(false);
            campaign.setStatus("SENDING");
            campaign.setNotes("Bulk email with AI-generated content. Subject ID: " + subjectContent.getId() + 
                            ", Body ID: " + bodyContent.getId() + ". Sent to " + targetClients.size() + " recipients.");
            campaign.setTotalRecipients(targetClients.size());
            
            // Save campaign first
            campaign = emailCampaignRepository.save(campaign);
            
            // Send the campaign immediately
            EmailService.EmailCampaignResult result = emailService.sendEmailCampaign(campaign, targetClients);
            
            // Update campaign with results
            campaign.setStatus("SENT");
            campaign.setSentDate(LocalDateTime.now());
            campaign.setSentCount(result.getSentCount());
            
            emailCampaignRepository.save(campaign);
            
            logger.info("Bulk email campaign '{}' sent successfully. Sent: {}, Failed: {}, Skipped: {}", 
                campaignName, result.getSentCount(), result.getFailedCount(), result.getSkippedCount());
            
            return campaign;
            
        } catch (Exception e) {
            logger.error("Error sending bulk email: {}", e.getMessage());
            throw new RuntimeException("Failed to send bulk email", e);
        }
    }
    
    /**
     * Send a single email to filtered clients with AI-generated content
     */
    public EmailCampaign sendFilteredBulkEmail(String campaignName,
                                              String targetAudience,
                                              String clientTypeFilter,
                                              String leadSourceFilter,
                                              String aiPrompt,
                                              String contentType,
                                              String category,
                                              String context) {
        try {
            // Generate AI content for subject and body
            AIGeneratedContent subjectContent = aiContentGenerationService.generateContent(
                aiPrompt + " Create a compelling email subject line for " + targetAudience,
                "EMAIL_SUBJECT",
                targetAudience,
                category,
                context,
                3
            );
            
            AIGeneratedContent bodyContent = aiContentGenerationService.generateContent(
                aiPrompt + " Create an engaging email body for " + targetAudience,
                "EMAIL_BODY",
                targetAudience,
                category,
                context,
                3
            );
            
            // Get filtered clients
            List<Client> targetClients = getTargetClients(targetAudience, clientTypeFilter, leadSourceFilter);
            
            // Create the email campaign
            EmailCampaign campaign = new EmailCampaign();
            campaign.setCampaignName(campaignName);
            campaign.setSubjectLine(subjectContent.getContent());
            campaign.setContent(bodyContent.getContent());
            campaign.setTargetAudience(targetAudience);
            campaign.setClientTypeFilter(clientTypeFilter);
            campaign.setLeadSourceFilter(leadSourceFilter);
            campaign.setIsScheduled(false);
            campaign.setStatus("SENDING");
            campaign.setNotes("Filtered bulk email with AI-generated content. Subject ID: " + subjectContent.getId() + 
                            ", Body ID: " + bodyContent.getId() + ". Sent to " + targetClients.size() + " recipients.");
            campaign.setTotalRecipients(targetClients.size());
            
            // Save campaign first
            campaign = emailCampaignRepository.save(campaign);
            
            // Send the campaign immediately
            EmailService.EmailCampaignResult result = emailService.sendEmailCampaign(campaign, targetClients);
            
            // Update campaign with results
            campaign.setStatus("SENT");
            campaign.setSentDate(LocalDateTime.now());
            campaign.setSentCount(result.getSentCount());
            
            emailCampaignRepository.save(campaign);
            
            logger.info("Filtered bulk email campaign '{}' sent successfully. Sent: {}, Failed: {}, Skipped: {}", 
                campaignName, result.getSentCount(), result.getFailedCount(), result.getSkippedCount());
            
            return campaign;
            
        } catch (Exception e) {
            logger.error("Error sending filtered bulk email: {}", e.getMessage());
            throw new RuntimeException("Failed to send filtered bulk email", e);
        }
    }
    
    /**
     * Get email list statistics
     */
    public Map<String, Object> getEmailListStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<Client> allClients = clientRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        List<Client> optedInClients = allClients.stream()
            .filter(c -> c.getEmailOptedIn() != null && c.getEmailOptedIn())
            .collect(java.util.stream.Collectors.toList());
        
        stats.put("totalClients", allClients.size());
        stats.put("optedInClients", optedInClients.size());
        stats.put("optInRate", allClients.size() > 0 ? (double) optedInClients.size() / allClients.size() * 100 : 0.0);
        
        // Breakdown by client type
        Map<String, Long> clientTypeBreakdown = optedInClients.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                c -> c.getClientType() != null ? c.getClientType() : "Unknown",
                java.util.stream.Collectors.counting()
            ));
        stats.put("clientTypeBreakdown", clientTypeBreakdown);
        
        // Breakdown by lead source
        Map<String, Long> leadSourceBreakdown = optedInClients.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                c -> c.getLeadSource() != null ? c.getLeadSource() : "Unknown",
                java.util.stream.Collectors.counting()
            ));
        stats.put("leadSourceBreakdown", leadSourceBreakdown);
        
        return stats;
    }
    
    /**
     * Get email list for export
     */
    public List<Map<String, Object>> getEmailListForExport() {
        List<Client> optedInClients = clientRepository.findByIsActiveTrueOrderByCreatedAtDesc().stream()
            .filter(c -> c.getEmailOptedIn() != null && c.getEmailOptedIn())
            .collect(java.util.stream.Collectors.toList());
        
        return optedInClients.stream().map(client -> {
            Map<String, Object> clientData = new HashMap<>();
            clientData.put("email", client.getEmail());
            clientData.put("firstName", client.getFirstName());
            clientData.put("lastName", client.getLastName());
            clientData.put("clientType", client.getClientType());
            clientData.put("leadSource", client.getLeadSource());
            clientData.put("createdAt", client.getCreatedAt());
            clientData.put("lastContactDate", client.getLastContactDate());
            return clientData;
        }).collect(java.util.stream.Collectors.toList());
    }
}
