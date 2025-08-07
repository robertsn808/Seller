package com.realestate.sellerfunnel.controller;

import com.realestate.sellerfunnel.service.EmailCampaignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/email")
public class EmailCampaignController {

    @Autowired
    private EmailCampaignService emailCampaignService;

    @GetMapping
    public String showEmailCampaignForm(Model model) {
        return "admin/email/campaign";
    }

    @PostMapping("/send")
    public String sendBulkEmail(@RequestParam String subject,
                              @RequestParam String messageText,
                              @RequestParam(required = false) String clientType,
                              @RequestParam(required = false) String clientStatus,
                              @RequestParam(required = false) String leadSource,
                              @RequestParam(required = false) String city,
                              @RequestParam(required = false) String state,
                              @RequestParam(defaultValue = "false") boolean useAI,
                              @RequestParam(defaultValue = "false") boolean useTemplate,
                              @RequestParam(required = false) String templateName,
                              RedirectAttributes redirectAttributes) {
        try {
            String campaignId = emailCampaignService.startEmailCampaign(
                subject, messageText, clientType, clientStatus, leadSource, 
                city, state, useAI, useTemplate, templateName).get();
            
            redirectAttributes.addFlashAttribute("message", 
                "Email campaign started successfully. Use the progress tracker to monitor the campaign.");
            return "redirect:/admin/email/progress/" + campaignId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to start email campaign: " + e.getMessage());
            return "redirect:/admin/email";
        }
    }

    @GetMapping("/progress/{campaignId}")
    public String showCampaignProgress(@PathVariable String campaignId, Model model) {
        EmailCampaignService.CampaignProgress progress = emailCampaignService.getCampaignProgress(campaignId);
        if (progress == null) {
            model.addAttribute("error", "Campaign not found");
            return "admin/email/campaign";
        }
        
        model.addAttribute("campaignId", campaignId);
        model.addAttribute("progress", progress);
        return "admin/email/progress";
    }

    @GetMapping("/progress/{campaignId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCampaignStatus(@PathVariable String campaignId) {
        EmailCampaignService.CampaignProgress progress = emailCampaignService.getCampaignProgress(campaignId);
        if (progress == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", progress.getStatus());
        response.put("progress", progress.getProgress());
        response.put("processedCount", progress.getProcessedCount());
        response.put("totalRecipients", progress.getTotalRecipients());
        response.put("successCount", progress.getSuccessCount());
        response.put("errorCount", progress.getErrorCount());
        response.put("skippedCount", progress.getSkippedCount());
        response.put("recentErrors", progress.getRecentErrors());
        
        if (progress.getErrorMessage() != null) {
            response.put("errorMessage", progress.getErrorMessage());
        }

        return ResponseEntity.ok(response);
    }
}