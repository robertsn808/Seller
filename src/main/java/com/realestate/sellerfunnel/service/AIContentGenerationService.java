package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.AIGeneratedContent;
import com.realestate.sellerfunnel.repository.AIGeneratedContentRepository;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AIContentGenerationService {
    
    @Autowired
    private AIGeneratedContentRepository aiGeneratedContentRepository;
    
    @Autowired
    private ContentMemoryService contentMemoryService;
    
    @Value("${openai.api.key:}")
    private String defaultOpenaiApiKey;
    
    @Value("${openai.model:gpt-3.5-turbo}")
    String defaultOpenaiModel;
    
    private OpenAiService openAiService;

    @Autowired
    private SettingsService settingsService;
    
    /**
     * Initialize OpenAI service
     */
    private OpenAiService getOpenAiService() {
        if (openAiService == null) {
            String apiKey = getEffectiveApiKey();
            if (apiKey != null && !apiKey.isEmpty()) {
                openAiService = new OpenAiService(apiKey);
            }
        }
        return openAiService;
    }

    private String getEffectiveApiKey() {
        var s = settingsService.getSettingsOrDefault();
        return (s.getOpenaiApiKey() != null && !s.getOpenaiApiKey().isEmpty()) ? s.getOpenaiApiKey() : defaultOpenaiApiKey;
    }

    private String getEffectiveModel() {
        var s = settingsService.getSettingsOrDefault();
        return (s.getOpenaiModel() != null && !s.getOpenaiModel().isEmpty()) ? s.getOpenaiModel() : defaultOpenaiModel;
    }
    
    /**
     * Generate content using AI with memory consideration
     */
    public AIGeneratedContent generateContent(String prompt, String contentType, String targetAudience, 
                                            String category, String context, int maxAttempts) {
        
        // Check if we have AI service available
        if (getOpenAiService() == null) {
            return generateFallbackContent(prompt, contentType, targetAudience, category, context);
        }
        
        // Get existing content for context
        List<AIGeneratedContent> existingContent = aiGeneratedContentRepository
            .findByContentTypeAndTargetAudienceOrderByCreatedAtDesc(contentType, targetAudience);
        
        // Get content suggestions
        List<String> suggestions = contentMemoryService.getContentSuggestions(contentType, targetAudience);
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                String generatedContent = generateWithAI(prompt, contentType, targetAudience, 
                                                       category, context, existingContent, suggestions, attempt);
                
                // Check if content is too similar to existing content
                if (!contentMemoryService.isContentTooSimilar(generatedContent, contentType, targetAudience, 0.7)) {
                    // Create and save the new content
                    AIGeneratedContent aiContent = new AIGeneratedContent();
                    aiContent.setPrompt(prompt);
                    aiContent.setContent(generatedContent);
                    aiContent.setContentType(contentType);
                    aiContent.setTargetAudience(targetAudience);
                    aiContent.setCategory(category);
                    aiContent.setContext(context);
                    aiContent.setKeywords(contentMemoryService.extractKeywords(generatedContent));
                    aiContent.setSimilarityScore(0.0); // New content
                    aiContent.setGenerationCount(1);
                    aiContent.setLastUsed(LocalDateTime.now());
                    
                    return aiGeneratedContentRepository.save(aiContent);
                } else {
                    // Content is too similar, try again with different instructions
                    prompt += " Make this content more unique and different from typical real estate content.";
                }
            } catch (Exception e) {
                // If AI generation fails, fall back to template-based generation
                if (attempt == maxAttempts) {
                    return generateFallbackContent(prompt, contentType, targetAudience, category, context);
                }
            }
        }
        
        // If all attempts failed, return fallback content
        return generateFallbackContent(prompt, contentType, targetAudience, category, context);
    }
    
    /**
     * Generate content using OpenAI API
     */
    private String generateWithAI(String prompt, String contentType, String targetAudience, 
                                String category, String context, List<AIGeneratedContent> existingContent,
                                List<String> suggestions, int attempt) {
        
        StringBuilder systemPrompt = new StringBuilder();
        systemPrompt.append("You are an expert real estate marketing copywriter. Generate engaging, conversion-focused content for ");
        systemPrompt.append(targetAudience.toLowerCase()).append(".\n\n");
        
        // Add content type specific instructions
        switch (contentType) {
            case "FACEBOOK_POST":
                systemPrompt.append("Create a Facebook post that is:\n");
                systemPrompt.append("- Under 200 characters for better engagement\n");
                systemPrompt.append("- Includes emojis and engaging language\n");
                systemPrompt.append("- Has a clear call-to-action\n");
                systemPrompt.append("- Avoids being too salesy\n");
                break;
            case "GOOGLE_AD":
                systemPrompt.append("Create a Google Ad that includes:\n");
                systemPrompt.append("- A compelling headline (under 30 characters)\n");
                systemPrompt.append("- Descriptive ad copy (under 90 characters)\n");
                systemPrompt.append("- A strong call-to-action\n");
                systemPrompt.append("- Target keywords naturally integrated\n");
                break;
            case "EMAIL":
                systemPrompt.append("Create an email that is:\n");
                systemPrompt.append("- Professional yet conversational\n");
                systemPrompt.append("- Includes a compelling subject line\n");
                systemPrompt.append("- Has clear, scannable content\n");
                systemPrompt.append("- Ends with a strong call-to-action\n");
                break;
            case "CRAIGSLIST_AD":
                systemPrompt.append("Create a Craigslist ad that is:\n");
                systemPrompt.append("- Detailed and informative\n");
                systemPrompt.append("- Includes relevant keywords\n");
                systemPrompt.append("- Professional but approachable\n");
                systemPrompt.append("- Has contact information\n");
                break;
        }
        
        // Add memory context
        if (!existingContent.isEmpty()) {
            systemPrompt.append("\nIMPORTANT: Avoid creating content too similar to these existing examples:\n");
            for (int i = 0; i < Math.min(3, existingContent.size()); i++) {
                AIGeneratedContent content = existingContent.get(i);
                systemPrompt.append("- ").append(content.getContent().substring(0, Math.min(100, content.getContent().length()))).append("...\n");
            }
        }
        
        // Add suggestions
        if (!suggestions.isEmpty()) {
            systemPrompt.append("\nConsider these suggestions:\n");
            for (String suggestion : suggestions.subList(0, Math.min(5, suggestions.size()))) {
                systemPrompt.append("- ").append(suggestion).append("\n");
            }
        }
        
        // Add attempt-specific instructions
        if (attempt > 1) {
            systemPrompt.append("\nThis is attempt ").append(attempt).append(". Make the content more unique and different from typical real estate content.\n");
        }
        
        String userPrompt = "Generate content for: " + prompt;
        if (context != null && !context.isEmpty()) {
            userPrompt += "\nContext: " + context;
        }
        if (category != null && !category.isEmpty()) {
            userPrompt += "\nCategory: " + category;
        }
        
        List<ChatMessage> messages = Arrays.asList(
            new ChatMessage("system", systemPrompt.toString()),
            new ChatMessage("user", userPrompt)
        );
        
        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model(getEffectiveModel())
            .messages(messages)
            .maxTokens(500)
            .temperature(0.8)
            .build();
        
        String response = getOpenAiService().createChatCompletion(request)
            .getChoices().get(0).getMessage().getContent();
        
        return response.trim();
    }
    
    /**
     * Generate fallback content when AI is not available
     */
    private AIGeneratedContent generateFallbackContent(String prompt, String contentType, String targetAudience, 
                                                      String category, String context) {
        
        // Use predefined templates based on content type and target audience
        String content = getFallbackTemplate(contentType, targetAudience, category);
        
        AIGeneratedContent aiContent = new AIGeneratedContent();
        aiContent.setPrompt(prompt);
        aiContent.setContent(content);
        aiContent.setContentType(contentType);
        aiContent.setTargetAudience(targetAudience);
        aiContent.setCategory(category);
        aiContent.setContext(context);
        aiContent.setKeywords(contentMemoryService.extractKeywords(content));
        aiContent.setSimilarityScore(0.0);
        aiContent.setGenerationCount(1);
        aiContent.setLastUsed(LocalDateTime.now());
        
        return aiGeneratedContentRepository.save(aiContent);
    }
    
    /**
     * Get fallback templates when AI is not available
     */
    private String getFallbackTemplate(String contentType, String targetAudience, String category) {
        Map<String, String> templates = new HashMap<>();
        
        // Seller-focused templates
        templates.put("SELLERS_FACEBOOK_POST", 
            "🏠 Need to sell your house fast? We buy houses in any condition - no repairs needed! 💰 Cash offer in 24 hours. Call now for a free consultation! 📞");
        
        templates.put("SELLERS_GOOGLE_AD", 
            "We Buy Houses Fast | Cash Offer in 24 Hours | No Repairs Needed | Free Consultation");
        
        templates.put("SELLERS_EMAIL", 
            "Subject: Quick Cash Offer for Your Property\n\n" +
            "Hi there,\n\n" +
            "I hope this email finds you well. I wanted to reach out because we're actively buying properties in your area and can provide a cash offer within 24 hours.\n\n" +
            "Whether your property needs repairs or is move-in ready, we're interested. No fees, no commissions, and no hassle.\n\n" +
            "Would you be interested in a free, no-obligation property evaluation?\n\n" +
            "Best regards,\n" +
            "Your Real Estate Team");
        
        // Buyer-focused templates
        templates.put("BUYERS_FACEBOOK_POST", 
            "🔍 Looking for your next investment property? We have off-market deals with great potential returns! 📈 Join our exclusive buyers list today! 🏘️");
        
        templates.put("BUYERS_GOOGLE_AD", 
            "Investment Properties Available | Off-Market Deals | Great Returns | Join Buyers List");
        
        templates.put("BUYERS_EMAIL", 
            "Subject: Exclusive Investment Opportunities Available\n\n" +
            "Hello,\n\n" +
            "I hope you're doing well. I wanted to let you know about some exclusive investment opportunities we have available in your area.\n\n" +
            "These are off-market properties with excellent potential returns. As a member of our buyers list, you'll get first access to these deals.\n\n" +
            "Would you like to schedule a call to discuss your investment goals?\n\n" +
            "Best regards,\n" +
            "Your Investment Team");
        
        // Category-specific templates
        if (category != null) {
            switch (category.toUpperCase()) {
                case "DISTRESSED":
                    return "🚨 Urgent: We buy distressed properties! Behind on payments? Facing foreclosure? We can help! 💰 Fast cash solutions available. Call now! 📞";
                case "INHERITED":
                    return "🏛️ Inherited a property you don't want? We specialize in helping families sell inherited homes quickly and hassle-free. 💝 Compassionate service guaranteed.";
                case "INVESTORS":
                    return "📊 Real estate investors wanted! Access to off-market deals with proven ROI. Join our network of successful investors today! 💼";
            }
        }
        
        // Default template based on content type and target audience
        String key = targetAudience + "_" + contentType;
        return templates.getOrDefault(key, 
            "Looking for real estate solutions? We're here to help! Contact us today for personalized assistance.");
    }
    
    /**
     * Generate multiple content variations
     */
    public List<AIGeneratedContent> generateContentVariations(String prompt, String contentType, 
                                                             String targetAudience, String category, 
                                                             String context, int count) {
        List<AIGeneratedContent> variations = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            String variationPrompt = prompt + " (Variation " + (i + 1) + ")";
            AIGeneratedContent variation = generateContent(variationPrompt, contentType, targetAudience, 
                                                         category, context, 3);
            variations.add(variation);
        }
        
        return variations;
    }
    
    /**
     * Generate email subject line using AI
     */
    public String generateEmailSubject(String messageText) {
        if (getOpenAiService() == null) {
            return "Important Update from Real Estate Connect";
        }
        
        List<ChatMessage> messages = Arrays.asList(
            new ChatMessage("system", 
                "You are an expert email copywriter. Create a compelling, professional email subject line " +
                "based on the email content. The subject line should be:\n" +
                "- Under 50 characters\n" +
                "- Engaging but not clickbait\n" +
                "- Clear and descriptive\n" +
                "- Professional and trustworthy\n" +
                "Return ONLY the subject line, nothing else."),
            new ChatMessage("user", "Generate a subject line for this email content: " + messageText)
        );
        
        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model(getEffectiveModel())
            .messages(messages)
            .maxTokens(50)
            .temperature(0.7)
            .build();
        
        try {
            String response = getOpenAiService().createChatCompletion(request)
                .getChoices().get(0).getMessage().getContent();
            return response.trim();
        } catch (Exception e) {
            return "Important Update from Real Estate Connect";
        }
    }
    
    /**
     * Generate email content using AI
     */
    public String generateEmailContent(String messageText) {
        if (getOpenAiService() == null) {
            return messageText;
        }
        
        List<ChatMessage> messages = Arrays.asList(
            new ChatMessage("system", 
                "You are an expert email copywriter for a real estate company. Enhance the given email content to be:\n" +
                "- Professional yet warm and engaging\n" +
                "- Well-structured with clear paragraphs\n" +
                "- Persuasive without being pushy\n" +
                "- Focused on value and benefits\n" +
                "- Including a clear call-to-action\n" +
                "Maintain the core message and key information while improving the writing quality."),
            new ChatMessage("user", "Enhance this email content while keeping its core message: " + messageText)
        );
        
        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model(getEffectiveModel())
            .messages(messages)
            .maxTokens(1000)
            .temperature(0.7)
            .build();
        
        try {
            String response = getOpenAiService().createChatCompletion(request)
                .getChoices().get(0).getMessage().getContent();
            return response.trim();
        } catch (Exception e) {
            return messageText;
        }
    }
} 