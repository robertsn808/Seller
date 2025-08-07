package com.realestate.sellerfunnel.controller;

import com.realestate.sellerfunnel.model.EmailCampaign;
import com.realestate.sellerfunnel.service.AutomatedEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.realestate.sellerfunnel.model.Client;

@Controller
@RequestMapping("/admin/automated-emails")
public class AutomatedEmailController {
    
    @Autowired
    private AutomatedEmailService automatedEmailService;
    
    @GetMapping
    public String automatedEmailsDashboard(Model model) {
        Map<String, Object> stats = automatedEmailService.getCampaignStatistics();
        model.addAttribute("stats", stats);
        return "admin/automated-emails/dashboard";
    }
    
    @GetMapping("/schedule")
    public String scheduleEmailForm(Model model) {
        model.addAttribute("campaign", new EmailCampaign());
        return "admin/automated-emails/schedule-form";
    }
    
    @PostMapping("/schedule")
    public String scheduleEmail(@RequestParam String campaignName,
                               @RequestParam String targetAudience,
                               @RequestParam(required = false) String clientTypeFilter,
                               @RequestParam(required = false) String leadSourceFilter,
                               @RequestParam String scheduledDate,
                               @RequestParam String scheduledTime,
                               @RequestParam String aiPrompt,
                               @RequestParam String contentType,
                               @RequestParam(required = false) String category,
                               RedirectAttributes redirectAttributes) {
        try {
            // Parse scheduled date and time
            String dateTimeString = scheduledDate + " " + scheduledTime;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime scheduledDateTime = LocalDateTime.parse(dateTimeString, formatter);
            
            // Schedule the email
            EmailCampaign campaign = automatedEmailService.scheduleAutomatedEmail(
                campaignName, targetAudience, clientTypeFilter, leadSourceFilter,
                scheduledDateTime, aiPrompt, contentType, category, "scheduled_campaign"
            );
            
            redirectAttributes.addFlashAttribute("message", 
                "Email campaign '" + campaignName + "' scheduled successfully for " + scheduledDateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")));
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to schedule email: " + e.getMessage());
        }
        
        return "redirect:/admin/automated-emails";
    }
    
    @PostMapping("/schedule-welcome-sequence")
    public String scheduleWelcomeSequence(@RequestParam String targetAudience,
                                         @RequestParam(required = false) String clientTypeFilter,
                                         @RequestParam(required = false) String leadSourceFilter,
                                         @RequestParam String aiPrompt,
                                         @RequestParam(required = false) String category,
                                         RedirectAttributes redirectAttributes) {
        try {
            List<EmailCampaign> campaigns = automatedEmailService.scheduleWelcomeSequence(
                targetAudience, clientTypeFilter, leadSourceFilter, aiPrompt, category
            );
            
            redirectAttributes.addFlashAttribute("message", 
                "Welcome sequence scheduled successfully with " + campaigns.size() + " emails");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to schedule welcome sequence: " + e.getMessage());
        }
        
        return "redirect:/admin/automated-emails";
    }
    
    @PostMapping("/schedule-follow-up-sequence")
    public String scheduleFollowUpSequence(@RequestParam String baseCampaignName,
                                          @RequestParam String targetAudience,
                                          @RequestParam(required = false) String clientTypeFilter,
                                          @RequestParam(required = false) String leadSourceFilter,
                                          @RequestParam String startDate,
                                          @RequestParam String startTime,
                                          @RequestParam String aiPrompt,
                                          @RequestParam String contentType,
                                          @RequestParam(required = false) String category,
                                          @RequestParam int numberOfEmails,
                                          @RequestParam int daysBetweenEmails,
                                          RedirectAttributes redirectAttributes) {
        try {
            // Parse start date and time
            String dateTimeString = startDate + " " + startTime;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime startDateTime = LocalDateTime.parse(dateTimeString, formatter);
            
            List<EmailCampaign> campaigns = automatedEmailService.scheduleFollowUpSequence(
                baseCampaignName, targetAudience, clientTypeFilter, leadSourceFilter,
                startDateTime, aiPrompt, contentType, category, numberOfEmails, daysBetweenEmails
            );
            
            redirectAttributes.addFlashAttribute("message", 
                "Follow-up sequence scheduled successfully with " + campaigns.size() + " emails");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to schedule follow-up sequence: " + e.getMessage());
        }
        
        return "redirect:/admin/automated-emails";
    }
    
    @PostMapping("/schedule-re-engagement")
    public String scheduleReEngagement(@RequestParam String targetAudience,
                                      @RequestParam(required = false) String clientTypeFilter,
                                      @RequestParam(required = false) String leadSourceFilter,
                                      @RequestParam String scheduledDate,
                                      @RequestParam String scheduledTime,
                                      @RequestParam String aiPrompt,
                                      @RequestParam(required = false) String category,
                                      RedirectAttributes redirectAttributes) {
        try {
            // Parse scheduled date and time
            String dateTimeString = scheduledDate + " " + scheduledTime;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime scheduledDateTime = LocalDateTime.parse(dateTimeString, formatter);
            
            EmailCampaign campaign = automatedEmailService.scheduleReEngagementCampaign(
                targetAudience, clientTypeFilter, leadSourceFilter, scheduledDateTime, aiPrompt, category
            );
            
            redirectAttributes.addFlashAttribute("message", 
                "Re-engagement campaign scheduled successfully for " + scheduledDateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")));
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to schedule re-engagement campaign: " + e.getMessage());
        }
        
        return "redirect:/admin/automated-emails";
    }
    
    @PostMapping("/schedule-seasonal")
    public String scheduleSeasonal(@RequestParam String season,
                                  @RequestParam String targetAudience,
                                  @RequestParam(required = false) String clientTypeFilter,
                                  @RequestParam(required = false) String leadSourceFilter,
                                  @RequestParam String scheduledDate,
                                  @RequestParam String scheduledTime,
                                  @RequestParam String aiPrompt,
                                  @RequestParam(required = false) String category,
                                  RedirectAttributes redirectAttributes) {
        try {
            // Parse scheduled date and time
            String dateTimeString = scheduledDate + " " + scheduledTime;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime scheduledDateTime = LocalDateTime.parse(dateTimeString, formatter);
            
            EmailCampaign campaign = automatedEmailService.scheduleSeasonalCampaign(
                season, targetAudience, clientTypeFilter, leadSourceFilter, scheduledDateTime, aiPrompt, category
            );
            
            redirectAttributes.addFlashAttribute("message", 
                season + " campaign scheduled successfully for " + scheduledDateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")));
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to schedule seasonal campaign: " + e.getMessage());
        }
        
        return "redirect:/admin/automated-emails";
    }
    
    @PostMapping("/{id}/cancel")
    public String cancelCampaign(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean cancelled = automatedEmailService.cancelScheduledCampaign(id);
            if (cancelled) {
                redirectAttributes.addFlashAttribute("message", "Campaign cancelled successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Cannot cancel campaign - it may have already been sent");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to cancel campaign: " + e.getMessage());
        }
        
        return "redirect:/admin/automated-emails";
    }
    
    @PostMapping("/{id}/reschedule")
    public String rescheduleCampaign(@PathVariable Long id,
                                    @RequestParam String newScheduledDate,
                                    @RequestParam String newScheduledTime,
                                    RedirectAttributes redirectAttributes) {
        try {
            // Parse new scheduled date and time
            String dateTimeString = newScheduledDate + " " + newScheduledTime;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime newScheduledDateTime = LocalDateTime.parse(dateTimeString, formatter);
            
            boolean rescheduled = automatedEmailService.rescheduleCampaign(id, newScheduledDateTime);
            if (rescheduled) {
                redirectAttributes.addFlashAttribute("message", 
                    "Campaign rescheduled successfully for " + newScheduledDateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")));
            } else {
                redirectAttributes.addFlashAttribute("error", "Cannot reschedule campaign - it may have already been sent");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to reschedule campaign: " + e.getMessage());
        }
        
        return "redirect:/admin/automated-emails";
    }
    
    @GetMapping("/templates")
    public String emailTemplates(Model model) {
        // Add common email templates and AI prompts
        model.addAttribute("templates", Map.of(
            "welcome", "Create a warm welcome email for new real estate clients",
            "follow_up", "Create a follow-up email to nurture leads and encourage engagement",
            "re_engagement", "Create a re-engagement email for inactive clients",
            "seasonal", "Create a seasonal email that's relevant and timely",
            "market_update", "Create a market update email with valuable insights",
            "property_alert", "Create a property alert email for new listings"
        ));
        
        return "admin/automated-emails/templates";
    }
    
    @GetMapping("/analytics")
    public String analytics(Model model) {
        Map<String, Object> stats = automatedEmailService.getCampaignStatistics();
        model.addAttribute("stats", stats);
        return "admin/automated-emails/analytics";
    }
    
    @GetMapping("/bulk-email")
    public String bulkEmailForm(Model model) {
        Map<String, Object> emailListStats = automatedEmailService.getEmailListStatistics();
        model.addAttribute("emailListStats", emailListStats);
        return "admin/automated-emails/bulk-email-form";
    }
    
    @PostMapping("/send-bulk-email")
    public String sendBulkEmail(@RequestParam String campaignName,
                               @RequestParam String aiPrompt,
                               @RequestParam String contentType,
                               @RequestParam(required = false) String category,
                               RedirectAttributes redirectAttributes) {
        try {
            EmailCampaign campaign = automatedEmailService.sendBulkEmail(
                campaignName, aiPrompt, contentType, category, "bulk_email"
            );
            
            redirectAttributes.addFlashAttribute("message", 
                "Bulk email '" + campaignName + "' sent successfully to " + campaign.getTotalRecipients() + " recipients!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to send bulk email: " + e.getMessage());
        }
        
        return "redirect:/admin/automated-emails";
    }
    
    @PostMapping("/send-filtered-bulk-email")
    public String sendFilteredBulkEmail(@RequestParam String campaignName,
                                       @RequestParam String targetAudience,
                                       @RequestParam(required = false) String clientTypeFilter,
                                       @RequestParam(required = false) String leadSourceFilter,
                                       @RequestParam String aiPrompt,
                                       @RequestParam String contentType,
                                       @RequestParam(required = false) String category,
                                       RedirectAttributes redirectAttributes) {
        try {
            EmailCampaign campaign = automatedEmailService.sendFilteredBulkEmail(
                campaignName, targetAudience, clientTypeFilter, leadSourceFilter, 
                aiPrompt, contentType, category, "filtered_bulk_email"
            );
            
            redirectAttributes.addFlashAttribute("message", 
                "Filtered bulk email '" + campaignName + "' sent successfully to " + campaign.getTotalRecipients() + " recipients!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to send filtered bulk email: " + e.getMessage());
        }
        
        return "redirect:/admin/automated-emails";
    }
    
    @GetMapping("/email-list")
    public String emailList(Model model) {
        Map<String, Object> emailListStats = automatedEmailService.getEmailListStatistics();
        List<Map<String, Object>> emailList = automatedEmailService.getEmailListForExport();
        
        model.addAttribute("emailListStats", emailListStats);
        model.addAttribute("emailList", emailList);
        return "admin/automated-emails/email-list";
    }
    
    @GetMapping("/email-list/export")
    @ResponseBody
    public String exportEmailList() {
        try {
            List<Map<String, Object>> emailList = automatedEmailService.getEmailListForExport();
            
            // Create CSV content
            StringBuilder csv = new StringBuilder();
            csv.append("Email,First Name,Last Name,Client Type,Lead Source,Created Date,Last Contact Date\n");
            
            for (Map<String, Object> client : emailList) {
                csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    client.get("email"),
                    client.get("firstName"),
                    client.get("lastName"),
                    client.get("clientType"),
                    client.get("leadSource"),
                    client.get("createdAt"),
                    client.get("lastContactDate")
                ));
            }
            
            return csv.toString();
            
        } catch (Exception e) {
            return "Error exporting email list: " + e.getMessage();
        }
    }
    
    @GetMapping("/email-list/preview")
    @ResponseBody
    public Map<String, Object> previewEmailList(@RequestParam(required = false) String targetAudience,
                                               @RequestParam(required = false) String clientTypeFilter,
                                               @RequestParam(required = false) String leadSourceFilter) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Client> targetClients = automatedEmailService.getTargetClients(targetAudience, clientTypeFilter, leadSourceFilter);
            
            response.put("success", true);
            response.put("recipientCount", targetClients.size());
            response.put("recipients", targetClients.stream()
                .map(c -> Map.of(
                    "email", c.getEmail(),
                    "name", c.getFullName(),
                    "clientType", c.getClientType(),
                    "leadSource", c.getLeadSource()
                ))
                .collect(java.util.stream.Collectors.toList()));
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
}
