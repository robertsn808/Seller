package com.realestate.sellerfunnel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "buyers")
public class Buyer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    private String email;
    
    @NotBlank(message = "Phone is required")
    private String phone;
    
    @NotNull(message = "Minimum budget is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Budget must be greater than 0")
    private BigDecimal minBudget;
    
    @NotNull(message = "Maximum budget is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Budget must be greater than 0")
    private BigDecimal maxBudget;
    
    private String preferredAreas;
    
    @Min(value = 1, message = "Minimum bedrooms must be at least 1")
    private Integer minBedrooms;
    
    @Min(value = 1, message = "Minimum bathrooms must be at least 1")
    private Integer minBathrooms;
    
    private String propertyType; // Single Family, Condo, Townhouse, etc.
    
    private Boolean needsFinancing;
    
    private Boolean openToCreativeFinancing;
    
    private String purchasePurpose; // Primary Residence, Investment Property, Vacation Home, etc.
    
    private String timeframe; // ASAP, 30 days, 90 days, etc.
    
    @Column(length = 1000)
    private String additionalNotes;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public Buyer() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public BigDecimal getMinBudget() { return minBudget; }
    public void setMinBudget(BigDecimal minBudget) { this.minBudget = minBudget; }
    
    public BigDecimal getMaxBudget() { return maxBudget; }
    public void setMaxBudget(BigDecimal maxBudget) { this.maxBudget = maxBudget; }
    
    public String getPreferredAreas() { return preferredAreas; }
    public void setPreferredAreas(String preferredAreas) { this.preferredAreas = preferredAreas; }
    
    public Integer getMinBedrooms() { return minBedrooms; }
    public void setMinBedrooms(Integer minBedrooms) { this.minBedrooms = minBedrooms; }
    
    public Integer getMinBathrooms() { return minBathrooms; }
    public void setMinBathrooms(Integer minBathrooms) { this.minBathrooms = minBathrooms; }
    
    public String getPropertyType() { return propertyType; }
    public void setPropertyType(String propertyType) { this.propertyType = propertyType; }
    
    public Boolean getNeedsFinancing() { return needsFinancing; }
    public void setNeedsFinancing(Boolean needsFinancing) { this.needsFinancing = needsFinancing; }
    
    public Boolean getOpenToCreativeFinancing() { return openToCreativeFinancing; }
    public void setOpenToCreativeFinancing(Boolean openToCreativeFinancing) { this.openToCreativeFinancing = openToCreativeFinancing; }
    
    public String getPurchasePurpose() { return purchasePurpose; }
    public void setPurchasePurpose(String purchasePurpose) { this.purchasePurpose = purchasePurpose; }
    
    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
    
    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}