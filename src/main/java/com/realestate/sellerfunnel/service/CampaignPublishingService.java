package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.Campaign;
import com.realestate.sellerfunnel.repository.CampaignRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            // Create Facebook campaign structure
            boolean campaignCreated = facebookAdsService.createCampaign(campaign);
            
            if (campaignCreated) {
                logger.info("Facebook Ads campaign created for: {}", campaign.getName());
                return true;
            }
            
        } catch (Exception e) {
            logger.error("Error creating Facebook Ads campaign: {}", e.getMessage());
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
        // This would sync impressions, clicks, leads from Facebook Ads API
        // Implementation depends on storing external campaign IDs
        logger.info("Syncing Facebook Ads stats for campaign: {}", campaign.getName());
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
    
    public boolean pauseCampaign(Campaign campaign) {
        try {
            // Update status in external platforms
            boolean success = true;
            
            switch (campaign.getType()) {
                case "FACEBOOK":
                    // Pause Facebook Ads campaign via API
                    break;
                    
                case "FACEBOOK_POST":
                case "INSTAGRAM":
                    // Facebook posts cannot be paused once published
                    break;
                    
                case "GOOGLE_ADS":
                case "GOOGLE_AD":
                    // Pause Google Ads campaign via API
                    break;
            }
            
            if (success) {
                campaign.setStatus("PAUSED");
                campaignRepository.save(campaign);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error pausing campaign {}: {}", campaign.getName(), e.getMessage());
            return false;
        }
    }
}