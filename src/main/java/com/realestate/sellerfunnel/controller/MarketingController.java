package com.realestate.sellerfunnel.controller;

import com.realestate.sellerfunnel.model.Campaign;
import com.realestate.sellerfunnel.model.ContentTemplate;
import com.realestate.sellerfunnel.repository.CampaignRepository;
import com.realestate.sellerfunnel.repository.CampaignLeadRepository;
import com.realestate.sellerfunnel.repository.ContentTemplateRepository;
import com.realestate.sellerfunnel.service.CampaignPublishingService;
import com.realestate.sellerfunnel.service.AIContentGenerationService;
import com.realestate.sellerfunnel.service.ContentMemoryService;
import com.realestate.sellerfunnel.service.CampaignPostSubmissionService;
import com.realestate.sellerfunnel.service.CampaignValidationService;
import com.realestate.sellerfunnel.model.AIGeneratedContent;
import com.realestate.sellerfunnel.repository.AIGeneratedContentRepository;
import com.realestate.sellerfunnel.repository.ClientRepository;
import com.realestate.sellerfunnel.service.FacebookPostService;
import com.realestate.sellerfunnel.service.CredentialManagementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

// Rest of the code remains unchanged

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
    private CampaignPublishingService campaignPublishingService;
    
    @Autowired
    private AIContentGenerationService aiContentGenerationService;
    
    @Autowired
    private ContentMemoryService contentMemoryService;
    
    @Autowired
    private AIGeneratedContentRepository aiGeneratedContentRepository;
    
    @Autowired
    private CampaignPostSubmissionService campaignPostSubmissionService;
    
    @Autowired
    private CampaignValidationService campaignValidationService;
    
    @Autowired
    private FacebookPostService facebookPostService;
    
    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private CredentialManagementService credentialManagementService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Campaign statistics
        model.addAttribute("totalCampaigns", campaignRepository.count());
        model.addAttribute("activeCampaigns", campaignRepository.findActiveCampaigns(LocalDateTime.now()).size());
        model.addAttribute("totalLeads", campaignLeadRepository.count());
        
        // Client statistics
        model.addAttribute("totalClients", clientRepository.count());
        model.addAttribute("activeClients", clientRepository.findByIsActiveTrueOrderByCreatedAtDesc().size());
        model.addAttribute("emailOptedInClients", clientRepository.findByEmailOptedInTrueAndIsActiveTrueOrderByCreatedAtDesc().size());
        
        // Client status breakdown
        List<Object[]> statusCounts = clientRepository.countByClientStatus();
        Map<String, Long> statusMap = statusCounts.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (Long) row[1]
            ));
        model.addAttribute("clientStatusCounts", statusMap);
        
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
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        // Basic form validation
        if (result.hasErrors()) {
            model.addAttribute("campaign", campaign);
            return "admin/marketing/campaign-form";
        }
        
        // Determine if this is a new campaign
        boolean isNewCampaign = campaign.getId() == null;
        
        // Process the campaign submission using our enhanced service
        CampaignPostSubmissionService.CampaignProcessResult processResult = 
            campaignPostSubmissionService.processCampaignSubmission(campaign, isNewCampaign);
        
        if (!processResult.isSuccess()) {
            // If processing failed, return to form with errors
            for (String error : processResult.getErrors()) {
                result.rejectValue("", "", error);
            }
            model.addAttribute("campaign", campaign);
            return "admin/marketing/campaign-form";
        }
        
        // Success! Add messages and redirect
        redirectAttributes.addFlashAttribute("message", processResult.getSuccessMessage());
        
        // Add warnings if any
        if (processResult.hasWarnings()) {
            redirectAttributes.addFlashAttribute("warnings", processResult.getWarnings());
        }
        
        // Add suggestions if any
        if (processResult.hasSuggestions()) {
            redirectAttributes.addFlashAttribute("suggestions", processResult.getSuggestions());
        }
        
        // Add completed actions
        if (!processResult.getCompletedActions().isEmpty()) {
            redirectAttributes.addFlashAttribute("actions", processResult.getCompletedActions());
        }
        
        // Get next steps for the user
        List<String> nextSteps = campaignPostSubmissionService.getNextSteps(processResult.getCampaign());
        redirectAttributes.addFlashAttribute("nextSteps", nextSteps);
        
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
    
    @PostMapping("/campaigns/{id}/activate")
    public String activateCampaign(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        boolean activated = campaignPublishingService.activateCampaign(campaign);
        
        if (activated) {
            redirectAttributes.addFlashAttribute("message", "Campaign activated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("message", "Failed to activate campaign. Check API configuration.");
        }
        
        return "redirect:/admin/marketing/campaigns/" + id;
    }
    
    @PostMapping("/campaigns/{id}/pause")
    public String pauseCampaign(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        boolean paused = campaignPublishingService.pauseCampaign(campaign);
        
        if (paused) {
            redirectAttributes.addFlashAttribute("message", "Campaign paused successfully!");
        } else {
            redirectAttributes.addFlashAttribute("message", "Failed to pause campaign. Check API configuration.");
        }
        
        return "redirect:/admin/marketing/campaigns/" + id;
    }
    
    @PostMapping("/campaigns/{id}/sync-stats")
    public String syncCampaignStats(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        try {
            campaignPublishingService.syncCampaignStats(campaign);
            redirectAttributes.addFlashAttribute("message", "Campaign statistics synced successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to sync campaign statistics: " + e.getMessage());
        }
        
        return "redirect:/admin/marketing/campaigns/" + id;
    }
    
    @PostMapping("/campaigns/{id}/post-now")
    public String postToFacebookNow(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        if (!campaign.getType().equals("FACEBOOK_POST") && !campaign.getType().equals("INSTAGRAM")) {
            redirectAttributes.addFlashAttribute("error", "This campaign type doesn't support direct posting.");
            return "redirect:/admin/marketing/campaigns/" + id;
        }
        
        if (!credentialManagementService.hasValidCredentials("FACEBOOK")) {
            // Redirect to credential configuration page
            redirectAttributes.addFlashAttribute("message", 
                "Please configure your Facebook API credentials first.");
            try {
                String returnUrl = java.net.URLEncoder.encode("/admin/marketing/campaigns/" + id, "UTF-8");
                return "redirect:/admin/marketing/facebook-credentials?returnUrl=" + returnUrl;
            } catch (Exception e) {
                return "redirect:/admin/marketing/facebook-credentials";
            }
        }
        
        boolean posted = facebookPostService.createPost(campaign);
        
        if (posted) {
            campaign.setStatus("ACTIVE");
            campaignRepository.save(campaign);
            redirectAttributes.addFlashAttribute("message", 
                "Successfully posted to Facebook! Campaign is now active.");
        } else {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to post to Facebook. Please check your API configuration and try again.");
        }
        
        return "redirect:/admin/marketing/campaigns/" + id;
    }
    
    @GetMapping("/campaigns/{id}/validate")
    @ResponseBody
    public CampaignValidationService.ValidationResult validateCampaign(@PathVariable Long id) {
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        return campaignValidationService.validateCampaign(campaign);
    }
    
    @PostMapping("/campaigns/{id}/duplicate")
    public String duplicateCampaign(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Campaign originalCampaign = campaignRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
        
        // Create a copy of the campaign
        Campaign duplicatedCampaign = new Campaign();
        duplicatedCampaign.setName(originalCampaign.getName() + " (Copy)");
        duplicatedCampaign.setType(originalCampaign.getType());
        duplicatedCampaign.setTargetAudience(originalCampaign.getTargetAudience());
        duplicatedCampaign.setStatus("DRAFT"); // Always start as draft
        duplicatedCampaign.setDescription(originalCampaign.getDescription());
        duplicatedCampaign.setBudget(originalCampaign.getBudget());
        duplicatedCampaign.setAdCopy(originalCampaign.getAdCopy());
        duplicatedCampaign.setHeadline(originalCampaign.getHeadline());
        duplicatedCampaign.setCallToAction(originalCampaign.getCallToAction());
        duplicatedCampaign.setKeywords(originalCampaign.getKeywords());
        duplicatedCampaign.setDemographicTargeting(originalCampaign.getDemographicTargeting());
        
        // Reset performance metrics
        duplicatedCampaign.setImpressions(0);
        duplicatedCampaign.setClicks(0);
        duplicatedCampaign.setLeads(0);
        duplicatedCampaign.setCost(null);
        
        CampaignPostSubmissionService.CampaignProcessResult result = 
            campaignPostSubmissionService.processCampaignSubmission(duplicatedCampaign, true);
        
        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("message", 
                "Campaign duplicated successfully as '" + duplicatedCampaign.getName() + "'");
        } else {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to duplicate campaign: " + String.join(", ", result.getErrors()));
        }
        
        return "redirect:/admin/marketing/campaigns";
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
        
        // Add AI-generated content
        model.addAttribute("aiSellerContent", aiGeneratedContentRepository.findByContentTypeAndTargetAudienceOrderByCreatedAtDesc("FACEBOOK_POST", "SELLERS"));
        model.addAttribute("aiBuyerContent", aiGeneratedContentRepository.findByContentTypeAndTargetAudienceOrderByCreatedAtDesc("FACEBOOK_POST", "BUYERS"));
        
        // Add content statistics
        Map<String, Object> contentStats = contentMemoryService.getContentStats();
        model.addAttribute("contentStats", contentStats);
        
        return "admin/marketing/content-generator";
    }
    
    @PostMapping("/content-generator/generate")
    @ResponseBody
    public Map<String, Object> generateAIContent(@RequestParam String prompt,
                                                @RequestParam String contentType,
                                                @RequestParam String targetAudience,
                                                @RequestParam(required = false) String category,
                                                @RequestParam(required = false) String context) {
        
        try {
            AIGeneratedContent generatedContent = aiContentGenerationService.generateContent(
                prompt, contentType, targetAudience, category, context, 3);
            
            return Map.of(
                "success", true,
                "content", generatedContent,
                "message", "Content generated successfully!"
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "Failed to generate content: " + e.getMessage()
            );
        }
    }
    
    @PostMapping("/content-generator/generate-variations")
    @ResponseBody
    public Map<String, Object> generateContentVariations(@RequestParam String prompt,
                                                        @RequestParam String contentType,
                                                        @RequestParam String targetAudience,
                                                        @RequestParam(required = false) String category,
                                                        @RequestParam(required = false) String context,
                                                        @RequestParam(defaultValue = "3") int count) {
        
        try {
            List<AIGeneratedContent> variations = aiContentGenerationService.generateContentVariations(
                prompt, contentType, targetAudience, category, context, count);
            
            return Map.of(
                "success", true,
                "variations", variations,
                "message", count + " content variations generated successfully!"
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "Failed to generate variations: " + e.getMessage()
            );
        }
    }
    
    @GetMapping("/content-generator/similarity-check")
    @ResponseBody
    public Map<String, Object> checkSimilarity(@RequestParam String content,
                                              @RequestParam String contentType,
                                              @RequestParam String targetAudience) {
        
        try {
            List<Map<String, Object>> similarContent = contentMemoryService.findSimilarContent(
                content, contentType, targetAudience);
            
            return Map.of(
                "success", true,
                "similarContent", similarContent,
                "count", similarContent.size()
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "Failed to check similarity: " + e.getMessage()
            );
        }
    }
    
    @GetMapping("/content-generator/suggestions")
    @ResponseBody
    public Map<String, Object> getSuggestions(@RequestParam String contentType,
                                             @RequestParam String targetAudience) {
        
        try {
            List<String> suggestions = contentMemoryService.getContentSuggestions(contentType, targetAudience);
            
            return Map.of(
                "success", true,
                "suggestions", suggestions
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "Failed to get suggestions: " + e.getMessage()
            );
        }
    }
    
    @PostMapping("/content-generator/use-content")
    @ResponseBody
    public Map<String, Object> useContent(@RequestParam Long contentId) {
        
        try {
            contentMemoryService.updateContentUsage(contentId);
            
            return Map.of(
                "success", true,
                "message", "Content usage updated successfully!"
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "Failed to update usage: " + e.getMessage()
            );
        }
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
    
    @GetMapping("/facebook-credentials")
    public String facebookCredentials(@RequestParam(required = false) String returnUrl, Model model) {
        model.addAttribute("returnUrl", returnUrl);
        model.addAttribute("isConfigured", credentialManagementService.hasValidCredentials("FACEBOOK"));
        
        // Load existing credentials if available
        var credentials = credentialManagementService.getFacebookCredentials();
        if (!credentials.isEmpty()) {
            // Show partial token for security (first 20 chars + ...)
            String accessToken = credentials.get("accessToken");
            if (accessToken != null && accessToken.length() > 20) {
                model.addAttribute("currentAccessToken", accessToken.substring(0, 20) + "...");
            }
            model.addAttribute("currentPageId", credentials.get("pageId"));
        }
        
        return "admin/marketing/facebook-credentials";
    }
    
    @PostMapping("/facebook-credentials")
    public String saveFacebookCredentials(@RequestParam String accessToken,
                                        @RequestParam String pageId,
                                        @RequestParam(required = false) String returnUrl,
                                        RedirectAttributes redirectAttributes) {
        try {
            // Test credentials first
            if (!credentialManagementService.testFacebookCredentials(accessToken, pageId)) {
                redirectAttributes.addFlashAttribute("error", "Invalid credentials provided.");
                if (returnUrl != null) {
                    redirectAttributes.addAttribute("returnUrl", returnUrl);
                }
                return "redirect:/admin/marketing/facebook-credentials";
            }
            
            // Save credentials
            credentialManagementService.saveFacebookCredentials(accessToken, pageId);
            redirectAttributes.addFlashAttribute("message", 
                "Facebook credentials saved successfully! You can now post to Facebook.");
            
            // Redirect back to original page or campaigns
            if (returnUrl != null && !returnUrl.isEmpty()) {
                return "redirect:" + returnUrl;
            } else {
                return "redirect:/admin/marketing/campaigns";
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error saving credentials: " + e.getMessage());
            if (returnUrl != null) {
                redirectAttributes.addAttribute("returnUrl", returnUrl);
            }
            return "redirect:/admin/marketing/facebook-credentials";
        }
    }
    
    @PostMapping("/facebook-credentials/test")
    @ResponseBody
    public Map<String, Object> testFacebookCredentials(@RequestParam String accessToken,
                                                      @RequestParam String pageId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isValid = credentialManagementService.testFacebookCredentials(accessToken, pageId);
            response.put("success", isValid);
            
            if (isValid) {
                response.put("message", "Credentials are valid and working!");
            } else {
                response.put("message", "Invalid credentials. Please check your access token and page ID.");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error testing credentials: " + e.getMessage());
        }
        
        return response;
    }
}