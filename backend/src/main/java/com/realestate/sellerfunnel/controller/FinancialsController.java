package com.realestate.sellerfunnel.controller;

import com.realestate.sellerfunnel.model.Booking;
import com.realestate.sellerfunnel.model.Payment;
import com.realestate.sellerfunnel.model.Room;
import com.realestate.sellerfunnel.repository.BookingRepository;
import com.realestate.sellerfunnel.repository.PaymentRepository;
import com.realestate.sellerfunnel.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/financials")
public class FinancialsController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    // Simple DTOs for the views
    public static class RoomBalanceView {
        public Long id;
        public String displayName;
        public BigDecimal balance;
    }

    public static class TransactionView {
        public LocalDateTime createdAt;
        public Room room;
        public String description;
        public BigDecimal amount;
        public String paidBy;
        public String collectedBy;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Total holdings = sum of completed payments
        BigDecimal totalHoldings = paymentRepository
            .findByPaymentStatusAndIsActiveTrue("COMPLETED")
            .stream()
            .map(Payment::getAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Rooms by balance (use active booking balance when present)
        List<RoomBalanceView> roomsByBalance = roomRepository.findByIsActiveTrueOrderByRoomNumberAsc()
            .stream()
            .map(r -> {
                RoomBalanceView v = new RoomBalanceView();
                v.id = r.getId();
                v.displayName = r.getDisplayName();
                Optional<Booking> active = bookingRepository.findActiveBookingByRoom(r);
                v.balance = active.map(Booking::getCurrentBalance).orElse(BigDecimal.ZERO);
                return v;
            })
            .sorted(Comparator.comparing((RoomBalanceView v) -> v.balance == null ? BigDecimal.ZERO : v.balance).reversed())
            .collect(Collectors.toList());

        // Recent transactions (latest payments)
        List<TransactionView> recentTransactions = paymentRepository.findTop10ByIsActiveTrueOrderByCreatedAtDesc()
            .stream()
            .map(this::toTransactionView)
            .collect(Collectors.toList());

        model.addAttribute("totalHoldings", totalHoldings != null ? totalHoldings : BigDecimal.ZERO);
        model.addAttribute("roomsByBalance", roomsByBalance);
        model.addAttribute("recentTransactions", recentTransactions);
        return "financials/dashboard";
    }

    @GetMapping("/ledger")
    public String ledger(
        Model model,
        @RequestParam(value = "roomId", required = false) Long roomId,
        @RequestParam(value = "start", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam(value = "end", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        // Defaults: last 7 days
        LocalDateTime defaultEnd = LocalDateTime.now();
        LocalDateTime defaultStart = defaultEnd.minusDays(7);
        LocalDateTime effectiveStart = start != null ? start : defaultStart;
        LocalDateTime effectiveEnd = end != null ? end : defaultEnd;

        List<Room> rooms = roomRepository.findByIsActiveTrueOrderByRoomNumberAsc();
        List<Payment> paymentsInRange = paymentRepository.findByCreatedAtBetweenAndIsActiveTrue(effectiveStart, effectiveEnd);

        List<TransactionView> transactions = paymentsInRange.stream()
            .filter(p -> roomId == null || (p.getBooking() != null && p.getBooking().getRoom() != null && Objects.equals(p.getBooking().getRoom().getId(), roomId)))
            .sorted(Comparator.comparing(Payment::getCreatedAt).reversed())
            .map(this::toTransactionView)
            .collect(Collectors.toList());

        model.addAttribute("rooms", rooms);
        model.addAttribute("transactions", transactions);
        model.addAttribute("selectedRoomId", roomId);
        model.addAttribute("start", formatForDatetimeLocal(effectiveStart));
        model.addAttribute("end", formatForDatetimeLocal(effectiveEnd));

        return "financials/ledger";
    }

    @GetMapping(value = "/export.csv", produces = "text/csv")
    @ResponseBody
    public String exportCsv(
        @RequestParam(value = "roomId", required = false) Long roomId,
        @RequestParam(value = "start", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam(value = "end", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        LocalDateTime defaultEnd = LocalDateTime.now();
        LocalDateTime defaultStart = defaultEnd.minusDays(30);
        LocalDateTime effectiveStart = start != null ? start : defaultStart;
        LocalDateTime effectiveEnd = end != null ? end : defaultEnd;

        List<Payment> payments = paymentRepository.findByCreatedAtBetweenAndIsActiveTrue(effectiveStart, effectiveEnd);
        List<TransactionView> transactions = payments.stream()
            .filter(p -> roomId == null || (p.getBooking() != null && p.getBooking().getRoom() != null && Objects.equals(p.getBooking().getRoom().getId(), roomId)))
            .sorted(Comparator.comparing(Payment::getCreatedAt).reversed())
            .map(this::toTransactionView)
            .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        sb.append("Date,Room,Description,Amount,Paid By,Received By\n");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
        for (TransactionView t : transactions) {
            String roomName = t.room != null ? t.room.getDisplayName().replace(",", " ") : "-";
            sb.append(t.createdAt != null ? fmt.format(t.createdAt) : "").append(',')
              .append(roomName).append(',')
              .append(safeCsv(t.description)).append(',')
              .append(t.amount != null ? t.amount : BigDecimal.ZERO).append(',')
              .append(safeCsv(t.paidBy)).append(',')
              .append(safeCsv(t.collectedBy))
              .append("\n");
        }
        return sb.toString();
    }

    private TransactionView toTransactionView(Payment p) {
        TransactionView v = new TransactionView();
        v.createdAt = p.getCreatedAt();
        v.room = (p.getBooking() != null) ? p.getBooking().getRoom() : null;
        v.description = p.getDescription() != null ? p.getDescription() : "Payment";
        v.amount = p.getAmount();
        // Paid by = guest name if available, else email
        String paidBy = null;
        if (p.getBooking() != null && p.getBooking().getGuest() != null) {
            paidBy = p.getBooking().getGuest().getFullName();
        }
        if (paidBy == null) {
            paidBy = p.getCustomerEmail();
        }
        v.paidBy = paidBy != null ? paidBy : "-";
        // Collected by: not tracked; leave blank dash
        v.collectedBy = null;
        return v;
    }

    private String formatForDatetimeLocal(LocalDateTime dt) {
        if (dt == null) return "";
        return dt.toString().length() > 16 ? dt.toString().substring(0, 16) : dt.toString();
    }

    private String safeCsv(String s) {
        if (s == null) return "";
        String cleaned = s.replace('\n', ' ').replace('\r', ' ').replace("\"", "''");
        if (cleaned.contains(",") || cleaned.contains("\"")) {
            return '"' + cleaned + '"';
        }
        return cleaned;
    }
}

