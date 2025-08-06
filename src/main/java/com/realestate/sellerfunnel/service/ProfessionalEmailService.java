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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.InternetAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProfessionalEmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfessionalEmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private TemplateEngine templateEngine;
    
    @Autowired
    private EmailLogRepository emailLogRepository;
    
    @Value("${app.email.sender-email}")
    private String defaultSenderEmail;
    
    @Value("${app.email.sender-name}")
    private String defaultSenderName;
    
    @Value("${app.email.from-domain:}")
    private String fromDomain;
    
    @Value("${app.email.reply-to:}")
    private String replyToEmail;
    
    @Value("${app.email.dkim.enabled:false}")
    private boolean dkimEnabled;
    
    @Value("${app.email.dkim.domain:}")
    private String dkimDomain;
    
    @Value("${app.email.dkim.selector:default}")
    private String dkimSelector;
    
    /**
     * Send professional email with proper headers and DKIM support
     */
    public boolean sendProfessionalEmail(Client client, String subject, String htmlContent, 
                                       String senderName, String senderEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Set recipient
            helper.setTo(new InternetAddress(client.getEmail(), client.getFirstName() + " " + client.getLastName()));
            
            // Set sender with proper from domain if configured
            String fromEmail = (senderEmail != null && !senderEmail.isEmpty()) ? senderEmail : defaultSenderEmail;
            String displayName = (senderName != null && !senderName.isEmpty()) ? senderName : defaultSenderName;
            
            if (fromDomain != null && !fromDomain.isEmpty() && !fromEmail.contains("@" + fromDomain)) {
                // Use domain-based from address for better deliverability
                String localPart = fromEmail.split("@")[0];
                fromEmail = localPart + "@" + fromDomain;
            }
            
            helper.setFrom(new InternetAddress(fromEmail, displayName));
            
            // Set reply-to if configured
            if (replyToEmail != null && !replyToEmail.isEmpty()) {
                helper.setReplyTo(replyToEmail);
            }
            
            // Set subject
            helper.setSubject(subject);
            
            // Set HTML content
            helper.setText(htmlContent, true);
            
            // Add professional headers
            message.addHeader("X-Mailer", "Real Estate Connect CRM");
            message.addHeader("X-Priority", "3");
            message.addHeader("Importance", "Normal");
            message.addHeader("List-Unsubscribe", "<mailto:unsubscribe@" + (fromDomain.isEmpty() ? "yourdomain.com" : fromDomain) + ">");
            
            // Add DKIM headers if enabled
            if (dkimEnabled && dkimDomain != null && !dkimDomain.isEmpty()) {
                message.addHeader("X-DKIM-Domain", dkimDomain);
                message.addHeader("X-DKIM-Selector", dkimSelector);
            }
            
            // Send the email
            mailSender.send(message);
            
            // Log the email
            logEmailSent(client, subject, htmlContent, "SUCCESS", null);
            
            logger.info("Professional email sent successfully to: {}", client.getEmail());
            return true;
            
        } catch (Exception e) {
            logger.error("Error sending professional email to {}: {}", client.getEmail(), e.getMessage());
            logEmailSent(client, subject, htmlContent, "FAILED", e.getMessage());
            return false;
        }
    }
    
    /**
     * Send campaign email with tracking and personalization
     */
    public boolean sendCampaignEmail(EmailCampaign campaign, Client client) {
        try {
            // Personalize content
            String personalizedContent = personalizeEmailContent(campaign.getHtmlContent(), client);
            String personalizedSubject = personalizeEmailContent(campaign.getSubject(), client);
            
            // Add campaign tracking
            String trackingId = UUID.randomUUID().toString();
            String contentWithTracking = addEmailTracking(personalizedContent, campaign.getId(), client.getId(), trackingId);
            
            return sendProfessionalEmail(client, personalizedSubject, contentWithTracking, 
                                       campaign.getSenderName(), campaign.getSenderEmail());
            
        } catch (Exception e) {
            logger.error("Error sending campaign email to {}: {}", client.getEmail(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Send bulk campaign emails
     */
    public EmailCampaignResult sendBulkCampaign(EmailCampaign campaign, List<Client> recipients) {
        EmailCampaignResult result = new EmailCampaignResult();
        result.setCampaignId(campaign.getId());
        result.setTotalRecipients(recipients.size());
        
        int successCount = 0;
        int failureCount = 0;
        
        for (Client client : recipients) {
            if (client.getEmailOptedIn() && client.getIsActive()) {
                if (sendCampaignEmail(campaign, client)) {
                    successCount++;
                } else {
                    failureCount++;
                }
                
                // Small delay to avoid overwhelming SMTP server
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                logger.debug("Skipping client {} - not opted in or inactive", client.getEmail());
            }
        }
        
        result.setSuccessfulSends(successCount);
        result.setFailedSends(failureCount);
        result.setCompletedAt(LocalDateTime.now());
        
        logger.info("Bulk campaign sent: {} successful, {} failed out of {} total", 
                   successCount, failureCount, recipients.size());
        
        return result;
    }
    
    /**
     * Create professional email templates
     */
    public String generateWelcomeEmail(Client client) {
        Context context = new Context();
        context.setVariable("client", client);
        context.setVariable("companyName", "Real Estate Connect");
        
        return templateEngine.process("email/welcome", context);
    }
    
    public String generatePropertyMatchEmail(Client client, Map<String, Object> propertyDetails) {
        Context context = new Context();
        context.setVariable("client", client);
        context.setVariable("property", propertyDetails);
        context.setVariable("companyName", "Real Estate Connect");
        
        return templateEngine.process("email/property-match", context);
    }
    
    public String generateMarketUpdateEmail(Client client, Map<String, Object> marketData) {
        Context context = new Context();
        context.setVariable("client", client);
        context.setVariable("marketData", marketData);
        context.setVariable("companyName", "Real Estate Connect");
        
        return templateEngine.process("email/market-update", context);
    }
    
    private String personalizeEmailContent(String content, Client client) {
        if (content == null) return "";
        
        return content
            .replace("{{firstName}}", client.getFirstName() != null ? client.getFirstName() : "")
            .replace("{{lastName}}", client.getLastName() != null ? client.getLastName() : "")
            .replace("{{fullName}}", client.getFirstName() + " " + client.getLastName())
            .replace("{{email}}", client.getEmail() != null ? client.getEmail() : "");
    }
    
    private String addEmailTracking(String content, Long campaignId, Long clientId, String trackingId) {
        // Add invisible tracking pixel
        String trackingPixel = String.format(
            "<img src=\"/api/email-tracking/pixel?c=%d&u=%d&t=%s\" width=\"1\" height=\"1\" style=\"display:none;\" />",
            campaignId, clientId, trackingId
        );
        
        // Add unsubscribe link
        String unsubscribeLink = String.format(
            "<p style=\"font-size:12px;color:#666;text-align:center;margin-top:20px;\">" +
            "Don't want to receive these emails? <a href=\"/unsubscribe?t=%s\">Unsubscribe</a>" +
            "</p>", trackingId
        );
        
        return content + trackingPixel + unsubscribeLink;
    }
    
    private void logEmailSent(Client client, String subject, String content, String status, String errorMessage) {
        EmailLog log = new EmailLog();
        log.setRecipientEmail(client.getEmail());
        log.setSubject(subject);
        log.setStatus(status);
        log.setErrorMessage(errorMessage);
        log.setSentAt(LocalDateTime.now());
        
        emailLogRepository.save(log);
    }
    
    public boolean isConfigured() {
        return defaultSenderEmail != null && !defaultSenderEmail.isEmpty();
    }
    
    public String getConfigurationStatus() {
        if (!isConfigured()) {
            return "Email service not configured - missing sender email";
        }
        
        StringBuilder status = new StringBuilder("Email service configured");
        if (dkimEnabled) {
            status.append(" with DKIM authentication");
        }
        if (fromDomain != null && !fromDomain.isEmpty()) {
            status.append(" using domain: ").append(fromDomain);
        }
        
        return status.toString();
    }
    
    // Result class for bulk email operations
    public static class EmailCampaignResult {
        private Long campaignId;
        private int totalRecipients;
        private int successfulSends;
        private int failedSends;
        private LocalDateTime completedAt;
        
        // Getters and setters
        public Long getCampaignId() { return campaignId; }
        public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }
        
        public int getTotalRecipients() { return totalRecipients; }
        public void setTotalRecipients(int totalRecipients) { this.totalRecipients = totalRecipients; }
        
        public int getSuccessfulSends() { return successfulSends; }
        public void setSuccessfulSends(int successfulSends) { this.successfulSends = successfulSends; }
        
        public int getFailedSends() { return failedSends; }
        public void setFailedSends(int failedSends) { this.failedSends = failedSends; }
        
        public LocalDateTime getCompletedAt() { return completedAt; }
        public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
        
        public double getSuccessRate() {
            return totalRecipients > 0 ? (successfulSends / (double) totalRecipients) * 100 : 0;
        }
    }
}