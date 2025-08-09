package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.Campaign;
import com.realestate.sellerfunnel.repository.CampaignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CampaignValidationService {
    
    @Autowired
    private CampaignRepository campaignRepository;
    
    public static class ValidationResult {
        private boolean valid;
        private List<String> errors;
        private List<String> warnings;
        
        public ValidationResult() {
            this.errors = new ArrayList<>();
            this.warnings = new ArrayList<>();
            this.valid = true;
        }
        
        public void addError(String error) {
            errors.add(error);
            valid = false;
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        // Getters
        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public boolean hasWarnings() { return !warnings.isEmpty(); }
    }
    
    /**
     * Comprehensive campaign validation
     */
    public ValidationResult validateCampaign(Campaign campaign) {
        ValidationResult result = new ValidationResult();
        
        System.out.println("=== CAMPAIGN VALIDATION STARTED ===");
        System.out.println("Campaign name: " + campaign.getName());
        System.out.println("Campaign type: " + campaign.getType());
        System.out.println("Campaign target audience: " + campaign.getTargetAudience());
        
        // Basic validation
        validateBasicFields(campaign, result);
        
        // Business logic validation
        validateBusinessRules(campaign, result);
        
        // Duplicate campaign check
        validateDuplicateName(campaign, result);
        
        // Campaign type specific validation
        validateCampaignTypeSpecificRules(campaign, result);
        
        // Budget validation
        validateBudgetRules(campaign, result);
        
        // Date validation
        validateDateRules(campaign, result);
        
        System.out.println("=== VALIDATION COMPLETE ===");
        System.out.println("Validation result: " + (result.isValid() ? "VALID" : "INVALID"));
        System.out.println("Errors: " + result.getErrors());
        System.out.println("Warnings: " + result.getWarnings());
        
        return result;
    }
    
    private void validateBasicFields(Campaign campaign, ValidationResult result) {
        if (campaign.getName() == null || campaign.getName().trim().isEmpty()) {
            result.addError("Campaign name is required");
        } else if (campaign.getName().length() < 3) {
            result.addError("Campaign name must be at least 3 characters");
        } else if (campaign.getName().length() > 100) {
            result.addError("Campaign name cannot exceed 100 characters");
        }
        
        if (campaign.getType() == null || campaign.getType().trim().isEmpty()) {
            result.addError("Campaign type is required");
        }
        
        if (campaign.getTargetAudience() == null || campaign.getTargetAudience().trim().isEmpty()) {
            result.addError("Target audience is required");
        }
    }
    
    private void validateBusinessRules(Campaign campaign, ValidationResult result) {
        // Validate campaign content requirements
        if ("FACEBOOK".equals(campaign.getType()) || "GOOGLE_ADS".equals(campaign.getType())) {
            if (campaign.getAdCopy() == null || campaign.getAdCopy().trim().isEmpty()) {
                result.addWarning("Ad copy is highly recommended for " + campaign.getType() + " campaigns");
            }
        }
        
        // Validate keyword requirements for Google Ads
        if ("GOOGLE_ADS".equals(campaign.getType())) {
            if (campaign.getKeywords() == null || campaign.getKeywords().trim().isEmpty()) {
                result.addError("Keywords are required for Google Ads campaigns");
            } else {
                validateKeywords(campaign.getKeywords(), result);
            }
        }
        
        // Validate headline for ad campaigns
        if (("FACEBOOK".equals(campaign.getType()) || "GOOGLE_ADS".equals(campaign.getType())) &&
            (campaign.getHeadline() == null || campaign.getHeadline().trim().isEmpty())) {
            result.addWarning("Headlines improve campaign performance for ad campaigns");
        }
    }
    
    private void validateDuplicateName(Campaign campaign, ValidationResult result) {
        // Check for duplicate names (case-insensitive)
        List<Campaign> existingCampaigns = campaignRepository.findByNameIgnoreCase(campaign.getName());
        
        if (campaign.getId() == null) {
            // New campaign
            if (!existingCampaigns.isEmpty()) {
                result.addError("A campaign with this name already exists");
            }
        } else {
            // Editing existing campaign
            boolean foundDuplicate = existingCampaigns.stream()
                .anyMatch(c -> !c.getId().equals(campaign.getId()));
            if (foundDuplicate) {
                result.addError("Another campaign with this name already exists");
            }
        }
    }
    
    private void validateCampaignTypeSpecificRules(Campaign campaign, ValidationResult result) {
        switch (campaign.getType()) {
            case "FACEBOOK":
                validateFacebookCampaign(campaign, result);
                break;
            case "GOOGLE_ADS":
                validateGoogleAdsCampaign(campaign, result);
                break;
            case "CRAIGSLIST":
                validateCraigslistCampaign(campaign, result);
                break;
            case "DIRECT_MAIL":
                validateDirectMailCampaign(campaign, result);
                break;
        }
    }
    
    private void validateFacebookCampaign(Campaign campaign, ValidationResult result) {
        if (campaign.getBudget() == null || campaign.getBudget().compareTo(BigDecimal.valueOf(5)) < 0) {
            result.addWarning("Facebook campaigns typically require a minimum daily budget of $5");
        }
        
        if (campaign.getAdCopy() != null && campaign.getAdCopy().length() > 1250) {
            result.addError("Facebook ad copy cannot exceed 1,250 characters");
        }
        
        if (campaign.getHeadline() != null && campaign.getHeadline().length() > 40) {
            result.addError("Facebook headlines cannot exceed 40 characters");
        }
    }
    
    private void validateGoogleAdsCampaign(Campaign campaign, ValidationResult result) {
        if (campaign.getBudget() == null || campaign.getBudget().compareTo(BigDecimal.valueOf(10)) < 0) {
            result.addWarning("Google Ads campaigns typically require a minimum daily budget of $10");
        }
        
        if (campaign.getHeadline() != null && campaign.getHeadline().length() > 30) {
            result.addError("Google Ads headlines cannot exceed 30 characters");
        }
        
        if (campaign.getAdCopy() != null && campaign.getAdCopy().length() > 90) {
            result.addWarning("Google Ads descriptions over 90 characters may be truncated");
        }
    }
    
    private void validateCraigslistCampaign(Campaign campaign, ValidationResult result) {
        if (campaign.getBudget() != null && campaign.getBudget().compareTo(BigDecimal.ZERO) > 0) {
            result.addWarning("Craigslist posts are typically free. Budget may not be necessary.");
        }
        
        if (campaign.getAdCopy() == null || campaign.getAdCopy().trim().isEmpty()) {
            result.addError("Ad copy is required for Craigslist postings");
        }
    }
    
    private void validateDirectMailCampaign(Campaign campaign, ValidationResult result) {
        if (campaign.getBudget() == null || campaign.getBudget().compareTo(BigDecimal.valueOf(100)) < 0) {
            result.addWarning("Direct mail campaigns typically require higher budgets due to printing and postage costs");
        }
        
        if (campaign.getStartDate() != null && campaign.getStartDate().isBefore(LocalDateTime.now().plusWeeks(1))) {
            result.addWarning("Direct mail campaigns typically need at least 1 week lead time for preparation");
        }
    }
    
    private void validateBudgetRules(Campaign campaign, ValidationResult result) {
        if (campaign.getBudget() != null) {
            if (campaign.getBudget().compareTo(BigDecimal.ZERO) < 0) {
                result.addError("Budget cannot be negative");
            }
            
            if (campaign.getBudget().compareTo(BigDecimal.valueOf(10000)) > 0) {
                result.addWarning("Large budget detected. Please verify the amount is correct");
            }
        }
    }
    
    private void validateDateRules(Campaign campaign, ValidationResult result) {
        LocalDateTime now = LocalDateTime.now();
        
        if (campaign.getStartDate() != null) {
            if (campaign.getStartDate().isBefore(now.minusHours(1))) {
                result.addWarning("Start date is in the past");
            }
        }
        
        if (campaign.getEndDate() != null) {
            if (campaign.getEndDate().isBefore(now)) {
                result.addError("End date cannot be in the past");
            }
            
            if (campaign.getStartDate() != null && campaign.getEndDate().isBefore(campaign.getStartDate())) {
                result.addError("End date must be after start date");
            }
            
            if (campaign.getStartDate() != null) {
                long daysBetween = java.time.Duration.between(campaign.getStartDate(), campaign.getEndDate()).toDays();
                if (daysBetween > 365) {
                    result.addWarning("Campaign duration exceeds 1 year. Consider breaking into shorter campaigns.");
                }
            }
        }
    }
    
    private void validateKeywords(String keywords, ValidationResult result) {
        if (keywords == null || keywords.trim().isEmpty()) {
            return;
        }
        
        String[] keywordArray = keywords.split(",");
        
        if (keywordArray.length > 20) {
            result.addWarning("Consider limiting keywords to 20 or fewer for better performance");
        }
        
        for (String keyword : keywordArray) {
            keyword = keyword.trim();
            if (keyword.length() > 80) {
                result.addWarning("Keyword '" + keyword.substring(0, 20) + "...' is very long and may not perform well");
            }
        }
    }
    
    /**
     * Pre-publish validation for campaigns being set to ACTIVE
     */
    public ValidationResult validateForPublication(Campaign campaign) {
        ValidationResult result = validateCampaign(campaign);
        
        // Additional validation for active campaigns
        if ("ACTIVE".equals(campaign.getStatus())) {
            if (campaign.getStartDate() == null) {
                result.addError("Start date is required for active campaigns");
            }
            
            if (campaign.getBudget() == null && needsBudget(campaign.getType())) {
                result.addError("Budget is required for " + campaign.getType() + " campaigns");
            }
            
            if (campaign.getAdCopy() == null || campaign.getAdCopy().trim().isEmpty()) {
                if ("FACEBOOK".equals(campaign.getType()) || "GOOGLE_ADS".equals(campaign.getType()) || "CRAIGSLIST".equals(campaign.getType())) {
                    result.addError("Ad copy is required for active " + campaign.getType() + " campaigns");
                }
            }
        }
        
        return result;
    }
    
    private boolean needsBudget(String campaignType) {
        return "FACEBOOK".equals(campaignType) || "GOOGLE_ADS".equals(campaignType);
    }
    
    /**
     * Suggest campaign improvements
     */
    public List<String> suggestImprovements(Campaign campaign) {
        List<String> suggestions = new ArrayList<>();
        
        // Content suggestions
        if (campaign.getAdCopy() != null && campaign.getAdCopy().length() < 50) {
            suggestions.add("Consider expanding your ad copy with more details about your services");
        }
        
        if (campaign.getHeadline() == null || campaign.getHeadline().isEmpty()) {
            suggestions.add("Add a compelling headline to grab attention");
        }
        
        if (campaign.getCallToAction() == null || campaign.getCallToAction().isEmpty()) {
            suggestions.add("Include a clear call-to-action (e.g., 'Call Now', 'Get Free Quote')");
        }
        
        // Targeting suggestions
        if ("BOTH".equals(campaign.getTargetAudience())) {
            suggestions.add("Consider creating separate campaigns for buyers and sellers for better targeting");
        }
        
        // Budget suggestions
        if (campaign.getBudget() != null && campaign.getEndDate() != null && campaign.getStartDate() != null) {
            long days = java.time.Duration.between(campaign.getStartDate(), campaign.getEndDate()).toDays();
            if (days > 0) {
                BigDecimal dailyBudget = campaign.getBudget().divide(BigDecimal.valueOf(days), 2, java.math.RoundingMode.HALF_UP);
                if (dailyBudget.compareTo(BigDecimal.valueOf(5)) < 0) {
                    suggestions.add("Consider increasing your budget for better reach and performance");
                }
            }
        }
        
        return suggestions;
    }
}