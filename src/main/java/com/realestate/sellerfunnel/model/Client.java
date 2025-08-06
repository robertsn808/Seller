package com.realestate.sellerfunnel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "clients")
public class Client {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "First name is required")
    @Column(name = "first_name")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Column(name = "last_name")
    private String lastName;
    
    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    private String email;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "company_name")
    private String companyName;
    
    @Column(name = "job_title")
    private String jobTitle;
    
    @Column(length = 500)
    private String address;
    
    private String city;
    
    private String state;
    
    @Column(name = "zip_code")
    private String zipCode;
    
    @Column(name = "client_type")
    private String clientType; // SELLER, BUYER, INVESTOR, AGENT, VENDOR
    
    @Column(name = "client_status")
    private String clientStatus; // SUSPECT, PROSPECT, LEAD, CONTRACT, DEAL
    
    @Column(name = "lead_source")
    private String leadSource; // WEBSITE, REFERRAL, SOCIAL_MEDIA, COLD_CALL, etc.
    
    @Column(length = 1000)
    private String notes;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "email_opted_in")
    private Boolean emailOptedIn;
    
    @Column(name = "last_contact_date")
    private LocalDateTime lastContactDate;
    
    @Column(name = "email_contact_count")
    private Integer emailContactCount;
    
    @Column(name = "phone_contact_count")
    private Integer phoneContactCount;
    
    @Column(name = "total_contact_count")
    private Integer totalContactCount;
    
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
        if (emailOptedIn == null) {
            emailOptedIn = true;
        }
        if (emailContactCount == null) {
            emailContactCount = 0;
        }
        if (phoneContactCount == null) {
            phoneContactCount = 0;
        }
        if (totalContactCount == null) {
            totalContactCount = 0;
        }
        if (clientStatus == null) {
            clientStatus = "SUSPECT";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public Client() {}
    
    public Client(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    
    public String getClientType() { return clientType; }
    public void setClientType(String clientType) { this.clientType = clientType; }
    
    public String getClientStatus() { return clientStatus; }
    public void setClientStatus(String clientStatus) { this.clientStatus = clientStatus; }
    
    public String getLeadSource() { return leadSource; }
    public void setLeadSource(String leadSource) { this.leadSource = leadSource; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Boolean getEmailOptedIn() { return emailOptedIn; }
    public void setEmailOptedIn(Boolean emailOptedIn) { this.emailOptedIn = emailOptedIn; }
    
    public LocalDateTime getLastContactDate() { return lastContactDate; }
    public void setLastContactDate(LocalDateTime lastContactDate) { this.lastContactDate = lastContactDate; }
    
    public Integer getEmailContactCount() { return emailContactCount; }
    public void setEmailContactCount(Integer emailContactCount) { this.emailContactCount = emailContactCount; }
    
    public Integer getPhoneContactCount() { return phoneContactCount; }
    public void setPhoneContactCount(Integer phoneContactCount) { this.phoneContactCount = phoneContactCount; }
    
    public Integer getTotalContactCount() { return totalContactCount; }
    public void setTotalContactCount(Integer totalContactCount) { this.totalContactCount = totalContactCount; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        if (this.address != null && !this.address.isEmpty()) {
            address.append(this.address);
        }
        if (city != null && !city.isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(city);
        }
        if (state != null && !state.isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(state);
        }
        if (zipCode != null && !zipCode.isEmpty()) {
            if (address.length() > 0) address.append(" ");
            address.append(zipCode);
        }
        return address.toString();
    }
    
    // Contact tracking methods
    public void incrementEmailContact() {
        this.emailContactCount = (this.emailContactCount != null ? this.emailContactCount : 0) + 1;
        this.totalContactCount = (this.totalContactCount != null ? this.totalContactCount : 0) + 1;
        this.lastContactDate = LocalDateTime.now();
    }
    
    public void incrementPhoneContact() {
        this.phoneContactCount = (this.phoneContactCount != null ? this.phoneContactCount : 0) + 1;
        this.totalContactCount = (this.totalContactCount != null ? this.totalContactCount : 0) + 1;
        this.lastContactDate = LocalDateTime.now();
    }
    
    public void incrementContact(String contactType) {
        if ("EMAIL".equalsIgnoreCase(contactType)) {
            incrementEmailContact();
        } else if ("PHONE".equalsIgnoreCase(contactType)) {
            incrementPhoneContact();
        }
    }
} 