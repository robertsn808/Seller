import { useState } from "react";
import { useQuery, useMutation } from "@tanstack/react-query";
import TopBar from "@/components/layout/topbar";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Badge } from "@/components/ui/badge";
import { useToast } from "@/hooks/use-toast";
import { apiRequest } from "@/lib/queryClient";
import { 
  TrendingUp, 
  AlertTriangle, 
  Heart, 
  DollarSign, 
  ChefHat, 
  BarChart3,
  Lightbulb,
  Loader2,
  Brain
} from "lucide-react";
import type { AiInsight } from "@shared/schema";

export default function GrokAI() {
  const { toast } = useToast();
  const [trendKeywords, setTrendKeywords] = useState("");
  const [competitors, setCompetitors] = useState("");
  const [reviews, setReviews] = useState("");

  const { data: insights } = useQuery<AiInsight[]>({
    queryKey: ["/api/ai/insights"],
    refetchInterval: 60000,
  });

  const trendsMutation = useMutation({
    mutationFn: async (keywords: string[]) => {
      const response = await apiRequest("POST", "/api/ai/analyze-trends", { keywords });
      return response.json();
    },
    onSuccess: (data) => {
      toast({
        title: "Trends Analysis Complete",
        description: `Found ${data.length} trending insights`,
      });
    },
    onError: (error: any) => {
      toast({
        title: "Analysis Failed",
        description: error.message,
        variant: "destructive",
      });
    },
  });

  const competitorMutation = useMutation({
    mutationFn: async (competitorList: string[]) => {
      const response = await apiRequest("POST", "/api/ai/analyze-competitors", { competitors: competitorList });
      return response.json();
    },
    onSuccess: (data) => {
      toast({
        title: "Competitor Analysis Complete",
        description: `Analyzed ${data.length} competitor insights`,
      });
    },
  });

  const sentimentMutation = useMutation({
    mutationFn: async (reviewsList: string[]) => {
      const response = await apiRequest("POST", "/api/ai/analyze-sentiment", { reviews: reviewsList });
      return response.json();
    },
    onSuccess: (data) => {
      toast({
        title: "Sentiment Analysis Complete",
        description: `Analyzed sentiment from ${data.length} topics`,
      });
    },
  });

  const handleTrendsAnalysis = () => {
    const keywords = trendKeywords.split(",").map(k => k.trim()).filter(k => k);
    if (keywords.length === 0) {
      toast({
        title: "Error",
        description: "Please enter at least one keyword",
        variant: "destructive",
      });
      return;
    }
    trendsMutation.mutate(keywords);
  };

  const handleCompetitorAnalysis = () => {
    const competitorList = competitors.split(",").map(c => c.trim()).filter(c => c);
    if (competitorList.length === 0) {
      toast({
        title: "Error",
        description: "Please enter at least one competitor",
        variant: "destructive",
      });
      return;
    }
    competitorMutation.mutate(competitorList);
  };

  const handleSentimentAnalysis = () => {
    const reviewsList = reviews.split("\n").map(r => r.trim()).filter(r => r);
    if (reviewsList.length === 0) {
      toast({
        title: "Error",
        description: "Please enter at least one review",
        variant: "destructive",
      });
      return;
    }
    sentimentMutation.mutate(reviewsList);
  };

  const getInsightIcon = (source: string) => {
    switch (source) {
      case 'social_trend': return TrendingUp;
      case 'pricing': return DollarSign;
      case 'sentiment': return Heart;
      case 'menu_suggestion': return ChefHat;
      default: return Lightbulb;
    }
  };

  const getInsightColor = (source: string) => {
    switch (source) {
      case 'social_trend': return 'bg-primary text-white';
      case 'pricing': return 'bg-warning text-white';
      case 'sentiment': return 'bg-success text-white';
      case 'menu_suggestion': return 'bg-accent text-white';
      default: return 'bg-gray-500 text-white';
    }
  };

  return (
    <>
      <TopBar
        title="Grok AI Insights"
        subtitle="Advanced AI analysis for strategic decision making"
        notificationCount={3}
      />
      
      <div className="flex-1 overflow-auto p-6 bg-background">
        <Tabs defaultValue="dashboard" className="space-y-6">
          <TabsList className="grid w-full grid-cols-5">
            <TabsTrigger value="dashboard" data-testid="tab-dashboard">Dashboard</TabsTrigger>
            <TabsTrigger value="trends" data-testid="tab-trends">Social Trends</TabsTrigger>
            <TabsTrigger value="competitors" data-testid="tab-competitors">Competitors</TabsTrigger>
            <TabsTrigger value="sentiment" data-testid="tab-sentiment">Sentiment</TabsTrigger>
            <TabsTrigger value="insights" data-testid="tab-insights">All Insights</TabsTrigger>
          </TabsList>

          <TabsContent value="dashboard" className="space-y-6">
            {/* AI Analysis Overview */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              <Card>
                <CardContent className="p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-text-secondary text-sm font-medium">Social Trends</p>
                      <p className="text-2xl font-bold text-text mt-1">12</p>
                      <p className="text-primary text-sm mt-1">
                        <TrendingUp className="w-4 h-4 inline mr-1" />
                        Active trends
                      </p>
                    </div>
                    <div className="w-12 h-12 bg-primary/10 rounded-lg flex items-center justify-center">
                      <TrendingUp className="text-primary text-xl" />
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-text-secondary text-sm font-medium">Competitor Alerts</p>
                      <p className="text-2xl font-bold text-text mt-1">3</p>
                      <p className="text-warning text-sm mt-1">
                        <AlertTriangle className="w-4 h-4 inline mr-1" />
                        Requiring attention
                      </p>
                    </div>
                    <div className="w-12 h-12 bg-warning/10 rounded-lg flex items-center justify-center">
                      <AlertTriangle className="text-warning text-xl" />
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-text-secondary text-sm font-medium">Sentiment Score</p>
                      <p className="text-2xl font-bold text-text mt-1">87%</p>
                      <p className="text-success text-sm mt-1">
                        <Heart className="w-4 h-4 inline mr-1" />
                        Positive sentiment
                      </p>
                    </div>
                    <div className="w-12 h-12 bg-success/10 rounded-lg flex items-center justify-center">
                      <Heart className="text-success text-xl" />
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-text-secondary text-sm font-medium">AI Recommendations</p>
                      <p className="text-2xl font-bold text-text mt-1">{insights?.length || 0}</p>
                      <p className="text-primary text-sm mt-1">
                        <Brain className="w-4 h-4 inline mr-1" />
                        Active insights
                      </p>
                    </div>
                    <div className="w-12 h-12 bg-primary/10 rounded-lg flex items-center justify-center">
                      <Brain className="text-primary text-xl" />
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* Recent AI Insights */}
            <Card>
              <CardHeader>
                <CardTitle className="font-roboto">Recent AI Insights</CardTitle>
              </CardHeader>
              <CardContent>
                {insights && insights.length > 0 ? (
                  <div className="space-y-4">
                    {insights.slice(0, 5).map((insight) => {
                      const IconComponent = getInsightIcon(insight.source);
                      const colorClass = getInsightColor(insight.source);
                      
                      return (
                        <div key={insight.id} className="flex items-start space-x-4 p-4 bg-gray-50 rounded-lg">
                          <div className={`w-10 h-10 ${colorClass} rounded-full flex items-center justify-center`}>
                            <IconComponent className="w-5 h-5" />
                          </div>
                          <div className="flex-1">
                            <div className="flex items-center justify-between mb-2">
                              <h4 className="font-medium text-text">{insight.title}</h4>
                              <Badge variant="secondary">{insight.source.replace('_', ' ')}</Badge>
                            </div>
                            <p className="text-sm text-text-secondary mb-2">{insight.description}</p>
                            <div className="flex items-center space-x-4 text-xs text-text-secondary">
                              {insight.confidence && (
                                <span>Confidence: {Math.round(Number(insight.confidence) * 100)}%</span>
                              )}
                              <span>{new Date(insight.createdAt).toLocaleString()}</span>
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                ) : (
                  <div className="text-center py-8">
                    <Brain className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                    <p className="text-text-secondary">No AI insights available yet</p>
                    <p className="text-sm text-text-secondary mt-1">
                      Use the analysis tools to generate insights
                    </p>
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="trends" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle className="font-roboto">Social Media Trends Analysis</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <Label htmlFor="trends-keywords">Keywords (comma-separated)</Label>
                  <Input
                    id="trends-keywords"
                    value={trendKeywords}
                    onChange={(e) => setTrendKeywords(e.target.value)}
                    placeholder="poke, ahi tuna, spicy mayo, hawaii food"
                    data-testid="input-trend-keywords"
                  />
                </div>
                <Button 
                  onClick={handleTrendsAnalysis}
                  disabled={trendsMutation.isPending}
                  data-testid="button-analyze-trends"
                >
                  {trendsMutation.isPending ? (
                    <>
                      <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                      Analyzing...
                    </>
                  ) : (
                    <>
                      <TrendingUp className="w-4 h-4 mr-2" />
                      Analyze Trends
                    </>
                  )}
                </Button>

                {trendsMutation.data && (
                  <div className="mt-6 space-y-4">
                    <h4 className="font-medium text-text">Trend Analysis Results:</h4>
                    {trendsMutation.data.map((trend: any, index: number) => (
                      <div key={index} className="p-4 bg-primary/5 rounded-lg border border-primary/20">
                        <div className="flex items-center justify-between mb-2">
                          <h5 className="font-medium text-text">{trend.ingredient}</h5>
                          <Badge className="bg-primary text-white">
                            +{trend.trendPercentage}%
                          </Badge>
                        </div>
                        <p className="text-sm text-text-secondary mb-2">{trend.recommendation}</p>
                        <div className="flex items-center space-x-4 text-xs text-text-secondary">
                          <span>Platform: {trend.platform}</span>
                          <span>Confidence: {Math.round(trend.confidence * 100)}%</span>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="competitors" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle className="font-roboto">Competitor Analysis</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <Label htmlFor="competitors">Competitor Names (comma-separated)</Label>
                  <Input
                    id="competitors"
                    value={competitors}
                    onChange={(e) => setCompetitors(e.target.value)}
                    placeholder="Poke Palace, Ocean Fresh, Island Bowl"
                    data-testid="input-competitors"
                  />
                </div>
                <Button 
                  onClick={handleCompetitorAnalysis}
                  disabled={competitorMutation.isPending}
                  data-testid="button-analyze-competitors"
                >
                  {competitorMutation.isPending ? (
                    <>
                      <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                      Analyzing...
                    </>
                  ) : (
                    <>
                      <BarChart3 className="w-4 h-4 mr-2" />
                      Analyze Competitors
                    </>
                  )}
                </Button>

                {competitorMutation.data && (
                  <div className="mt-6 space-y-4">
                    <h4 className="font-medium text-text">Competitor Analysis Results:</h4>
                    {competitorMutation.data.map((insight: any, index: number) => (
                      <div key={index} className="p-4 bg-warning/5 rounded-lg border border-warning/20">
                        <div className="flex items-center justify-between mb-2">
                          <h5 className="font-medium text-text">{insight.competitor}</h5>
                          <Badge variant={insight.impact === 'high' ? 'destructive' : insight.impact === 'medium' ? 'default' : 'secondary'}>
                            {insight.impact} impact
                          </Badge>
                        </div>
                        <p className="text-sm text-text-secondary mb-2">{insight.action}</p>
                        <p className="text-sm font-medium text-warning">{insight.recommendation}</p>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="sentiment" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle className="font-roboto">Sentiment Analysis</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <Label htmlFor="reviews">Customer Reviews (one per line)</Label>
                  <Textarea
                    id="reviews"
                    value={reviews}
                    onChange={(e) => setReviews(e.target.value)}
                    placeholder="Great poke bowl! Fresh fish and excellent service.&#10;The spicy mayo was amazing, will definitely come back.&#10;Prices are a bit high but quality is worth it."
                    rows={6}
                    data-testid="textarea-reviews"
                  />
                </div>
                <Button 
                  onClick={handleSentimentAnalysis}
                  disabled={sentimentMutation.isPending}
                  data-testid="button-analyze-sentiment"
                >
                  {sentimentMutation.isPending ? (
                    <>
                      <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                      Analyzing...
                    </>
                  ) : (
                    <>
                      <Heart className="w-4 h-4 mr-2" />
                      Analyze Sentiment
                    </>
                  )}
                </Button>

                {sentimentMutation.data && (
                  <div className="mt-6 space-y-4">
                    <h4 className="font-medium text-text">Sentiment Analysis Results:</h4>
                    {sentimentMutation.data.map((sentiment: any, index: number) => (
                      <div key={index} className="p-4 bg-success/5 rounded-lg border border-success/20">
                        <div className="flex items-center justify-between mb-2">
                          <h5 className="font-medium text-text">{sentiment.topic}</h5>
                          <Badge className={
                            sentiment.sentiment === 'positive' ? 'bg-success text-white' :
                            sentiment.sentiment === 'negative' ? 'bg-error text-white' :
                            'bg-gray-500 text-white'
                          }>
                            {sentiment.sentiment}
                          </Badge>
                        </div>
                        <p className="text-sm text-text-secondary mb-2">{sentiment.recommendation}</p>
                        <div className="flex items-center space-x-4 text-xs text-text-secondary">
                          <span>Mentions: {sentiment.mentions}</span>
                          <span>Confidence: {Math.round(sentiment.confidence * 100)}%</span>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="insights" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle className="font-roboto">All AI Insights</CardTitle>
              </CardHeader>
              <CardContent>
                {insights && insights.length > 0 ? (
                  <div className="space-y-4">
                    {insights.map((insight) => {
                      const IconComponent = getInsightIcon(insight.source);
                      const colorClass = getInsightColor(insight.source);
                      
                      return (
                        <div key={insight.id} className="flex items-start space-x-4 p-4 bg-gray-50 rounded-lg">
                          <div className={`w-10 h-10 ${colorClass} rounded-full flex items-center justify-center`}>
                            <IconComponent className="w-5 h-5" />
                          </div>
                          <div className="flex-1">
                            <div className="flex items-center justify-between mb-2">
                              <h4 className="font-medium text-text">{insight.title}</h4>
                              <div className="flex items-center space-x-2">
                                <Badge variant="secondary">{insight.source.replace('_', ' ')}</Badge>
                                {insight.applied && <Badge className="bg-success text-white">Applied</Badge>}
                              </div>
                            </div>
                            <p className="text-sm text-text-secondary mb-2">{insight.description}</p>
                            <div className="flex items-center space-x-4 text-xs text-text-secondary">
                              {insight.confidence && (
                                <span>Confidence: {Math.round(Number(insight.confidence) * 100)}%</span>
                              )}
                              <span>{new Date(insight.createdAt).toLocaleString()}</span>
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                ) : (
                  <div className="text-center py-8">
                    <Brain className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                    <p className="text-text-secondary">No AI insights available</p>
                    <p className="text-sm text-text-secondary mt-1">
                      Start analyzing data to generate insights
                    </p>
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </>
  );
}
