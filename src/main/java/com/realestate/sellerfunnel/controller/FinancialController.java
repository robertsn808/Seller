package com.realestate.sellerfunnel.controller;

import com.realestate.sellerfunnel.service.FinancialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;

@Controller
@RequestMapping("/financials")
public class FinancialController {

    @Autowired
    private FinancialService financialService;

    private boolean isAuthenticated(HttpSession session) {
        Boolean authenticated = (Boolean) session.getAttribute("propertyAuthenticated");
        return authenticated != null && authenticated;
    }

    @GetMapping("/dashboard")
    public String getFinancialDashboard(Model model, HttpSession session) {
        if (!isAuthenticated(session)) {
            return "redirect:/property/login";
        }

        BigDecimal totalHoldings = financialService.calculateTotalHoldings();
        model.addAttribute("totalHoldings", totalHoldings);
        model.addAttribute("roomsByBalance", financialService.listRoomsByBalanceDesc());
        model.addAttribute("recentTransactions", financialService.recentTransactions());

        return "financials/dashboard";
    }

    @GetMapping("/ledger")
    public String getLedger(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            Model model,
            HttpSession session) {
        if (!isAuthenticated(session)) {
            return "redirect:/property/login";
        }
        java.util.List<com.realestate.sellerfunnel.model.Transaction> txs;
        if (start != null && end != null) {
            var s = java.time.LocalDateTime.parse(start);
            var e = java.time.LocalDateTime.parse(end);
            txs = financialService.transactionsByDateRange(s, e);
        } else if (roomId != null) {
            txs = financialService.transactionsForRoom(roomId);
        } else {
            txs = financialService.recentTransactions();
        }
        model.addAttribute("transactions", txs);
        model.addAttribute("rooms", financialService.listRoomsByBalanceDesc());
        model.addAttribute("selectedRoomId", roomId);
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        return "financials/ledger";
    }

    @GetMapping(value = "/export.csv")
    @ResponseBody
    public String exportCsv(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            HttpSession session) {
        if (!isAuthenticated(session)) {
            return "";
        }
        java.util.List<com.realestate.sellerfunnel.model.Transaction> txs;
        if (start != null && end != null) {
            var s = java.time.LocalDateTime.parse(start);
            var e = java.time.LocalDateTime.parse(end);
            txs = financialService.transactionsByDateRange(s, e);
        } else if (roomId != null) {
            txs = financialService.transactionsForRoom(roomId);
        } else {
            txs = financialService.recentTransactions();
        }
        return financialService.exportCsv(txs);
    }
}
