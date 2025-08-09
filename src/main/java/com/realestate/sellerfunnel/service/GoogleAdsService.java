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
public class GoogleAdsService {
    
    private static final Logger logger = LoggerFactory.getLogger(GoogleAdsService.class);
    
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
            // Refresh access token if needed
            if (!refreshAccessToken()) {
                return false;
            }
            
            String url = "https://googleads.googleapis.com/v14/customers/" + customerId + "/campaigns:mutate";
            
            Map<String, Object> campaignData = new HashMap<>();
            campaignData.put("name", campaign.getName());
            campaignData.put("status", "PAUSED"); // Start paused for review
            campaignData.put("advertisingChannelType", "SEARCH");
            campaignData.put("biddingStrategy", Map.of("targetCpa", Map.of("targetCpaMicros", 5000000))); // $5 target CPA
            
            // Budget
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
    
    public boolean createAdGroup(Campaign campaign, String campaignResourceName) {
        if (!isConfigured()) {
            return false;
        }
        
        try {
            String url = "https://googleads.googleapis.com/v14/customers/" + customerId + "/adGroups:mutate";
            
            Map<String, Object> adGroupData = new HashMap<>();
            adGroupData.put("name", campaign.getName() + " - Ad Group");
            adGroupData.put("campaign", campaignResourceName);
            adGroupData.put("status", "ENABLED");
            adGroupData.put("type", "SEARCH_STANDARD");
            adGroupData.put("cpcBidMicros", 2000000); // $2.00 bid
            
            Map<String, Object> operation = new HashMap<>();
            operation.put("create", adGroupData);
            
            Map<String, Object> request = new HashMap<>();
            request.put("operations", new Object[]{operation});
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            headers.set("developer-token", developerToken);
            headers.set("login-customer-id", customerId);
            
            HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, httpRequest, new ParameterizedTypeReference<Map<String, Object>>() {});
            
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            logger.error("Error creating Google Ads ad group: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean createKeywords(Campaign campaign, String adGroupResourceName) {
        if (!isConfigured() || campaign.getKeywords() == null) {
            return false;
        }
        
        try {
            String url = "https://googleads.googleapis.com/v14/customers/" + customerId + "/adGroupCriteria:mutate";
            
            String[] keywords = campaign.getKeywords().split(",");
            Object[] operations = new Object[keywords.length];
            
            for (int i = 0; i < keywords.length; i++) {
                String keyword = keywords[i].trim();
                
                Map<String, Object> criterionData = new HashMap<>();
                criterionData.put("adGroup", adGroupResourceName);
                criterionData.put("status", "ENABLED");
                criterionData.put("keyword", Map.of(
                    "text", keyword,
                    "matchType", "BROAD"
                ));
                criterionData.put("cpcBidMicros", 1500000); // $1.50 per keyword
                
                Map<String, Object> operation = new HashMap<>();
                operation.put("create", criterionData);
                operations[i] = operation;
            }
            
            Map<String, Object> request = new HashMap<>();
            request.put("operations", operations);
            
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
            
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            logger.error("Error creating Google Ads keywords: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean createAd(Campaign campaign, String adGroupResourceName) {
        if (!isConfigured()) {
            return false;
        }
        
        try {
            String url = "https://googleads.googleapis.com/v14/customers/" + customerId + "/adGroupAds:mutate";
            
            Map<String, Object> adData = new HashMap<>();
            adData.put("adGroup", adGroupResourceName);
            adData.put("status", "ENABLED");
            
            // Create responsive search ad
            Map<String, Object> responsiveSearchAd = new HashMap<>();
            responsiveSearchAd.put("headlines", new Object[]{
                Map.of("text", campaign.getHeadline() != null ? campaign.getHeadline() : campaign.getName())
            });
            responsiveSearchAd.put("descriptions", new Object[]{
                Map.of("text", campaign.getDescription() != null ? campaign.getDescription() : "Contact us today!")
            });
            responsiveSearchAd.put("path1", "real-estate");
            responsiveSearchAd.put("path2", "deals");
            
            Map<String, Object> ad = new HashMap<>();
            ad.put("responsiveSearchAd", responsiveSearchAd);
            ad.put("finalUrls", new String[]{"https://your-website.com"}); // Replace with your actual website
            
            adData.put("ad", ad);
            
            Map<String, Object> operation = new HashMap<>();
            operation.put("create", adData);
            
            Map<String, Object> request = new HashMap<>();
            request.put("operations", new Object[]{operation});
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            headers.set("developer-token", developerToken);
            headers.set("login-customer-id", customerId);
            
            HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, httpRequest, new ParameterizedTypeReference<Map<String, Object>>() {});
            
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            logger.error("Error creating Google Ads ad: {}", e.getMessage());
            return false;
        }
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
    
    public Map<String, Object> getCampaignStats(String campaignResourceName) {
        if (!isConfigured()) {
            return new HashMap<>();
        }
        
        try {
            String url = "https://googleads.googleapis.com/v14/customers/" + customerId + "/googleAds:searchStream";
            
            Map<String, Object> query = new HashMap<>();
            query.put("query", "SELECT campaign.id, campaign.name, metrics.impressions, metrics.clicks, metrics.cost_micros " +
                              "FROM campaign WHERE campaign.resource_name = '" + campaignResourceName + "'");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            headers.set("developer-token", developerToken);
            headers.set("login-customer-id", customerId);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(query, headers);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, request, new ParameterizedTypeReference<Map<String, Object>>() {});
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            
        } catch (Exception e) {
            logger.error("Error fetching Google Ads campaign stats: {}", e.getMessage());
        }
        
        return new HashMap<>();
    }
    
    public boolean isConfigured() {
        return !developerToken.isEmpty() && !clientId.isEmpty() && 
               !clientSecret.isEmpty() && !refreshToken.isEmpty() && !customerId.isEmpty();
    }
}