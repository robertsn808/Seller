package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.Client;
import com.realestate.sellerfunnel.repository.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SMSCampaignService {
    private static final Logger logger = LoggerFactory.getLogger(SMSCampaignService.class);
    
    @Autowired
    private SMSService smsService;
    
    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private AIContentGenerationService aiContentService;
    
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
    public CompletableFuture<String> startBulkSMSCampaign(
            String messageText,
            String clientType,
            String clientStatus,
            String leadSource,
            String city,
            String state,
            boolean useAI) {
        
        String campaignId = UUID.randomUUID().toString();
        
        CompletableFuture.runAsync(() -> {
            try {
                // Get filtered clients
                List<Client> clients = clientRepository.findByFilters(
                    clientType, clientStatus, leadSource, city, state);
                
                // Filter for SMS opted-in clients
                clients = clients.stream()
                    .filter(client -> Boolean.TRUE.equals(client.getSmsOptedIn()))
                    .filter(client -> client.getPhoneNumber() != null && !client.getPhoneNumber().trim().isEmpty())
                    .collect(Collectors.toList());
                
                CampaignProgress progress = new CampaignProgress(campaignId, clients.size());
                campaignProgress.put(campaignId, progress);
                
                if (clients.isEmpty()) {
                    progress.setStatus("COMPLETED");
                    progress.setEndTime(LocalDateTime.now());
                    return;
                }
                
                // Generate message using AI if requested
                String finalMessage = messageText;
                if (useAI) {
                    try {
                        finalMessage = aiContentService.generateSMSContent(messageText);
                    } catch (Exception e) {
                        logger.warn("Failed to generate AI content, using original message: {}", e.getMessage());
                    }
                }
                
                progress.setStatus("PROCESSING");
                
                // Send messages
                SMSService.BatchSMSResult result = smsService.sendBulkSMS(clients, finalMessage).get();
                
                // Update progress
                result.getSuccesses().forEach(r -> progress.incrementSuccess());
                result.getErrors().forEach(r -> progress.incrementError(r.getMessage()));
                result.getSkipped().forEach(r -> {
                    progress.incrementSkipped();
                    progress.recentErrors.add("Skipped " + r.getClientName() + ": " + r.getMessage());
                });
                result.getInvalid().forEach(r -> {
                    progress.incrementError("Invalid: " + r.getMessage());
                    progress.recentErrors.add("Invalid " + r.getClientName() + ": " + r.getMessage());
                });
                
                // Save updated clients
                clientRepository.saveAll(clients);
                
                progress.setStatus("COMPLETED");
                progress.setEndTime(LocalDateTime.now());
                
            } catch (Exception e) {
                logger.error("Error processing SMS campaign {}: {}", campaignId, e.getMessage(), e);
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
