package com.realestate.sellerfunnel.repository;

import com.realestate.sellerfunnel.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    java.util.List<Transaction> findTop50ByOrderByCreatedAtDesc();

    java.util.List<Transaction> findByRoomIdOrderByCreatedAtDesc(Long roomId);
    
    java.util.List<Transaction> findByRoomIdOrderByCreatedAtAsc(Long roomId);

    java.util.List<Transaction> findByRoomIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long roomId, 
                                                                                   java.time.LocalDateTime start, 
                                                                                   java.time.LocalDateTime end);

    java.util.List<Transaction> findByTransactionTypeOrderByCreatedAtDesc(String transactionType);

    java.util.List<Transaction> findByTransactionCategoryOrderByCreatedAtDesc(String transactionCategory);

    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :start AND :end ORDER BY t.createdAt DESC")
    java.util.List<Transaction> findByDateRange(@Param("start") java.time.LocalDateTime start,
                                               @Param("end") java.time.LocalDateTime end);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.transactionType = 'PAYMENT'")
    java.math.BigDecimal getTotalPayments();

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.transactionType = 'CHARGE'")
    java.math.BigDecimal getTotalCharges();

    @Query("SELECT SUM(CASE WHEN t.transactionType IN ('PAYMENT', 'DEPOSIT', 'REFUND') THEN -t.amount ELSE t.amount END) FROM Transaction t WHERE t.room.id = :roomId")
    java.math.BigDecimal calculateRoomBalance(@Param("roomId") Long roomId);
}
