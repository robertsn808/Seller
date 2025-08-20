import { useQuery } from "@tanstack/react-query";
import { Lightbulb, AlertTriangle, Heart, CheckCircle, Eye, PlusCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useToast } from "@/hooks/use-toast";
import type { AiInsight } from "@shared/schema";

const iconMap = {
  social_trend: Lightbulb,
  pricing: AlertTriangle,
  sentiment: Heart,
  menu_suggestion: PlusCircle,
};

const colorMap = {
  social_trend: "bg-primary",
  pricing: "bg-warning",
  sentiment: "bg-success",
  menu_suggestion: "bg-accent",
};

export default function GrokInsights() {
  const { toast } = useToast();
  
  const { data: insights, isLoading } = useQuery<AiInsight[]>({
    queryKey: ["/api/ai/insights"],
    refetchInterval: 60000, // Refetch every minute
  });

  const handleApplyInsight = async (insightId: number) => {
    try {
      // TODO: Implement insight application logic
      toast({
        title: "Insight Applied",
        description: "The AI insight has been successfully applied to your business operations.",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to apply insight. Please try again.",
        variant: "destructive",
      });
    }
  };

  const handleReviewInsight = async (insightId: number) => {
    try {
      // TODO: Implement insight review logic
      toast({
        title: "Insight Under Review",
        description: "The insight has been flagged for detailed review.",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to review insight. Please try again.",
        variant: "destructive",
      });
    }
  };

  if (isLoading) {
    return (
      <div className="lg:col-span-2 bg-surface rounded-xl shadow-sm border border-gray-200">
        <div className="p-6 border-b border-gray-200">
          <div className="flex items-center justify-between">
            <h3 className="font-roboto font-bold text-lg text-text">Grok AI Real-Time Insights</h3>
            <div className="flex items-center space-x-2">
              <div className="w-2 h-2 bg-success rounded-full animate-pulse-dot"></div>
              <span className="text-sm text-success font-medium">Live</span>
            </div>
          </div>
        </div>
        <div className="p-6 space-y-4">
          {[...Array(3)].map((_, i) => (
            <div key={i} className="insight-card animate-pulse">
              <div className="w-10 h-10 bg-gray-200 rounded-full"></div>
              <div className="flex-1 space-y-2">
                <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                <div className="h-3 bg-gray-200 rounded w-full"></div>
                <div className="h-3 bg-gray-200 rounded w-1/2"></div>
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  const recentInsights = insights?.slice(0, 5) || [];

  return (
    <div className="lg:col-span-2 bg-surface rounded-xl shadow-sm border border-gray-200" data-testid="grok-insights">
      <div className="p-6 border-b border-gray-200">
        <div className="flex items-center justify-between">
          <h3 className="font-roboto font-bold text-lg text-text">Grok AI Real-Time Insights</h3>
          <div className="flex items-center space-x-2">
            <div className="w-2 h-2 bg-success rounded-full animate-pulse-dot"></div>
            <span className="text-sm text-success font-medium">Live</span>
          </div>
        </div>
      </div>
      <div className="p-6 space-y-4 custom-scrollbar max-h-96 overflow-y-auto">
        {recentInsights.length === 0 ? (
          <div className="text-center py-8">
            <div className="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center mx-auto mb-4">
              <span className="text-gray-400 text-lg font-bold">AI</span>
            </div>
            <p className="text-text-secondary">No AI insights available yet</p>
            <p className="text-sm text-text-secondary mt-1">
              Grok AI is analyzing your data to provide actionable insights
            </p>
          </div>
        ) : (
          recentInsights.map((insight) => {
            const IconComponent = iconMap[insight.source] || Lightbulb;
            const bgColor = colorMap[insight.source] || "bg-primary";
            
            return (
              <div key={insight.id} className="insight-card" data-testid={`insight-${insight.id}`}>
                <div className={`w-10 h-10 ${bgColor} rounded-full flex items-center justify-center flex-shrink-0`}>
                  <IconComponent className="text-white w-5 h-5" />
                </div>
                <div className="flex-1">
                  <div className="flex items-center justify-between mb-2">
                    <h4 className="font-medium text-text" data-testid={`insight-${insight.id}-title`}>
                      {insight.title}
                    </h4>
                    <span className="text-xs text-text-secondary bg-white px-2 py-1 rounded capitalize">
                      {insight.source.replace('_', ' ')}
                    </span>
                  </div>
                  <p className="text-sm text-text-secondary mb-2" data-testid={`insight-${insight.id}-description`}>
                    {insight.description}
                  </p>
                  <div className="flex items-center space-x-4 text-xs text-text-secondary">
                    {insight.confidence && (
                      <span>Confidence: {Math.round(Number(insight.confidence) * 100)}%</span>
                    )}
                    <span>Source: Grok AI Analysis</span>
                    <span>{insight.createdAt ? new Date(insight.createdAt).toLocaleTimeString() : 'Recently'}</span>
                  </div>
                </div>
                <div className="flex flex-col space-y-1">
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => handleApplyInsight(insight.id)}
                    className="text-primary hover:text-primary/80"
                    data-testid={`button-apply-insight-${insight.id}`}
                  >
                    <CheckCircle className="w-4 h-4" />
                  </Button>
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => handleReviewInsight(insight.id)}
                    className="text-primary hover:text-primary/80"
                    data-testid={`button-review-insight-${insight.id}`}
                  >
                    <Eye className="w-4 h-4" />
                  </Button>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
