package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.ApiCredential;
import com.realestate.sellerfunnel.repository.ApiCredentialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CredentialManagementService {
    
    private static final Logger logger = LoggerFactory.getLogger(CredentialManagementService.class);
    
    @Autowired
    private ApiCredentialRepository credentialRepository;
    
    public boolean hasValidCredentials(String platform) {
        return credentialRepository.hasValidCredentials(platform);
    }
    
    public Optional<ApiCredential> getCredentials(String platform) {
        return credentialRepository.findValidCredentialByPlatform(platform);
    }
    
    public ApiCredential saveCredentials(String platform, String accessToken, Map<String, String> additionalConfig) {
        // Deactivate existing credentials for this platform
        Optional<ApiCredential> existing = credentialRepository.findByPlatform(platform);
        if (existing.isPresent()) {
            existing.get().setIsActive(false);
            credentialRepository.save(existing.get());
        }
        
        // Create new credentials
        ApiCredential credential = new ApiCredential(platform, accessToken);
        
        if (additionalConfig != null && !additionalConfig.isEmpty()) {
            // Convert to JSON string (simple implementation)
            StringBuilder json = new StringBuilder("{");
            additionalConfig.forEach((key, value) -> {
                if (json.length() > 1) json.append(",");
                json.append("\"").append(key).append("\":\"").append(value).append("\"");
            });
            json.append("}");
            credential.setAdditionalConfig(json.toString());
        }
        
        ApiCredential saved = credentialRepository.save(credential);
        logger.info("Saved new credentials for platform: {}", platform);
        
        return saved;
    }
    
    public Map<String, String> getFacebookCredentials() {
        Map<String, String> credentials = new HashMap<>();
        Optional<ApiCredential> cred = getCredentials("FACEBOOK");
        
        if (cred.isPresent()) {
            credentials.put("accessToken", cred.get().getAccessToken());
            
            // Parse additional config JSON
            String additionalConfig = cred.get().getAdditionalConfig();
            if (additionalConfig != null && !additionalConfig.isEmpty()) {
                // Simple JSON parsing for page ID
                if (additionalConfig.contains("pageId")) {
                    String pageId = extractValueFromJson(additionalConfig, "pageId");
                    if (pageId != null) {
                        credentials.put("pageId", pageId);
                    }
                }
            }
        }
        
        return credentials;
    }
    
    public void saveFacebookCredentials(String accessToken, String pageId) {
        Map<String, String> additionalConfig = new HashMap<>();
        additionalConfig.put("pageId", pageId);
        saveCredentials("FACEBOOK", accessToken, additionalConfig);
    }
    
    public boolean testFacebookCredentials(String accessToken, String pageId) {
        // This would test the credentials by making a simple API call to Facebook
        // For now, we'll just validate they're not empty
        return accessToken != null && !accessToken.trim().isEmpty() && 
               pageId != null && !pageId.trim().isEmpty();
    }
    
    public void deactivateCredentials(String platform) {
        Optional<ApiCredential> credential = credentialRepository.findByPlatformAndIsActiveTrue(platform);
        if (credential.isPresent()) {
            credential.get().setIsActive(false);
            credentialRepository.save(credential.get());
            logger.info("Deactivated credentials for platform: {}", platform);
        }
    }
    
    public Map<String, Boolean> getPlatformStatus() {
        Map<String, Boolean> status = new HashMap<>();
        status.put("FACEBOOK", hasValidCredentials("FACEBOOK"));
        status.put("GOOGLE_ADS", hasValidCredentials("GOOGLE_ADS"));
        status.put("INSTAGRAM", hasValidCredentials("INSTAGRAM"));
        return status;
    }
    
    public String getCredentialStatus(String platform) {
        if (hasValidCredentials(platform)) {
            Optional<ApiCredential> cred = getCredentials(platform);
            if (cred.isPresent()) {
                LocalDateTime updated = cred.get().getUpdatedAt();
                return "Configured (last updated: " + updated.toString() + ")";
            }
        }
        return "Not configured";
    }
    
    private String extractValueFromJson(String json, String key) {
        // Simple JSON value extraction
        String searchKey = "\"" + key + "\":\"";
        int startIndex = json.indexOf(searchKey);
        if (startIndex != -1) {
            startIndex += searchKey.length();
            int endIndex = json.indexOf("\"", startIndex);
            if (endIndex != -1) {
                return json.substring(startIndex, endIndex);
            }
        }
        return null;
    }
}