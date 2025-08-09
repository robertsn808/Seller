package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.Campaign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class GoogleAdsServiceComplete {
    
    private static final Logger logger = LoggerFactory.getLogger(GoogleAdsServiceComplete.class);
    
    @Value("${google.ads.developer-token:}")
    private String developerToken;
    
    @Value("${google.ads.client-id:}")
    private String clientId;
    
    @Value("${google.ads.client-secret:}")
    private String clientSecret;
    
    @Value("${google.ads.refresh-token:}")
    private String refreshToken;
    
    @Value("${google.ads.customer-id:}")
    private String customerId;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private String accessToken;
    
    public boolean createCampaign(Campaign campaign) {
        if (!isConfigured()) {
            logger.info("Google Ads API credentials not configured. Campaign saved locally only.");
            return false;
        }
        
        try {
            if (!refreshAccessToken()) {
                return false;
            }
            
            String url = "https://googleads.googleapis.com/v14/customers/" + customerId + "/campaigns:mutate";
            
            Map<String, Object> campaignData = new HashMap<>();
            campaignData.put("name", campaign.getName());
            campaignData.put("status", "PAUSED");
            campaignData.put("advertisingChannelType", "SEARCH");
            campaignData.put("biddingStrategy", Map.of("targetCpa", Map.of("targetCpaMicros", 5000000)));
            
            Map<String, Object> budget = new HashMap<>();
            budget.put("name", campaign.getName() + " Budget");
            budget.put("amountMicros", campaign.getBudget().multiply(java.math.BigDecimal.valueOf(1000000)).longValue());
            budget.put("deliveryMethod", "STANDARD");
            
            Map<String, Object> operation = new HashMap<>();
            operation.put("create", campaignData);
            
            Map<String, Object> request = new HashMap<>();
            request.put("operations", new Object[]{operation});
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            headers.set("developer-token", developerToken);
            headers.set("login-customer-id", customerId);
            
            HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, httpRequest, new ParameterizedTypeReference<Map<String, Object>>() {});
            
            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("Google Ads campaign created successfully for: {}", campaign.getName());
                return true;
            }
            
        } catch (Exception e) {
            logger.error("Error creating Google Ads campaign: {}", e.getMessage());
        }
        
        return false;
    }
    
    public boolean activateCampaign(String campaignResourceName) {
        if (!isConfigured()) {
            logger.info("Google Ads API credentials not configured. Cannot activate campaign.");
            return false;
        }
        
        try {
            if (!refreshAccessToken()) {
                return false;
            }
            
            String url = "https://googleads.googleapis.com/v14/customers/" + customerId + "/campaigns:mutate";
            
            Map<String, Object> campaignData = new HashMap<>();
            campaignData.put("resourceName", campaignResourceName);
            campaignData.put("status", "ENABLED");
            
            Map<String, Object> operation = new HashMap<>();
            operation.put("update", campaignData);
            operation.put("updateMask", "status");
            
            Map<String, Object> request = new HashMap<>();
            request.put("operations", new Object[]{operation});
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            headers.set("developer-token", developerToken);
            headers.set("login-customer-id", customerId);
            
            HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                httpRequest, 
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("Google Ads campaign activated successfully: {}", campaignResourceName);
                return true;
            }
            
        } catch (Exception e) {
            logger.error("Error activating Google Ads campaign: {}", e.getMessage());
        }
        
        return false;
    }
    
    public boolean pauseCampaign(String campaignResourceName) {
        if (!isConfigured()) {
            logger.info("Google Ads API credentials not configured. Cannot pause campaign.");
            return false;
        }
        
        try {
            if (!refreshAccessToken()) {
                return false;
            }
            
            String url = "https://googleads.googleapis.com/v14/customers/" + customerId + "/campaigns:mutate";
            
            Map<String, Object> campaignData = new HashMap<>();
            campaignData.put("resourceName", campaignResourceName);
            campaignData.put("status", "PAUSED");
            
            Map<String, Object> operation = new HashMap<>();
            operation.put("update", campaignData);
            operation.put("updateMask", "status");
            
            Map<String, Object> request = new HashMap<>();
            request.put("operations", new Object[]{operation});
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            headers.set("developer-token", developerToken);
            headers.set("login-customer-id", customerId);
            
            HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                httpRequest, 
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("Google Ads campaign paused successfully: {}", campaignResourceName);
                return true;
            }
            
        } catch (Exception e) {
            logger.error("Error pausing Google Ads campaign: {}", e.getMessage());
        }
        
        return false;
    }
    
    private boolean refreshAccessToken() {
        try {
            String url = "https://oauth2.googleapis.com/token";
            
            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("client_id", clientId);
            tokenData.put("client_secret", clientSecret);
            tokenData.put("refresh_token", refreshToken);
            tokenData.put("grant_type", "refresh_token");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(tokenData, headers);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, request, new ParameterizedTypeReference<Map<String, Object>>() {});
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                this.accessToken = (String) response.getBody().get("access_token");
                return true;
            }
            
        } catch (Exception e) {
            logger.error("Error refreshing Google Ads access token: {}", e.getMessage());
        }
        
        return false;
    }
    
    public boolean isConfigured() {
        return !developerToken.isEmpty() && !clientId.isEmpty() && 
               !clientSecret.isEmpty() && !refreshToken.isEmpty() && !customerId.isEmpty();
    }
}
