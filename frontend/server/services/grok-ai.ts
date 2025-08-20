import OpenAI from "openai";

if (!process.env.XAI_API_KEY) {
  throw new Error("XAI_API_KEY environment variable is required");
}

const openai = new OpenAI({ 
  baseURL: "https://api.x.ai/v1", 
  apiKey: process.env.XAI_API_KEY 
});

export interface SocialTrendInsight {
  ingredient: string;
  trendPercentage: number;
  platform: string;
  confidence: number;
  recommendation: string;
}

export interface CompetitorInsight {
  competitor: string;
  action: string;
  impact: 'low' | 'medium' | 'high';
  recommendation: string;
}

export interface SentimentInsight {
  topic: string;
  sentiment: 'positive' | 'negative' | 'neutral';
  mentions: number;
  confidence: number;
  recommendation: string;
}

export interface PricingInsight {
  item: string;
  currentPrice: number;
  suggestedPrice: number;
  reason: string;
  confidence: number;
}

export class GrokAIService {
  async analyzeSocialTrends(keywords: string[]): Promise<SocialTrendInsight[]> {
    try {
      const prompt = `
        Analyze current social media trends for these Hawaiian poke shop keywords: ${keywords.join(', ')}.
        Focus on trending ingredients, preparation styles, and customer preferences.
        Provide insights in JSON format with ingredient, trendPercentage, platform, confidence, and recommendation fields.
        Base analysis on real Hawaiian food trends and poke shop market data.
      `;

      const response = await openai.chat.completions.create({
        model: "grok-2-1212",
        messages: [
          {
            role: "system",
            content: "You are a Hawaiian food trend analyst specializing in poke shop market intelligence. Provide actionable insights based on current social media trends."
          },
          {
            role: "user",
            content: prompt
          }
        ],
        response_format: { type: "json_object" }
      });

      const result = JSON.parse(response.choices[0].message.content || '{"trends": []}');
      return result.trends || [];
    } catch (error) {
      console.error('Error analyzing social trends:', error);
      throw new Error('Failed to analyze social trends');
    }
  }

  async analyzeCompetitorActivity(competitors: string[]): Promise<CompetitorInsight[]> {
    try {
      const prompt = `
        Analyze competitor activity for these poke shops in Hawaii: ${competitors.join(', ')}.
        Focus on pricing changes, menu updates, promotions, and market positioning.
        Provide insights in JSON format with competitor, action, impact, and recommendation fields.
      `;

      const response = await openai.chat.completions.create({
        model: "grok-2-1212",
        messages: [
          {
            role: "system",
            content: "You are a competitive intelligence analyst for Hawaiian poke restaurants. Provide strategic insights about competitor activities."
          },
          {
            role: "user",
            content: prompt
          }
        ],
        response_format: { type: "json_object" }
      });

      const result = JSON.parse(response.choices[0].message.content || '{"insights": []}');
      return result.insights || [];
    } catch (error) {
      console.error('Error analyzing competitor activity:', error);
      throw new Error('Failed to analyze competitor activity');
    }
  }

  async analyzeSentiment(reviews: string[]): Promise<SentimentInsight[]> {
    try {
      const prompt = `
        Analyze customer sentiment from these reviews for Allii Fish Market: ${reviews.slice(0, 10).join(' | ')}.
        Identify key topics, sentiment trends, and actionable insights.
        Provide results in JSON format with topic, sentiment, mentions, confidence, and recommendation fields.
      `;

      const response = await openai.chat.completions.create({
        model: "grok-2-1212",
        messages: [
          {
            role: "system",
            content: "You are a customer sentiment analyst specializing in restaurant feedback analysis. Provide actionable insights from customer reviews."
          },
          {
            role: "user",
            content: prompt
          }
        ],
        response_format: { type: "json_object" }
      });

      const result = JSON.parse(response.choices[0].message.content || '{"sentiments": []}');
      return result.sentiments || [];
    } catch (error) {
      console.error('Error analyzing sentiment:', error);
      throw new Error('Failed to analyze sentiment');
    }
  }

  async optimizePricing(menuItems: any[], salesData: any[]): Promise<PricingInsight[]> {
    try {
      const prompt = `
        Analyze pricing optimization for these menu items and sales data:
        Menu: ${JSON.stringify(menuItems.slice(0, 5))}
        Sales: ${JSON.stringify(salesData.slice(0, 10))}
        
        Consider market positioning, cost margins, and customer demand.
        Provide pricing recommendations in JSON format with item, currentPrice, suggestedPrice, reason, and confidence fields.
      `;

      const response = await openai.chat.completions.create({
        model: "grok-2-1212",
        messages: [
          {
            role: "system",
            content: "You are a restaurant pricing strategist specializing in Hawaiian poke shops. Provide data-driven pricing recommendations."
          },
          {
            role: "user",
            content: prompt
          }
        ],
        response_format: { type: "json_object" }
      });

      const result = JSON.parse(response.choices[0].message.content || '{"pricing": []}');
      return result.pricing || [];
    } catch (error) {
      console.error('Error optimizing pricing:', error);
      throw new Error('Failed to optimize pricing');
    }
  }

  async generateMenuSuggestions(seasonalIngredients: string[], customerPreferences: any[]): Promise<any[]> {
    try {
      const prompt = `
        Generate new menu item suggestions for Allii Fish Market based on:
        Seasonal ingredients: ${seasonalIngredients.join(', ')}
        Customer preferences: ${JSON.stringify(customerPreferences.slice(0, 5))}
        
        Focus on authentic Hawaiian poke combinations that leverage trending ingredients.
        Provide suggestions in JSON format with name, description, ingredients, category, and estimatedPrice fields.
      `;

      const response = await openai.chat.completions.create({
        model: "grok-2-1212",
        messages: [
          {
            role: "system",
            content: "You are a Hawaiian cuisine chef and menu developer specializing in authentic poke creations. Suggest innovative yet traditional menu items."
          },
          {
            role: "user",
            content: prompt
          }
        ],
        response_format: { type: "json_object" }
      });

      const result = JSON.parse(response.choices[0].message.content || '{"suggestions": []}');
      return result.suggestions || [];
    } catch (error) {
      console.error('Error generating menu suggestions:', error);
      throw new Error('Failed to generate menu suggestions');
    }
  }

  async forecastDemand(historicalData: any[], weatherData?: any, events?: any[]): Promise<any> {
    try {
      const prompt = `
        Forecast demand for Allii Fish Market based on:
        Historical sales: ${JSON.stringify(historicalData.slice(0, 10))}
        Weather: ${JSON.stringify(weatherData)}
        Local events: ${JSON.stringify(events)}
        
        Provide demand forecast in JSON format with date, expectedOrders, confidence, and factors fields.
      `;

      const response = await openai.chat.completions.create({
        model: "grok-2-1212",
        messages: [
          {
            role: "system",
            content: "You are a restaurant demand forecasting specialist. Analyze patterns and external factors to predict customer demand."
          },
          {
            role: "user",
            content: prompt
          }
        ],
        response_format: { type: "json_object" }
      });

      const result = JSON.parse(response.choices[0].message.content || '{"forecast": []}');
      return result.forecast || [];
    } catch (error) {
      console.error('Error forecasting demand:', error);
      throw new Error('Failed to forecast demand');
    }
  }
}

export const grokAI = new GrokAIService();
