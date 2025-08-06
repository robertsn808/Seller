package com.realestate.sellerfunnel.controller;

import com.realestate.sellerfunnel.model.Client;
import com.realestate.sellerfunnel.model.EmailCampaign;
import com.realestate.sellerfunnel.repository.ClientRepository;
import com.realestate.sellerfunnel.repository.EmailCampaignRepository;
import com.realestate.sellerfunnel.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/email-campaigns")
public class EmailCampaignController {
    
    @Autowired
    private EmailCampaignRepository emailCampaignRepository;
    
    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private EmailService emailService;
    
    @GetMapping
    public String listCampaigns(Model model) {
        List<EmailCampaign> campaigns = emailCampaignRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        model.addAttribute("campaigns", campaigns);
        
        // Add statistics
        model.addAttribute("totalCampaigns", emailCampaignRepository.count());
        model.addAttribute("draftCampaigns", emailCampaignRepository.findByStatusOrderByCreatedAtDesc("DRAFT").size());
        model.addAttribute("scheduledCampaigns", emailCampaignRepository.findByStatusOrderByCreatedAtDesc("SCHEDULED").size());
        model.addAttribute("sentCampaigns", emailCampaignRepository.findByStatusOrderByCreatedAtDesc("SENT").size());
        
        return "admin/email-campaigns/list";
    }
    
    @GetMapping("/new")
    public String newCampaign(Model model) {
        EmailCampaign campaign = new EmailCampaign();
        campaign.setSenderName("Real Estate Team");
        campaign.setSenderEmail("noreply@realestate.com");
        model.addAttribute("campaign", campaign);
        
        // Add client counts for targeting
        model.addAttribute("totalClients", clientRepository.count());
        model.addAttribute("emailOptedInClients", clientRepository.findByEmailOptedInTrueAndIsActiveTrueOrderByCreatedAtDesc().size());
        model.addAttribute("sellerClients", clientRepository.findByClientTypeAndEmailOptedInTrueAndIsActiveTrueOrderByCreatedAtDesc("SELLER").size());
        model.addAttribute("buyerClients", clientRepository.findByClientTypeAndEmailOptedInTrueAndIsActiveTrueOrderByCreatedAtDesc("BUYER").size());
        model.addAttribute("investorClients", clientRepository.findByClientTypeAndEmailOptedInTrueAndIsActiveTrueOrderByCreatedAtDesc("INVESTOR").size());
        
        return "admin/email-campaigns/form";
    }
    
    @GetMapping("/{id}")
    public String viewCampaign(@PathVariable Long id, Model model) {
        Optional<EmailCampaign> campaignOpt = emailCampaignRepository.findById(id);
        if (campaignOpt.isPresent()) {
            EmailCampaign campaign = campaignOpt.get();
            model.addAttribute("campaign", campaign);
            
            // Calculate estimated recipients
            int estimatedRecipients = calculateEstimatedRecipients(campaign);
            model.addAttribute("estimatedRecipients", estimatedRecipients);
            
            return "admin/email-campaigns/view";
        }
        return "redirect:/admin/email-campaigns";
    }
    
    @GetMapping("/{id}/edit")
    public String editCampaign(@PathVariable Long id, Model model) {
        Optional<EmailCampaign> campaignOpt = emailCampaignRepository.findById(id);
        if (campaignOpt.isPresent()) {
            model.addAttribute("campaign", campaignOpt.get());
            return "admin/email-campaigns/form";
        }
        return "redirect:/admin/email-campaigns";
    }
    
    @PostMapping
    public String saveCampaign(@Valid @ModelAttribute EmailCampaign campaign, 
                              BindingResult result, 
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/email-campaigns/form";
        }
        
        // Set default values
        if (campaign.getSenderName() == null || campaign.getSenderName().isEmpty()) {
            campaign.setSenderName("Real Estate Team");
        }
        if (campaign.getSenderEmail() == null || campaign.getSenderEmail().isEmpty()) {
            campaign.setSenderEmail("noreply@realestate.com");
        }
        
