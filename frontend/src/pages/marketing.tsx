import { useState } from "react";
import { useQuery, useMutation } from "@tanstack/react-query";
import TopBar from "@/components/layout/topbar";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useToast } from "@/hooks/use-toast";
import { apiRequest, queryClient } from "@/lib/queryClient";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
// Campaign schema not implemented yet - using basic validation
// Campaign type defined locally since schema not implemented yet
interface Campaign {
  id: number;
  name: string;
  platform: string;
  objective: string;
  budget: string;
  startDate: string;
  endDate?: string;
  status: string;
}
import { 
  Megaphone, 
  Plus, 
  Search, 
  DollarSign, 
  Target,
  TrendingUp,
  Eye,
  Edit,
  Play,
  Pause,
  BarChart3,
  Users,
  MousePointer,
  Heart,
  Share2,
  Calendar,
  Globe
} from "lucide-react";
import { z } from "zod";

const campaignFormSchema = z.object({
  name: z.string().min(1, "Campaign name is required"),
  platform: z.string(),
  objective: z.string(),
  budget: z.string().refine((val) => !isNaN(Number(val)) && Number(val) > 0, "Valid budget required"),
  startDate: z.string().min(1, "Start date is required"),
  endDate: z.string().optional(),
  status: z.string().default("active"),
});

