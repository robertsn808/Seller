package com.realestate.sellerfunnel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "campaigns")
public class Campaign {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Campaign name is required")
    private String name;
    
    @NotBlank(message = "Campaign type is required")
    private String type; // FACEBOOK, GOOGLE_ADS, CRAIGSLIST, DIRECT_MAIL, etc.
    
    @NotBlank(message = "Target audience is required")
    private String targetAudience; // SELLERS, BUYERS, BOTH
    
    private String status; // DRAFT, ACTIVE, PAUSED, COMPLETED
    
    @Column(length = 2000)
    private String description;
    
    private BigDecimal budget;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    @Column(length = 5000)
    private String adCopy;
    
    @Column(length = 1000)
    private String headline;
    
    @Column(length = 500)
    private String callToAction;
    
    private String targetKeywords; // For Google Ads
    
    private String demographicTargeting; // Age, location, interests
    
    private Integer impressions;
    
    private Integer clicks;
    
    private Integer leads;
    
    private BigDecimal cost;
    
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CampaignLead> campaignLeads = new ArrayList<>();
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "DRAFT";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public Campaign() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }
    
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    
    public String getAdCopy() { return adCopy; }
    public void setAdCopy(String adCopy) { this.adCopy = adCopy; }
    
    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }
    
    public String getCallToAction() { return callToAction; }
    public void setCallToAction(String callToAction) { this.callToAction = callToAction; }
    
    public String getTargetKeywords() { return targetKeywords; }
    public void setTargetKeywords(String targetKeywords) { this.targetKeywords = targetKeywords; }
    
    public String getDemographicTargeting() { return demographicTargeting; }
    public void setDemographicTargeting(String demographicTargeting) { this.demographicTargeting = demographicTargeting; }
    
    public Integer getImpressions() { return impressions; }
    public void setImpressions(Integer impressions) { this.impressions = impressions; }
    
    public Integer getClicks() { return clicks; }
    public void setClicks(Integer clicks) { this.clicks = clicks; }
    
    public Integer getLeads() { return leads; }
    public void setLeads(Integer leads) { this.leads = leads; }
    
    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
    
    public List<CampaignLead> getCampaignLeads() { return campaignLeads; }
    public void setCampaignLeads(List<CampaignLead> campaignLeads) { this.campaignLeads = campaignLeads; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods
    public Double getClickThroughRate() {
        if (impressions == null || impressions == 0 || clicks == null) return 0.0;
        return (clicks.doubleValue() / impressions.doubleValue()) * 100;
    }
    
    public Double getConversionRate() {
        if (clicks == null || clicks == 0 || leads == null) return 0.0;
        return (leads.doubleValue() / clicks.doubleValue()) * 100;
    }
    
    public BigDecimal getCostPerLead() {
        if (cost == null || leads == null || leads == 0) return BigDecimal.ZERO;
        return cost.divide(BigDecimal.valueOf(leads), 2, BigDecimal.ROUND_HALF_UP);
    }
}