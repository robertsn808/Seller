import { useState, useEffect } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Progress } from "@/components/ui/progress";
import { Switch } from "@/components/ui/switch";
import { PenTool, Sparkles, BarChart3, Calendar, Target, Zap, TrendingUp, Users, Brain, Wand2, Eye, Share2, Hash, Copy, Check, Play, Download, Edit3 } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { apiRequest } from "@/lib/queryClient";
import { useBusinessContext } from "@/hooks/use-business-context";

interface ContentRequest {
  type: string;
  platform?: string;
  topic: string;
  tone: string;
  targetAudience: string;
  keywords: string[];
  callToAction?: string;
  includeHashtags?: boolean;
  maxLength?: number;
}

interface GeneratedContent {
  id?: number;
  title: string;
  content: string;
  hashtags?: string[];
  wordCount?: number;
  estimatedReadTime?: number;
  seoScore?: number;
  engagementPrediction?: number;
  viralityScore?: number;
  sentimentScore?: number;
  platform?: string;
  type: string;
  status: string;
  aiGeneratedAt?: string;
}

export default function ContentCreator() {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const { currentBusiness, getBusinessContext, getTargetAudiences, getMenuItems } = useBusinessContext();
  const [activeTab, setActiveTab] = useState("generate");
  const [isGenerating, setIsGenerating] = useState(false);
  const [copiedContent, setCopiedContent] = useState<string | null>(null);
  const [keywordInput, setKeywordInput] = useState("");
  const [selectedContent, setSelectedContent] = useState<GeneratedContent | null>(null);
  
  // Content generation form state
  const [contentRequest, setContentRequest] = useState<ContentRequest>({
    type: "social_post",
    platform: "instagram",
    topic: "",
    tone: "friendly",
    targetAudience: "poke lovers",
    keywords: [],
    callToAction: "",
    includeHashtags: true,
    maxLength: 280
  });

  // Real-time content fetching for current business
  const { data: contentHistory, isLoading: historyLoading } = useQuery({
    queryKey: ["/api", currentBusiness.id, "content"],
    queryFn: () => fetch(`/api/${currentBusiness.id}/content`).then(res => res.json()),
    refetchInterval: 5000,
  });

  // Content generation mutation
  const generateContentMutation = useMutation({
    mutationFn: async (request: ContentRequest) => {
      const requestWithBusiness = { 
        ...request, 
        businessId: currentBusiness.id,
        businessContext: getBusinessContext()
      };
      const response = await apiRequest("POST", `/api/${currentBusiness.id}/content/generate`, requestWithBusiness);
      return response.json();
    },
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["/api", currentBusiness.id, "content"] });
      setSelectedContent(data);
      setActiveTab("preview");
      toast({
        title: "Content Generated Successfully!",
        description: `Created ${data.type.replace('_', ' ')} for ${data.platform || 'multiple platforms'}`,
      });
    },
    onError: (error: any) => {
      toast({
        title: "Generation Failed",
        description: error.message || "Please check your inputs and try again.",
        variant: "destructive"
      });
    }
  });

  // Content analysis mutation  
  const analyzeContentMutation = useMutation({
    mutationFn: async ({ content, platform }: { content: string; platform: string }) => {
      const response = await apiRequest("POST", "/api/content/analyze", { content, platform });
      return response.json();
    }
  });

  // Platform variations mutation
  const generateVariationsMutation = useMutation({
    mutationFn: async ({ content, platforms }: { content: string; platforms: string[] }) => {
      const response = await apiRequest("POST", "/api/content/variations", { content, platforms });
      return response.json();
    }
  });

  const handleGenerateContent = () => {
    if (!contentRequest.topic.trim()) {
      toast({
        title: "Topic Required",
        description: "Please enter a topic for your content.",
        variant: "destructive"
      });
      return;
    }
    setIsGenerating(true);
    generateContentMutation.mutate(contentRequest);
    setIsGenerating(false);
  };

  const handleCopyContent = (content: string) => {
    navigator.clipboard.writeText(content);
    setCopiedContent(content);
    setTimeout(() => setCopiedContent(null), 2000);
    toast({
      title: "Copied to Clipboard",
      description: "Content ready to paste!"
    });
  };

  const updateContentRequest = (field: keyof ContentRequest, value: any) => {
    setContentRequest(prev => ({ ...prev, [field]: value }));
  };

  const addKeyword = () => {
    if (keywordInput.trim() && !contentRequest.keywords.includes(keywordInput.trim())) {
      updateContentRequest('keywords', [...contentRequest.keywords, keywordInput.trim()]);
      setKeywordInput("");
    }
  };

  const removeKeyword = (keyword: string) => {
    updateContentRequest('keywords', contentRequest.keywords.filter(k => k !== keyword));
  };

  const latestContent = contentHistory?.[0] as GeneratedContent;

  return (
    <div className="flex-1 space-y-4 p-4 pt-6" data-testid="content-creator-page">
      <div className="flex items-center justify-between space-y-2">
        <h2 className="text-3xl font-bold tracking-tight text-text">Content Creator Studio</h2>
        <div className="flex items-center space-x-2">
          <Badge variant="secondary" className="bg-primary/10 text-primary">
            <Sparkles className="w-3 h-3 mr-1" />
            Powered by Grok AI
          </Badge>
        </div>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-4">
        <TabsList className="grid w-full grid-cols-5">
          <TabsTrigger value="generate" className="flex items-center gap-2" data-testid="tab-generate">
            <Wand2 className="w-4 h-4" />
            Generate
          </TabsTrigger>
          <TabsTrigger value="preview" className="flex items-center gap-2" data-testid="tab-preview">
            <Eye className="w-4 h-4" />
            Preview
          </TabsTrigger>
          <TabsTrigger value="calendar" className="flex items-center gap-2" data-testid="tab-calendar">
            <Calendar className="w-4 h-4" />
            Calendar
          </TabsTrigger>
          <TabsTrigger value="performance" className="flex items-center gap-2" data-testid="tab-performance">
            <BarChart3 className="w-4 h-4" />
            Analytics
          </TabsTrigger>
          <TabsTrigger value="history" className="flex items-center gap-2" data-testid="tab-history">
            <Brain className="w-4 h-4" />
            History
          </TabsTrigger>
        </TabsList>

        {/* Generate Content Tab */}
        <TabsContent value="generate" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card data-testid="content-generation-form">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <PenTool className="w-5 h-5" />
                  Content Generator
                </CardTitle>
                <CardDescription>
                  Create authentic {currentBusiness.name} content powered by Grok AI
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="content-type">Content Type</Label>
                    <Select value={contentRequest.type} onValueChange={(value) => updateContentRequest('type', value)}>
                      <SelectTrigger data-testid="select-content-type">
                        <SelectValue placeholder="Select type" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="social_post">Social Media Post</SelectItem>
                        <SelectItem value="email_campaign">Email Campaign</SelectItem>
                        <SelectItem value="blog_post">Blog Post</SelectItem>
                        <SelectItem value="product_description">Product Description</SelectItem>
                        <SelectItem value="menu_description">Menu Description</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="platform">Platform</Label>
                    <Select value={contentRequest.platform} onValueChange={(value) => updateContentRequest('platform', value)}>
                      <SelectTrigger data-testid="select-platform">
                        <SelectValue placeholder="Select platform" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="instagram">Instagram</SelectItem>
                        <SelectItem value="facebook">Facebook</SelectItem>
                        <SelectItem value="twitter">Twitter/X</SelectItem>
                        <SelectItem value="tiktok">TikTok</SelectItem>
                        <SelectItem value="email">Email</SelectItem>
                        <SelectItem value="website">Website</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="topic">Topic/Subject</Label>
                  <Input
                    id="topic"
                    placeholder={`e.g., ${getMenuItems().slice(0, 2).join(', ')}, Daily Special`}
                    value={contentRequest.topic}
                    onChange={(e) => updateContentRequest('topic', e.target.value)}
                    data-testid="input-topic"
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="tone">Tone</Label>
                    <Select value={contentRequest.tone} onValueChange={(value) => updateContentRequest('tone', value)}>
                      <SelectTrigger data-testid="select-tone">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="friendly">Friendly</SelectItem>
                        <SelectItem value="professional">Professional</SelectItem>
                        <SelectItem value="casual">Casual</SelectItem>
                        <SelectItem value="enthusiastic">Enthusiastic</SelectItem>
                        <SelectItem value="educational">Educational</SelectItem>
                        <SelectItem value="playful">Playful</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="audience">Target Audience</Label>
                    <Input
                      id="audience"
                      placeholder={`e.g., ${getTargetAudiences().slice(0, 3).join(', ')}`}
                      value={contentRequest.targetAudience}
                      onChange={(e) => updateContentRequest('targetAudience', e.target.value)}
                      data-testid="input-audience"
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="keywords">Keywords</Label>
                  <div className="flex gap-2">
                    <Input
                      placeholder="Add keyword and press Enter"
                      value={keywordInput}
                      onChange={(e) => setKeywordInput(e.target.value)}
                      onKeyPress={(e) => e.key === 'Enter' && addKeyword()}
                      data-testid="input-keyword"
                    />
                    <Button onClick={addKeyword} size="sm" data-testid="button-add-keyword">
                      Add
                    </Button>
                  </div>
                  <div className="flex flex-wrap gap-2 mt-2">
                    {contentRequest.keywords.map((keyword) => (
                      <Badge key={keyword} variant="secondary" className="cursor-pointer" onClick={() => removeKeyword(keyword)}>
                        {keyword} ×
                      </Badge>
                    ))}
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="cta">Call to Action (Optional)</Label>
                  <Input
                    id="cta"
                    placeholder="e.g., Visit us today, Order online, Book a table"
                    value={contentRequest.callToAction}
                    onChange={(e) => updateContentRequest('callToAction', e.target.value)}
                    data-testid="input-cta"
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <Switch
                      checked={contentRequest.includeHashtags}
                      onCheckedChange={(checked) => updateContentRequest('includeHashtags', checked)}
                      data-testid="switch-hashtags"
                    />
                    <Label>Include Hashtags</Label>
                  </div>
                  
                  <div className="space-y-1">
                    <Label className="text-sm">Max Length: {contentRequest.maxLength}</Label>
                    <Input
                      type="number"
                      min="50"
                      max="2000"
                      value={contentRequest.maxLength}
                      onChange={(e) => updateContentRequest('maxLength', parseInt(e.target.value))}
                      className="w-20"
                      data-testid="input-max-length"
                    />
                  </div>
                </div>

                <Button 
                  onClick={handleGenerateContent} 
                  className="w-full" 
                  disabled={generateContentMutation.isPending || !contentRequest.topic.trim()}
                  data-testid="button-generate-content"
                >
                  {generateContentMutation.isPending ? (
                    <>
                      <Sparkles className="w-4 h-4 mr-2 animate-spin" />
                      Generating with Grok AI...
                    </>
                  ) : (
                    <>
                      <Wand2 className="w-4 h-4 mr-2" />
                      Generate Content
                    </>
                  )}
                </Button>
              </CardContent>
            </Card>

            {/* Quick Actions */}
            <Card data-testid="quick-actions">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Zap className="w-5 h-5" />
                  Quick Actions
                </CardTitle>
                <CardDescription>
                  Pre-configured content templates for Allii Fish Market
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-3">
                <Button 
                  variant="outline" 
                  className="w-full justify-start"
                  onClick={() => {
                    setContentRequest({
                      ...contentRequest,
                      type: "social_post",
                      platform: "instagram",
                      topic: "Daily Fresh Catch Special",
                      tone: "enthusiastic",
                      targetAudience: "seafood lovers",
                      keywords: ["fresh", "daily", "catch", "special", "Hawaiian"]
                    });
                  }}
                  data-testid="button-daily-special"
                >
                  <Target className="w-4 h-4 mr-2" />
                  Daily Special Post
                </Button>
                
                <Button 
                  variant="outline" 
                  className="w-full justify-start"
                  onClick={() => {
                    setContentRequest({
                      ...contentRequest,
                      type: "menu_description",
                      platform: "website",
                      topic: "Signature Poke Bowl Collection",
                      tone: "professional",
                      targetAudience: "food enthusiasts",
                      keywords: ["signature", "poke", "authentic", "Hawaiian", "fresh"]
                    });
                  }}
                  data-testid="button-menu-description"
                >
                  <Users className="w-4 h-4 mr-2" />
                  Menu Description
                </Button>
                
                <Button 
                  variant="outline" 
                  className="w-full justify-start"
                  onClick={() => {
                    setContentRequest({
                      ...contentRequest,
                      type: "email_campaign",
                      platform: "email",
                      topic: "Weekend Special Promotion",
                      tone: "friendly",
                      targetAudience: "loyal customers",
                      keywords: ["weekend", "special", "promotion", "loyalty", "savings"]
                    });
                  }}
                  data-testid="button-email-campaign"
                >
                  <TrendingUp className="w-4 h-4 mr-2" />
                  Email Campaign
                </Button>

                <Separator />
                
                <div className="space-y-2">
                  <h4 className="font-medium text-sm">Trending Topics</h4>
                  <div className="grid grid-cols-2 gap-2 text-xs">
                    <Badge variant="outline" className="justify-center">Sustainable Fishing</Badge>
                    <Badge variant="outline" className="justify-center">Aloha Friday</Badge>
                    <Badge variant="outline" className="justify-center">Local Ingredients</Badge>
                    <Badge variant="outline" className="justify-center">Hawaiian Culture</Badge>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        {/* Preview Tab */}
        <TabsContent value="preview" className="space-y-4">
          {selectedContent ? (
            <div className="grid gap-4 md:grid-cols-2">
              <Card data-testid="content-preview">
                <CardHeader>
                  <CardTitle className="flex items-center justify-between">
                    <span>Generated Content</span>
                    <Badge variant="secondary">{selectedContent.platform}</Badge>
                  </CardTitle>
                  <CardDescription>{selectedContent.type.replace('_', ' ')}</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="space-y-2">
                    <Label className="font-medium">Title</Label>
                    <p className="p-3 bg-surface rounded-lg border">{selectedContent.title}</p>
                  </div>
                  
                  <div className="space-y-2">
                    <Label className="font-medium">Content</Label>
                    <div className="p-3 bg-surface rounded-lg border relative">
                      <p className="whitespace-pre-wrap">{selectedContent.content}</p>
                      <Button
                        size="sm"
                        variant="ghost"
                        className="absolute top-2 right-2"
                        onClick={() => handleCopyContent(selectedContent.content)}
                        data-testid="button-copy-content"
                      >
                        {copiedContent === selectedContent.content ? (
                          <Check className="w-4 h-4" />
                        ) : (
                          <Copy className="w-4 h-4" />
                        )}
                      </Button>
                    </div>
                  </div>

                  {selectedContent.hashtags && selectedContent.hashtags.length > 0 && (
                    <div className="space-y-2">
                      <Label className="font-medium flex items-center gap-2">
                        <Hash className="w-4 h-4" />
                        Hashtags
                      </Label>
                      <div className="flex flex-wrap gap-2">
                        {selectedContent.hashtags.map((tag) => (
                          <Badge key={tag} variant="outline" className="cursor-pointer" onClick={() => handleCopyContent(`#${tag}`)}>
                            #{tag}
                          </Badge>
                        ))}
                      </div>
                    </div>
                  )}

                  <div className="flex gap-2">
                    <Button size="sm" data-testid="button-edit-content">
                      <Edit3 className="w-4 h-4 mr-2" />
                      Edit
                    </Button>
                    <Button size="sm" variant="outline" data-testid="button-publish">
                      <Play className="w-4 h-4 mr-2" />
                      Publish
                    </Button>
                    <Button size="sm" variant="outline" data-testid="button-export">
                      <Download className="w-4 h-4 mr-2" />
                      Export
                    </Button>
                  </div>
                </CardContent>
              </Card>

              <Card data-testid="content-analytics">
                <CardHeader>
                  <CardTitle>Performance Prediction</CardTitle>
                  <CardDescription>AI-powered engagement forecasting</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="space-y-3">
                    <div className="space-y-2">
                      <div className="flex justify-between text-sm">
                        <span>Engagement Score</span>
                        <span>{selectedContent.seoScore || 75}%</span>
                      </div>
                      <Progress value={selectedContent.seoScore || 75} className="h-2" />
                    </div>
                    
                    <div className="space-y-2">
                      <div className="flex justify-between text-sm">
                        <span>Virality Potential</span>
                        <span>68%</span>
                      </div>
                      <Progress value={68} className="h-2" />
                    </div>
                    
                    <div className="space-y-2">
                      <div className="flex justify-between text-sm">
                        <span>SEO Score</span>
                        <span>{selectedContent.seoScore || 82}%</span>
                      </div>
                      <Progress value={selectedContent.seoScore || 82} className="h-2" />
                    </div>
                  </div>

                  <Separator />

                  <div className="grid grid-cols-2 gap-4 text-center">
                    <div>
                      <p className="text-2xl font-bold text-primary">{selectedContent.wordCount || 0}</p>
                      <p className="text-xs text-muted-foreground">Words</p>
                    </div>
                    <div>
                      <p className="text-2xl font-bold text-primary">{selectedContent.estimatedReadTime || 1}</p>
                      <p className="text-xs text-muted-foreground">Min Read</p>
                    </div>
                  </div>

                  <Button 
                    variant="outline" 
                    className="w-full"
                    onClick={() => {
                      if (selectedContent.content) {
                        analyzeContentMutation.mutate({
                          content: selectedContent.content,
                          platform: selectedContent.platform || 'instagram'
                        });
                      }
                    }}
                    disabled={analyzeContentMutation.isPending}
                    data-testid="button-analyze-performance"
                  >
                    {analyzeContentMutation.isPending ? (
                      <>
                        <BarChart3 className="w-4 h-4 mr-2 animate-pulse" />
                        Analyzing...
                      </>
                    ) : (
                      <>
                        <BarChart3 className="w-4 h-4 mr-2" />
                        Deep Analysis
                      </>
                    )}
                  </Button>
                </CardContent>
              </Card>
            </div>
          ) : (
            <Card>
              <CardContent className="flex flex-col items-center justify-center py-12">
                <Eye className="w-12 h-12 text-muted-foreground mb-4" />
                <h3 className="text-lg font-medium mb-2">No Content Selected</h3>
                <p className="text-muted-foreground text-center mb-4">
                  Generate content from the Generate tab to see it here.
                </p>
                <Button onClick={() => setActiveTab("generate")} data-testid="button-go-generate">
                  <Wand2 className="w-4 h-4 mr-2" />
                  Generate Content
                </Button>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        {/* Content History Tab */}
        <TabsContent value="history" className="space-y-4">
          <Card data-testid="content-history">
            <CardHeader>
              <CardTitle>Content History</CardTitle>
              <CardDescription>Your recently generated content</CardDescription>
            </CardHeader>
            <CardContent>
              {historyLoading ? (
                <div className="space-y-3">
                  {[1, 2, 3].map((i) => (
                    <div key={i} className="animate-pulse">
                      <div className="h-4 bg-muted rounded w-3/4 mb-2"></div>
                      <div className="h-3 bg-muted rounded w-1/2"></div>
                    </div>
                  ))}
                </div>
              ) : contentHistory && contentHistory.length > 0 ? (
                <div className="space-y-4">
                  {contentHistory.map((content: GeneratedContent) => (
                    <Card key={content.id} className="cursor-pointer hover:bg-surface/50 transition-colors" onClick={() => setSelectedContent(content)} data-testid={`content-item-${content.id}`}>
                      <CardContent className="p-4">
                        <div className="flex items-start justify-between">
                          <div className="space-y-1 flex-1">
                            <h4 className="font-medium text-sm line-clamp-1">{content.title}</h4>
                            <p className="text-sm text-muted-foreground line-clamp-2">{content.content}</p>
                            <div className="flex items-center gap-2 text-xs text-muted-foreground">
                              <Badge variant="outline" className="text-xs">{content.type.replace('_', ' ')}</Badge>
                              <Badge variant="outline" className="text-xs">{content.platform}</Badge>
                              <span>{new Date(content.aiGeneratedAt || '').toLocaleDateString()}</span>
                            </div>
                          </div>
                          <Button size="sm" variant="ghost" onClick={(e) => {
                            e.stopPropagation();
                            setSelectedContent(content);
                            setActiveTab("preview");
                          }}>
                            <Eye className="w-4 h-4" />
                          </Button>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>
              ) : (
                <div className="text-center py-8">
                  <Brain className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                  <h3 className="text-lg font-medium mb-2">No Content Yet</h3>
                  <p className="text-muted-foreground mb-4">
                    Start generating content to build your history.
                  </p>
                  <Button onClick={() => setActiveTab("generate")} data-testid="button-start-generating">
                    <PenTool className="w-4 h-4 mr-2" />
                    Start Creating
                  </Button>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* Calendar Placeholder */}
        <TabsContent value="calendar">
          <Card>
            <CardHeader>
              <CardTitle>Content Calendar</CardTitle>
              <CardDescription>Schedule and manage your content publishing</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="text-center py-8">
                <Calendar className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-medium mb-2">Calendar Coming Soon</h3>
                <p className="text-muted-foreground">
                  Advanced content scheduling and calendar management features will be available soon.
                </p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Performance Placeholder */}
        <TabsContent value="performance">
          <Card>
            <CardHeader>
              <CardTitle>Performance Analytics</CardTitle>
              <CardDescription>Track engagement and optimize your content strategy</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="text-center py-8">
                <BarChart3 className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-medium mb-2">Analytics Coming Soon</h3>
                <p className="text-muted-foreground">
                  Comprehensive performance tracking and analytics dashboard will be available soon.
                </p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}