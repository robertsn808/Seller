
package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.Campaign;
import com.realestate.sellerfunnel.repository.CampaignRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CampaignPublishingServiceEnhanced {
    
    private static final Logger logger = LoggerFactory.getLogger(CampaignPublishingServiceEnhanced.class);
    
    @Autowired
    private FacebookAdsService facebookAdsService;
    
    @Autowired
    private FacebookPostService facebookPostService;
    
    @Autowired
    private GoogleAdsServiceComplete googleAdsServiceComplete;
    
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
                    success = true;
            }
            
            if (success) {
                campaign.setStatus("ACTIVE");
                campaignRepository.save(campaign);
                logger.info("Campaign {} published successfully", campaign.getName());
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
            return true;
        }
        
        try {
            boolean success = facebookAdsService.createCompleteFacebookAdsStructure(campaign);
            if (success) {
                logger.info("Facebook Ads structure created for: {}", campaign.getName());
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
            return true;
        }
        
        try {
            boolean success = facebookPostService.createPost(campaign);
            if (success) {
                logger.info("Facebook post created for campaign: {}", campaign.getName());
                return true;
            }
        } catch (Exception e) {
            logger.error("Error creating Facebook post: {}", e.getMessage());
        }
        return false;
    }
    
    private boolean publishToGoogleAds(Campaign campaign) {
        if (!googleAdsServiceComplete.isConfigured()) {
            logger.info("Google Ads API not configured. Campaign saved locally only.");
            return true;
        }
        
        try {
            boolean success = googleAdsServiceComplete.createCampaign(campaign);
            if (success) {
                logger.info("Google Ads campaign created for: {}", campaign.getName());
                return true;
            }
        } catch (Exception e) {
            logger.error("Error creating Google Ads campaign: {}", e.getMessage());
        }
        return false;
    }
    
    private boolean publishToCraigslist(Campaign campaign) {
        logger.info("Craigslist posting requires manual action. Campaign saved for reference.");
        return true;
    }
    
    private boolean publishToDirectMail(Campaign campaign) {
        logger.info("Direct mail campaign created. Integrate with mail service provider.");
        return true;
    }
    
    public boolean activateCampaign(Campaign campaign) {
        try {
            boolean success = true;
            
            switch (campaign.getType()) {
                case "FACEBOOK":
                    if (campaign.getFacebookCampaignId() != null) {
                        success = facebookAdsService.activateCampaign(campaign.getFacebookCampaignId());
                    }
                    break;
                    
                case "FACEBOOK_POST":
                case "INSTAGRAM":
                    logger.info("Facebook posts are already active once published for campaign: {}", campaign.getName());
                    break;
                    
                case "GOOGLE_ADS":
                case "GOOGLE_AD":
                    if (campaign.getGoogleAdsCampaignId() != null) {
                        success = googleAdsServiceComplete.activateCampaign(campaign.getGoogleAdsCampaignId());
                    } else {
                        logger.warn("Google Ads campaign ID not found for campaign: {}", campaign.getName());
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
            boolean success = true;
            
            switch (campaign.getType()) {
                case "FACEBOOK":
                    if (campaign.getFacebookCampaignId() != null) {
                        success = facebookAdsService.pauseCampaign(campaign.getFacebookCampaignId());
                    }
                    break;
                    
                case "FACEBOOK_POST":
                case "INSTAGRAM":
                    logger.info("Facebook posts cannot be paused once published for campaign: {}", campaign.getName());
                    break;
                    
                case "GOOGLE_ADS":
                case "GOOGLE_AD":
                    if (campaign.getGoogleAdsCampaignId() != null) {
                        success = googleAdsServiceComplete.pauseCampaign(campaign.getGoogleAdsCampaignId());
                    } else {
                        logger.warn("Google Ads campaign ID not found for campaign: {}", campaign.getName());
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
