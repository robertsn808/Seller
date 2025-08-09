package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.AIGeneratedContent;
import com.realestate.sellerfunnel.repository.AIGeneratedContentRepository;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContentMemoryService {
    
    @Autowired
    private AIGeneratedContentRepository aiGeneratedContentRepository;
    
    private final JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();
    
    /**
     * Check if new content is too similar to existing content
     */
    public boolean isContentTooSimilar(String newContent, String contentType, String targetAudience, double threshold) {
        List<AIGeneratedContent> existingContent = aiGeneratedContentRepository
            .findByContentTypeAndTargetAudienceOrderByCreatedAtDesc(contentType, targetAudience);
        
        for (AIGeneratedContent content : existingContent) {
            double similarityScore = calculateSimilarity(newContent, content.getContent());
            if (similarityScore >= threshold) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Calculate similarity between two content strings
     */
    public double calculateSimilarity(String content1, String content2) {
        if (content1 == null || content2 == null) {
            return 0.0;
        }
        
        // Normalize content for comparison
        String normalized1 = normalizeContent(content1);
        String normalized2 = normalizeContent(content2);
        
        return similarity.apply(normalized1, normalized2);
    }
    
    /**
     * Find similar content and return similarity scores
     */
    public List<Map<String, Object>> findSimilarContent(String newContent, String contentType, String targetAudience) {
        List<AIGeneratedContent> existingContent = aiGeneratedContentRepository
            .findByContentTypeAndTargetAudienceOrderByCreatedAtDesc(contentType, targetAudience);
        
        List<Map<String, Object>> similarContent = new ArrayList<>();
        
        for (AIGeneratedContent content : existingContent) {
            double similarityScore = calculateSimilarity(newContent, content.getContent());
            if (similarityScore > 0.3) { // Only include content with >30% similarity
                Map<String, Object> result = new HashMap<>();
                result.put("content", content);
                result.put("similarityScore", similarityScore);
                similarContent.add(result);
            }
        }
        
        // Sort by similarity score descending
        similarContent.sort((a, b) -> Double.compare((Double) b.get("similarityScore"), (Double) a.get("similarityScore")));
        
        return similarContent;
    }
    
    /**
     * Get content generation statistics
     */
    public Map<String, Object> getContentStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Most generated content types
        List<Object[]> contentTypes = aiGeneratedContentRepository.findMostGeneratedContentTypes();
        stats.put("contentTypes", contentTypes);
        
        // Recent content count (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<AIGeneratedContent> recentContent = aiGeneratedContentRepository.findRecentContent(thirtyDaysAgo);
        stats.put("recentContentCount", recentContent.size());
        
        // Frequently generated content
        List<AIGeneratedContent> frequentContent = aiGeneratedContentRepository.findFrequentlyGeneratedContent(3);
        stats.put("frequentContent", frequentContent);
        
        // Unused content (not used in last 7 days)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<AIGeneratedContent> unusedContent = aiGeneratedContentRepository.findUnusedContent(sevenDaysAgo);
        stats.put("unusedContent", unusedContent);
        
        return stats;
    }
    
    /**
     * Get content suggestions based on existing patterns
     */
    public List<String> getContentSuggestions(String contentType, String targetAudience) {
        List<AIGeneratedContent> existingContent = aiGeneratedContentRepository
            .findByContentTypeAndTargetAudienceOrderByCreatedAtDesc(contentType, targetAudience);
        
        List<String> suggestions = new ArrayList<>();
        
        // Extract common patterns and keywords
        Set<String> commonKeywords = new HashSet<>();
        for (AIGeneratedContent content : existingContent) {
            if (content.getKeywords() != null) {
                String[] keywords = content.getKeywords().split(",");
                for (String keyword : keywords) {
                    commonKeywords.add(keyword.trim().toLowerCase());
                }
            }
        }
        
        // Generate suggestions based on common keywords
        for (String keyword : commonKeywords) {
            if (keyword.length() > 3) {
                suggestions.add("Include keyword: " + keyword);
            }
        }
        
        // Add suggestions based on content type
        switch (contentType) {
            case "FACEBOOK_POST":
                suggestions.add("Keep content under 200 characters for better engagement");
                suggestions.add("Include a clear call-to-action");
                suggestions.add("Use emojis to make content more engaging");
                break;
            case "GOOGLE_AD":
                suggestions.add("Include target keywords in headline");
                suggestions.add("Keep headline under 30 characters");
                suggestions.add("Include location-specific terms");
                break;
            case "EMAIL":
                suggestions.add("Use a compelling subject line");
                suggestions.add("Keep content concise and scannable");
                suggestions.add("Include personalization elements");
                break;
        }
        
        return suggestions;
    }
    
    /**
     * Update content usage tracking
     */
    public void updateContentUsage(Long contentId) {
        Optional<AIGeneratedContent> contentOpt = aiGeneratedContentRepository.findById(contentId);
        if (contentOpt.isPresent()) {
            AIGeneratedContent content = contentOpt.get();
            content.setLastUsed(LocalDateTime.now());
            content.setGenerationCount(content.getGenerationCount() + 1);
            aiGeneratedContentRepository.save(content);
        }
    }
    
    /**
     * Normalize content for similarity comparison
     */
    private String normalizeContent(String content) {
        return content.toLowerCase()
            .replaceAll("[^a-zA-Z0-9\\s]", " ") // Remove special characters
            .replaceAll("\\s+", " ") // Normalize whitespace
            .trim();
    }
    
    /**
     * Extract keywords from content
     */
    public String extractKeywords(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        
        // Simple keyword extraction - in a real implementation, you might use NLP libraries
        String[] words = content.toLowerCase()
            .replaceAll("[^a-zA-Z0-9\\s]", " ")
            .split("\\s+");
        
        // Filter out common stop words
        Set<String> stopWords = new HashSet<>(Arrays.asList(
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by",
            "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "do", "does", "did",
            "will", "would", "could", "should", "may", "might", "must", "can", "this", "that", "these", "those"
        ));
        
        List<String> keywords = Arrays.stream(words)
            .filter(word -> word.length() > 3 && !stopWords.contains(word))
            .distinct()
            .limit(10) // Limit to top 10 keywords
            .collect(Collectors.toList());
        
        return String.join(", ", keywords);
    }
} 