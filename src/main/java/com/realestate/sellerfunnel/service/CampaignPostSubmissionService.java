package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.Campaign;
import com.realestate.sellerfunnel.repository.CampaignRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CampaignPostSubmissionService {
    
    private static final Logger logger = LoggerFactory.getLogger(CampaignPostSubmissionService.class);
    
    @Autowired
    private CampaignRepository campaignRepository;
    
    @Autowired
    private CampaignValidationService validationService;
    
    @Autowired
    private CampaignPublishingService publishingService;
    
    // We'll create this service next
    // @Autowired
    // private NotificationService notificationService;
    
    /**
     * Process campaign after successful form submission
     */
    public CampaignProcessResult processCampaignSubmission(Campaign campaign, boolean isNewCampaign) {
        CampaignProcessResult result = new CampaignProcessResult();
        
        try {
            // Step 1: Set timestamps
            if (isNewCampaign) {
                campaign.setCreatedAt(LocalDateTime.now());
            }
            campaign.setUpdatedAt(LocalDateTime.now());
            
            // Step 2: Set default values
            setDefaultValues(campaign);
            
            // Step 3: Advanced validation
            CampaignValidationService.ValidationResult validation = validationService.validateCampaign(campaign);
            if (!validation.isValid()) {
                result.setSuccess(false);
                result.getErrors().addAll(validation.getErrors());
                return result;
            }
            result.getWarnings().addAll(validation.getWarnings());
            
            // Step 4: Save the campaign
            Campaign savedCampaign = campaignRepository.save(campaign);
            result.setCampaign(savedCampaign);
            
            // Step 5: Post-save processing
            performPostSaveActions(savedCampaign, isNewCampaign, result);
            
            // Step 6: Generate success message and suggestions
            generateSuccessMessage(savedCampaign, isNewCampaign, result);
            
            result.setSuccess(true);
            logger.info("Campaign {} processed successfully", savedCampaign.getName());
            
        } catch (Exception e) {
            logger.error("Error processing campaign submission: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.addError("An unexpected error occurred while saving the campaign. Please try again.");
        }
        
        return result;
    }
    
    private void setDefaultValues(Campaign campaign) {
        // Set default status if not provided
        if (campaign.getStatus() == null || campaign.getStatus().trim().isEmpty()) {
            campaign.setStatus("DRAFT");
        }
        
        // Initialize performance metrics if null
        if (campaign.getImpressions() == null) {
            campaign.setImpressions(0);
        }
        if (campaign.getClicks() == null) {
            campaign.setClicks(0);
        }
        if (campaign.getLeads() == null) {
            campaign.setLeads(0);
        }
        
        // Set default start date if active campaign has no start date
        if ("ACTIVE".equals(campaign.getStatus()) && campaign.getStartDate() == null) {
            campaign.setStartDate(LocalDateTime.now());
        }
    }
    
    private void performPostSaveActions(Campaign campaign, boolean isNewCampaign, CampaignProcessResult result) {
        List<String> actions = new ArrayList<>();
        
        // Action 1: Attempt to publish if active
        if ("ACTIVE".equals(campaign.getStatus())) {
            boolean published = attemptPublication(campaign, result);
            if (published) {
                actions.add("Campaign published to " + campaign.getType());
            } else {
                actions.add("Campaign saved (publishing will need to be done manually)");
            }
        }
        
        // Action 2: Create follow-up tasks
        createFollowUpTasks(campaign, isNewCampaign, actions);
        
        // Action 3: Send notifications (if we had a notification service)
        // sendNotifications(campaign, isNewCampaign);
        
        // Action 4: Log important events
        logCampaignEvents(campaign, isNewCampaign);
        
        result.getCompletedActions().addAll(actions);
    }
    
    private boolean attemptPublication(Campaign campaign, CampaignProcessResult result) {
        try {
            // Additional validation for publication
            CampaignValidationService.ValidationResult pubValidation = validationService.validateForPublication(campaign);
            
            if (!pubValidation.isValid()) {
                result.addWarning("Campaign saved but not published: " + String.join(", ", pubValidation.getErrors()));
                // Set status back to draft if publication validation fails
                campaign.setStatus("DRAFT");
                campaignRepository.save(campaign);
                return false;
            }
            
            // Attempt to publish
            return publishingService.publishCampaign(campaign);
            
        } catch (Exception e) {
            logger.error("Error during campaign publication: {}", e.getMessage());
            result.addWarning("Campaign saved but publication failed: " + e.getMessage());
            return false;
        }
    }
    
    private void createFollowUpTasks(Campaign campaign, boolean isNewCampaign, List<String> actions) {
        // Create follow-up tasks based on campaign type and status
        switch (campaign.getType()) {
            case "FACEBOOK":
                if ("ACTIVE".equals(campaign.getStatus())) {
                    actions.add("Monitor Facebook campaign performance in Ads Manager");
                    actions.add("Check campaign metrics in 24-48 hours");
                }
                break;
                
            case "GOOGLE_ADS":
                if ("ACTIVE".equals(campaign.getStatus())) {
                    actions.add("Monitor Google Ads campaign in Google Ads console");
                    actions.add("Review keyword performance after first week");
                }
                break;
                
            case "CRAIGSLIST":
                actions.add("Manually post to Craigslist using the saved ad copy");
                actions.add("Schedule renewal posts for maximum visibility");
                break;
                
            case "DIRECT_MAIL":
                actions.add("Prepare mailing list and design materials");
                actions.add("Schedule printing and mailing timeline");
                break;
                
            case "YARD_SIGNS":
                actions.add("Order yard signs with campaign messaging");
                actions.add("Plan optimal locations for sign placement");
                break;
        }
        
        // General follow-up tasks
        if (isNewCampaign) {
            actions.add("Set up tracking and monitoring schedule");
        }
        
        if (campaign.getEndDate() != null) {
            actions.add("Schedule campaign performance review for " + campaign.getEndDate().toLocalDate());
        }
    }
    
    private void logCampaignEvents(Campaign campaign, boolean isNewCampaign) {
        if (isNewCampaign) {
            logger.info("New campaign created: {} (Type: {}, Audience: {})", 
                       campaign.getName(), campaign.getType(), campaign.getTargetAudience());
        } else {
            logger.info("Campaign updated: {} (Status: {})", 
                       campaign.getName(), campaign.getStatus());
        }
    }
    
    private void generateSuccessMessage(Campaign campaign, boolean isNewCampaign, CampaignProcessResult result) {
        StringBuilder message = new StringBuilder();
        
        if (isNewCampaign) {
            message.append("Campaign '").append(campaign.getName()).append("' created successfully! ");
        } else {
            message.append("Campaign '").append(campaign.getName()).append("' updated successfully! ");
        }
        
        // Add status-specific message
        switch (campaign.getStatus()) {
            case "ACTIVE":
                message.append("Your campaign is now live and running.");
                break;
            case "DRAFT":
                message.append("Your campaign is saved as a draft. Set status to 'Active' when ready to launch.");
                break;
            case "PAUSED":
                message.append("Your campaign is paused. Change status to 'Active' to resume.");
                break;
        }
        
        result.setSuccessMessage(message.toString());
        
        // Add improvement suggestions
        List<String> suggestions = validationService.suggestImprovements(campaign);
        result.getSuggestions().addAll(suggestions);
    }
    
    /**
     * Get next steps for user after campaign creation
     */
    public List<String> getNextSteps(Campaign campaign) {
        List<String> steps = new ArrayList<>();
        
        switch (campaign.getStatus()) {
            case "DRAFT":
                steps.add("Review and refine your campaign content");
                steps.add("Set a start date and budget if needed");
                steps.add("Change status to 'Active' when ready to launch");
                break;
                
            case "ACTIVE":
                steps.add("Monitor campaign performance regularly");
                steps.add("Check metrics and adjust targeting as needed");
                steps.add("Track leads and conversions");
                break;
                
            case "PAUSED":
                steps.add("Review campaign performance data");
                steps.add("Make necessary adjustments to improve results");
                steps.add("Reactivate when ready to continue");
                break;
        }
        
        // Add campaign-type specific steps
        switch (campaign.getType()) {
            case "FACEBOOK":
            case "GOOGLE_ADS":
                steps.add("Review ad performance in the respective platform");
                steps.add("Optimize targeting based on early results");
                break;
                
            case "CRAIGSLIST":
                steps.add("Post your ad to Craigslist manually");
                steps.add("Prepare variations for multiple postings");
                break;
                
            case "DIRECT_MAIL":
                steps.add("Finalize your mailing list");
                steps.add("Design and order marketing materials");
                break;
        }
        
        return steps;
    }
    
    /**
     * Result class for campaign processing
     */
    public static class CampaignProcessResult {
        private boolean success = false;
        private Campaign campaign;
        private String successMessage;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private List<String> suggestions = new ArrayList<>();
        private List<String> completedActions = new ArrayList<>();
        
        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public Campaign getCampaign() { return campaign; }
        public void setCampaign(Campaign campaign) { this.campaign = campaign; }
        
        public String getSuccessMessage() { return successMessage; }
        public void setSuccessMessage(String successMessage) { this.successMessage = successMessage; }
        
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public List<String> getSuggestions() { return suggestions; }
        public List<String> getCompletedActions() { return completedActions; }
        
        public void addError(String error) { errors.add(error); }
        public void addWarning(String warning) { warnings.add(warning); }
        public void addSuggestion(String suggestion) { suggestions.add(suggestion); }
        
        public boolean hasWarnings() { return !warnings.isEmpty(); }
        public boolean hasSuggestions() { return !suggestions.isEmpty(); }
    }
}