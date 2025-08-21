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

    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :start AND :end ORDER BY t.createdAt DESC")
    java.util.List<Transaction> findByDateRange(@Param("start") java.time.LocalDateTime start,
                                               @Param("end") java.time.LocalDateTime end);
}
