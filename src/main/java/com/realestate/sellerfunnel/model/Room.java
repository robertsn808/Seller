package com.realestate.sellerfunnel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
public class Room {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Room number is required")
    @Column(name = "room_number", unique = true)
    private String roomNumber;
    
    @Column(name = "room_name")
    private String roomName; // Optional friendly name like "Ocean View", "Garden Suite"
    
    @Column(name = "room_type")
    private String roomType; // Single, Double, Suite, etc.
    
    @Column(name = "base_rate")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base rate must be greater than 0")
    private BigDecimal baseRate;
    
    @Column(name = "is_vacant")
    private Boolean isVacant = true;
    
    @Column(name = "current_code")
    private String currentCode; // Current door code for the room
    
    @Column(name = "reset_code")
    private String resetCode; // Master code to reset the door
    
    @Column(name = "gate_key_assigned")
    private Boolean gateKeyAssigned = false;
    
    @Column(name = "gate_key_number")
    private String gateKeyNumber; // If gate key is assigned, store the key number
    
    @Column(name = "notes", length = 1000)
    private String notes;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("room")
    private List<Transaction> transactions = new ArrayList<>();

    @Column(name = "balance")
    private BigDecimal balance = BigDecimal.ZERO;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isVacant == null) {
            isVacant = true;
        }
        if (gateKeyAssigned == null) {
            gateKeyAssigned = false;
        }
        if (isActive == null) {
            isActive = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public Room() {}
    
    public Room(String roomNumber, String roomName, String roomType, BigDecimal baseRate) {
        this.roomNumber = roomNumber;
        this.roomName = roomName;
        this.roomType = roomType;
        this.baseRate = baseRate;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    
    public BigDecimal getBaseRate() { return baseRate; }
    public void setBaseRate(BigDecimal baseRate) { this.baseRate = baseRate; }
    
    public Boolean getIsVacant() { return isVacant; }
    public void setIsVacant(Boolean isVacant) { this.isVacant = isVacant; }
    
    public String getCurrentCode() { return currentCode; }
    public void setCurrentCode(String currentCode) { this.currentCode = currentCode; }
    
    public String getResetCode() { return resetCode; }
    public void setResetCode(String resetCode) { this.resetCode = resetCode; }
    
    public Boolean getGateKeyAssigned() { return gateKeyAssigned; }
    public void setGateKeyAssigned(Boolean gateKeyAssigned) { this.gateKeyAssigned = gateKeyAssigned; }
    
    public String getGateKeyNumber() { return gateKeyNumber; }
    public void setGateKeyNumber(String gateKeyNumber) { this.gateKeyNumber = gateKeyNumber; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    // Helper methods
    public String getDisplayName() {
        if (roomName != null && !roomName.trim().isEmpty()) {
            return roomNumber + " - " + roomName;
        }
        return roomNumber;
    }
    
    public String getStatusDisplay() {
        return isVacant ? "Vacant" : "Occupied";
    }
    
    public String getGateKeyStatus() {
        if (gateKeyAssigned != null && gateKeyAssigned) {
            return gateKeyNumber != null ? "Key #" + gateKeyNumber : "Assigned";
        }
        return "Not Assigned";
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        transaction.setRoom(this);
        calculateBalance();
    }

    public void calculateBalance() {
        if (transactions == null) {
            this.balance = BigDecimal.ZERO;
            return;
        }
        this.balance = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
