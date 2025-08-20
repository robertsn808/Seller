package com.realestate.sellerfunnel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sellers")
public class Seller {
    
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
    
    @NotBlank(message = "Property address is required")
    private String propertyAddress;
    
    private String city;
    
    private String state;
    
    private String zipCode;
    
    private String propertyType; // Single Family, Condo, Townhouse, etc.
    
    private Integer bedrooms;
    
    private BigDecimal bathrooms; // Allow for half baths like 2.5
    
    private Integer squareFootage;
    
    private Integer yearBuilt;
    
    @NotNull(message = "Asking price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal askingPrice;
    
    private String sellingReason; // Relocation, upgrade, downsize, etc.
    
    private String condition; // Excellent, Good, Needs Work, etc.
    
    private String timeframe; // ASAP, 30 days, 90 days, flexible
    
    private Boolean ownerFinancing;
    
    private Boolean openToCreativeFinancing;
    
    private Boolean needsRepairs;
    
    @Column(length = 1000)
    private String repairDetails;
    
    @Column(length = 1000)
    private String additionalNotes;
    
    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    private List<PropertyPhoto> photos = new ArrayList<>();
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public Seller() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getPropertyAddress() { return propertyAddress; }
    public void setPropertyAddress(String propertyAddress) { this.propertyAddress = propertyAddress; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    
    public String getPropertyType() { return propertyType; }
    public void setPropertyType(String propertyType) { this.propertyType = propertyType; }
    
    public Integer getBedrooms() { return bedrooms; }
    public void setBedrooms(Integer bedrooms) { this.bedrooms = bedrooms; }
    
    public BigDecimal getBathrooms() { return bathrooms; }
    public void setBathrooms(BigDecimal bathrooms) { this.bathrooms = bathrooms; }
    
    public Integer getSquareFootage() { return squareFootage; }
    public void setSquareFootage(Integer squareFootage) { this.squareFootage = squareFootage; }
    
    public Integer getYearBuilt() { return yearBuilt; }
    public void setYearBuilt(Integer yearBuilt) { this.yearBuilt = yearBuilt; }
    
    public BigDecimal getAskingPrice() { return askingPrice; }
    public void setAskingPrice(BigDecimal askingPrice) { this.askingPrice = askingPrice; }
    
    public String getSellingReason() { return sellingReason; }
    public void setSellingReason(String sellingReason) { this.sellingReason = sellingReason; }
    
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    
    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
    
    public Boolean getOwnerFinancing() { return ownerFinancing; }
    public void setOwnerFinancing(Boolean ownerFinancing) { this.ownerFinancing = ownerFinancing; }
    
    public Boolean getOpenToCreativeFinancing() { return openToCreativeFinancing; }
    public void setOpenToCreativeFinancing(Boolean openToCreativeFinancing) { this.openToCreativeFinancing = openToCreativeFinancing; }
    
    public Boolean getNeedsRepairs() { return needsRepairs; }
    public void setNeedsRepairs(Boolean needsRepairs) { this.needsRepairs = needsRepairs; }
    
    public String getRepairDetails() { return repairDetails; }
    public void setRepairDetails(String repairDetails) { this.repairDetails = repairDetails; }
    
    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public List<PropertyPhoto> getPhotos() { return photos; }
    public void setPhotos(List<PropertyPhoto> photos) { this.photos = photos; }
    
    public void addPhoto(PropertyPhoto photo) {
        photos.add(photo);
        photo.setSeller(this);
    }
    
    public void removePhoto(PropertyPhoto photo) {
        photos.remove(photo);
        photo.setSeller(null);
    }
}