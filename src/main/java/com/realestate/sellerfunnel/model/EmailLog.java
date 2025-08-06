package com.realestate.sellerfunnel.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_logs")
public class EmailLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "email_campaign_id")
    private EmailCampaign emailCampaign;
    
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
    
    @Column(name = "recipient_email")
    private String recipientEmail;
    
    @Column(name = "recipient_name")
    private String recipientName;
    
    @Column(name = "subject_line")
    private String subjectLine;
    
    @Column(length = 5000)
    private String content;
    
    @Column(name = "sender_email")
    private String senderEmail;
    
    @Column(name = "sender_name")
    private String senderName;
    
    private String status; // PENDING, SENT, DELIVERED, OPENED, CLICKED, BOUNCED, FAILED
    
    @Column(name = "sent_date")
    private LocalDateTime sentDate;
    
    @Column(name = "delivered_date")
    private LocalDateTime deliveredDate;
    
    @Column(name = "opened_date")
    private LocalDateTime openedDate;
    
    @Column(name = "clicked_date")
    private LocalDateTime clickedDate;
    
    @Column(name = "bounced_date")
    private LocalDateTime bouncedDate;
    
    @Column(name = "bounce_reason")
    private String bounceReason;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "tracking_id")
    private String trackingId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public EmailLog() {}
    
    public EmailLog(EmailCampaign emailCampaign, Client client, String recipientEmail, String subjectLine, String content) {
        this.emailCampaign = emailCampaign;
        this.client = client;
        this.recipientEmail = recipientEmail;
        this.subjectLine = subjectLine;
        this.content = content;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public EmailCampaign getEmailCampaign() { return emailCampaign; }
    public void setEmailCampaign(EmailCampaign emailCampaign) { this.emailCampaign = emailCampaign; }
    
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    
    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    
    public String getSubjectLine() { return subjectLine; }
    public void setSubjectLine(String subjectLine) { this.subjectLine = subjectLine; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }
    
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getSentDate() { return sentDate; }
    public void setSentDate(LocalDateTime sentDate) { this.sentDate = sentDate; }
    
    public LocalDateTime getDeliveredDate() { return deliveredDate; }
    public void setDeliveredDate(LocalDateTime deliveredDate) { this.deliveredDate = deliveredDate; }
    
    public LocalDateTime getOpenedDate() { return openedDate; }
    public void setOpenedDate(LocalDateTime openedDate) { this.openedDate = openedDate; }
    
    public LocalDateTime getClickedDate() { return clickedDate; }
    public void setClickedDate(LocalDateTime clickedDate) { this.clickedDate = clickedDate; }
    
    public LocalDateTime getBouncedDate() { return bouncedDate; }
    public void setBouncedDate(LocalDateTime bouncedDate) { this.bouncedDate = bouncedDate; }
    
    public String getBounceReason() { return bounceReason; }
    public void setBounceReason(String bounceReason) { this.bounceReason = bounceReason; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public String getTrackingId() { return trackingId; }
    public void setTrackingId(String trackingId) { this.trackingId = trackingId; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods
    public boolean isSuccessful() {
        return "SENT".equals(status) || "DELIVERED".equals(status) || 
               "OPENED".equals(status) || "CLICKED".equals(status);
    }
    
    public boolean isFailed() {
        return "BOUNCED".equals(status) || "FAILED".equals(status);
    }
    
    public boolean isEngaged() {
        return "OPENED".equals(status) || "CLICKED".equals(status);
    }
} 