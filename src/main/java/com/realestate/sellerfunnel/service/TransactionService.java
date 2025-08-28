package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.Room;
import com.realestate.sellerfunnel.model.Transaction;
import com.realestate.sellerfunnel.repository.RoomRepository;
import com.realestate.sellerfunnel.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Transactional
    public Transaction addTransaction(Long roomId, String description, BigDecimal amount, String paidBy, String collectedBy) {
        return addTransaction(roomId, description, amount, paidBy, collectedBy, "PAYMENT", "RENT", null, null);
    }

    @Transactional
    public Transaction addTransaction(Long roomId, String description, BigDecimal amount, String paidBy, 
                                    String collectedBy, String transactionType, String transactionCategory, 
                                    String referenceNumber, String notes) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + roomId));

        // Get current balance from room
        BigDecimal currentBalance = room.getBalance() != null ? room.getBalance() : BigDecimal.ZERO;
        
        // Calculate new running balance based on transaction type
        BigDecimal transactionAmount = amount != null ? amount : BigDecimal.ZERO;
        BigDecimal newBalance;
        
        if ("PAYMENT".equals(transactionType) || "DEPOSIT".equals(transactionType) || "REFUND".equals(transactionType)) {
            // Credits reduce the balance (customer owes less)
            newBalance = currentBalance.subtract(transactionAmount);
        } else {
            // Debits increase the balance (customer owes more)
            newBalance = currentBalance.add(transactionAmount);
        }

        Transaction transaction = new Transaction(room, description, transactionAmount, paidBy, transactionType, transactionCategory);
        transaction.setCollectedBy(collectedBy);
        transaction.setRunningBalance(newBalance);
        transaction.setReferenceNumber(referenceNumber);
        transaction.setNotes(notes);

        // Update room balance
        room.setBalance(newBalance);
        room.addTransaction(transaction);

        transactionRepository.save(transaction);
        roomRepository.save(room);

        return transaction;
    }

    @Transactional
    public Transaction addPayment(Long roomId, String description, BigDecimal amount, String paidBy, 
                                String collectedBy, String referenceNumber) {
        return addTransaction(roomId, description, amount, paidBy, collectedBy, "PAYMENT", "RENT", referenceNumber, null);
    }

    @Transactional
    public Transaction addCharge(Long roomId, String description, BigDecimal amount, String category, String notes) {
        return addTransaction(roomId, description, amount, null, "System", "CHARGE", category, null, notes);
    }

    @Transactional
    public Transaction addDeposit(Long roomId, String description, BigDecimal amount, String paidBy, 
                                String collectedBy, String referenceNumber) {
        return addTransaction(roomId, description, amount, paidBy, collectedBy, "DEPOSIT", "DEPOSIT", referenceNumber, null);
    }

    @Transactional
    public Transaction addFee(Long roomId, String description, BigDecimal amount, String feeType, String notes) {
        return addTransaction(roomId, description, amount, null, "System", "FEE", feeType, null, notes);
    }

    public List<Transaction> getRoomLedger(Long roomId) {
        return transactionRepository.findByRoomIdOrderByCreatedAtDesc(roomId);
    }

    public List<Transaction> getRoomLedger(Long roomId, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByRoomIdAndCreatedAtBetweenOrderByCreatedAtDesc(roomId, startDate, endDate);
    }

    @Transactional
    public void recalculateRoomBalance(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + roomId));

        List<Transaction> transactions = transactionRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
        
        BigDecimal runningBalance = BigDecimal.ZERO;
        
        for (Transaction transaction : transactions) {
            if ("PAYMENT".equals(transaction.getTransactionType()) || 
                "DEPOSIT".equals(transaction.getTransactionType()) || 
                "REFUND".equals(transaction.getTransactionType())) {
                runningBalance = runningBalance.subtract(transaction.getAmount());
            } else {
                runningBalance = runningBalance.add(transaction.getAmount());
            }
            
            transaction.setRunningBalance(runningBalance);
            transactionRepository.save(transaction);
        }
        
        room.setBalance(runningBalance);
        roomRepository.save(room);
    }
}
