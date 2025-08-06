package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.Campaign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class FacebookAdsService {
    
    private static final Logger logger = LoggerFactory.getLogger(FacebookAdsService.class);
    
    @Value("${facebook.access-token:}")
    private String accessToken;
    
    @Value("${facebook.ad-account-id:}")
    private String adAccountId;
    
    @Value("${facebook.page-id:}")
    private String pageId;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public boolean createCampaign(Campaign campaign) {
        if (accessToken.isEmpty() || adAccountId.isEmpty()) {
            logger.info("Facebook API credentials not configured. Campaign saved locally only.");
            return false;
        }
        
        try {
            String url = "https://graph.facebook.com/v18.0/act_" + adAccountId + "/campaigns";
            
            Map<String, Object> campaignData = new HashMap<>();
            campaignData.put("name", campaign.getName());
            campaignData.put("objective", getObjectiveFromType(campaign.getType()));
            campaignData.put("status", "PAUSED"); // Start paused for review
            campaignData.put("access_token", accessToken);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(campaignData, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("Facebook campaign created successfully for: {}", campaign.getName());
                return true;
            }
            
        } catch (Exception e) {
            logger.error("Error creating Facebook campaign: {}", e.getMessage());
        }
        
        return false;
    }
    
    public boolean createAdSet(Campaign campaign, String campaignId) {
        if (accessToken.isEmpty() || adAccountId.isEmpty()) {
            return false;
        }
        
        try {
            String url = "https://graph.facebook.com/v18.0/act_" + adAccountId + "/adsets";
            
            Map<String, Object> adSetData = new HashMap<>();
            adSetData.put("name", campaign.getName() + " - AdSet");
            adSetData.put("campaign_id", campaignId);
            adSetData.put("daily_budget", campaign.getBudget().multiply(java.math.BigDecimal.valueOf(100)).intValue());
            adSetData.put("billing_event", "IMPRESSIONS");
            adSetData.put("optimization_goal", "REACH");
            adSetData.put("bid_amount", 1000); // $10.00 in cents
            adSetData.put("status", "PAUSED");
            
            // Targeting
            Map<String, Object> targeting = new HashMap<>();
            targeting.put("geo_locations", Map.of("countries", new String[]{"US"}));
            targeting.put("age_min", 25);
            targeting.put("age_max", 65);
            
            if (campaign.getTargetAudience().equals("SELLERS")) {
                targeting.put("interests", new Map[]{
                    Map.of("id", "6003139266461", "name", "Real estate")
                });
            } else if (campaign.getTargetAudience().equals("BUYERS")) {
                targeting.put("interests", new Map[]{
                    Map.of("id", "6003605442926", "name", "Real estate investing")
                });
            }
            
            adSetData.put("targeting", targeting);
            adSetData.put("access_token", accessToken);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(adSetData, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            logger.error("Error creating Facebook ad set: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean createAd(Campaign campaign, String adSetId) {
        if (accessToken.isEmpty() || adAccountId.isEmpty() || pageId.isEmpty()) {
            return false;
        }
        
        try {
            // First create ad creative
            String creativeId = createAdCreative(campaign);
            if (creativeId == null) return false;
            
            String url = "https://graph.facebook.com/v18.0/act_" + adAccountId + "/ads";
            
            Map<String, Object> adData = new HashMap<>();
            adData.put("name", campaign.getName() + " - Ad");
            adData.put("adset_id", adSetId);
            adData.put("creative", Map.of("creative_id", creativeId));
            adData.put("status", "PAUSED");
            adData.put("access_token", accessToken);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(adData, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            logger.error("Error creating Facebook ad: {}", e.getMessage());
            return false;
        }
    }
    
    private String createAdCreative(Campaign campaign) {
        try {
            String url = "https://graph.facebook.com/v18.0/act_" + adAccountId + "/adcreatives";
            
            Map<String, Object> creativeData = new HashMap<>();
            creativeData.put("name", campaign.getName() + " - Creative");
            
            Map<String, Object> objectStorySpec = new HashMap<>();
            objectStorySpec.put("page_id", pageId);
            
            Map<String, Object> linkData = new HashMap<>();
            linkData.put("message", campaign.getAdCopy());
            linkData.put("link", "https://your-website.com"); // Replace with your actual website
            linkData.put("name", campaign.getHeadline());
            linkData.put("description", campaign.getDescription());
            linkData.put("call_to_action", Map.of("type", "LEARN_MORE"));
            
            objectStorySpec.put("link_data", linkData);
            creativeData.put("object_story_spec", objectStorySpec);
            creativeData.put("access_token", accessToken);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(creativeData, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("id");
            }
            
        } catch (Exception e) {
            logger.error("Error creating Facebook ad creative: {}", e.getMessage());
        }
        
        return null;
    }
    
    public Map<String, Object> getCampaignStats(String campaignId) {
        if (accessToken.isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            String url = "https://graph.facebook.com/v18.0/" + campaignId + 
                        "/insights?fields=impressions,clicks,spend,actions&access_token=" + accessToken;
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            
        } catch (Exception e) {
            logger.error("Error fetching Facebook campaign stats: {}", e.getMessage());
        }
        
        return new HashMap<>();
    }
    
    private String getObjectiveFromType(String type) {
        return switch (type) {
            case "FACEBOOK" -> "TRAFFIC";
            case "FACEBOOK_LEADS" -> "LEAD_GENERATION";
            case "FACEBOOK_AWARENESS" -> "REACH";
            default -> "TRAFFIC";
        };
    }
    
    public boolean isConfigured() {
        return !accessToken.isEmpty() && !adAccountId.isEmpty() && !pageId.isEmpty();
    }
}