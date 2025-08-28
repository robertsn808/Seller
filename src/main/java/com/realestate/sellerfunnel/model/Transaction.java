package com.realestate.sellerfunnel.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @JsonIgnoreProperties("transactions")
    private Room room;

    @Column(name = "description")
    private String description;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "paid_by")
    private String paidBy;

    @Column(name = "collected_by")
    private String collectedBy; // User/staff who received the payment

    @Column(name = "transaction_type")
    private String transactionType; // PAYMENT, CHARGE, REFUND, FEE, DEPOSIT

    @Column(name = "transaction_category")
    private String transactionCategory; // RENT, UTILITIES, DAMAGES, DEPOSIT, LATE_FEE, etc.

    @Column(name = "running_balance")
    private BigDecimal runningBalance; // Running balance after this transaction

    @Column(name = "reference_number")
    private String referenceNumber; // External reference (check number, payment ID, etc.)

    @Column(name = "notes", length = 500)
    private String notes; // Additional notes about the transaction

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructors
    public Transaction() {}

    public Transaction(Room room, String description, BigDecimal amount, String paidBy) {
        this.room = room;
        this.description = description;
        this.amount = amount;
        this.paidBy = paidBy;
        this.transactionType = "PAYMENT"; // Default to PAYMENT
        this.transactionCategory = "RENT"; // Default to RENT
    }

    public Transaction(Room room, String description, BigDecimal amount, String paidBy, String transactionType, String transactionCategory) {
        this.room = room;
        this.description = description;
        this.amount = amount;
        this.paidBy = paidBy;
        this.transactionType = transactionType;
        this.transactionCategory = transactionCategory;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(String paidBy) {
        this.paidBy = paidBy;
    }

    public String getCollectedBy() {
        return collectedBy;
    }

    public void setCollectedBy(String collectedBy) {
        this.collectedBy = collectedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionCategory() {
        return transactionCategory;
    }

    public void setTransactionCategory(String transactionCategory) {
        this.transactionCategory = transactionCategory;
    }

    public BigDecimal getRunningBalance() {
        return runningBalance;
    }

    public void setRunningBalance(BigDecimal runningBalance) {
        this.runningBalance = runningBalance;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Helper methods
    public boolean isCredit() {
        return "PAYMENT".equals(transactionType) || "DEPOSIT".equals(transactionType) || "REFUND".equals(transactionType);
    }

    public boolean isDebit() {
        return "CHARGE".equals(transactionType) || "FEE".equals(transactionType);
    }

    public String getFormattedAmount() {
        if (amount == null) return "$0.00";
        String prefix = isCredit() ? "+" : "-";
        return prefix + "$" + amount.abs().toString();
    }
}
