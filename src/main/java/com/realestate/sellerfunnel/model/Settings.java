package com.realestate.sellerfunnel.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_settings")
public class Settings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // OpenAI
    private String openaiApiKey;
    private String openaiModel;

    // Twilio
    private String twilioAccountSid;
    private String twilioAuthToken;
    private String twilioPhoneNumber;
    private String twilioMessagingServiceSid;
    private String twilioStatusCallbackUrl;

    // Email / SMTP
    private String emailHost;
    private Integer emailPort;
    private String emailUsername;
    private String emailPassword;
    private String emailSenderName;
    private String emailSenderEmail;
    private String emailReplyTo;
    private String emailFromDomain;
    private String emailProvider; // smtp, ses, sendgrid
    private String awsSesRegion;
    private String sendgridApiKey;

    // DKIM
    private Boolean dkimEnabled;
    private String dkimDomain;
    private String dkimSelector;
    private String dkimPrivateKeyPath;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOpenaiApiKey() { return openaiApiKey; }
    public void setOpenaiApiKey(String openaiApiKey) { this.openaiApiKey = openaiApiKey; }
    public String getOpenaiModel() { return openaiModel; }
    public void setOpenaiModel(String openaiModel) { this.openaiModel = openaiModel; }

    public String getTwilioAccountSid() { return twilioAccountSid; }
    public void setTwilioAccountSid(String twilioAccountSid) { this.twilioAccountSid = twilioAccountSid; }
    public String getTwilioAuthToken() { return twilioAuthToken; }
    public void setTwilioAuthToken(String twilioAuthToken) { this.twilioAuthToken = twilioAuthToken; }
    public String getTwilioPhoneNumber() { return twilioPhoneNumber; }
    public void setTwilioPhoneNumber(String twilioPhoneNumber) { this.twilioPhoneNumber = twilioPhoneNumber; }
    public String getTwilioMessagingServiceSid() { return twilioMessagingServiceSid; }
    public void setTwilioMessagingServiceSid(String twilioMessagingServiceSid) { this.twilioMessagingServiceSid = twilioMessagingServiceSid; }
    public String getTwilioStatusCallbackUrl() { return twilioStatusCallbackUrl; }
    public void setTwilioStatusCallbackUrl(String twilioStatusCallbackUrl) { this.twilioStatusCallbackUrl = twilioStatusCallbackUrl; }

    public String getEmailHost() { return emailHost; }
    public void setEmailHost(String emailHost) { this.emailHost = emailHost; }
    public Integer getEmailPort() { return emailPort; }
    public void setEmailPort(Integer emailPort) { this.emailPort = emailPort; }
    public String getEmailUsername() { return emailUsername; }
    public void setEmailUsername(String emailUsername) { this.emailUsername = emailUsername; }
    public String getEmailPassword() { return emailPassword; }
    public void setEmailPassword(String emailPassword) { this.emailPassword = emailPassword; }
    public String getEmailSenderName() { return emailSenderName; }
    public void setEmailSenderName(String emailSenderName) { this.emailSenderName = emailSenderName; }
    public String getEmailSenderEmail() { return emailSenderEmail; }
    public void setEmailSenderEmail(String emailSenderEmail) { this.emailSenderEmail = emailSenderEmail; }
    public String getEmailReplyTo() { return emailReplyTo; }
    public void setEmailReplyTo(String emailReplyTo) { this.emailReplyTo = emailReplyTo; }
    public String getEmailFromDomain() { return emailFromDomain; }
    public void setEmailFromDomain(String emailFromDomain) { this.emailFromDomain = emailFromDomain; }
    public String getEmailProvider() { return emailProvider; }
    public void setEmailProvider(String emailProvider) { this.emailProvider = emailProvider; }
    public String getAwsSesRegion() { return awsSesRegion; }
    public void setAwsSesRegion(String awsSesRegion) { this.awsSesRegion = awsSesRegion; }
    public String getSendgridApiKey() { return sendgridApiKey; }
    public void setSendgridApiKey(String sendgridApiKey) { this.sendgridApiKey = sendgridApiKey; }

    public Boolean getDkimEnabled() { return dkimEnabled; }
    public void setDkimEnabled(Boolean dkimEnabled) { this.dkimEnabled = dkimEnabled; }
    public String getDkimDomain() { return dkimDomain; }
    public void setDkimDomain(String dkimDomain) { this.dkimDomain = dkimDomain; }
    public String getDkimSelector() { return dkimSelector; }
    public void setDkimSelector(String dkimSelector) { this.dkimSelector = dkimSelector; }
    public String getDkimPrivateKeyPath() { return dkimPrivateKeyPath; }
    public void setDkimPrivateKeyPath(String dkimPrivateKeyPath) { this.dkimPrivateKeyPath = dkimPrivateKeyPath; }
}
