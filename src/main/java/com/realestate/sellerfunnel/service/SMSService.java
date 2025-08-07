package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.Client;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class SMSService {
    private static final Logger logger = LoggerFactory.getLogger(SMSService.class);
    private static final int BATCH_SIZE = 50; // Twilio's recommended batch size
    private static final int RATE_LIMIT_DELAY = 1000; // 1 second delay between batches
    
    @Value("${twilio.account-sid}")
    private String accountSid;
    
    @Value("${twilio.auth-token}")
    private String authToken;
    
    @Value("${twilio.phone-number}")
    private String fromPhoneNumber;
    
    @Value("${twilio.messaging-service-sid:}")
    private String messagingServiceSid;
    
    @Value("${twilio.status-callback-url:}")
    private String statusCallbackUrl;
    
    @PostConstruct
    public void init() {
        if (accountSid != null && !accountSid.isEmpty() && authToken != null && !authToken.isEmpty()) {
            Twilio.init(accountSid, authToken);
            logger.info("Twilio client initialized successfully");
        } else {
            logger.warn("Twilio credentials not configured. SMS functionality will be disabled.");
        }
    }
    
    public boolean isConfigured() {
        return accountSid != null && !accountSid.isEmpty() && 
               authToken != null && !authToken.isEmpty() && 
               (fromPhoneNumber != null && !fromPhoneNumber.isEmpty() || 
                messagingServiceSid != null && !messagingServiceSid.isEmpty());
    }
    
    public CompletableFuture<BatchSMSResult> sendBulkSMS(List<Client> clients, String messageText) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConfigured()) {
                throw new IllegalStateException("Twilio is not configured");
            }
            
            BatchSMSResult result = new BatchSMSResult();
            List<Client> currentBatch = new ArrayList<>();
            
            for (Client client : clients) {
                if (!isValidPhoneNumber(client.getPhoneNumber())) {
                    result.addInvalid(client, "Invalid phone number format");
                    continue;
                }
                
                if (!Boolean.TRUE.equals(client.getSmsOptedIn())) {
                    result.addSkipped(client, "Client has not opted in for SMS");
                    continue;
                }
                
                currentBatch.add(client);
                
                if (currentBatch.size() >= BATCH_SIZE) {
                    processBatch(currentBatch, messageText, result);
                    currentBatch = new ArrayList<>();
                    try {
                        Thread.sleep(RATE_LIMIT_DELAY);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("SMS sending interrupted", e);
                    }
                }
            }
            
            // Process remaining clients
            if (!currentBatch.isEmpty()) {
                processBatch(currentBatch, messageText, result);
            }
            
            return result;
        });
    }
    
    private void processBatch(List<Client> batch, String messageText, BatchSMSResult result) {
        for (Client client : batch) {
            try {
                Message.creator(
                    new PhoneNumber(client.getPhoneNumber()),
                    messagingServiceSid != null && !messagingServiceSid.isEmpty() ?
                        new com.twilio.type.MessagingServiceSid(messagingServiceSid) :
                        new PhoneNumber(fromPhoneNumber),
                    messageText
                )
                .setStatusCallback(statusCallbackUrl != null && !statusCallbackUrl.isEmpty() ?
                    statusCallbackUrl : null)
                .create();
                
                result.addSuccess(client);
                client.incrementSmsContact();
                
            } catch (Exception e) {
                logger.error("Error sending SMS to {}: {}", client.getPhoneNumber(), e.getMessage());
                result.addError(client, e.getMessage());
            }
        }
    }
    
    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        // Remove all non-numeric characters
        String cleanNumber = phoneNumber.replaceAll("[^0-9+]", "");
        
        // Check if it's a valid E.164 format or at least has enough digits
        return cleanNumber.matches("\\+?[1-9]\\d{10,14}");
    }
    
    public static class BatchSMSResult {
        private List<SMSResult> successes = new ArrayList<>();
        private List<SMSResult> errors = new ArrayList<>();
        private List<SMSResult> skipped = new ArrayList<>();
        private List<SMSResult> invalid = new ArrayList<>();
        
        public void addSuccess(Client client) {
            successes.add(new SMSResult(client));
        }
        
        public void addError(Client client, String error) {
            errors.add(new SMSResult(client, error));
        }
        
        public void addSkipped(Client client, String reason) {
            skipped.add(new SMSResult(client, reason));
        }
        
        public void addInvalid(Client client, String reason) {
            invalid.add(new SMSResult(client, reason));
        }
        
        public List<SMSResult> getSuccesses() { return successes; }
        public List<SMSResult> getErrors() { return errors; }
        public List<SMSResult> getSkipped() { return skipped; }
        public List<SMSResult> getInvalid() { return invalid; }
        
        public int getSuccessCount() { return successes.size(); }
        public int getErrorCount() { return errors.size(); }
        public int getSkippedCount() { return skipped.size(); }
        public int getInvalidCount() { return invalid.size(); }
        public int getTotalProcessed() { return successes.size() + errors.size() + skipped.size() + invalid.size(); }
    }
    
    public static class SMSResult {
        private final String clientName;
        private final String phoneNumber;
        private final String message;
        
        public SMSResult(Client client) {
            this.clientName = client.getFullName();
            this.phoneNumber = client.getPhoneNumber();
            this.message = null;
        }
        
        public SMSResult(Client client, String message) {
            this.clientName = client.getFullName();
            this.phoneNumber = client.getPhoneNumber();
            this.message = message;
        }
        
        public String getClientName() { return clientName; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getMessage() { return message; }
    }
}
