package com.realestate.sellerfunnel.repository;

import com.realestate.sellerfunnel.model.Campaign;
import com.realestate.sellerfunnel.model.CampaignLead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CampaignLeadRepository extends JpaRepository<CampaignLead, Long> {
    
    List<CampaignLead> findByCampaignOrderByCreatedAtDesc(Campaign campaign);
    
    List<CampaignLead> findBySourceOrderByCreatedAtDesc(String source);
    
    @Query("SELECT cl FROM CampaignLead cl WHERE cl.createdAt >= :startDate AND cl.createdAt <= :endDate ORDER BY cl.createdAt DESC")
    List<CampaignLead> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT cl.source, COUNT(cl) FROM CampaignLead cl GROUP BY cl.source ORDER BY COUNT(cl) DESC")
    List<Object[]> getLeadCountBySource();
    
    @Query("SELECT cl.campaign.name, COUNT(cl) FROM CampaignLead cl GROUP BY cl.campaign ORDER BY COUNT(cl) DESC")
    List<Object[]> getLeadCountByCampaign();
}