package com.realestate.sellerfunnel.repository;

import com.realestate.sellerfunnel.model.AIGeneratedContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AIGeneratedContentRepository extends JpaRepository<AIGeneratedContent, Long> {
    
    // Find content by type and target audience
    List<AIGeneratedContent> findByContentTypeAndTargetAudienceOrderByCreatedAtDesc(
        String contentType, String targetAudience);
    
    // Find content by category
    List<AIGeneratedContent> findByCategoryOrderByCreatedAtDesc(String category);
    
    // Find recent content (last 30 days)
    @Query("SELECT a FROM AIGeneratedContent a WHERE a.createdAt >= :startDate ORDER BY a.createdAt DESC")
    List<AIGeneratedContent> findRecentContent(@Param("startDate") LocalDateTime startDate);
    
    // Find content with similar keywords
    @Query("SELECT a FROM AIGeneratedContent a WHERE a.keywords LIKE %:keyword% ORDER BY a.createdAt DESC")
    List<AIGeneratedContent> findByKeyword(@Param("keyword") String keyword);
    
    // Find content by similarity score threshold
    @Query("SELECT a FROM AIGeneratedContent a WHERE a.similarityScore >= :threshold ORDER BY a.similarityScore DESC")
    List<AIGeneratedContent> findBySimilarityThreshold(@Param("threshold") Double threshold);
    
    // Find most frequently generated content types
    @Query("SELECT a.contentType, COUNT(a) as count FROM AIGeneratedContent a GROUP BY a.contentType ORDER BY count DESC")
    List<Object[]> findMostGeneratedContentTypes();
    
    // Find content by target audience and date range
    @Query("SELECT a FROM AIGeneratedContent a WHERE a.targetAudience = :audience AND a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<AIGeneratedContent> findByTargetAudienceAndDateRange(
        @Param("audience") String audience, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate);
    
    // Find content with high generation count (frequently used patterns)
    @Query("SELECT a FROM AIGeneratedContent a WHERE a.generationCount >= :minCount ORDER BY a.generationCount DESC")
    List<AIGeneratedContent> findFrequentlyGeneratedContent(@Param("minCount") Integer minCount);
    
    // Find content by context similarity
    @Query("SELECT a FROM AIGeneratedContent a WHERE a.context LIKE %:context% ORDER BY a.createdAt DESC")
    List<AIGeneratedContent> findByContext(@Param("context") String context);
    
    // Find content that hasn't been used recently
    @Query("SELECT a FROM AIGeneratedContent a WHERE a.lastUsed <= :date ORDER BY a.lastUsed ASC")
    List<AIGeneratedContent> findUnusedContent(@Param("date") LocalDateTime date);
} 