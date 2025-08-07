package com.realestate.sellerfunnel.controller;

import com.realestate.sellerfunnel.service.SMSCampaignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/sms")
public class SMSCampaignController {

    @Autowired
    private SMSCampaignService smsCampaignService;

    @GetMapping
    public String showSMSCampaignForm(Model model) {
        return "admin/sms/campaign";
    }

    @PostMapping("/send")
    public String sendBulkSMS(@RequestParam String messageText,
                            @RequestParam(required = false) String clientType,
                            @RequestParam(required = false) String clientStatus,
                            @RequestParam(required = false) String leadSource,
                            @RequestParam(required = false) String city,
                            @RequestParam(required = false) String state,
                            @RequestParam(defaultValue = "false") boolean useAI,
                            RedirectAttributes redirectAttributes) {
        try {
            String campaignId = smsCampaignService.startBulkSMSCampaign(
                messageText, clientType, clientStatus, leadSource, city, state, useAI).get();
            
            redirectAttributes.addFlashAttribute("message", 
                "SMS campaign started successfully. Use the progress tracker to monitor the campaign.");
            return "redirect:/admin/sms/progress/" + campaignId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to start SMS campaign: " + e.getMessage());
            return "redirect:/admin/sms";
        }
    }

    @GetMapping("/progress/{campaignId}")
    public String showCampaignProgress(@PathVariable String campaignId, Model model) {
        SMSCampaignService.CampaignProgress progress = smsCampaignService.getCampaignProgress(campaignId);
        if (progress == null) {
            model.addAttribute("error", "Campaign not found");
            return "admin/sms/campaign";
        }
        
        model.addAttribute("campaignId", campaignId);
        model.addAttribute("progress", progress);
        return "admin/sms/progress";
    }

    @GetMapping("/progress/{campaignId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCampaignStatus(@PathVariable String campaignId) {
        SMSCampaignService.CampaignProgress progress = smsCampaignService.getCampaignProgress(campaignId);
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
