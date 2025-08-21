package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.Room;
import com.realestate.sellerfunnel.repository.RoomRepository;
import com.realestate.sellerfunnel.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class FinancialService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public BigDecimal calculateTotalHoldings() {
        return roomRepository.findAll().stream()
                .map(Room::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public java.util.List<Room> listRoomsByBalanceDesc() {
        return roomRepository.findAll().stream()
                .sorted((a, b) -> b.getBalance().compareTo(a.getBalance()))
                .toList();
    }

    public java.util.List<com.realestate.sellerfunnel.model.Transaction> recentTransactions() {
        return transactionRepository.findTop50ByOrderByCreatedAtDesc();
    }

    public java.util.List<com.realestate.sellerfunnel.model.Transaction> transactionsForRoom(Long roomId) {
        return transactionRepository.findByRoomIdOrderByCreatedAtDesc(roomId);
    }

    public java.util.List<com.realestate.sellerfunnel.model.Transaction> transactionsByDateRange(java.time.LocalDateTime start,
                                                                                               java.time.LocalDateTime end) {
        return transactionRepository.findByDateRange(start, end);
    }

    public String exportCsv(java.util.List<com.realestate.sellerfunnel.model.Transaction> txs) {
        StringBuilder sb = new StringBuilder();
        sb.append("date,room,description,amount,paid_by,collected_by\n");
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        for (var t : txs) {
            String roomName = t.getRoom() != null ? t.getRoom().getDisplayName() : "";
            sb.append(fmt.format(t.getCreatedAt() != null ? t.getCreatedAt() : java.time.LocalDateTime.now())).append(',')
              .append(escape(roomName)).append(',')
              .append(escape(nullToEmpty(t.getDescription()))).append(',')
              .append(t.getAmount() != null ? t.getAmount() : java.math.BigDecimal.ZERO).append(',')
              .append(escape(nullToEmpty(t.getPaidBy()))).append(',')
              .append(escape(nullToEmpty(t.getCollectedBy()))).append('\n');
        }
        return sb.toString();
    }

    private static String escape(String s) {
        String v = s.replace("\"", "\"\"");
        return '"' + v + '"';
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }
}
