package com.realestate.sellerfunnel.repository;

import com.realestate.sellerfunnel.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Find payments by booking
    List<Payment> findByBookingIdAndIsActiveTrueOrderByCreatedAtDesc(Long bookingId);
    
    // Find payments by status
    List<Payment> findByPaymentStatusAndIsActiveTrue(String paymentStatus);
    
    // Find payments by payment method
    List<Payment> findByPaymentMethodAndIsActiveTrue(String paymentMethod);
    
    // Find payments by device type
    List<Payment> findByDeviceTypeAndIsActiveTrue(String deviceType);
    
    // Find UPP payments
    List<Payment> findByPaymentMethodAndIsActiveTrueOrderByCreatedAtDesc(String paymentMethod);
    
    // Find payments by date range
    List<Payment> findByCreatedAtBetweenAndIsActiveTrue(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find payments by amount range
    List<Payment> findByAmountBetweenAndIsActiveTrue(BigDecimal minAmount, BigDecimal maxAmount);
    
    // Find payments by customer email
    List<Payment> findByCustomerEmailAndIsActiveTrueOrderByCreatedAtDesc(String customerEmail);
    
    // Find payments by Stripe payment intent ID
    Payment findByStripePaymentIntentId(String stripePaymentIntentId);
    
    // Find payments by UPP transaction ID
    Payment findByUppTransactionId(String uppTransactionId);
    
    // Count payments by status
    Long countByPaymentStatusAndIsActiveTrue(String paymentStatus);
    
    // Count payments by payment method
    Long countByPaymentMethodAndIsActiveTrue(String paymentMethod);
    
    // Count payments by device type
    Long countByDeviceTypeAndIsActiveTrue(String deviceType);
    
    // Sum total payments by booking
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.booking.id = :bookingId AND p.paymentStatus = 'COMPLETED' AND p.isActive = true")
    BigDecimal sumCompletedPaymentsByBooking(@Param("bookingId") Long bookingId);
    
    // Sum total payments by date range
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate AND p.paymentStatus = 'COMPLETED' AND p.isActive = true")
    BigDecimal sumCompletedPaymentsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Sum total UPP payments
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentMethod = 'UPP_DEVICE' AND p.paymentStatus = 'COMPLETED' AND p.isActive = true")
    BigDecimal sumCompletedUppPayments();
    
    // Find recent payments
    List<Payment> findTop10ByIsActiveTrueOrderByCreatedAtDesc();
    
    // Find failed payments
    List<Payment> findByPaymentStatusAndIsActiveTrueOrderByCreatedAtDesc(String paymentStatus);
    
    // Search payments by description
    @Query("SELECT p FROM Payment p WHERE p.description LIKE %:searchTerm% AND p.isActive = true ORDER BY p.createdAt DESC")
    List<Payment> searchByDescription(@Param("searchTerm") String searchTerm);
    
    // Find payments needing attention (failed or pending for too long)
    @Query("SELECT p FROM Payment p WHERE (p.paymentStatus = 'FAILED' OR (p.paymentStatus = 'PENDING' AND p.createdAt < :cutoffDate)) AND p.isActive = true ORDER BY p.createdAt DESC")
    List<Payment> findPaymentsNeedingAttention(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Get payment statistics
    @Query("SELECT p.paymentMethod, COUNT(p), COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = 'COMPLETED' AND p.isActive = true GROUP BY p.paymentMethod")
    List<Object[]> getPaymentMethodStatistics();
    
    // Get device type statistics
    @Query("SELECT p.deviceType, COUNT(p), COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentMethod = 'UPP_DEVICE' AND p.paymentStatus = 'COMPLETED' AND p.isActive = true GROUP BY p.deviceType")
    List<Object[]> getDeviceTypeStatistics();
}
