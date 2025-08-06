package com.realestate.sellerfunnel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "email_campaigns")
public class EmailCampaign {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Campaign name is required")
    @Column(name = "campaign_name")
    private String campaignName;
    
    @NotBlank(message = "Subject line is required")
    @Column(name = "subject_line")
    private String subjectLine;
    
    @Column(length = 5000)
    private String content;
    
    @Column(name = "sender_name")
    private String senderName;
    
    @Column(name = "sender_email")
    private String senderEmail;
    
    @Column(name = "target_audience")
    private String targetAudience; // ALL, SELLERS, BUYERS, INVESTORS, etc.
    
    @Column(name = "client_type_filter")
    private String clientTypeFilter; // Specific client types to target
    
    @Column(name = "lead_source_filter")
    private String leadSourceFilter; // Specific lead sources to target
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "is_scheduled")
    private Boolean isScheduled;
    
    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate;
    
    @Column(name = "sent_date")
    private LocalDateTime sentDate;
    
    @Column(name = "total_recipients")
    private Integer totalRecipients;
    
    @Column(name = "sent_count")
    private Integer sentCount;
    
    @Column(name = "opened_count")
    private Integer openedCount;
    
    @Column(name = "clicked_count")
    private Integer clickedCount;
    
    @Column(name = "bounced_count")
    private Integer bouncedCount;
    
    @Column(name = "unsubscribed_count")
    private Integer unsubscribedCount;
    
    private String status; // DRAFT, SCHEDULED, SENDING, SENT, PAUSED, CANCELLED
    
    @Column(length = 1000)
    private String notes;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (isScheduled == null) {
            isScheduled = false;
        }
        if (status == null) {
            status = "DRAFT";
        }
        if (sentCount == null) {
            sentCount = 0;
        }
        if (openedCount == null) {
            openedCount = 0;
        }
        if (clickedCount == null) {
            clickedCount = 0;
        }
        if (bouncedCount == null) {
            bouncedCount = 0;
        }
        if (unsubscribedCount == null) {
            unsubscribedCount = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public EmailCampaign() {}
    
    public EmailCampaign(String campaignName, String subjectLine, String content) {
        this.campaignName = campaignName;
        this.subjectLine = subjectLine;
        this.content = content;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }
    
    public String getSubjectLine() { return subjectLine; }
    public void setSubjectLine(String subjectLine) { this.subjectLine = subjectLine; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    
    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }
    
    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }
    
    public String getClientTypeFilter() { return clientTypeFilter; }
    public void setClientTypeFilter(String clientTypeFilter) { this.clientTypeFilter = clientTypeFilter; }
    
    public String getLeadSourceFilter() { return leadSourceFilter; }
    public void setLeadSourceFilter(String leadSourceFilter) { this.leadSourceFilter = leadSourceFilter; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Boolean getIsScheduled() { return isScheduled; }
    public void setIsScheduled(Boolean isScheduled) { this.isScheduled = isScheduled; }
    
    public LocalDateTime getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDateTime scheduledDate) { this.scheduledDate = scheduledDate; }
    
    public LocalDateTime getSentDate() { return sentDate; }
    public void setSentDate(LocalDateTime sentDate) { this.sentDate = sentDate; }
    
    public Integer getTotalRecipients() { return totalRecipients; }
    public void setTotalRecipients(Integer totalRecipients) { this.totalRecipients = totalRecipients; }
    
    public Integer getSentCount() { return sentCount; }
    public void setSentCount(Integer sentCount) { this.sentCount = sentCount; }
    
    public Integer getOpenedCount() { return openedCount; }
    public void setOpenedCount(Integer openedCount) { this.openedCount = openedCount; }
    
    public Integer getClickedCount() { return clickedCount; }
    public void setClickedCount(Integer clickedCount) { this.clickedCount = clickedCount; }
    
    public Integer getBouncedCount() { return bouncedCount; }
    public void setBouncedCount(Integer bouncedCount) { this.bouncedCount = bouncedCount; }
    
    public Integer getUnsubscribedCount() { return unsubscribedCount; }
    public void setUnsubscribedCount(Integer unsubscribedCount) { this.unsubscribedCount = unsubscribedCount; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods
    public double getOpenRate() {
        if (sentCount == null || sentCount == 0) return 0.0;
        return (double) openedCount / sentCount * 100;
    }
    
    public double getClickRate() {
        if (sentCount == null || sentCount == 0) return 0.0;
        return (double) clickedCount / sentCount * 100;
    }
    
    public double getBounceRate() {
        if (sentCount == null || sentCount == 0) return 0.0;
        return (double) bouncedCount / sentCount * 100;
    }
    
    public boolean isReadyToSend() {
        return "SCHEDULED".equals(status) && scheduledDate != null && 
               scheduledDate.isBefore(LocalDateTime.now());
    }
} 