        emailCampaignRepository.save(campaign);
        redirectAttributes.addFlashAttribute("message", "Email campaign saved successfully!");
        
        return "redirect:/admin/email-campaigns";
    }
    
    @PostMapping("/{id}/send")
    public String sendCampaign(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<EmailCampaign> campaignOpt = emailCampaignRepository.findById(id);
        if (campaignOpt.isPresent()) {
            EmailCampaign campaign = campaignOpt.get();
            
            // Get target clients
            List<Client> targetClients = getTargetClients(campaign);
            
            if (targetClients.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No eligible recipients found for this campaign.");
                return "redirect:/admin/email-campaigns/" + id;
            }
            
            // Send the campaign
            EmailService.EmailCampaignResult result = emailService.sendEmailCampaign(campaign, targetClients);
            
            // Save updated campaign
            emailCampaignRepository.save(campaign);
            
            redirectAttributes.addFlashAttribute("message", 
                String.format("Campaign sent! %d emails sent, %d failed, %d skipped.", 
                    result.getSentCount(), result.getFailedCount(), result.getSkippedCount()));
        }
        
        return "redirect:/admin/email-campaigns/" + id;
    }
    
    @PostMapping("/{id}/schedule")
    public String scheduleCampaign(@PathVariable Long id, 
                                  @RequestParam String scheduledDate,
                                  RedirectAttributes redirectAttributes) {
        Optional<EmailCampaign> campaignOpt = emailCampaignRepository.findById(id);
        if (campaignOpt.isPresent()) {
            EmailCampaign campaign = campaignOpt.get();
            
            try {
                LocalDateTime scheduledDateTime = LocalDateTime.parse(scheduledDate);
                campaign.setScheduledDate(scheduledDateTime);
                campaign.setIsScheduled(true);
                campaign.setStatus("SCHEDULED");
                
                emailCampaignRepository.save(campaign);
                redirectAttributes.addFlashAttribute("message", "Campaign scheduled successfully!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Invalid date format. Please use YYYY-MM-DDTHH:MM:SS");
            }
        }
        
        return "redirect:/admin/email-campaigns/" + id;
    }
    
    @PostMapping("/{id}/cancel")
    public String cancelCampaign(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<EmailCampaign> campaignOpt = emailCampaignRepository.findById(id);
        if (campaignOpt.isPresent()) {
            EmailCampaign campaign = campaignOpt.get();
            campaign.setStatus("CANCELLED");
            campaign.setIsScheduled(false);
            emailCampaignRepository.save(campaign);
            redirectAttributes.addFlashAttribute("message", "Campaign cancelled successfully!");
        }
        return "redirect:/admin/email-campaigns/" + id;
    }
    
    @PostMapping("/{id}/delete")
    public String deleteCampaign(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<EmailCampaign> campaignOpt = emailCampaignRepository.findById(id);
        if (campaignOpt.isPresent()) {
            EmailCampaign campaign = campaignOpt.get();
            campaign.setIsActive(false);
            emailCampaignRepository.save(campaign);
            redirectAttributes.addFlashAttribute("message", "Campaign deleted successfully!");
        }
        return "redirect:/admin/email-campaigns";
    }
    
    @GetMapping("/{id}/preview")
    public String previewCampaign(@PathVariable Long id, Model model) {
        Optional<EmailCampaign> campaignOpt = emailCampaignRepository.findById(id);
        if (campaignOpt.isPresent()) {
            EmailCampaign campaign = campaignOpt.get();
            model.addAttribute("campaign", campaign);
            
            // Get a sample client for preview
            List<Client> sampleClients = clientRepository.findByEmailOptedInTrueAndIsActiveTrueOrderByCreatedAtDesc();
            if (!sampleClients.isEmpty()) {
                Client sampleClient = sampleClients.get(0);
                model.addAttribute("sampleClient", sampleClient);
                
                // Personalize content for preview
                String personalizedContent = personalizeContent(campaign.getContent(), sampleClient);
                String personalizedSubject = personalizeContent(campaign.getSubjectLine(), sampleClient);
                model.addAttribute("personalizedContent", personalizedContent);
                model.addAttribute("personalizedSubject", personalizedSubject);
            }
            
            return "admin/email-campaigns/preview";
        }
        return "redirect:/admin/email-campaigns";
    }
    
    @GetMapping("/analytics")
    public String analytics(Model model) {
        // Campaign statistics
        List<Object[]> statusStats = emailCampaignRepository.countByStatus();
        model.addAttribute("statusStats", statusStats);
        
        List<Object[]> audienceStats = emailCampaignRepository.countByTargetAudience();
        model.addAttribute("audienceStats", audienceStats);
        
        // Recent campaigns
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<EmailCampaign> recentCampaigns = emailCampaignRepository.findRecentCampaigns(thirtyDaysAgo);
        model.addAttribute("recentCampaigns", recentCampaigns);
        
        // High engagement campaigns
        List<EmailCampaign> highEngagementCampaigns = emailCampaignRepository.findHighEngagementCampaigns(25.0);
        model.addAttribute("highEngagementCampaigns", highEngagementCampaigns);
        
        // Campaigns needing follow-up
        List<EmailCampaign> campaignsNeedingFollowUp = emailCampaignRepository.findCampaignsNeedingFollowUp();
        model.addAttribute("campaignsNeedingFollowUp", campaignsNeedingFollowUp);
        
        return "admin/email-campaigns/analytics";
    }
    
    @PostMapping("/test-email")
    @ResponseBody
    public String testEmail(@RequestParam String testEmail) {
        boolean success = emailService.testEmailConfiguration(testEmail);
        return success ? "Test email sent successfully!" : "Failed to send test email. Check configuration.";
    }
    
    /**
     * Calculate estimated recipients for a campaign
     */
    private int calculateEstimatedRecipients(EmailCampaign campaign) {
        List<Client> targetClients = getTargetClients(campaign);
        return targetClients.size();
    }
    
    /**
     * Get target clients for a campaign based on filters
     */
    private List<Client> getTargetClients(EmailCampaign campaign) {
        if ("ALL".equals(campaign.getTargetAudience())) {
            return clientRepository.findByEmailOptedInTrueAndIsActiveTrueOrderByCreatedAtDesc();
        } else if (campaign.getClientTypeFilter() != null && !campaign.getClientTypeFilter().isEmpty()) {
            return clientRepository.findByClientTypeAndEmailOptedInTrueAndIsActiveTrueOrderByCreatedAtDesc(campaign.getClientTypeFilter());
        } else if (campaign.getLeadSourceFilter() != null && !campaign.getLeadSourceFilter().isEmpty()) {
            return clientRepository.findByLeadSourceAndEmailOptedInTrueAndIsActiveTrueOrderByCreatedAtDesc(campaign.getLeadSourceFilter());
        } else {
            // Default to all opted-in clients
            return clientRepository.findByEmailOptedInTrueAndIsActiveTrueOrderByCreatedAtDesc();
        }
    }
    
    /**
     * Personalize content with client information
     */
    private String personalizeContent(String content, Client client) {
        if (content == null) return "";
        
        return content
            .replace("{{firstName}}", client.getFirstName() != null ? client.getFirstName() : "")
            .replace("{{lastName}}", client.getLastName() != null ? client.getLastName() : "")
            .replace("{{fullName}}", client.getFullName())
            .replace("{{email}}", client.getEmail())
            .replace("{{company}}", client.getCompanyName() != null ? client.getCompanyName() : "")
            .replace("{{city}}", client.getCity() != null ? client.getCity() : "")
            .replace("{{state}}", client.getState() != null ? client.getState() : "")
            .replace("{{phone}}", client.getPhoneNumber() != null ? client.getPhoneNumber() : "");
    }
} 