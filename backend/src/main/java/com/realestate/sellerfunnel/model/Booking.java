package com.realestate.sellerfunnel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;
    
    @Column(name = "check_in_date")
    @NotNull(message = "Check-in date is required")
    private LocalDateTime checkInDate;
    
    @Column(name = "check_out_date")
    private LocalDateTime checkOutDate;
    
    @Column(name = "expected_check_out_date")
    private LocalDateTime expectedCheckOutDate;
    
    @Column(name = "nightly_rate")
    @DecimalMin(value = "0.0", inclusive = false, message = "Nightly rate must be greater than 0")
    private BigDecimal nightlyRate;
    
    @Column(name = "payment_frequency")
    private String paymentFrequency; // DAILY, WEEKLY, MONTHLY
    
    @Column(name = "total_charges")
    @DecimalMin(value = "0.0", message = "Total charges cannot be negative")
    private BigDecimal totalCharges = BigDecimal.ZERO;
    
    @Column(name = "total_payments")
    @DecimalMin(value = "0.0", message = "Total payments cannot be negative")
    private BigDecimal totalPayments = BigDecimal.ZERO;
    
    @Column(name = "current_balance")
    @DecimalMin(value = "0.0", message = "Current balance cannot be negative")
    private BigDecimal currentBalance = BigDecimal.ZERO;
    
    @Column(name = "number_of_nights")
    private Integer numberOfNights = 0;
    
    @Column(name = "booking_status")
    private String bookingStatus = "ACTIVE"; // ACTIVE, COMPLETED, CANCELLED
    
    @Column(name = "payment_status")
    private String paymentStatus = "PENDING"; // PENDING, PAID, PARTIAL, OVERDUE
    
    @Column(name = "special_instructions", length = 1000)
    private String specialInstructions;
    
    @Column(name = "notes", length = 1000)
    private String notes;
    
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
        if (totalCharges == null) {
            totalCharges = BigDecimal.ZERO;
        }
        if (totalPayments == null) {
            totalPayments = BigDecimal.ZERO;
        }
        if (currentBalance == null) {
            currentBalance = BigDecimal.ZERO;
        }
        if (numberOfNights == null) {
            numberOfNights = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // Recalculate balance
        if (totalCharges != null && totalPayments != null) {
            currentBalance = totalCharges.subtract(totalPayments);
        }
    }
    
    // Constructors
    public Booking() {}
    
    public Booking(Room room, Guest guest, LocalDateTime checkInDate, BigDecimal nightlyRate, String paymentFrequency) {
        this.room = room;
        this.guest = guest;
        this.checkInDate = checkInDate;
        this.nightlyRate = nightlyRate;
        this.paymentFrequency = paymentFrequency;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    
    public Guest getGuest() { return guest; }
    public void setGuest(Guest guest) { this.guest = guest; }
    
    public LocalDateTime getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDateTime checkInDate) { this.checkInDate = checkInDate; }
    
    public LocalDateTime getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDateTime checkOutDate) { this.checkOutDate = checkOutDate; }
    
    public LocalDateTime getExpectedCheckOutDate() { return expectedCheckOutDate; }
    public void setExpectedCheckOutDate(LocalDateTime expectedCheckOutDate) { this.expectedCheckOutDate = expectedCheckOutDate; }
    
    public BigDecimal getNightlyRate() { return nightlyRate; }
    public void setNightlyRate(BigDecimal nightlyRate) { this.nightlyRate = nightlyRate; }
    
    public String getPaymentFrequency() { return paymentFrequency; }
    public void setPaymentFrequency(String paymentFrequency) { this.paymentFrequency = paymentFrequency; }
    
    public BigDecimal getTotalCharges() { return totalCharges; }
    public void setTotalCharges(BigDecimal totalCharges) { this.totalCharges = totalCharges; }
    
    public BigDecimal getTotalPayments() { return totalPayments; }
    public void setTotalPayments(BigDecimal totalPayments) { this.totalPayments = totalPayments; }
    
    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }
    
    public Integer getNumberOfNights() { return numberOfNights; }
    public void setNumberOfNights(Integer numberOfNights) { this.numberOfNights = numberOfNights; }
    
    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }
    
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods
    public String getDisplayName() {
        return room.getDisplayName() + " - " + guest.getFullName();
    }
    
    public String getStatusDisplay() {
        return bookingStatus + " (" + paymentStatus + ")";
    }
    
    public String getPaymentFrequencyDisplay() {
        if (paymentFrequency == null) return "Not Set";
        switch (paymentFrequency.toUpperCase()) {
            case "DAILY": return "Daily";
            case "WEEKLY": return "Weekly";
            case "MONTHLY": return "Monthly";
            default: return paymentFrequency;
        }
    }
    
    public boolean isOverdue() {
        return "OVERDUE".equals(paymentStatus);
    }
    
    public boolean isActive() {
        return "ACTIVE".equals(bookingStatus) && isActive;
    }
    
    public void addCharge(BigDecimal amount) {
        if (totalCharges == null) {
            totalCharges = BigDecimal.ZERO;
        }
        totalCharges = totalCharges.add(amount);
        updateBalance();
    }
    
    public void addPayment(BigDecimal amount) {
        if (totalPayments == null) {
            totalPayments = BigDecimal.ZERO;
        }
        totalPayments = totalPayments.add(amount);
        updateBalance();
    }
    
    private void updateBalance() {
        if (totalCharges != null && totalPayments != null) {
            currentBalance = totalCharges.subtract(totalPayments);
            
            // Update payment status based on balance
            if (currentBalance.compareTo(BigDecimal.ZERO) <= 0) {
                paymentStatus = "PAID";
            } else if (totalPayments.compareTo(BigDecimal.ZERO) > 0) {
                paymentStatus = "PARTIAL";
            } else {
                paymentStatus = "PENDING";
            }
        }
    }
    
    public void calculateNights() {
        if (checkInDate != null) {
            LocalDateTime endDate = checkOutDate != null ? checkOutDate : LocalDateTime.now();
            if (checkInDate.isBefore(endDate)) {
                long days = java.time.Duration.between(checkInDate, endDate).toDays();
                numberOfNights = (int) days;
            }
        }
    }
}
