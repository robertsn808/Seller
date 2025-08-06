package com.realestate.sellerfunnel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.realestate.sellerfunnel.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UniversalPaymentProtocolService {
    
    private static final Logger logger = LoggerFactory.getLogger(UniversalPaymentProtocolService.class);
    
    @Value("${upp.api.base-url:http://localhost:3000}")
    private String uppBaseUrl;
    
    @Value("${upp.api.device-id:property_management_system}")
    private String defaultDeviceId;
    
    @Value("${upp.api.device-type:smartphone}")
    private String defaultDeviceType;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public UniversalPaymentProtocolService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Process a payment through the Universal Payment Protocol
     */
    public UppPaymentResult processPayment(Payment payment, String deviceType, String deviceId) {
        try {
            logger.info("Processing UPP payment: ${} for booking {}", payment.getAmount(), payment.getBooking().getId());
            
            // Prepare payment request
            Map<String, Object> paymentRequest = new HashMap<>();
            paymentRequest.put("amount", payment.getAmount().doubleValue());
            paymentRequest.put("currency", payment.getCurrency());
            paymentRequest.put("deviceType", deviceType != null ? deviceType : defaultDeviceType);
            paymentRequest.put("deviceId", deviceId != null ? deviceId : defaultDeviceId);
            paymentRequest.put("description", payment.getDescription());
            paymentRequest.put("customerEmail", payment.getCustomerEmail());
            paymentRequest.put("metadata", createMetadata(payment));
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Device-ID", deviceId != null ? deviceId : defaultDeviceId);
            headers.set("X-Device-Type", deviceType != null ? deviceType : defaultDeviceType);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(paymentRequest, headers);
            
            // Make API call
            String url = uppBaseUrl + "/api/v1/payments/process";
            ResponseEntity<Map> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                requestEntity, 
                Map.class
            );
            
            // Process response
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && Boolean.TRUE.equals(responseBody.get("success"))) {
                return createSuccessResult(payment, responseBody);
            } else {
                return createFailureResult(payment, responseBody);
            }
            
        } catch (HttpClientErrorException e) {
            logger.error("UPP API error: {}", e.getMessage());
            return createFailureResult(payment, Map.of("error", e.getMessage()));
        } catch (ResourceAccessException e) {
            logger.error("UPP API connection error: {}", e.getMessage());
            return createFailureResult(payment, Map.of("error", "Connection failed"));
        } catch (Exception e) {
            logger.error("UPP payment processing error: {}", e.getMessage(), e);
            return createFailureResult(payment, Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Register a device with the UPP system
     */
    public UppDeviceRegistration registerDevice(String deviceType, String deviceId, String[] capabilities) {
        try {
            logger.info("Registering device with UPP: {} ({})", deviceType, deviceId);
            
            // Prepare registration request
            Map<String, Object> registrationRequest = new HashMap<>();
            registrationRequest.put("deviceType", deviceType);
            registrationRequest.put("capabilities", capabilities);
            registrationRequest.put("fingerprint", generateDeviceFingerprint(deviceType, deviceId));
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(registrationRequest, headers);
            
            // Make API call
            String url = uppBaseUrl + "/api/v1/devices/register";
            ResponseEntity<Map> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                requestEntity, 
                Map.class
            );
            
            // Process response
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && Boolean.TRUE.equals(responseBody.get("success"))) {
                return new UppDeviceRegistration(
                    (String) responseBody.get("deviceId"),
                    deviceType,
                    (Integer) responseBody.get("trustScore"),
                    (String) responseBody.get("expiresAt")
                );
            } else {
                throw new RuntimeException("Device registration failed: " + responseBody);
            }
            
        } catch (Exception e) {
            logger.error("UPP device registration error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to register device with UPP", e);
        }
    }
    
    /**
     * Get device capabilities from UPP
     */
    public Map<String, Object> getDeviceCapabilities(String deviceType) {
        try {
            String url = uppBaseUrl + "/api/v1/devices/" + deviceType + "/capabilities";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && Boolean.TRUE.equals(responseBody.get("success"))) {
                return (Map<String, Object>) responseBody.get("capabilities");
            } else {
                logger.warn("Failed to get device capabilities for: {}", deviceType);
                return new HashMap<>();
            }
            
        } catch (Exception e) {
            logger.error("Error getting device capabilities for {}: {}", deviceType, e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * Check UPP service health
     */
    public boolean isServiceHealthy() {
        try {
            String url = uppBaseUrl + "/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            Map<String, Object> responseBody = response.getBody();
            return responseBody != null && "healthy".equals(responseBody.get("status"));
            
        } catch (Exception e) {
            logger.error("UPP health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get supported currencies from UPP
     */
    public Map<String, Object> getSupportedCurrencies() {
        try {
            String url = uppBaseUrl + "/api/v1/currencies/supported";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && Boolean.TRUE.equals(responseBody.get("success"))) {
                return responseBody;
            } else {
                return Map.of("currencies", new String[]{"USD"}, "baseCurrency", "USD");
            }
            
        } catch (Exception e) {
            logger.error("Error getting supported currencies: {}", e.getMessage());
            return Map.of("currencies", new String[]{"USD"}, "baseCurrency", "USD");
        }
    }
    
    // Helper methods
    private Map<String, Object> createMetadata(Payment payment) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("booking_id", payment.getBooking().getId());
        metadata.put("room_number", payment.getBooking().getRoom().getRoomNumber());
        metadata.put("guest_name", payment.getBooking().getGuest().getFullName());
        metadata.put("property_management", "true");
        metadata.put("hawaii_processing", "true");
        return metadata;
    }
    
    private String generateDeviceFingerprint(String deviceType, String deviceId) {
        return deviceType + "_" + deviceId + "_" + System.currentTimeMillis();
    }
    
    private UppPaymentResult createSuccessResult(Payment payment, Map<String, Object> responseBody) {
        UppPaymentResult result = new UppPaymentResult();
        result.setSuccess(true);
        result.setTransactionId((String) responseBody.get("transaction_id"));
        result.setPaymentIntentId((String) responseBody.get("payment_intent_id"));
        result.setAmount(payment.getAmount());
        result.setCurrency(payment.getCurrency());
        result.setStatus("COMPLETED");
        result.setProcessedAt(LocalDateTime.now());
        result.setDeviceType((String) responseBody.get("deviceType"));
        result.setRiskScore((Integer) responseBody.get("riskScore"));
        result.setMessage("Payment processed successfully");
        return result;
    }
    
    private UppPaymentResult createFailureResult(Payment payment, Map<String, Object> responseBody) {
        UppPaymentResult result = new UppPaymentResult();
        result.setSuccess(false);
        result.setAmount(payment.getAmount());
        result.setCurrency(payment.getCurrency());
        result.setStatus("FAILED");
        result.setProcessedAt(LocalDateTime.now());
        result.setErrorMessage((String) responseBody.get("error"));
        result.setMessage("Payment processing failed");
        return result;
    }
    
    // Result classes
    public static class UppPaymentResult {
        private boolean success;
        private String transactionId;
        private String paymentIntentId;
        private BigDecimal amount;
        private String currency;
        private String status;
        private String errorMessage;
        private String message;
        private LocalDateTime processedAt;
        private String deviceType;
        private Integer riskScore;
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        
        public String getPaymentIntentId() { return paymentIntentId; }
        public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public LocalDateTime getProcessedAt() { return processedAt; }
        public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
        
        public String getDeviceType() { return deviceType; }
        public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
        
        public Integer getRiskScore() { return riskScore; }
        public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }
    }
    
    public static class UppDeviceRegistration {
        private String deviceId;
        private String deviceType;
        private Integer trustScore;
        private String expiresAt;
        
        public UppDeviceRegistration(String deviceId, String deviceType, Integer trustScore, String expiresAt) {
            this.deviceId = deviceId;
            this.deviceType = deviceType;
            this.trustScore = trustScore;
            this.expiresAt = expiresAt;
        }
        
        // Getters
        public String getDeviceId() { return deviceId; }
        public String getDeviceType() { return deviceType; }
        public Integer getTrustScore() { return trustScore; }
        public String getExpiresAt() { return expiresAt; }
    }
}
