package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.Client;
import com.realestate.sellerfunnel.repository.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class EmailCampaignService {
    private static final Logger logger = LoggerFactory.getLogger(EmailCampaignService.class);
    private static final int BATCH_SIZE = 50;
    private static final int RATE_LIMIT_DELAY = 1000; // 1 second delay between batches
    
    @Autowired
    private JavaMailSender emailSender;
    
    @Autowired
    private TemplateEngine templateEngine;
    
    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private AIContentGenerationService aiContentService;

    @Autowired
    private SettingsService settingsService;
    
    @Value("${app.email.sender-name:}")
    private String defaultSenderName;
    
    @Value("${app.email.sender-email:}")
    private String defaultSenderEmail;

    private String getSenderName() {
        var s = settingsService.getSettingsOrDefault();
        return (s.getEmailSenderName() != null && !s.getEmailSenderName().isEmpty()) ? s.getEmailSenderName() : (defaultSenderName != null && !defaultSenderName.isEmpty() ? defaultSenderName : "Real Estate Team");
    }

    private String getSenderEmail() {
        var s = settingsService.getSettingsOrDefault();
        return (s.getEmailSenderEmail() != null && !s.getEmailSenderEmail().isEmpty()) ? s.getEmailSenderEmail() : defaultSenderEmail;
    }
    
    private final Map<String, CampaignProgress> campaignProgress = new ConcurrentHashMap<>();
    
    public static class CampaignProgress {
        private final String campaignId;
        private final int totalRecipients;
        private int processedCount;
        private int successCount;
        private int errorCount;
        private int skippedCount;
        private String status; // PREPARING, PROCESSING, COMPLETED, FAILED
        private String errorMessage;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private List<String> recentErrors = new ArrayList<>();
        
        public CampaignProgress(String campaignId, int totalRecipients) {
            this.campaignId = campaignId;
            this.totalRecipients = totalRecipients;
            this.status = "PREPARING";
            this.startTime = LocalDateTime.now();
        }
        
        public void incrementSuccess() {
            successCount++;
            processedCount++;
        }
        
        public void incrementError(String error) {
            errorCount++;
            processedCount++;
            if (recentErrors.size() < 10) {
                recentErrors.add(error);
            }
        }
        
        public void incrementSkipped() {
            skippedCount++;
            processedCount++;
        }
        
        public double getProgress() {
            return totalRecipients == 0 ? 0 : (double) processedCount / totalRecipients * 100;
        }
        
        // Getters
        public String getCampaignId() { return campaignId; }
        public int getTotalRecipients() { return totalRecipients; }
        public int getProcessedCount() { return processedCount; }
        public int getSuccessCount() { return successCount; }
        public int getErrorCount() { return errorCount; }
        public int getSkippedCount() { return skippedCount; }
        public String getStatus() { return status; }
        public String getErrorMessage() { return errorMessage; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public List<String> getRecentErrors() { return recentErrors; }
        
        // Setters
        public void setStatus(String status) { this.status = status; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    }
    
    @Async
    public CompletableFuture<String> startEmailCampaign(
            String subject,
            String messageText,
            String clientType,
            String clientStatus,
            String leadSource,
            String city,
            String state,
            boolean useAI,
            boolean useTemplate,
            String templateName) {
        
        String campaignId = UUID.randomUUID().toString();
        
        CompletableFuture.runAsync(() -> {
            try {
                // Get filtered clients
                List<Client> clients = clientRepository.findByFilters(
                    clientType, clientStatus, leadSource, city, state);
                
                // Filter for email opted-in clients
                clients = clients.stream()
                    .filter(client -> Boolean.TRUE.equals(client.getEmailOptedIn()))
                    .filter(client -> client.getEmail() != null && !client.getEmail().trim().isEmpty())
                    .collect(Collectors.toList());
                
                CampaignProgress progress = new CampaignProgress(campaignId, clients.size());
                campaignProgress.put(campaignId, progress);
                
                if (clients.isEmpty()) {
                    progress.setStatus("COMPLETED");
                    progress.setEndTime(LocalDateTime.now());
                    return;
                }
                
                // Generate content using AI if requested
                String finalSubject = subject;
                String finalMessage = messageText;
                if (useAI) {
                    try {
                        if (subject.trim().isEmpty()) {
                            finalSubject = aiContentService.generateEmailSubject(messageText);
                        }
                        finalMessage = aiContentService.generateEmailContent(messageText);
                    } catch (Exception e) {
                        logger.warn("Failed to generate AI content, using original message: {}", e.getMessage());
                    }
                }
                
                progress.setStatus("PROCESSING");
                
                // Process clients in batches
                List<Client> currentBatch = new ArrayList<>();
                for (Client client : clients) {
                    currentBatch.add(client);
                    
                    if (currentBatch.size() >= BATCH_SIZE) {
                        processBatch(currentBatch, finalSubject, finalMessage, useTemplate, templateName, progress);
                        currentBatch = new ArrayList<>();
                        Thread.sleep(RATE_LIMIT_DELAY);
                    }
                }
                
                // Process remaining clients
                if (!currentBatch.isEmpty()) {
                    processBatch(currentBatch, finalSubject, finalMessage, useTemplate, templateName, progress);
                }
                
                // Save updated clients
                clientRepository.saveAll(clients);
                
                progress.setStatus("COMPLETED");
                progress.setEndTime(LocalDateTime.now());
                
            } catch (Exception e) {
                logger.error("Error processing email campaign {}: {}", campaignId, e.getMessage(), e);
                CampaignProgress progress = campaignProgress.get(campaignId);
                if (progress != null) {
                    progress.setStatus("FAILED");
                    progress.setErrorMessage(e.getMessage());
                    progress.setEndTime(LocalDateTime.now());
                }
            }
        });
        
        return CompletableFuture.completedFuture(campaignId);
    }
    
    private void processBatch(List<Client> batch, String subject, String messageText, 
                            boolean useTemplate, String templateName, CampaignProgress progress) {
        for (Client client : batch) {
            try {
                MimeMessage message = emailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                
                helper.setFrom(getSenderEmail(), getSenderName());
                helper.setTo(client.getEmail());
                helper.setSubject(subject);
                
                String content;
                if (useTemplate) {
                    Context context = new Context();
                    context.setVariable("client", client);
                    context.setVariable("message", messageText);
                    content = templateEngine.process(templateName, context);
                } else {
                    content = messageText;
                }
                
                helper.setText(content, true);
                
                emailSender.send(message);
                
                progress.incrementSuccess();
                client.incrementEmailContact();
                
            } catch (Exception e) {
                logger.error("Error sending email to {}: {}", client.getEmail(), e.getMessage());
                progress.incrementError("Failed to send to " + client.getEmail() + ": " + e.getMessage());
            }
        }
    }
    
    public CampaignProgress getCampaignProgress(String campaignId) {
        return campaignProgress.get(campaignId);
    }
    
    public void cleanupOldCampaigns() {
        // Remove completed campaigns older than 24 hours
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        campaignProgress.entrySet().removeIf(entry -> {
            CampaignProgress progress = entry.getValue();
            return progress.getEndTime() != null && progress.getEndTime().isBefore(cutoff);
        });
    }
}
