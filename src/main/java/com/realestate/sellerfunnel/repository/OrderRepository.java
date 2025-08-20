package com.realestate.sellerfunnel.repository;

import com.realestate.sellerfunnel.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Find orders by business
    List<Order> findByBusinessIdOrderByOrderDateDesc(String businessId);
    
    // Find orders by client
    List<Order> findByClientIdOrderByOrderDateDesc(Long clientId);
    
    // Find orders by status
    List<Order> findByStatusOrderByOrderDateDesc(String status);
    
    // Find orders by business and status
    List<Order> findByBusinessIdAndStatusOrderByOrderDateDesc(String businessId, String status);
    
    // Find orders by order type
    List<Order> findByOrderTypeOrderByOrderDateDesc(String orderType);
    
    // Find orders by business and order type
    List<Order> findByBusinessIdAndOrderTypeOrderByOrderDateDesc(String businessId, String orderType);
    
    // Find recent orders
    List<Order> findTop5ByBusinessIdOrderByOrderDateDesc(String businessId);
    
    // Find orders by customer email
    List<Order> findByCustomerEmailOrderByOrderDateDesc(String customerEmail);
    
    // Find orders in date range
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate DESC")
    List<Order> findByOrderDateBetween(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    // Find orders by business and date range
    @Query("SELECT o FROM Order o WHERE o.businessId = :businessId AND o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate DESC")
    List<Order> findByBusinessIdAndOrderDateBetween(@Param("businessId") String businessId,
                                                     @Param("startDate") LocalDateTime startDate, 
                                                     @Param("endDate") LocalDateTime endDate);
    
    // Count orders by status
    @Query("SELECT o.status, COUNT(o) FROM Order o WHERE o.businessId = :businessId GROUP BY o.status")
    List<Object[]> countByBusinessIdAndStatus(@Param("businessId") String businessId);
    
    // Sum total by business and status
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.businessId = :businessId AND o.status = :status")
    BigDecimal sumTotalByBusinessIdAndStatus(@Param("businessId") String businessId, @Param("status") String status);
    
    // Count orders by business
    long countByBusinessId(String businessId);
    
    // Sum total by business
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.businessId = :businessId")
    BigDecimal sumTotalByBusinessId(@Param("businessId") String businessId);
    
    // Find real estate transactions
    @Query("SELECT o FROM Order o WHERE o.orderType IN ('PROPERTY_SALE', 'PROPERTY_PURCHASE') ORDER BY o.orderDate DESC")
    List<Order> findRealEstateTransactions();
    
    // Find real estate transactions by business
    @Query("SELECT o FROM Order o WHERE o.businessId = :businessId AND o.orderType IN ('PROPERTY_SALE', 'PROPERTY_PURCHASE') ORDER BY o.orderDate DESC")
    List<Order> findRealEstateTransactionsByBusinessId(@Param("businessId") String businessId);
    
    // Find orders with commission
    @Query("SELECT o FROM Order o WHERE o.commissionAmount IS NOT NULL AND o.commissionAmount > 0 ORDER BY o.orderDate DESC")
    List<Order> findOrdersWithCommission();
    
    // Sum commission by business
    @Query("SELECT COALESCE(SUM(o.commissionAmount), 0) FROM Order o WHERE o.businessId = :businessId AND o.commissionAmount IS NOT NULL")
    BigDecimal sumCommissionByBusinessId(@Param("businessId") String businessId);
    
    // Find orders by payment status
    List<Order> findByPaymentStatusOrderByOrderDateDesc(String paymentStatus);
    
    // Find unpaid orders
    @Query("SELECT o FROM Order o WHERE o.paymentStatus = 'PENDING' OR o.paymentStatus = 'FAILED' ORDER BY o.orderDate DESC")
    List<Order> findUnpaidOrders();
    
    // Find orders needing attention (pending for too long)
    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' AND o.orderDate < :cutoffDate ORDER BY o.orderDate ASC")
    List<Order> findOrdersNeedingAttention(@Param("cutoffDate") LocalDateTime cutoffDate);
}