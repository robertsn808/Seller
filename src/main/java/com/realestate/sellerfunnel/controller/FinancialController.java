package com.realestate.sellerfunnel.controller;

import com.realestate.sellerfunnel.service.FinancialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

        return "financials/dashboard";
    }
}
