package com.realestate.sellerfunnel.repository;

import com.realestate.sellerfunnel.model.ContentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContentTemplateRepository extends JpaRepository<ContentTemplate, Long> {
    
    List<ContentTemplate> findAllByOrderByCreatedAtDesc();
    
    List<ContentTemplate> findByTypeAndIsActiveTrueOrderByCreatedAtDesc(String type);
    
    List<ContentTemplate> findByTargetAudienceAndIsActiveTrueOrderByCreatedAtDesc(String targetAudience);
    
    List<ContentTemplate> findByCategoryAndIsActiveTrueOrderByCreatedAtDesc(String category);
    
    List<ContentTemplate> findByTypeAndTargetAudienceAndIsActiveTrueOrderByCreatedAtDesc(String type, String targetAudience);
}