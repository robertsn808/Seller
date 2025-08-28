package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.Room;
import com.realestate.sellerfunnel.model.Transaction;
import com.realestate.sellerfunnel.repository.RoomRepository;
import com.realestate.sellerfunnel.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FinancialService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public BigDecimal calculateTotalHoldings() {
        return roomRepository.findAll().stream()
                .map(room -> room.getBalance() != null ? room.getBalance() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Room> listRoomsByBalanceDesc() {
        return roomRepository.findAll().stream()
                .sorted((a, b) -> {
                    BigDecimal balanceA = a.getBalance() != null ? a.getBalance() : BigDecimal.ZERO;
                    BigDecimal balanceB = b.getBalance() != null ? b.getBalance() : BigDecimal.ZERO;
                    return balanceB.compareTo(balanceA);
                })
                .collect(Collectors.toList());
    }

    public List<Transaction> recentTransactions() {
        return transactionRepository.findTop50ByOrderByCreatedAtDesc();
    }

    public List<Transaction> transactionsForRoom(Long roomId) {
        return transactionRepository.findByRoomIdOrderByCreatedAtDesc(roomId);
    }

    public List<Transaction> transactionsByDateRange(LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByDateRange(start, end);
    }

    public List<Transaction> getRoomLedger(Long roomId) {
        return transactionRepository.findByRoomIdOrderByCreatedAtDesc(roomId);
    }

    public List<Transaction> getRoomLedger(Long roomId, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByRoomIdAndCreatedAtBetweenOrderByCreatedAtDesc(roomId, startDate, endDate);
    }

    public FinancialSummary getFinancialSummary() {
        FinancialSummary summary = new FinancialSummary();
        
        BigDecimal totalPayments = transactionRepository.getTotalPayments();
        BigDecimal totalCharges = transactionRepository.getTotalCharges();
        
        summary.setTotalPayments(totalPayments != null ? totalPayments : BigDecimal.ZERO);
        summary.setTotalCharges(totalCharges != null ? totalCharges : BigDecimal.ZERO);
        summary.setTotalHoldings(calculateTotalHoldings());
        
        List<Room> rooms = roomRepository.findAll();
        summary.setTotalRooms((long) rooms.size());
        
        long roomsWithBalance = rooms.stream()
                .mapToLong(room -> {
                    BigDecimal balance = room.getBalance() != null ? room.getBalance() : BigDecimal.ZERO;
                    return balance.compareTo(BigDecimal.ZERO) != 0 ? 1 : 0;
                })
                .sum();
        summary.setRoomsWithBalance(roomsWithBalance);
        
        return summary;
    }

    public RoomLedgerSummary getRoomLedgerSummary(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + roomId));
        
        List<Transaction> transactions = transactionRepository.findByRoomIdOrderByCreatedAtDesc(roomId);
        
        RoomLedgerSummary summary = new RoomLedgerSummary();
        summary.setRoom(room);
        summary.setCurrentBalance(room.getBalance() != null ? room.getBalance() : BigDecimal.ZERO);
        summary.setTransactionCount(transactions.size());
        
        BigDecimal totalPayments = transactions.stream()
                .filter(t -> "PAYMENT".equals(t.getTransactionType()) || "DEPOSIT".equals(t.getTransactionType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCharges = transactions.stream()
                .filter(t -> "CHARGE".equals(t.getTransactionType()) || "FEE".equals(t.getTransactionType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        summary.setTotalPayments(totalPayments);
        summary.setTotalCharges(totalCharges);
        summary.setRecentTransactions(transactions.stream().limit(10).collect(Collectors.toList()));
        
        return summary;
    }

    public String exportCsv(List<Transaction> txs) {
        StringBuilder sb = new StringBuilder();
        sb.append("date,room,description,type,category,amount,running_balance,paid_by,collected_by,reference,notes\n");
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        
        for (Transaction t : txs) {
            String roomName = t.getRoom() != null ? t.getRoom().getDisplayName() : "";
            sb.append(fmt.format(t.getCreatedAt() != null ? t.getCreatedAt() : LocalDateTime.now())).append(',')
              .append(escape(roomName)).append(',')
              .append(escape(nullToEmpty(t.getDescription()))).append(',')
              .append(escape(nullToEmpty(t.getTransactionType()))).append(',')
              .append(escape(nullToEmpty(t.getTransactionCategory()))).append(',')
              .append(t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO).append(',')
              .append(t.getRunningBalance() != null ? t.getRunningBalance() : BigDecimal.ZERO).append(',')
              .append(escape(nullToEmpty(t.getPaidBy()))).append(',')
              .append(escape(nullToEmpty(t.getCollectedBy()))).append(',')
              .append(escape(nullToEmpty(t.getReferenceNumber()))).append(',')
              .append(escape(nullToEmpty(t.getNotes()))).append('\n');
        }
        
        return sb.toString();
    }

    private static String escape(String s) {
        String v = s.replace("\"", "\"\"");
        return '"' + v + '"';
    }

    private static String nullToEmpty(String s) { 
        return s == null ? "" : s; 
    }

    // Inner classes for summary data
    public static class FinancialSummary {
        private BigDecimal totalHoldings;
        private BigDecimal totalPayments;
        private BigDecimal totalCharges;
        private Long totalRooms;
        private Long roomsWithBalance;

        // Getters and setters
        public BigDecimal getTotalHoldings() { return totalHoldings; }
        public void setTotalHoldings(BigDecimal totalHoldings) { this.totalHoldings = totalHoldings; }

        public BigDecimal getTotalPayments() { return totalPayments; }
        public void setTotalPayments(BigDecimal totalPayments) { this.totalPayments = totalPayments; }

        public BigDecimal getTotalCharges() { return totalCharges; }
        public void setTotalCharges(BigDecimal totalCharges) { this.totalCharges = totalCharges; }

        public Long getTotalRooms() { return totalRooms; }
        public void setTotalRooms(Long totalRooms) { this.totalRooms = totalRooms; }

        public Long getRoomsWithBalance() { return roomsWithBalance; }
        public void setRoomsWithBalance(Long roomsWithBalance) { this.roomsWithBalance = roomsWithBalance; }
    }

    public static class RoomLedgerSummary {
        private Room room;
        private BigDecimal currentBalance;
        private Integer transactionCount;
        private BigDecimal totalPayments;
        private BigDecimal totalCharges;
        private List<Transaction> recentTransactions;

        // Getters and setters
        public Room getRoom() { return room; }
        public void setRoom(Room room) { this.room = room; }

        public BigDecimal getCurrentBalance() { return currentBalance; }
        public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

        public Integer getTransactionCount() { return transactionCount; }
        public void setTransactionCount(Integer transactionCount) { this.transactionCount = transactionCount; }

        public BigDecimal getTotalPayments() { return totalPayments; }
        public void setTotalPayments(BigDecimal totalPayments) { this.totalPayments = totalPayments; }

        public BigDecimal getTotalCharges() { return totalCharges; }
        public void setTotalCharges(BigDecimal totalCharges) { this.totalCharges = totalCharges; }

        public List<Transaction> getRecentTransactions() { return recentTransactions; }
        public void setRecentTransactions(List<Transaction> recentTransactions) { this.recentTransactions = recentTransactions; }
    }
}
