package com.realestate.sellerfunnel.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaign_leads")
public class CampaignLead {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    private Buyer buyer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;
    
    private String source; // How they found us (Facebook, Google, etc.)
    
    private String medium; // paid, organic, referral
    
    private String utmCampaign;
    
    private String utmSource;
    
    private String utmMedium;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public CampaignLead() {}
    
    public CampaignLead(Campaign campaign, Buyer buyer, String source) {
        this.campaign = campaign;
        this.buyer = buyer;
        this.source = source;
    }
    
    public CampaignLead(Campaign campaign, Seller seller, String source) {
        this.campaign = campaign;
        this.seller = seller;
        this.source = source;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Campaign getCampaign() { return campaign; }
    public void setCampaign(Campaign campaign) { this.campaign = campaign; }
    
    public Buyer getBuyer() { return buyer; }
    public void setBuyer(Buyer buyer) { this.buyer = buyer; }
    
    public Seller getSeller() { return seller; }
    public void setSeller(Seller seller) { this.seller = seller; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getMedium() { return medium; }
    public void setMedium(String medium) { this.medium = medium; }
    
    public String getUtmCampaign() { return utmCampaign; }
    public void setUtmCampaign(String utmCampaign) { this.utmCampaign = utmCampaign; }
    
    public String getUtmSource() { return utmSource; }
    public void setUtmSource(String utmSource) { this.utmSource = utmSource; }
    
    public String getUtmMedium() { return utmMedium; }
    public void setUtmMedium(String utmMedium) { this.utmMedium = utmMedium; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}