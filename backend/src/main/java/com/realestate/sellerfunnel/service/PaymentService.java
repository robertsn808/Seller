package com.realestate.sellerfunnel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.realestate.sellerfunnel.model.Booking;
import com.realestate.sellerfunnel.model.Payment;
import com.realestate.sellerfunnel.repository.BookingRepository;
import com.realestate.sellerfunnel.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UniversalPaymentProtocolService uppService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Process a payment for a booking
     */
    @Transactional
    public PaymentResult processPayment(Long bookingId, BigDecimal amount, String paymentMethod, 
                                      String deviceType, String deviceId, String customerEmail) {
        try {
            // Find the booking
            Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
            
            // Create payment record
            Payment payment = new Payment(booking, amount, paymentMethod);
            payment.setCustomerEmail(customerEmail);
            payment.setDescription("Payment for Room " + booking.getRoom().getRoomNumber() + " - " + booking.getGuest().getFullName());
            
            // Process based on payment method
            if ("UPP_DEVICE".equals(paymentMethod)) {
                return processUppPayment(payment, deviceType, deviceId);
            } else {
                return processTraditionalPayment(payment);
            }
            
        } catch (Exception e) {
            logger.error("Payment processing error: {}", e.getMessage(), e);
            return new PaymentResult(false, "Payment processing failed: " + e.getMessage(), null);
        }
    }
    
    /**
     * Process payment through Universal Payment Protocol
     */
    private PaymentResult processUppPayment(Payment payment, String deviceType, String deviceId) {
        try {
            // Set device information
            payment.setDeviceType(deviceType);
            payment.setDeviceId(deviceId);
            payment.setPaymentMethod("UPP_DEVICE");
            
            // Save initial payment record
            payment = paymentRepository.save(payment);
            
            // Process through UPP
            UniversalPaymentProtocolService.UppPaymentResult uppResult = 
                uppService.processPayment(payment, deviceType, deviceId);
            
            // Update payment with UPP results
            if (uppResult.isSuccess()) {
                payment.setPaymentStatus("COMPLETED");
                payment.setUppTransactionId(uppResult.getTransactionId());
                payment.setStripePaymentIntentId(uppResult.getPaymentIntentId());
                payment.setProcessedAt(uppResult.getProcessedAt());
                
                // Add metadata
                Map<String, Object> metadata = Map.of(
                    "upp_device_type", deviceType,
                    "upp_device_id", deviceId,
                    "upp_risk_score", uppResult.getRiskScore(),
                    "upp_processed_at", uppResult.getProcessedAt().toString()
                );
                payment.setMetadata(serializeMetadata(metadata));
                
            } else {
                payment.setPaymentStatus("FAILED");
                payment.setProcessedAt(uppResult.getProcessedAt());
                
                Map<String, Object> metadata = Map.of(
                    "upp_error", uppResult.getErrorMessage(),
                    "upp_device_type", deviceType,
                    "upp_device_id", deviceId
                );
                payment.setMetadata(serializeMetadata(metadata));
            }
            
            // Save updated payment
            payment = paymentRepository.save(payment);
            
            // Update booking if payment was successful
            if (uppResult.isSuccess()) {
                updateBookingPayment(payment.getBooking(), payment.getAmount());
            }
            
            return new PaymentResult(
                uppResult.isSuccess(),
                uppResult.getMessage(),
                payment
            );
            
        } catch (Exception e) {
            logger.error("UPP payment processing error: {}", e.getMessage(), e);
            
            // Mark payment as failed
            payment.setPaymentStatus("FAILED");
            payment.setProcessedAt(LocalDateTime.now());
            payment.setMetadata(serializeMetadata(Map.of("error", e.getMessage())));
            paymentRepository.save(payment);
            
            return new PaymentResult(false, "UPP payment failed: " + e.getMessage(), payment);
        }
    }
    
    /**
     * Process traditional payment (cash, transfer, etc.)
     */
    private PaymentResult processTraditionalPayment(Payment payment) {
        try {
            // For traditional payments, we assume they're completed immediately
            payment.setPaymentStatus("COMPLETED");
            payment.setProcessedAt(LocalDateTime.now());
            
            // Save payment
            payment = paymentRepository.save(payment);
            
            // Update booking
            updateBookingPayment(payment.getBooking(), payment.getAmount());
            
            return new PaymentResult(true, "Payment processed successfully", payment);
            
        } catch (Exception e) {
            logger.error("Traditional payment processing error: {}", e.getMessage(), e);
            return new PaymentResult(false, "Traditional payment failed: " + e.getMessage(), null);
        }
    }
    
    /**
     * Update booking with payment information
     */
    private void updateBookingPayment(Booking booking, BigDecimal amount) {
        booking.addPayment(amount);
        bookingRepository.save(booking);
        logger.info("Updated booking {} with payment of ${}", booking.getId(), amount);
    }
    
    /**
     * Get payment history for a booking
     */
    public List<Payment> getPaymentHistory(Long bookingId) {
        return paymentRepository.findByBookingIdAndIsActiveTrueOrderByCreatedAtDesc(bookingId);
    }
    
    /**
     * Get payment statistics
     */
    public PaymentStatistics getPaymentStatistics() {
        PaymentStatistics stats = new PaymentStatistics();
        
        // Count by status
        stats.setPendingCount(paymentRepository.countByPaymentStatusAndIsActiveTrue("PENDING"));
        stats.setCompletedCount(paymentRepository.countByPaymentStatusAndIsActiveTrue("COMPLETED"));
        stats.setFailedCount(paymentRepository.countByPaymentStatusAndIsActiveTrue("FAILED"));
        
        // Count by payment method
        stats.setUppCount(paymentRepository.countByPaymentMethodAndIsActiveTrue("UPP_DEVICE"));
        stats.setCardCount(paymentRepository.countByPaymentMethodAndIsActiveTrue("CARD"));
        stats.setCashCount(paymentRepository.countByPaymentMethodAndIsActiveTrue("CASH"));
        stats.setTransferCount(paymentRepository.countByPaymentMethodAndIsActiveTrue("TRANSFER"));
        
        // Sum totals
        stats.setTotalUppPayments(paymentRepository.sumCompletedUppPayments());
        
        // Recent payments
        stats.setRecentPayments(paymentRepository.findTop10ByIsActiveTrueOrderByCreatedAtDesc());
        
        return stats;
    }
    
    /**
     * Get UPP device capabilities
     */
    public Map<String, Object> getUppDeviceCapabilities(String deviceType) {
        return uppService.getDeviceCapabilities(deviceType);
    }
    
    /**
     * Check UPP service health
     */
    public boolean isUppServiceHealthy() {
        return uppService.isServiceHealthy();
    }
    
    /**
     * Get supported currencies
     */
    public Map<String, Object> getSupportedCurrencies() {
        return uppService.getSupportedCurrencies();
    }
    
    /**
     * Register a device with UPP
     */
    public UniversalPaymentProtocolService.UppDeviceRegistration registerUppDevice(
            String deviceType, String deviceId, String[] capabilities) {
        return uppService.registerDevice(deviceType, deviceId, capabilities);
    }
    
    /**
     * Refund a payment
     */
    @Transactional
    public PaymentResult refundPayment(Long paymentId, BigDecimal refundAmount) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
            
            if (!payment.isCompleted()) {
                return new PaymentResult(false, "Cannot refund incomplete payment", null);
            }
            
            if (refundAmount.compareTo(payment.getAmount()) > 0) {
                return new PaymentResult(false, "Refund amount cannot exceed original payment", null);
            }
            
            // Create refund payment record
            Payment refund = new Payment(payment.getBooking(), refundAmount.negate(), payment.getPaymentMethod());
            refund.setDescription("Refund for payment #" + payment.getId());
            refund.setPaymentStatus("COMPLETED");
            refund.setProcessedAt(LocalDateTime.now());
            refund.setCustomerEmail(payment.getCustomerEmail());
            
            // Copy device information if it's a UPP payment
            if (payment.isUppPayment()) {
                refund.setDeviceType(payment.getDeviceType());
                refund.setDeviceId(payment.getDeviceId());
                refund.setUppTransactionId(payment.getUppTransactionId() + "_refund");
            }
            
            refund = paymentRepository.save(refund);
            
            // Update booking
            updateBookingPayment(payment.getBooking(), refundAmount.negate());
            
            return new PaymentResult(true, "Refund processed successfully", refund);
            
        } catch (Exception e) {
            logger.error("Refund processing error: {}", e.getMessage(), e);
            return new PaymentResult(false, "Refund failed: " + e.getMessage(), null);
        }
    }
    
    // Helper methods
    private String serializeMetadata(Map<String, Object> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            logger.error("Error serializing metadata: {}", e.getMessage());
            return "{}";
        }
    }
    
    // Result classes
    public static class PaymentResult {
        private boolean success;
        private String message;
        private Payment payment;
        
        public PaymentResult(boolean success, String message, Payment payment) {
            this.success = success;
            this.message = message;
            this.payment = payment;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Payment getPayment() { return payment; }
    }
    
    public static class PaymentStatistics {
        private Long pendingCount;
        private Long completedCount;
        private Long failedCount;
        private Long uppCount;
        private Long cardCount;
        private Long cashCount;
        private Long transferCount;
        private BigDecimal totalUppPayments;
        private List<Payment> recentPayments;
        
        // Getters and Setters
        public Long getPendingCount() { return pendingCount; }
        public void setPendingCount(Long pendingCount) { this.pendingCount = pendingCount; }
        
        public Long getCompletedCount() { return completedCount; }
        public void setCompletedCount(Long completedCount) { this.completedCount = completedCount; }
        
        public Long getFailedCount() { return failedCount; }
        public void setFailedCount(Long failedCount) { this.failedCount = failedCount; }
        
        public Long getUppCount() { return uppCount; }
        public void setUppCount(Long uppCount) { this.uppCount = uppCount; }
        
        public Long getCardCount() { return cardCount; }
        public void setCardCount(Long cardCount) { this.cardCount = cardCount; }
        
        public Long getCashCount() { return cashCount; }
        public void setCashCount(Long cashCount) { this.cashCount = cashCount; }
        
        public Long getTransferCount() { return transferCount; }
        public void setTransferCount(Long transferCount) { this.transferCount = transferCount; }
        
        public BigDecimal getTotalUppPayments() { return totalUppPayments; }
        public void setTotalUppPayments(BigDecimal totalUppPayments) { this.totalUppPayments = totalUppPayments; }
        
        public List<Payment> getRecentPayments() { return recentPayments; }
        public void setRecentPayments(List<Payment> recentPayments) { this.recentPayments = recentPayments; }
    }
}
