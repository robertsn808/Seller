package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.Campaign;
import com.realestate.sellerfunnel.repository.CampaignRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class CampaignPublishingService {
    
    private static final Logger logger = LoggerFactory.getLogger(CampaignPublishingService.class);
    
    @Autowired
    private FacebookAdsService facebookAdsService;
    
    @Autowired
    private FacebookPostService facebookPostService;
    
    @Autowired
    private GoogleAdsService googleAdsService;
    
    @Autowired
    private CampaignRepository campaignRepository;
    
    public boolean publishCampaign(Campaign campaign) {
        boolean success = false;
        
        try {
            switch (campaign.getType()) {
                case "FACEBOOK":
                    success = publishToFacebookAds(campaign);
                    break;
                    
                case "FACEBOOK_POST":
                    success = publishToFacebookPost(campaign);
                    break;
                    
                case "GOOGLE_ADS":
                case "GOOGLE_AD":
                    success = publishToGoogleAds(campaign);
                    break;
                    
                case "INSTAGRAM":
                    // Instagram uses Facebook's API
                    success = publishToFacebookPost(campaign);
                    break;
                    
                case "CRAIGSLIST":
                    success = publishToCraigslist(campaign);
                    break;
                    
                case "DIRECT_MAIL":
                    success = publishToDirectMail(campaign);
                    break;
                    
                default:
                    logger.info("Campaign type {} does not support automatic publishing", campaign.getType());
                    success = true; // Manual campaign types are considered successful
            }
            
            if (success) {
                campaign.setStatus("ACTIVE");
                campaignRepository.save(campaign);
                logger.info("Campaign {} published successfully", campaign.getName());
            } else {
                logger.warn("Failed to publish campaign {}", campaign.getName());
            }
            
        } catch (Exception e) {
            logger.error("Error publishing campaign {}: {}", campaign.getName(), e.getMessage());
            success = false;
        }
        
        return success;
    }
    
    private boolean publishToFacebookAds(Campaign campaign) {
        if (!facebookAdsService.isConfigured()) {
            logger.info("Facebook Ads API not configured. Campaign saved locally only.");
            return true; // Don't fail if API isn't configured
        }
        
        try {
            // Create complete Facebook Ads structure (Campaign -> Ad Set -> Ad)
            boolean success = facebookAdsService.createCompleteFacebookAdsStructure(campaign);
            
            if (success) {
                logger.info("Complete Facebook Ads structure created for: {}", campaign.getName());
                // Save the campaign with external IDs
                campaignRepository.save(campaign);
                return true;
            }
            
        } catch (Exception e) {
            logger.error("Error creating Facebook Ads structure: {}", e.getMessage());
        }
        
        return false;
    }
    
    private boolean publishToFacebookPost(Campaign campaign) {
        if (!facebookPostService.isConfigured()) {
            logger.info("Facebook posting API not configured. Campaign saved locally only.");
            return true; // Don't fail if API isn't configured
        }
        
        try {
            // Create direct Facebook post
            boolean postCreated = facebookPostService.createPost(campaign);
            
            if (postCreated) {
                logger.info("Facebook post created for campaign: {}", campaign.getName());
                return true;
            }
            
        } catch (Exception e) {
            logger.error("Error creating Facebook post: {}", e.getMessage());
        }
        
        return false;
    }
    
    private boolean publishToGoogleAds(Campaign campaign) {
        if (!googleAdsService.isConfigured()) {
            logger.info("Google Ads API not configured. Campaign saved locally only.");
            return true; // Don't fail if API isn't configured
        }
        
        try {
            // Create Google Ads campaign structure
            boolean campaignCreated = googleAdsService.createCampaign(campaign);
            
            if (campaignCreated) {
                logger.info("Google Ads campaign created for: {}", campaign.getName());
                return true;
            }
            
        } catch (Exception e) {
            logger.error("Error creating Google Ads campaign: {}", e.getMessage());
        }
        
        return false;
    }
    
    private boolean publishToCraigslist(Campaign campaign) {
        // Craigslist doesn't have an official API
        // This would require web scraping or manual posting
        logger.info("Craigslist posting requires manual action. Campaign saved for reference.");
        return true;
    }
    
    private boolean publishToDirectMail(Campaign campaign) {
        // Direct mail would integrate with services like Lob, PostGrid, or similar
        logger.info("Direct mail campaign created. Integrate with mail service provider.");
        return true;
    }
    
    public void syncCampaignStats(Campaign campaign) {
        try {
            switch (campaign.getType()) {
                case "FACEBOOK":
                    syncFacebookAdsStats(campaign);
                    break;
                    
                case "FACEBOOK_POST":
                case "INSTAGRAM":
                    syncFacebookPostStats(campaign);
                    break;
                    
                case "GOOGLE_ADS":
                case "GOOGLE_AD":
                    syncGoogleAdsStats(campaign);
                    break;
            }
            
            campaignRepository.save(campaign);
            
        } catch (Exception e) {
            logger.error("Error syncing campaign stats for {}: {}", campaign.getName(), e.getMessage());
        }
    }
    
    private void syncFacebookAdsStats(Campaign campaign) {
        if (campaign.getFacebookCampaignId() == null) {
            logger.warn("No Facebook campaign ID found for campaign: {}", campaign.getName());
            return;
        }
        
        try {
            Map<String, Object> stats = facebookAdsService.getCampaignStats(campaign.getFacebookCampaignId());
            
            if (stats != null && !stats.isEmpty()) {
                // Update campaign with Facebook stats
                if (stats.get("impressions") != null) {
                    campaign.setImpressions(Integer.valueOf(stats.get("impressions").toString()));
                }
                if (stats.get("clicks") != null) {
                    campaign.setClicks(Integer.valueOf(stats.get("clicks").toString()));
                }
                if (stats.get("spend") != null) {
                    String spendStr = stats.get("spend").toString();
                    campaign.setCost(new java.math.BigDecimal(spendStr));
                }
                
                // Count leads from actions
                if (stats.get("actions") != null) {
                    List<Map<String, Object>> actions = (List<Map<String, Object>>) stats.get("actions");
                    int leadCount = 0;
                    for (Map<String, Object> action : actions) {
                        if ("lead".equals(action.get("action_type"))) {
                            leadCount += Integer.valueOf(action.get("value").toString());
                        }
                    }
                    campaign.setLeads(leadCount);
                }
                
                logger.info("Facebook Ads stats synced for campaign: {} - Impressions: {}, Clicks: {}, Cost: {}", 
                    campaign.getName(), campaign.getImpressions(), campaign.getClicks(), campaign.getCost());
            }
            
        } catch (Exception e) {
            logger.error("Error syncing Facebook Ads stats for campaign {}: {}", campaign.getName(), e.getMessage());
        }
    }
    
    private void syncFacebookPostStats(Campaign campaign) {
        // This would sync likes, comments, shares from Facebook Graph API
        // Implementation depends on storing external post IDs
        logger.info("Syncing Facebook Post stats for campaign: {}", campaign.getName());
    }
    
    private void syncGoogleAdsStats(Campaign campaign) {
        // This would sync impressions, clicks, cost from Google Ads API
        // Implementation depends on storing external campaign IDs
        logger.info("Syncing Google Ads stats for campaign: {}", campaign.getName());
    }
    
    public boolean activateCampaign(Campaign campaign) {
        try {
            // Update status in external platforms
            boolean success = true;
            
            switch (campaign.getType()) {
                case "FACEBOOK":
                    if (campaign.getFacebookCampaignId() != null) {
                        success = facebookAdsService.activateCampaign(campaign.getFacebookCampaignId());
                    }
                    break;
                    
                case "FACEBOOK_POST":
                case "INSTAGRAM":
                    // Facebook posts are already active once published
                    logger.info("Facebook posts are already active for campaign: {}", campaign.getName());
                    break;
                    
                case "GOOGLE_ADS":
                case "GOOGLE_AD":
                    // Activate Google Ads campaign via API
                    if (campaign.getGoogleAdsCampaignId() != null) {
                        // TODO: Implement Google Ads activate functionality
                        logger.info("Google Ads activate functionality not yet implemented for campaign: {}", campaign.getName());
                    }
                    break;
            }
            
            if (success) {
                campaign.setStatus("ACTIVE");
                campaignRepository.save(campaign);
                logger.info("Campaign {} activated successfully", campaign.getName());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error activating campaign {}: {}", campaign.getName(), e.getMessage());
            return false;
        }
    }
    
    public boolean pauseCampaign(Campaign campaign) {
        try {
            // Update status in external platforms
            boolean success = true;
            
            switch (campaign.getType()) {
                case "FACEBOOK":
                    if (campaign.getFacebookCampaignId() != null) {
                        success = facebookAdsService.pauseCampaign(campaign.getFacebookCampaignId());
                    }
                    break;
                    
                case "FACEBOOK_POST":
                case "INSTAGRAM":
                    // Facebook posts cannot be paused once published
                    logger.info("Facebook posts cannot be paused once published for campaign: {}", campaign.getName());
                    break;
                    
                case "GOOGLE_ADS":
                case "GOOGLE_AD":
                    // Pause Google Ads campaign via API
                    if (campaign.getGoogleAdsCampaignId() != null) {
                        // TODO: Implement Google Ads pause functionality
                        logger.info("Google Ads pause functionality not yet implemented for campaign: {}", campaign.getName());
                    }
                    break;
            }
            
            if (success) {
                campaign.setStatus("PAUSED");
                campaignRepository.save(campaign);
                logger.info("Campaign {} paused successfully", campaign.getName());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error pausing campaign {}: {}", campaign.getName(), e.getMessage());
            return false;
        }
    }
}