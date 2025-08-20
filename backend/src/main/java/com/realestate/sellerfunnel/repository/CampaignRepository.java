package com.realestate.sellerfunnel.repository;

import com.realestate.sellerfunnel.model.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    
    List<Campaign> findAllByOrderByCreatedAtDesc();
    
    List<Campaign> findByStatusOrderByCreatedAtDesc(String status);
    
    List<Campaign> findByTypeOrderByCreatedAtDesc(String type);
    
    List<Campaign> findByTargetAudienceOrderByCreatedAtDesc(String targetAudience);
    
    @Query("SELECT c FROM Campaign c WHERE c.status = 'ACTIVE' AND c.endDate > :now")
    List<Campaign> findActiveCampaigns(LocalDateTime now);
    
    @Query("SELECT c FROM Campaign c WHERE c.createdAt >= :startDate AND c.createdAt <= :endDate ORDER BY c.createdAt DESC")
    List<Campaign> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT SUM(c.cost) FROM Campaign c WHERE c.createdAt >= :startDate AND c.createdAt <= :endDate")
    Double getTotalSpendByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT SUM(c.leads) FROM Campaign c WHERE c.createdAt >= :startDate AND c.createdAt <= :endDate")
    Integer getTotalLeadsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Campaign> findByNameIgnoreCase(String name);
}