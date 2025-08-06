package com.realestate.sellerfunnel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "content_templates")
public class ContentTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Template name is required")
    private String name;
    
    @NotBlank(message = "Template type is required")
    private String type; // FACEBOOK_POST, GOOGLE_AD, CRAIGSLIST_AD, EMAIL, BUSINESS_CARD
    
    @NotBlank(message = "Target audience is required")
    private String targetAudience; // SELLERS, BUYERS, BOTH
    
    @Column(length = 1000)
    private String headline;
    
    @Column(length = 5000)
    private String content;
    
    @Column(length = 500)
    private String callToAction;
    
    private String category; // GENERAL, DISTRESSED, INVESTORS, INHERITED, etc.
    
    private Boolean isActive;
    
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
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public ContentTemplate() {}
    
    public ContentTemplate(String name, String type, String targetAudience, String content) {
        this.name = name;
        this.type = type;
        this.targetAudience = targetAudience;
        this.content = content;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }
    
    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getCallToAction() { return callToAction; }
    public void setCallToAction(String callToAction) { this.callToAction = callToAction; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}