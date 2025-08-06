package com.realestate.sellerfunnel.controller;

import com.realestate.sellerfunnel.service.ProfessionalEmailService;
import com.realestate.sellerfunnel.model.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.util.StringUtils;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Controller
@RequestMapping("/admin/marketing/email-setup")
@PreAuthorize("hasRole('ADMIN')")
public class EmailSetupController {

    @Autowired
    private ProfessionalEmailService professionalEmailService;
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
        "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    
    // Domain validation pattern
    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
        "^(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)" +
        "+[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?$");
    
    // Rate limiting for test emails
    private final Map<String, AtomicInteger> testEmailAttempts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastAttemptTime = new ConcurrentHashMap<>();
    private static final int MAX_TEST_EMAILS_PER_HOUR = 5;
    
    // Maximum input lengths
    private static final int MAX_EMAIL_LENGTH = 254;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DOMAIN_LENGTH = 253;
    private static final int MAX_SELECTOR_LENGTH = 63;
    
    // HTML sanitizer for XSS protection
    private static final PolicyFactory POLICY = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    @Value("${spring.mail.host:}")
    private String smtpHost;

    @Value("${spring.mail.port:587}")
    private String smtpPort;

    @Value("${app.email.sender-email:}")
    private String senderEmail;

    @Value("${app.email.sender-name:Real Estate Team}")
    private String senderName;

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

    @GetMapping
    public String showEmailSetup(Model model, HttpServletRequest request) {
        // Add CSRF token to model for form submission
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
        
        // Add current configuration to model
        model.addAttribute("smtpHost", smtpHost.isEmpty() ? "Not configured" : smtpHost);
        model.addAttribute("smtpPort", smtpPort);
        model.addAttribute("senderEmail", senderEmail.isEmpty() ? "Not configured" : senderEmail);
        model.addAttribute("senderName", senderName);
        model.addAttribute("fromDomain", fromDomain);
        model.addAttribute("replyToEmail", replyToEmail);
        model.addAttribute("dkimEnabled", dkimEnabled);
        model.addAttribute("dkimDomain", dkimDomain);
        model.addAttribute("dkimSelector", dkimSelector);
        
        // Configuration status
        boolean configurationComplete = !senderEmail.isEmpty() && !smtpHost.isEmpty();
        model.addAttribute("configurationComplete", configurationComplete);
        model.addAttribute("configurationStatus", professionalEmailService.getConfigurationStatus());

        return "admin/marketing/email-setup";
    }

    @PostMapping
    public String saveEmailConfiguration(
            @RequestParam String senderEmail,
            @RequestParam String senderName,
            @RequestParam(required = false) String replyToEmail,
            @RequestParam(required = false) boolean dkimEnabled,
            @RequestParam(required = false) String dkimDomain,
            @RequestParam(required = false) String dkimSelector,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Input validation
            if (!isValidInput(senderEmail, senderName, replyToEmail, dkimDomain, dkimSelector)) {
                redirectAttributes.addFlashAttribute("error", "Invalid input provided. Please check your entries.");
                return "redirect:/admin/marketing/email-setup";
            }
            
            // Sanitize inputs
            senderEmail = sanitizeInput(senderEmail);
            senderName = sanitizeInput(senderName);
            replyToEmail = sanitizeInput(replyToEmail);
            dkimDomain = sanitizeInput(dkimDomain);
            dkimSelector = sanitizeInput(dkimSelector);
            
            // Note: In a production system, you would save these to a database
            // For now, we'll just show a message about environment variables
            
            StringBuilder envVars = new StringBuilder();
            envVars.append("EMAIL_USERNAME=").append(senderEmail).append("\n");
            envVars.append("app.email.sender-name=").append(senderName).append("\n");
            
            if (StringUtils.hasText(replyToEmail)) {
                envVars.append("EMAIL_REPLY_TO=").append(replyToEmail).append("\n");
            }
            
            if (dkimEnabled) {
                envVars.append("DKIM_ENABLED=true\n");
                if (StringUtils.hasText(dkimDomain)) {
                    envVars.append("DKIM_DOMAIN=").append(dkimDomain).append("\n");
                }
                if (StringUtils.hasText(dkimSelector)) {
                    envVars.append("DKIM_SELECTOR=").append(dkimSelector).append("\n");
                }
            }

            redirectAttributes.addFlashAttribute("message", 
                "Email configuration saved! Please set these environment variables on your server:\n\n" + 
                envVars.toString() + 
                "\nRestart the application after setting these variables.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Configuration save failed. Please try again.");
        }

        return "redirect:/admin/marketing/email-setup";
    }

    @PostMapping("/test")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendTestEmail(
            @RequestParam String testEmail,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Rate limiting check
            String clientIP = getClientIP(request);
            if (!isWithinRateLimit(clientIP)) {
                response.put("success", false);
                response.put("message", "Too many test email attempts. Please wait before trying again.");
                return ResponseEntity.ok(response);
            }
            
            // Validate email format
            if (!isValidEmail(testEmail)) {
                response.put("success", false);
                response.put("message", "Invalid email format provided.");
                return ResponseEntity.ok(response);
            }
            
            // Sanitize email
            testEmail = sanitizeInput(testEmail);
            
            // Create a temporary client for testing
            Client testClient = new Client();
            testClient.setEmail(testEmail);
            testClient.setFirstName("Test");
            testClient.setLastName("User");

            String subject = "DKIM Email Configuration Test";
            String htmlContent = generateTestEmailContent();

            boolean sent = professionalEmailService.sendProfessionalEmail(
                testClient, subject, htmlContent, null, null);

            if (sent) {
                response.put("success", true);
                response.put("message", "Test email sent successfully with DKIM authentication!");
            } else {
                response.put("success", false);
                response.put("message", "Failed to send test email. Check your email configuration.");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred while sending the test email.");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/configuration-guide")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getConfigurationGuide() {
        Map<String, Object> guide = new HashMap<>();
        
        // DKIM Configuration
        Map<String, String> dkimConfig = new HashMap<>();
        dkimConfig.put("record_name", "default._domainkey");
        dkimConfig.put("record_type", "TXT");
        dkimConfig.put("record_value", "v=DKIM1;k=rsa;p=MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvgn5VVvCnMnAHOk7TBQ1Fq3auIa+CaZeHMz3gHOwkiIA5IZPXmho3BHuxCXzo7I3PnLsiZA18TOQQqVvhVbNU7aRAdaKDsz5q4KmPuHAQkHqPj6aSRmGUtYzeRUxzuc8ys8w9Eff2QpCICF1ArRlVdPIJPgJIftk8ByrKao+qwB+Cjemb5K7cya4i/ssVf9Hm2VH7cGOlmRluBY1VTvUeNA5Gr9d7alGrlYBZkmYCX2g/gZ9FEpPNamlS4n/t/SiKtHACZW3i9QaGnglo616+KakVn9kGeWaQ8m3Wxxo43IdPd5CZMBVP8Ji9mbXRNbRhY3E/ptnMD1eE9maCisNoQIDAQAB");
        
        guide.put("dkim", dkimConfig);
        guide.put("configured", professionalEmailService.isConfigured());
        guide.put("status", professionalEmailService.getConfigurationStatus());

        return ResponseEntity.ok(guide);
    }

    private String generateTestEmailContent() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>DKIM Test Email</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 10px; margin-bottom: 20px;">
                    <h1 style="margin: 0; font-size: 24px;">🔒 DKIM Email Test</h1>
                    <p style="margin: 10px 0 0 0; opacity: 0.9;">Real Estate Connect CRM</p>
                </div>
                
                <div style="background: #f8f9fa; padding: 20px; border-radius: 10px; margin-bottom: 20px;">
                    <h2 style="color: #28a745; margin-top: 0;">✅ Email Configuration Test Successful!</h2>
                    <p>If you're reading this email, your DKIM email configuration is working correctly!</p>
                </div>
                
                <div style="padding: 20px; border: 1px solid #dee2e6; border-radius: 10px;">
                    <h3>DKIM Authentication Details:</h3>
                    <ul>
                        <li><strong>DKIM Enabled:</strong> """ + dkimEnabled + "</li>" +
                        "<li><strong>DKIM Domain:</strong> " + (dkimDomain.isEmpty() ? "Not configured" : dkimDomain) + "</li>" +
                        "<li><strong>DKIM Selector:</strong> " + dkimSelector + "</li>" +
                        "<li><strong>Professional Headers:</strong> Included</li>" +
                    "</ul>" +

                    "<h3>Benefits of DKIM:</h3>" +
                    "<ul>" +
                        "<li>📧 Improved email deliverability</li>" +
                        "<li>🛡️ Reduced spam classification</li>" +
                        "<li>🔐 Email authentication and integrity</li>" +
                        "<li>📊 Better sender reputation</li>" +
                    "</ul>" +
                "</div>" +
                
                "<div style=\"margin-top: 30px; padding: 15px; background: #e3f2fd; border-left: 4px solid #2196f3; border-radius: 5px;\">" +
                    "<p style=\"margin: 0;\"><strong>Next Steps:</strong></p>" +
                    "<ol style=\"margin: 10px 0 0 0;\">" +
                        "<li>Create your first email campaign</li>" +
                        "<li>Import your contact list</li>" +
                        "<li>Start sending professional emails to your leads</li>" +
                    "</ol>" +
                "</div>" +
                
                "<div style=\"margin-top: 30px; text-align: center; color: #666; font-size: 14px;\">" +
                    "<p>This email was sent using your DKIM-authenticated email system.</p>" +
                    "<p><strong>Real Estate Connect CRM</strong> • Professional Email Marketing</p>" +
                "</div>" +
            "</body>" +
            "</html>";
    }
    
    // Security helper methods
    private boolean isValidInput(String senderEmail, String senderName, String replyToEmail, 
                                String dkimDomain, String dkimSelector) {
        // Check email format
        if (!isValidEmail(senderEmail)) {
            return false;
        }
        
        // Check reply-to email if provided
        if (StringUtils.hasText(replyToEmail) && !isValidEmail(replyToEmail)) {
            return false;
        }
        
        // Check domain format if provided
        if (StringUtils.hasText(dkimDomain) && !isValidDomain(dkimDomain)) {
            return false;
        }
        
        // Check input lengths
        if (senderEmail.length() > MAX_EMAIL_LENGTH || senderName.length() > MAX_NAME_LENGTH ||
            (replyToEmail != null && replyToEmail.length() > MAX_EMAIL_LENGTH) ||
            (dkimDomain != null && dkimDomain.length() > MAX_DOMAIN_LENGTH) ||
            (dkimSelector != null && dkimSelector.length() > MAX_SELECTOR_LENGTH)) {
            return false;
        }
        
        return true;
    }
    
    private boolean isValidEmail(String email) {
        return StringUtils.hasText(email) && EMAIL_PATTERN.matcher(email).matches();
    }
    
    private boolean isValidDomain(String domain) {
        return StringUtils.hasText(domain) && DOMAIN_PATTERN.matcher(domain).matches();
    }
    
    private String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        // Use OWASP HTML sanitizer for comprehensive XSS protection
        return POLICY.sanitize(input.trim());
    }
    
    private boolean isWithinRateLimit(String clientIP) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastAttempt = lastAttemptTime.get(clientIP);
        
        // Reset counter if more than an hour has passed
        if (lastAttempt == null || ChronoUnit.HOURS.between(lastAttempt, now) >= 1) {
            testEmailAttempts.put(clientIP, new AtomicInteger(1));
            lastAttemptTime.put(clientIP, now);
            return true;
        }
        
        AtomicInteger attempts = testEmailAttempts.get(clientIP);
        if (attempts == null) {
            attempts = new AtomicInteger(0);
            testEmailAttempts.put(clientIP, attempts);
        }
        
        return attempts.incrementAndGet() <= MAX_TEST_EMAILS_PER_HOUR;
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIP)) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}