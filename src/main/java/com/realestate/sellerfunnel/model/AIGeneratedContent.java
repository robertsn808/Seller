package com.realestate.sellerfunnel.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ai_generated_content")
public class AIGeneratedContent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 500)
    private String prompt;
    
    @Column(length = 1000)
    private String headline;
    
    @Column(length = 5000)
    private String content;
    
    @Column(length = 500)
    private String callToAction;
    
    private String contentType; // FACEBOOK_POST, GOOGLE_AD, CRAIGSLIST_AD, EMAIL, BUSINESS_CARD
    
    private String targetAudience; // SELLERS, BUYERS, BOTH
    
    private String category; // GENERAL, DISTRESSED, INVESTORS, INHERITED, etc.
    
    @Column(length = 2000)
    private String keywords; // Comma-separated keywords used in generation
    
    @Column(length = 1000)
    private String context; // Additional context provided for generation
    
    @Column(name = "similarity_score")
    private Double similarityScore; // Score indicating similarity to previous content
    
    @Column(name = "generation_count")
    private Integer generationCount; // How many times this type of content was generated
    
    @Column(name = "last_used")
    private LocalDateTime lastUsed;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (generationCount == null) {
            generationCount = 1;
        }
        if (lastUsed == null) {
            lastUsed = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public AIGeneratedContent() {}
    
    public AIGeneratedContent(String prompt, String content, String contentType, String targetAudience) {
        this.prompt = prompt;
        this.content = content;
        this.contentType = contentType;
        this.targetAudience = targetAudience;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    
    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getCallToAction() { return callToAction; }
    public void setCallToAction(String callToAction) { this.callToAction = callToAction; }
    
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    
    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
    
    public Double getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(Double similarityScore) { this.similarityScore = similarityScore; }
    
    public Integer getGenerationCount() { return generationCount; }
    public void setGenerationCount(Integer generationCount) { this.generationCount = generationCount; }
    
    public LocalDateTime getLastUsed() { return lastUsed; }
    public void setLastUsed(LocalDateTime lastUsed) { this.lastUsed = lastUsed; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
} 