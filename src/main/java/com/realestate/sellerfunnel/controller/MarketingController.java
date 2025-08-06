package com.realestate.sellerfunnel.controller;

import com.realestate.sellerfunnel.model.Campaign;
import com.realestate.sellerfunnel.model.ContentTemplate;
import com.realestate.sellerfunnel.repository.CampaignRepository;
import com.realestate.sellerfunnel.repository.CampaignLeadRepository;
import com.realestate.sellerfunnel.repository.ContentTemplateRepository;
import com.realestate.sellerfunnel.repository.BuyerRepository;
import com.realestate.sellerfunnel.repository.SellerRepository;
import com.realestate.sellerfunnel.service.CampaignPublishingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/marketing")
public class MarketingController {

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private CampaignLeadRepository campaignLeadRepository;

    @Autowired
    private ContentTemplateRepository templateRepository;

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private SellerRepository sellerRepository;
    
    @Autowired
    private CampaignPublishingService campaignPublishingService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Campaign statistics
        model.addAttribute("totalCampaigns", campaignRepository.count());
        model.addAttribute("activeCampaigns", campaignRepository.findActiveCampaigns(LocalDateTime.now()).size());
        model.addAttribute("totalLeads", campaignLeadRepository.count());
        
        // Recent campaigns
        model.addAttribute("recentCampaigns", campaignRepository.findAllByOrderByCreatedAtDesc());
        
        // Lead sources
        model.addAttribute("leadSources", campaignLeadRepository.getLeadCountBySource());
        
        // Monthly stats
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        
        Double monthlySpend = campaignRepository.getTotalSpendByDateRange(startOfMonth, endOfMonth);
        Integer monthlyLeads = campaignRepository.getTotalLeadsByDateRange(startOfMonth, endOfMonth);
        
        model.addAttribute("monthlySpend", monthlySpend != null ? monthlySpend : 0.0);
        model.addAttribute("monthlyLeads", monthlyLeads != null ? monthlyLeads : 0);
        
        // Calculate cost per lead
        if (monthlyLeads != null && monthlyLeads > 0 && monthlySpend != null && monthlySpend > 0) {
            model.addAttribute("costPerLead", monthlySpend / monthlyLeads);
        } else {
            model.addAttribute("costPerLead", 0.0);
        }
        
