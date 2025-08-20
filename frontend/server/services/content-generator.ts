import OpenAI from "openai";

if (!process.env.XAI_API_KEY) {
  throw new Error('XAI_API_KEY environment variable is required');
}

const openai = new OpenAI({ 
  baseURL: "https://api.x.ai/v1", 
  apiKey: process.env.XAI_API_KEY 
});

interface ContentRequest {
  type: string;
  platform?: string;
  topic: string;
  tone: string;
  targetAudience: string;
  keywords: string[];
  businessContext: string;
  callToAction?: string;
  includeHashtags?: boolean;
  maxLength?: number;
}

interface GeneratedContent {
  title: string;
  content: string;
  hashtags?: string[];
  wordCount: number;
  estimatedReadTime?: number;
  seoScore?: number;
}

export class ContentGenerator {
  
  private formatPrompt(request: ContentRequest): string {
    const basePrompt = `You are an expert content creator for Allii Fish Market, a premium poke shop in Honolulu, Hawaii. 

BUSINESS CONTEXT: ${request.businessContext}

Create ${request.type === 'social_post' ? `a ${request.platform} post` : request.type.replace('_', ' ')} with these specifications:
- Topic: ${request.topic}
- Tone: ${request.tone}
- Target Audience: ${request.targetAudience}
- Keywords to include: ${request.keywords.join(', ')}
${request.callToAction ? `- Call to Action: ${request.callToAction}` : ''}
${request.maxLength ? `- Maximum length: ${request.maxLength} characters` : ''}

IMPORTANT REQUIREMENTS:
1. Make it authentic and specific to Hawaiian poke culture
2. Reference real menu items from Allii Fish Market (limu ahi, spicy creamy garlic ahi, wasabi ginger a'u, pipikaula, lechon, wasabi fried chicken)
3. Use local Hawaiian terminology when appropriate (ahi, a'u, poke, ohana)
4. Focus on freshness, quality ingredients, and authentic Hawaiian flavors
5. Include sensory details (taste, smell, texture, visual appeal)
6. Make it engaging and actionable
${request.includeHashtags ? '7. Include relevant hashtags at the end' : ''}

Format your response as JSON with these fields:
{
  "title": "Engaging title/headline",
  "content": "Main content body",
  ${request.includeHashtags ? '"hashtags": ["hashtag1", "hashtag2", "hashtag3"],' : ''}
  "wordCount": number,
  "estimatedReadTime": minutes_if_applicable,
  "seoScore": score_out_of_100
}`;

    return basePrompt;
  }

  async generateContent(request: ContentRequest): Promise<GeneratedContent> {
    try {
      const prompt = this.formatPrompt(request);
      
      const response = await openai.chat.completions.create({
        model: "grok-2-1212",
        messages: [
          {
            role: "system",
            content: "You are an expert content marketing specialist with deep knowledge of Hawaiian cuisine, social media trends, and local business marketing. Create authentic, engaging content that drives customer action."
          },
          {
            role: "user",
            content: prompt
          }
        ],
        response_format: { type: "json_object" },
        temperature: 0.8,
        max_tokens: 1000,
      });

      const result = JSON.parse(response.choices[0].message.content!);
      
      // Validate and clean the response
      return {
        title: result.title || `${request.type.replace('_', ' ')} for ${request.topic}`,
        content: result.content || "Content generation failed. Please try again.",
        hashtags: result.hashtags || [],
        wordCount: result.wordCount || result.content?.split(' ').length || 0,
        estimatedReadTime: result.estimatedReadTime,
        seoScore: result.seoScore || 75
      };
      
    } catch (error) {
      console.error('Content generation error:', error);
      throw new Error(`Failed to generate content: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
  }

  async generateSocialMediaVariations(baseContent: string, platforms: string[]): Promise<Record<string, string>> {
    const variations: Record<string, string> = {};
    
    for (const platform of platforms) {
      try {
        const prompt = `Adapt this content for ${platform} while maintaining the core message:

Original content: "${baseContent}"

Platform-specific requirements:
${platform === 'twitter' ? '- Maximum 280 characters' : ''}
${platform === 'instagram' ? '- Visual-focused, emoji-friendly' : ''}
${platform === 'facebook' ? '- Community-focused, conversational' : ''}
${platform === 'tiktok' ? '- Trendy, energetic, youth-oriented' : ''}

Respond with only the adapted content, no additional text.`;

        const response = await openai.chat.completions.create({
          model: "grok-2-1212",
          messages: [{ role: "user", content: prompt }],
          temperature: 0.7,
          max_tokens: 300,
        });

        variations[platform] = response.choices[0].message.content!.trim();
      } catch (error) {
        console.error(`Failed to generate ${platform} variation:`, error);
        variations[platform] = baseContent; // Fallback to original
      }
    }

    return variations;
  }

  async analyzeContentPerformance(content: string, platform: string): Promise<{
    engagementPrediction: number;
    viralityScore: number;
    sentimentScore: number;
    suggestions: string[];
  }> {
    try {
      const prompt = `Analyze this ${platform} content and predict its performance:

Content: "${content}"

Provide analysis as JSON:
{
  "engagementPrediction": score_0_to_100,
  "viralityScore": score_0_to_100,
  "sentimentScore": score_0_to_100,
  "suggestions": ["improvement1", "improvement2", "improvement3"]
}`;

      const response = await openai.chat.completions.create({
        model: "grok-2-1212",
        messages: [{ role: "user", content: prompt }],
        response_format: { type: "json_object" },
        temperature: 0.3,
        max_tokens: 500,
      });

      return JSON.parse(response.choices[0].message.content!);
    } catch (error) {
      console.error('Content analysis error:', error);
      return {
        engagementPrediction: 50,
        viralityScore: 25,
        sentimentScore: 75,
        suggestions: ["Add more visual elements", "Include a stronger call-to-action", "Use trending hashtags"]
      };
    }
  }

  async generateHashtagsForContent(content: string, platform: string = 'instagram'): Promise<string[]> {
    try {
      const prompt = `Generate relevant hashtags for this ${platform} content about Hawaiian poke:

Content: "${content}"

Requirements:
- 10-15 hashtags
- Mix of popular and niche tags
- Include location-based tags for Hawaii/Honolulu
- Include food/poke specific tags
- Include some trending tags

Respond with only the hashtags, one per line, without the # symbol.`;

      const response = await openai.chat.completions.create({
        model: "grok-2-1212",
        messages: [{ role: "user", content: prompt }],
        temperature: 0.6,
        max_tokens: 200,
      });

      return response.choices[0].message.content!
        .split('\n')
        .map(tag => tag.trim())
        .filter(tag => tag.length > 0)
        .slice(0, 15);
    } catch (error) {
      console.error('Hashtag generation error:', error);
      return ['poke', 'hawaiianfood', 'freshfish', 'honolulu', 'ahi', 'pokebowl', 'seafood', 'healthy', 'local', 'authentic'];
    }
  }
}

export const contentGenerator = new ContentGenerator();