export default function Marketing() {
  const { toast } = useToast();
  const [searchTerm, setSearchTerm] = useState("");
  const [platformFilter, setPlatformFilter] = useState("all");
  const [statusFilter, setStatusFilter] = useState("all");
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingCampaign, setEditingCampaign] = useState<Campaign | null>(null);

  const form = useForm<z.infer<typeof campaignFormSchema>>({
    resolver: zodResolver(campaignFormSchema),
    defaultValues: {
      name: "",
      platform: "facebook",
      objective: "",
      budget: "",
      startDate: "",
      endDate: "",
      status: "active",
    },
  });

  const { data: campaigns, isLoading } = useQuery<Campaign[]>({
    queryKey: ["/api/campaigns"],
    refetchInterval: 30000,
  });

  const createMutation = useMutation({
    mutationFn: async (data: any) => {
      const campaignData = {
        ...data,
        budget: Number(data.budget),
        startDate: new Date(data.startDate).toISOString(),
        endDate: data.endDate ? new Date(data.endDate).toISOString() : null,
      };
      const response = await apiRequest("POST", "/api/campaigns", campaignData);
      return response.json();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["/api/campaigns"] });
      setIsDialogOpen(false);
      form.reset();
      toast({
        title: "Campaign Created",
        description: "New marketing campaign has been created successfully",
      });
    },
    onError: (error: any) => {
      toast({
        title: "Error",
        description: error.message,
        variant: "destructive",
      });
    },
  });

  const updateMutation = useMutation({
    mutationFn: async ({ id, data }: { id: number; data: any }) => {
      const campaignData = {
        ...data,
        budget: Number(data.budget),
        startDate: new Date(data.startDate).toISOString(),
        endDate: data.endDate ? new Date(data.endDate).toISOString() : null,
      };
      const response = await apiRequest("PATCH", `/api/campaigns/${id}`, campaignData);
      return response.json();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["/api/campaigns"] });
      setIsDialogOpen(false);
      setEditingCampaign(null);
      form.reset();
      toast({
        title: "Campaign Updated",
        description: "Campaign has been updated successfully",
      });
    },
    onError: (error: any) => {
      toast({
        title: "Error",
        description: error.message,
        variant: "destructive",
      });
    },
  });

  const onSubmit = async (data: z.infer<typeof campaignFormSchema>) => {
    if (editingCampaign) {
      updateMutation.mutate({ id: editingCampaign.id, data });
    } else {
      createMutation.mutate(data);
    }
  };

  const handleEdit = (campaign: Campaign) => {
    setEditingCampaign(campaign);
    form.reset({
      name: campaign.name,
      platform: campaign.platform,
      objective: campaign.objective || "",
      budget: campaign.budget.toString(),
      startDate: new Date(campaign.startDate).toISOString().split('T')[0],
      endDate: campaign.endDate ? new Date(campaign.endDate).toISOString().split('T')[0] : "",
      status: campaign.status,
    });
    setIsDialogOpen(true);
  };

  const handleAdd = () => {
    setEditingCampaign(null);
    form.reset();
    setIsDialogOpen(true);
  };

  const filteredCampaigns = campaigns?.filter(campaign => {
    const matchesSearch = campaign.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      campaign.objective?.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesPlatform = platformFilter === "all" || campaign.platform === platformFilter;
    const matchesStatus = statusFilter === "all" || campaign.status === statusFilter;
    
    return matchesSearch && matchesPlatform && matchesStatus;
  }) || [];

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'active': return 'bg-success text-white';
      case 'paused': return 'bg-warning text-white';
      case 'completed': return 'bg-primary text-white';
      case 'draft': return 'bg-gray-500 text-white';
      default: return 'bg-gray-500 text-white';
    }
  };

  const getPlatformIcon = (platform: string) => {
    switch (platform) {
      case 'facebook': return '📘';
      case 'instagram': return '📷';
      case 'tiktok': return '🎵';
      case 'google': return '🔍';
      default: return '🌐';
    }
  };

  const getPlatformColor = (platform: string) => {
    switch (platform) {
      case 'facebook': return 'bg-blue-600 text-white';
      case 'instagram': return 'bg-pink-500 text-white';
      case 'tiktok': return 'bg-black text-white';
      case 'google': return 'bg-red-500 text-white';
      default: return 'bg-gray-500 text-white';
    }
  };

  // Calculate stats
  const activeCampaigns = campaigns?.filter(c => c.status === 'active') || [];
  const totalBudget = campaigns?.reduce((sum, c) => sum + Number(c.budget), 0) || 0;
  const totalSpent = campaigns?.reduce((sum, c) => sum + Number(c.spent || 0), 0) || 0;
  const totalReach = campaigns?.reduce((sum, c) => sum + (c.reach || 0), 0) || 0;
  const totalConversions = campaigns?.reduce((sum, c) => sum + (c.conversions || 0), 0) || 0;

  return (
    <>
      <TopBar
        title="Marketing Campaigns"
        subtitle="Manage multi-platform marketing campaigns and track performance"
        notificationCount={3}
      />
      
      <div className="flex-1 overflow-auto p-6 bg-background">
        <Tabs defaultValue="overview" className="space-y-6">
          <TabsList className="grid w-full grid-cols-4">
            <TabsTrigger value="overview" data-testid="tab-overview">Overview</TabsTrigger>
            <TabsTrigger value="campaigns" data-testid="tab-campaigns">Campaigns</TabsTrigger>
            <TabsTrigger value="analytics" data-testid="tab-analytics">Analytics</TabsTrigger>
            <TabsTrigger value="ai-optimization" data-testid="tab-ai">AI Optimization</TabsTrigger>
          </TabsList>

          <TabsContent value="overview" className="space-y-6">
            {/* Performance Overview */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
              <Card>
                <CardContent className="p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-text-secondary text-sm font-medium">Active Campaigns</p>
                      <p className="text-2xl font-bold text-text mt-1">{activeCampaigns.length}</p>
                    </div>
                    <Megaphone className="w-8 h-8 text-primary" />
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-text-secondary text-sm font-medium">Total Budget</p>
                      <p className="text-2xl font-bold text-text mt-1">${totalBudget.toLocaleString()}</p>
                    </div>
                    <DollarSign className="w-8 h-8 text-success" />
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-text-secondary text-sm font-medium">Total Reach</p>
                      <p className="text-2xl font-bold text-text mt-1">{totalReach.toLocaleString()}</p>
                    </div>
                    <Users className="w-8 h-8 text-accent" />
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-text-secondary text-sm font-medium">Conversions</p>
                      <p className="text-2xl font-bold text-text mt-1">{totalConversions}</p>
                    </div>
                    <Target className="w-8 h-8 text-primary" />
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* Platform Performance */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <Card>
                <CardHeader>
                  <CardTitle className="font-roboto">Platform Performance</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {['facebook', 'instagram', 'tiktok', 'google'].map((platform) => {
                      const platformCampaigns = campaigns?.filter(c => c.platform === platform) || [];
                      const platformSpent = platformCampaigns.reduce((sum, c) => sum + Number(c.spent || 0), 0);
                      const platformBudget = platformCampaigns.reduce((sum, c) => sum + Number(c.budget), 0);
                      const platformProgress = platformBudget > 0 ? (platformSpent / platformBudget) * 100 : 0;
                      
                      return (
                        <div key={platform} className="space-y-2">
                          <div className="flex items-center justify-between">
                            <div className="flex items-center space-x-2">
                              <span className="text-lg">{getPlatformIcon(platform)}</span>
                              <span className="font-medium capitalize">{platform}</span>
                              <Badge className={getPlatformColor(platform)}>
                                {platformCampaigns.length} campaigns
                              </Badge>
                            </div>
                            <span className="text-sm font-medium">
                              ${platformSpent.toLocaleString()} / ${platformBudget.toLocaleString()}
                            </span>
                          </div>
                          <Progress value={platformProgress} className="h-2" />
                        </div>
                      );
                    })}
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle className="font-roboto">ROI by Platform</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {['facebook', 'instagram', 'tiktok', 'google'].map((platform) => {
                      const platformCampaigns = campaigns?.filter(c => c.platform === platform) || [];
                      const platformSpent = platformCampaigns.reduce((sum, c) => sum + Number(c.spent || 0), 0);
                      const platformConversions = platformCampaigns.reduce((sum, c) => sum + (c.conversions || 0), 0);
                      const roi = platformSpent > 0 ? ((platformConversions * 25 - platformSpent) / platformSpent * 100) : 0; // Assuming $25 avg order
                      
                      return (
                        <div key={platform} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                          <div className="flex items-center space-x-3">
                            <span className="text-lg">{getPlatformIcon(platform)}</span>
                            <div>
                              <p className="font-medium capitalize">{platform}</p>
                              <p className="text-sm text-text-secondary">
                                {platformConversions} conversions
                              </p>
                            </div>
                          </div>
                          <div className="text-right">
                            <p className={`font-bold ${roi >= 0 ? 'text-success' : 'text-error'}`}>
                              {roi >= 0 ? '+' : ''}{roi.toFixed(1)}%
                            </p>
                            <p className="text-sm text-text-secondary">ROI</p>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </CardContent>
              </Card>
            </div>
          </TabsContent>

          <TabsContent value="campaigns" className="space-y-6">
            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle className="font-roboto">Campaign Management</CardTitle>
                  <div className="flex items-center space-x-4">
                    <div className="relative">
                      <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                      <Input
                        placeholder="Search campaigns..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="pl-10"
                        data-testid="input-search-campaigns"
                      />
                    </div>
                    <Select value={platformFilter} onValueChange={setPlatformFilter}>
                      <SelectTrigger className="w-32">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="all">All Platforms</SelectItem>
                        <SelectItem value="facebook">Facebook</SelectItem>
                        <SelectItem value="instagram">Instagram</SelectItem>
                        <SelectItem value="tiktok">TikTok</SelectItem>
                        <SelectItem value="google">Google</SelectItem>
                      </SelectContent>
                    </Select>
                    <Select value={statusFilter} onValueChange={setStatusFilter}>
                      <SelectTrigger className="w-32">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="all">All Status</SelectItem>
                        <SelectItem value="active">Active</SelectItem>
                        <SelectItem value="paused">Paused</SelectItem>
                        <SelectItem value="completed">Completed</SelectItem>
                        <SelectItem value="draft">Draft</SelectItem>
                      </SelectContent>
                    </Select>
                    <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
                      <DialogTrigger asChild>
                        <Button onClick={handleAdd} data-testid="button-add-campaign">
                          <Plus className="w-4 h-4 mr-2" />
                          New Campaign
                        </Button>
                      </DialogTrigger>
                      <DialogContent className="sm:max-w-[500px]">
                        <DialogHeader>
                          <DialogTitle>
                            {editingCampaign ? "Edit Campaign" : "Create New Campaign"}
                          </DialogTitle>
                        </DialogHeader>
                        <Form {...form}>
                          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                            <FormField
                              control={form.control}
                              name="name"
                              render={({ field }) => (
                                <FormItem>
                                  <FormLabel>Campaign Name</FormLabel>
                                  <FormControl>
                                    <Input {...field} data-testid="input-campaign-name" />
                                  </FormControl>
                                  <FormMessage />
                                </FormItem>
                              )}
                            />
                            <div className="grid grid-cols-2 gap-4">
                              <FormField
                                control={form.control}
                                name="platform"
                                render={({ field }) => (
                                  <FormItem>
                                    <FormLabel>Platform</FormLabel>
                                    <Select onValueChange={field.onChange} defaultValue={field.value}>
                                      <FormControl>
                                        <SelectTrigger data-testid="select-platform">
                                          <SelectValue />
                                        </SelectTrigger>
                                      </FormControl>
                                      <SelectContent>
                                        <SelectItem value="facebook">Facebook</SelectItem>
                                        <SelectItem value="instagram">Instagram</SelectItem>
                                        <SelectItem value="tiktok">TikTok</SelectItem>
                                        <SelectItem value="google">Google</SelectItem>
                                      </SelectContent>
                                    </Select>
                                    <FormMessage />
                                  </FormItem>
                                )}
                              />
                              <FormField
                                control={form.control}
                                name="budget"
                                render={({ field }) => (
                                  <FormItem>
                                    <FormLabel>Budget ($)</FormLabel>
                                    <FormControl>
                                      <Input 
                                        type="number" 
                                        step="0.01"
                                        {...field} 
                                        data-testid="input-budget"
                                      />
                                    </FormControl>
                                    <FormMessage />
                                  </FormItem>
                                )}
                              />
                            </div>
                            <FormField
                              control={form.control}
                              name="objective"
                              render={({ field }) => (
                                <FormItem>
                                  <FormLabel>Campaign Objective</FormLabel>
                                  <FormControl>
                                    <Textarea {...field} data-testid="textarea-objective" />
                                  </FormControl>
                                  <FormMessage />
                                </FormItem>
                              )}
                            />
                            <div className="grid grid-cols-2 gap-4">
                              <FormField
                                control={form.control}
                                name="startDate"
                                render={({ field }) => (
                                  <FormItem>
                                    <FormLabel>Start Date</FormLabel>
                                    <FormControl>
                                      <Input type="date" {...field} data-testid="input-start-date" />
                                    </FormControl>
                                    <FormMessage />
                                  </FormItem>
                                )}
                              />
                              <FormField
                                control={form.control}
                                name="endDate"
                                render={({ field }) => (
                                  <FormItem>
                                    <FormLabel>End Date (Optional)</FormLabel>
                                    <FormControl>
                                      <Input type="date" {...field} data-testid="input-end-date" />
                                    </FormControl>
                                    <FormMessage />
                                  </FormItem>
                                )}
                              />
                            </div>
                            <FormField
                              control={form.control}
                              name="status"
                              render={({ field }) => (
                                <FormItem>
                                  <FormLabel>Status</FormLabel>
                                  <Select onValueChange={field.onChange} defaultValue={field.value}>
                                    <FormControl>
                                      <SelectTrigger data-testid="select-status">
                                        <SelectValue />
                                      </SelectTrigger>
                                    </FormControl>
                                    <SelectContent>
                                      <SelectItem value="draft">Draft</SelectItem>
                                      <SelectItem value="active">Active</SelectItem>
                                      <SelectItem value="paused">Paused</SelectItem>
                                      <SelectItem value="completed">Completed</SelectItem>
                                    </SelectContent>
                                  </Select>
                                  <FormMessage />
                                </FormItem>
                              )}
                            />
                            <div className="flex justify-end space-x-2">
                              <Button
                                type="button"
                                variant="outline"
                                onClick={() => setIsDialogOpen(false)}
                              >
                                Cancel
                              </Button>
                              <Button 
                                type="submit" 
                                disabled={createMutation.isPending || updateMutation.isPending}
                                data-testid="button-save-campaign"
                              >
                                {editingCampaign ? "Update" : "Create"}
                              </Button>
                            </div>
                          </form>
                        </Form>
                      </DialogContent>
                    </Dialog>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                {isLoading ? (
                  <div className="space-y-4">
                    {[...Array(5)].map((_, i) => (
                      <div key={i} className="animate-pulse">
                        <div className="h-16 bg-gray-200 rounded"></div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Campaign</TableHead>
                        <TableHead>Platform</TableHead>
                        <TableHead>Budget</TableHead>
                        <TableHead>Spent</TableHead>
                        <TableHead>Reach</TableHead>
                        <TableHead>Conversions</TableHead>
                        <TableHead>Status</TableHead>
                        <TableHead>Actions</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {filteredCampaigns.length === 0 ? (
                        <TableRow>
                          <TableCell colSpan={8} className="text-center py-8">
                            <Megaphone className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                            <p className="text-text-secondary">No campaigns found</p>
                          </TableCell>
                        </TableRow>
                      ) : (
                        filteredCampaigns.map((campaign) => {
                          const progress = Number(campaign.budget) > 0 
                            ? (Number(campaign.spent || 0) / Number(campaign.budget)) * 100 
                            : 0;
                          
                          return (
                            <TableRow key={campaign.id} data-testid={`campaign-row-${campaign.id}`}>
                              <TableCell>
                                <div>
                                  <p className="font-medium">{campaign.name}</p>
                                  <p className="text-sm text-text-secondary">{campaign.objective}</p>
                                </div>
                              </TableCell>
                              <TableCell>
                                <div className="flex items-center space-x-2">
                                  <span className="text-lg">{getPlatformIcon(campaign.platform)}</span>
                                  <Badge className={getPlatformColor(campaign.platform)}>
                                    {campaign.platform.charAt(0).toUpperCase() + campaign.platform.slice(1)}
                                  </Badge>
                                </div>
                              </TableCell>
                              <TableCell>
                                <span className="font-medium">${Number(campaign.budget).toLocaleString()}</span>
                              </TableCell>
                              <TableCell>
                                <div className="space-y-1">
                                  <div className="flex items-center justify-between text-sm">
                                    <span>${Number(campaign.spent || 0).toLocaleString()}</span>
                                    <span className="text-text-secondary">{progress.toFixed(0)}%</span>
                                  </div>
                                  <Progress value={progress} className="h-1" />
                                </div>
                              </TableCell>
                              <TableCell>
                                <div className="flex items-center">
                                  <Users className="w-4 h-4 mr-1 text-text-secondary" />
                                  {(campaign.reach || 0).toLocaleString()}
                                </div>
                              </TableCell>
                              <TableCell>
                                <div className="flex items-center">
                                  <Target className="w-4 h-4 mr-1 text-text-secondary" />
                                  {campaign.conversions || 0}
                                </div>
                              </TableCell>
                              <TableCell>
                                <Badge className={getStatusColor(campaign.status)}>
                                  {campaign.status.charAt(0).toUpperCase() + campaign.status.slice(1)}
                                </Badge>
                              </TableCell>
                              <TableCell>
                                <div className="flex items-center space-x-2">
                                  <Button
                                    size="sm"
                                    variant="ghost"
                                    onClick={() => handleEdit(campaign)}
                                    data-testid={`button-edit-campaign-${campaign.id}`}
                                  >
                                    <Edit className="w-4 h-4" />
                                  </Button>
                                  <Button
                                    size="sm"
                                    variant="ghost"
                                    data-testid={`button-view-campaign-${campaign.id}`}
                                  >
                                    <Eye className="w-4 h-4" />
                                  </Button>
                                </div>
                              </TableCell>
                            </TableRow>
                          );
                        })
                      )}
                    </TableBody>
                  </Table>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="analytics" className="space-y-6">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <Card>
                <CardHeader>
                  <CardTitle className="font-roboto">Engagement Metrics</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                      <div className="flex items-center space-x-3">
                        <MousePointer className="w-5 h-5 text-primary" />
                        <span className="font-medium">Click-Through Rate</span>
                      </div>
                      <span className="font-bold text-primary">3.2%</span>
                    </div>
                    <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                      <div className="flex items-center space-x-3">
                        <Heart className="w-5 h-5 text-error" />
                        <span className="font-medium">Engagement Rate</span>
                      </div>
                      <span className="font-bold text-error">5.8%</span>
                    </div>
                    <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                      <div className="flex items-center space-x-3">
                        <Share2 className="w-5 h-5 text-success" />
                        <span className="font-medium">Share Rate</span>
                      </div>
                      <span className="font-bold text-success">2.1%</span>
                    </div>
                    <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                      <div className="flex items-center space-x-3">
                        <Target className="w-5 h-5 text-accent" />
                        <span className="font-medium">Conversion Rate</span>
                      </div>
                      <span className="font-bold text-accent">1.4%</span>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle className="font-roboto">Cost Efficiency</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                      <span className="font-medium">Cost Per Click (CPC)</span>
                      <span className="font-bold">$0.85</span>
                    </div>
                    <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                      <span className="font-medium">Cost Per Thousand (CPM)</span>
                      <span className="font-bold">$12.40</span>
                    </div>
                    <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                      <span className="font-medium">Cost Per Acquisition (CPA)</span>
                      <span className="font-bold">$18.75</span>
                    </div>
                    <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                      <span className="font-medium">Return on Ad Spend (ROAS)</span>
                      <span className="font-bold text-success">3.2x</span>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          </TabsContent>

          <TabsContent value="ai-optimization" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle className="font-roboto">AI-Powered Campaign Optimization</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-6">
                  <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    <div className="space-y-4">
                      <h4 className="font-medium text-text">Budget Optimization Recommendations</h4>
                      <div className="space-y-3">
                        <div className="p-4 bg-primary/5 rounded-lg border border-primary/20">
                          <div className="flex items-center justify-between mb-2">
                            <span className="font-medium">Instagram Campaign</span>
                            <Badge className="bg-primary text-white">+23% ROI</Badge>
                          </div>
                          <p className="text-sm text-text-secondary">
                            Increase budget by $200 to capitalize on high engagement rates during lunch hours
                          </p>
                        </div>
                        <div className="p-4 bg-warning/5 rounded-lg border border-warning/20">
                          <div className="flex items-center justify-between mb-2">
                            <span className="font-medium">Facebook Campaign</span>
                            <Badge className="bg-warning text-white">Optimize</Badge>
                          </div>
                          <p className="text-sm text-text-secondary">
                            Reduce budget by $150 and reallocate to TikTok for better conversion rates
                          </p>
                        </div>
                      </div>
                    </div>
                    
                    <div className="space-y-4">
                      <h4 className="font-medium text-text">Audience Insights</h4>
                      <div className="space-y-3">
                        <div className="p-4 bg-success/5 rounded-lg border border-success/20">
                          <div className="flex items-center justify-between mb-2">
                            <span className="font-medium">Lookalike Audiences</span>
                            <Badge className="bg-success text-white">High Potential</Badge>
                          </div>
                          <p className="text-sm text-text-secondary">
                            Create lookalike audience based on top 10% of customers for 35% better performance
                          </p>
                        </div>
                        <div className="p-4 bg-accent/5 rounded-lg border border-accent/20">
                          <div className="flex items-center justify-between mb-2">
                            <span className="font-medium">Timing Optimization</span>
                            <Badge className="bg-accent text-white">Schedule</Badge>
                          </div>
                          <p className="text-sm text-text-secondary">
                            Peak engagement at 11:30 AM and 6:30 PM - adjust ad scheduling accordingly
                          </p>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className="border-t pt-6">
                    <div className="flex items-center justify-between mb-4">
                      <h4 className="font-medium text-text">Auto-Optimization Settings</h4>
                      <Button variant="outline" data-testid="button-configure-ai">
                        Configure AI Rules
                      </Button>
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                      <div className="p-4 border rounded-lg">
                        <div className="flex items-center justify-between mb-2">
                          <span className="font-medium">Budget Auto-Adjust</span>
                          <Badge variant="secondary">Enabled</Badge>
                        </div>
                        <p className="text-sm text-text-secondary">
                          Automatically adjust budgets based on performance thresholds
                        </p>
                      </div>
                      <div className="p-4 border rounded-lg">
                        <div className="flex items-center justify-between mb-2">
                          <span className="font-medium">Bid Optimization</span>
                          <Badge variant="secondary">Enabled</Badge>
                        </div>
                        <p className="text-sm text-text-secondary">
                          AI manages bid strategies for optimal cost per conversion
                        </p>
                      </div>
                      <div className="p-4 border rounded-lg">
                        <div className="flex items-center justify-between mb-2">
                          <span className="font-medium">Creative Testing</span>
                          <Badge className="bg-gray-500 text-white">Disabled</Badge>
                        </div>
                        <p className="text-sm text-text-secondary">
                          Automatically test new ad creatives and pause underperformers
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </>
  );
}