        return "admin/marketing/dashboard";
    }

    @GetMapping("/campaigns")
    public String campaigns(Model model) {
        model.addAttribute("campaigns", campaignRepository.findAllByOrderByCreatedAtDesc());
        return "admin/marketing/campaigns";
    }

    @GetMapping("/campaigns/new")
    public String newCampaign(Model model) {
        model.addAttribute("campaign", new Campaign());
        return "admin/marketing/campaign-form";
    }

    @GetMapping("/campaigns/{id}")
    public String viewCampaign(@PathVariable Long id, Model model) {
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        model.addAttribute("campaign", campaign);
        model.addAttribute("campaignLeads", campaignLeadRepository.findByCampaignOrderByCreatedAtDesc(campaign));
        
        return "admin/marketing/campaign-detail";
    }

    @GetMapping("/campaigns/{id}/edit")
    public String editCampaign(@PathVariable Long id, Model model) {
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        model.addAttribute("campaign", campaign);
        return "admin/marketing/campaign-form";
    }

    @PostMapping("/campaigns")
    public String saveCampaign(@Valid @ModelAttribute Campaign campaign, 
                              BindingResult result, 
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/marketing/campaign-form";
        }
        
        Campaign savedCampaign = campaignRepository.save(campaign);
        
        // Attempt to publish campaign if it's active
        if ("ACTIVE".equals(campaign.getStatus())) {
            boolean published = campaignPublishingService.publishCampaign(savedCampaign);
            if (published) {
                redirectAttributes.addFlashAttribute("message", "Campaign created and published successfully!");
            } else {
                redirectAttributes.addFlashAttribute("message", "Campaign saved. Publishing failed - check API configuration.");
            }
        } else {
            redirectAttributes.addFlashAttribute("message", "Campaign saved successfully!");
        }
        
        return "redirect:/admin/marketing/campaigns";
    }

    @PostMapping("/campaigns/{id}/status")
    public String updateCampaignStatus(@PathVariable Long id, 
                                      @RequestParam String status,
                                      RedirectAttributes redirectAttributes) {
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        campaign.setStatus(status);
        campaignRepository.save(campaign);
        
        redirectAttributes.addFlashAttribute("message", "Campaign status updated to " + status);
        
        return "redirect:/admin/marketing/campaigns/" + id;
    }
    
    @PostMapping("/campaigns/{id}/publish")
    public String publishCampaign(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        boolean published = campaignPublishingService.publishCampaign(campaign);
        
        if (published) {
            redirectAttributes.addFlashAttribute("message", "Campaign published successfully!");
        } else {
            redirectAttributes.addFlashAttribute("message", "Failed to publish campaign. Check API configuration.");
        }
        
        return "redirect:/admin/marketing/campaigns/" + id;
    }
    
    @GetMapping("/api-config")
    public String apiConfig() {
        return "admin/marketing/api-config";
    }

    @GetMapping("/templates")
    public String templates(Model model) {
        model.addAttribute("templates", templateRepository.findAllByOrderByCreatedAtDesc());
        return "admin/marketing/templates";
    }

    @GetMapping("/templates/new")
    public String newTemplate(Model model) {
        model.addAttribute("template", new ContentTemplate());
        return "admin/marketing/template-form";
    }

    @GetMapping("/templates/{id}/edit")
    public String editTemplate(@PathVariable Long id, Model model) {
        ContentTemplate template = templateRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Template not found"));
        
        model.addAttribute("template", template);
        return "admin/marketing/template-form";
    }

    @PostMapping("/templates")
    public String saveTemplate(@Valid @ModelAttribute("template") ContentTemplate template, 
                              BindingResult result, 
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/marketing/template-form";
        }
        
        templateRepository.save(template);
        redirectAttributes.addFlashAttribute("message", "Template saved successfully!");
        
        return "redirect:/admin/marketing/templates";
    }

    @GetMapping("/content-generator")
    public String contentGenerator(Model model) {
        model.addAttribute("sellerTemplates", templateRepository.findByTargetAudienceAndIsActiveTrueOrderByCreatedAtDesc("SELLERS"));
        model.addAttribute("buyerTemplates", templateRepository.findByTargetAudienceAndIsActiveTrueOrderByCreatedAtDesc("BUYERS"));
        model.addAttribute("generalTemplates", templateRepository.findByTargetAudienceAndIsActiveTrueOrderByCreatedAtDesc("BOTH"));
        
        return "admin/marketing/content-generator";
    }

    @GetMapping("/analytics")
    public String analytics(Model model) {
        // Campaign performance data
        model.addAttribute("campaignStats", campaignLeadRepository.getLeadCountByCampaign());
        model.addAttribute("sourceStats", campaignLeadRepository.getLeadCountBySource());
        
        // Recent activity
        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
        model.addAttribute("recentLeads", campaignLeadRepository.findByDateRange(lastWeek, LocalDateTime.now()));
        
        // Monthly comparison
        LocalDateTime thisMonth = LocalDateTime.now().withDayOfMonth(1);
        LocalDateTime lastMonth = thisMonth.minusMonths(1);
        
        Integer thisMonthLeads = campaignRepository.getTotalLeadsByDateRange(thisMonth, LocalDateTime.now());
        Integer lastMonthLeads = campaignRepository.getTotalLeadsByDateRange(lastMonth, thisMonth);
        
        model.addAttribute("thisMonthLeads", thisMonthLeads != null ? thisMonthLeads : 0);
        model.addAttribute("lastMonthLeads", lastMonthLeads != null ? lastMonthLeads : 0);
        
        // Growth calculation
        if (lastMonthLeads != null && lastMonthLeads > 0) {
            int growth = thisMonthLeads != null ? 
                (int) (((thisMonthLeads - lastMonthLeads) / (double) lastMonthLeads) * 100) : 
                -100;
            model.addAttribute("leadGrowth", growth);
        } else {
            model.addAttribute("leadGrowth", thisMonthLeads != null && thisMonthLeads > 0 ? 100 : 0);
        }
        
        return "admin/marketing/analytics";
    }
}