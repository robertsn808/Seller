package com.realestate.sellerfunnel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
    
    @Column(name = "amount")
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @Column(name = "currency")
    private String currency = "USD";
    
    @Column(name = "payment_method")
    private String paymentMethod; // CARD, CASH, TRANSFER, UPP_DEVICE
    
    @Column(name = "device_type")
    private String deviceType; // smartphone, smart_tv, iot_device, voice_assistant, etc.
    
    @Column(name = "device_id")
    private String deviceId;
    
    @Column(name = "upp_transaction_id")
    private String uppTransactionId;
    
    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;
    
    @Column(name = "payment_status")
    private String paymentStatus = "PENDING"; // PENDING, COMPLETED, FAILED, REFUNDED
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "customer_email")
    private String customerEmail;
    
    @Column(name = "metadata", length = 2000)
    private String metadata; // JSON string for additional data
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
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
        if (currency == null) {
            currency = "USD";
        }
        if (paymentStatus == null) {
            paymentStatus = "PENDING";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public Payment() {}
    
    public Payment(Booking booking, BigDecimal amount, String paymentMethod) {
        this.booking = booking;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    
    public String getUppTransactionId() { return uppTransactionId; }
    public void setUppTransactionId(String uppTransactionId) { this.uppTransactionId = uppTransactionId; }
    
    public String getStripePaymentIntentId() { return stripePaymentIntentId; }
    public void setStripePaymentIntentId(String stripePaymentIntentId) { this.stripePaymentIntentId = stripePaymentIntentId; }
    
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods
    public String getDisplayName() {
        return "$" + amount + " - " + paymentMethod + " (" + paymentStatus + ")";
    }
    
    public String getPaymentMethodDisplay() {
        if (paymentMethod == null) return "Unknown";
        switch (paymentMethod.toUpperCase()) {
            case "CARD": return "Credit/Debit Card";
            case "CASH": return "Cash";
            case "TRANSFER": return "Bank Transfer";
            case "UPP_DEVICE": return "Universal Payment Device";
            default: return paymentMethod;
        }
    }
    
    public String getDeviceTypeDisplay() {
        if (deviceType == null) return "N/A";
        switch (deviceType.toLowerCase()) {
            case "smartphone": return "Smartphone";
            case "smart_tv": return "Smart TV";
            case "iot_device": return "IoT Device";
            case "voice_assistant": return "Voice Assistant";
            case "gaming_console": return "Gaming Console";
            case "smartwatch": return "Smartwatch";
            case "car_system": return "Car System";
            default: return deviceType;
        }
    }
    
    public boolean isCompleted() {
        return "COMPLETED".equals(paymentStatus);
    }
    
    public boolean isFailed() {
        return "FAILED".equals(paymentStatus);
    }
    
    public boolean isPending() {
        return "PENDING".equals(paymentStatus);
    }
    
    public boolean isUppPayment() {
        return "UPP_DEVICE".equals(paymentMethod);
    }
}
