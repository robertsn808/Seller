package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.Client;
import com.realestate.sellerfunnel.model.EmailCampaign;
import com.realestate.sellerfunnel.model.EmailLog;
import com.realestate.sellerfunnel.repository.EmailLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private TemplateEngine templateEngine;
    
    @Autowired
    private EmailLogRepository emailLogRepository;
    
    @Value("${spring.mail.username}")
    private String defaultSenderEmail;
    
    @Value("${app.email.sender-name:Real Estate Team}")
    private String defaultSenderName;
    
    /**
     * Send a single email to a client
     */
    public boolean sendEmail(Client client, String subject, String content, String senderName, String senderEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(client.getEmail());
            helper.setSubject(subject);
            helper.setText(content, true); // true indicates HTML content
            helper.setFrom(senderEmail != null ? senderEmail : defaultSenderEmail, 
                          senderName != null ? senderName : defaultSenderName);
            
            mailSender.send(message);
            
            // Log the email
            EmailLog emailLog = new EmailLog();
            emailLog.setClient(client);
            emailLog.setRecipientEmail(client.getEmail());
            emailLog.setRecipientName(client.getFullName());
            emailLog.setSubjectLine(subject);
            emailLog.setContent(content);
            emailLog.setSenderEmail(senderEmail != null ? senderEmail : defaultSenderEmail);
            emailLog.setSenderName(senderName != null ? senderName : defaultSenderName);
            emailLog.setStatus("SENT");
            emailLog.setSentDate(LocalDateTime.now());
            emailLog.setTrackingId(generateTrackingId());
            
            emailLogRepository.save(emailLog);
            
            // Update client's last contact date
            client.setLastContactDate(LocalDateTime.now());
            
            return true;
        } catch (Exception e) {
            // Log failed email
            EmailLog emailLog = new EmailLog();
            emailLog.setClient(client);
            emailLog.setRecipientEmail(client.getEmail());
            emailLog.setRecipientName(client.getFullName());
            emailLog.setSubjectLine(subject);
            emailLog.setContent(content);
            emailLog.setSenderEmail(senderEmail != null ? senderEmail : defaultSenderEmail);
            emailLog.setSenderName(senderName != null ? senderName : defaultSenderName);
            emailLog.setStatus("FAILED");
            emailLog.setErrorMessage(e.getMessage());
            emailLog.setTrackingId(generateTrackingId());
            
            emailLogRepository.save(emailLog);
            
            return false;
        }
    }
    
    /**
     * Send email campaign to multiple clients
     */
    public EmailCampaignResult sendEmailCampaign(EmailCampaign campaign, List<Client> clients) {
        EmailCampaignResult result = new EmailCampaignResult();
        result.setTotalRecipients(clients.size());
        
        for (Client client : clients) {
            if (!client.getEmailOptedIn() || !client.getIsActive()) {
                result.incrementSkipped();
                continue;
            }
            
            try {
                // Personalize content for the client
                String personalizedContent = personalizeContent(campaign.getContent(), client);
                String personalizedSubject = personalizeContent(campaign.getSubjectLine(), client);
                
                boolean sent = sendEmail(client, personalizedSubject, personalizedContent, 
                                       campaign.getSenderName(), campaign.getSenderEmail());
                
                if (sent) {
                    result.incrementSent();
                } else {
                    result.incrementFailed();
                }
            } catch (Exception e) {
                result.incrementFailed();
            }
        }
        
        // Update campaign statistics
        campaign.setSentDate(LocalDateTime.now());
        campaign.setStatus("SENT");
        campaign.setTotalRecipients(result.getTotalRecipients());
        campaign.setSentCount(result.getSentCount());
        
        return result;
    }
    
    /**
     * Send email using Thymeleaf template
     */
    public boolean sendTemplatedEmail(Client client, String subject, String templateName, 
                                    Map<String, Object> variables, String senderName, String senderEmail) {
        try {
            Context context = new Context();
            if (variables != null) {
                for (Map.Entry<String, Object> entry : variables.entrySet()) {
                    context.setVariable(entry.getKey(), entry.getValue());
                }
            }
            
            // Add client data to context
            context.setVariable("client", client);
            context.setVariable("firstName", client.getFirstName());
            context.setVariable("lastName", client.getLastName());
            context.setVariable("fullName", client.getFullName());
            context.setVariable("email", client.getEmail());
            context.setVariable("company", client.getCompanyName());
            
            String content = templateEngine.process(templateName, context);
            
            return sendEmail(client, subject, content, senderName, senderEmail);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Send bulk emails with rate limiting
     */
    public EmailCampaignResult sendBulkEmails(List<Client> clients, String subject, String content, 
                                            String senderName, String senderEmail, int delayMs) {
        EmailCampaignResult result = new EmailCampaignResult();
        result.setTotalRecipients(clients.size());
        
        for (Client client : clients) {
            if (!client.getEmailOptedIn() || !client.getIsActive()) {
                result.incrementSkipped();
                continue;
            }
            
            try {
                String personalizedContent = personalizeContent(content, client);
                String personalizedSubject = personalizeContent(subject, client);
                
                boolean sent = sendEmail(client, personalizedSubject, personalizedContent, 
                                       senderName, senderEmail);
                
                if (sent) {
                    result.incrementSent();
                } else {
                    result.incrementFailed();
                }
                
                // Rate limiting
                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
            } catch (Exception e) {
                result.incrementFailed();
            }
        }
        
        return result;
    }
    
    /**
     * Personalize content with client information
     */
    private String personalizeContent(String content, Client client) {
        if (content == null) return "";
        
        return content
            .replace("{{firstName}}", client.getFirstName() != null ? client.getFirstName() : "")
            .replace("{{lastName}}", client.getLastName() != null ? client.getLastName() : "")
            .replace("{{fullName}}", client.getFullName())
            .replace("{{email}}", client.getEmail())
            .replace("{{company}}", client.getCompanyName() != null ? client.getCompanyName() : "")
            .replace("{{city}}", client.getCity() != null ? client.getCity() : "")
            .replace("{{state}}", client.getState() != null ? client.getState() : "")
            .replace("{{phone}}", client.getPhoneNumber() != null ? client.getPhoneNumber() : "");
    }
    
    /**
     * Generate unique tracking ID
     */
    private String generateTrackingId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
    
    /**
     * Update email status (for webhook callbacks)
     */
    public void updateEmailStatus(String trackingId, String status) {
        emailLogRepository.findByTrackingId(trackingId).ifPresent(emailLog -> {
            emailLog.setStatus(status);
            
            switch (status) {
                case "DELIVERED":
                    emailLog.setDeliveredDate(LocalDateTime.now());
                    break;
                case "OPENED":
                    emailLog.setOpenedDate(LocalDateTime.now());
                    break;
                case "CLICKED":
                    emailLog.setClickedDate(LocalDateTime.now());
                    break;
                case "BOUNCED":
                    emailLog.setBouncedDate(LocalDateTime.now());
                    break;
            }
            
            emailLogRepository.save(emailLog);
        });
    }
    
    /**
     * Test email configuration
     */
    public boolean testEmailConfiguration(String testEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(testEmail);
            helper.setSubject("Email Configuration Test");
            helper.setText("<h2>Email Configuration Test</h2><p>If you receive this email, your email configuration is working correctly.</p>", true);
            helper.setFrom(defaultSenderEmail, defaultSenderName);
            
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Result class for email campaign operations
     */
    public static class EmailCampaignResult {
        private int totalRecipients;
        private int sentCount;
        private int failedCount;
        private int skippedCount;
        
        public void incrementSent() { sentCount++; }
        public void incrementFailed() { failedCount++; }
        public void incrementSkipped() { skippedCount++; }
        
        // Getters and Setters
        public int getTotalRecipients() { return totalRecipients; }
        public void setTotalRecipients(int totalRecipients) { this.totalRecipients = totalRecipients; }
        
        public int getSentCount() { return sentCount; }
        public void setSentCount(int sentCount) { this.sentCount = sentCount; }
        
        public int getFailedCount() { return failedCount; }
        public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
        
        public int getSkippedCount() { return skippedCount; }
        public void setSkippedCount(int skippedCount) { this.skippedCount = skippedCount; }
        
        public double getSuccessRate() {
            if (totalRecipients == 0) return 0.0;
            return (double) sentCount / totalRecipients * 100;
        }
    }
} 