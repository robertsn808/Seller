package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.Campaign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class FacebookPostService {
    
    private static final Logger logger = LoggerFactory.getLogger(FacebookPostService.class);
    
    @Value("${facebook.access-token:}")
    private String fallbackAccessToken;
    
    @Value("${facebook.page-id:}")
    private String fallbackPageId;
    
    @Autowired
    private CredentialManagementService credentialManagementService;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public boolean createPost(Campaign campaign) {
        Map<String, String> credentials = getCredentials();
        if (credentials.isEmpty()) {
            logger.info("Facebook API credentials not configured. Campaign saved locally only.");
            return false;
        }
        
        String accessToken = credentials.get("accessToken");
        String pageId = credentials.get("pageId");
        
        try {
            String url = "https://graph.facebook.com/v18.0/" + pageId + "/feed";
            
            // Build post content
            StringBuilder message = new StringBuilder();
            
            if (campaign.getHeadline() != null && !campaign.getHeadline().isEmpty()) {
                message.append(campaign.getHeadline()).append("\n\n");
            }
            
            if (campaign.getAdCopy() != null && !campaign.getAdCopy().isEmpty()) {
                message.append(campaign.getAdCopy()).append("\n\n");
            }
            
            if (campaign.getCallToAction() != null && !campaign.getCallToAction().isEmpty()) {
                message.append(campaign.getCallToAction());
            }
            
            // Add hashtags if keywords are provided
            if (campaign.getKeywords() != null && !campaign.getKeywords().isEmpty()) {
                String[] keywords = campaign.getKeywords().split(",");
                message.append("\n\n");
                for (String keyword : keywords) {
                    message.append("#").append(keyword.trim().replaceAll("\\s+", "")).append(" ");
                }
            }
            
            Map<String, Object> postData = new HashMap<>();
            postData.put("message", message.toString());
            postData.put("access_token", accessToken);
            
            // Add link if it's a campaign with a landing page
            if (campaign.getTargetAudience().equals("SELLERS")) {
                postData.put("link", "https://your-website.com/sell");
            } else if (campaign.getTargetAudience().equals("BUYERS")) {
                postData.put("link", "https://your-website.com/buy");
            } else {
                postData.put("link", "https://your-website.com");
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // Convert map to form data
            StringBuilder formData = new StringBuilder();
            postData.forEach((key, value) -> {
                if (formData.length() > 0) {
                    formData.append("&");
                }
                formData.append(key).append("=").append(value);
            });
            
            HttpEntity<String> request = new HttpEntity<>(formData.toString(), headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String postId = (String) response.getBody().get("id");
                logger.info("Facebook post created successfully: {} for campaign: {}", postId, campaign.getName());
                return true;
            }
            
        } catch (Exception e) {
            logger.error("Error creating Facebook post for campaign {}: {}", campaign.getName(), e.getMessage());
        }
        
        return false;
    }
    
    public boolean schedulePost(Campaign campaign, String scheduleTime) {
        Map<String, String> credentials = getCredentials();
        if (credentials.isEmpty()) {
            return false;
        }
        
        String accessToken = credentials.get("accessToken");
        String pageId = credentials.get("pageId");
        
        try {
            String url = "https://graph.facebook.com/v18.0/" + pageId + "/feed";
            
            StringBuilder message = new StringBuilder();
            
            if (campaign.getHeadline() != null && !campaign.getHeadline().isEmpty()) {
                message.append(campaign.getHeadline()).append("\n\n");
            }
            
            if (campaign.getAdCopy() != null && !campaign.getAdCopy().isEmpty()) {
                message.append(campaign.getAdCopy()).append("\n\n");
            }
            
            if (campaign.getCallToAction() != null && !campaign.getCallToAction().isEmpty()) {
                message.append(campaign.getCallToAction());
            }
            
            Map<String, Object> postData = new HashMap<>();
            postData.put("message", message.toString());
            postData.put("published", false); // Save as draft
            postData.put("scheduled_publish_time", scheduleTime);
            postData.put("access_token", accessToken);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            StringBuilder formData = new StringBuilder();
            postData.forEach((key, value) -> {
                if (formData.length() > 0) {
                    formData.append("&");
                }
                formData.append(key).append("=").append(value);
            });
            
            HttpEntity<String> request = new HttpEntity<>(formData.toString(), headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("Facebook post scheduled successfully for campaign: {}", campaign.getName());
                return true;
            }
            
        } catch (Exception e) {
            logger.error("Error scheduling Facebook post for campaign {}: {}", campaign.getName(), e.getMessage());
        }
        
        return false;
    }
    
    public Map<String, Object> getPostStats(String postId) {
        Map<String, String> credentials = getCredentials();
        if (credentials.isEmpty() || postId.isEmpty()) {
            return new HashMap<>();
        }
        
        String accessToken = credentials.get("accessToken");
        
        try {
            String url = "https://graph.facebook.com/v18.0/" + postId + 
                        "?fields=likes.summary(true),comments.summary(true),shares&access_token=" + accessToken;
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            
        } catch (Exception e) {
            logger.error("Error fetching Facebook post stats: {}", e.getMessage());
        }
        
        return new HashMap<>();
    }
    
    public boolean isConfigured() {
        return credentialManagementService.hasValidCredentials("FACEBOOK");
    }
    
    public String getConfigurationStatus() {
        return credentialManagementService.getCredentialStatus("FACEBOOK");
    }
    
    private Map<String, String> getCredentials() {
        // Try to get from database first
        Map<String, String> credentials = credentialManagementService.getFacebookCredentials();
        
        // Fallback to properties if database is empty
        if (credentials.isEmpty() && !fallbackAccessToken.isEmpty() && !fallbackPageId.isEmpty()) {
            credentials = new HashMap<>();
            credentials.put("accessToken", fallbackAccessToken);
            credentials.put("pageId", fallbackPageId);
        }
        
        return credentials;
    }
}