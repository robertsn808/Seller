import { useQuery } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { Badge } from "@/components/ui/badge";
import { Plus } from "lucide-react";
import type { Campaign } from "@shared/schema";

export default function ActiveCampaigns() {
  const { data: campaigns, isLoading } = useQuery<Campaign[]>({
    queryKey: ["/api/campaigns"],
    refetchInterval: 60000, // Refetch every minute
  });

  const activeCampaigns = campaigns?.filter(campaign => campaign.status === 'active').slice(0, 3) || [];

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

  if (isLoading) {
    return (
      <div className="bg-surface rounded-xl shadow-sm border border-gray-200">
        <div className="p-6 border-b border-gray-200">
          <div className="flex items-center justify-between">
            <h3 className="font-roboto font-bold text-lg text-text">Active Campaigns</h3>
            <div className="w-32 h-8 bg-gray-200 rounded animate-pulse"></div>
          </div>
        </div>
        <div className="p-6">
          <div className="space-y-4">
            {[...Array(2)].map((_, i) => (
              <div key={i} className="p-4 border border-gray-200 rounded-lg animate-pulse">
                <div className="space-y-3">
                  <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                  <div className="h-3 bg-gray-200 rounded w-full"></div>
                  <div className="h-2 bg-gray-200 rounded w-full"></div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-surface rounded-xl shadow-sm border border-gray-200" data-testid="active-campaigns">
      <div className="p-6 border-b border-gray-200">
        <div className="flex items-center justify-between">
          <h3 className="font-roboto font-bold text-lg text-text">Active Campaigns</h3>
          <Button 
            className="bg-primary text-white hover:bg-primary/90"
            data-testid="button-new-campaign"
          >
            <Plus className="w-4 h-4 mr-2" />
            New Campaign
          </Button>
        </div>
      </div>
      <div className="p-6">
        <div className="space-y-4">
          {activeCampaigns.length === 0 ? (
            <div className="text-center py-8">
              <div className="w-12 h-12 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <Plus className="w-6 h-6 text-gray-400" />
              </div>
              <p className="text-text-secondary">No active campaigns</p>
              <p className="text-sm text-text-secondary mt-1">
                Create your first marketing campaign to start reaching customers
              </p>
            </div>
          ) : (
            activeCampaigns.map((campaign) => {
              const progress = Number(campaign.budget) > 0 
                ? (Number(campaign.spent || 0) / Number(campaign.budget)) * 100 
                : 0;
              
              return (
                <div key={campaign.id} className="p-4 border border-gray-200 rounded-lg" data-testid={`campaign-${campaign.id}`}>
                  <div className="flex items-center justify-between mb-3">
                    <h4 className="font-medium text-text" data-testid={`campaign-${campaign.id}-name`}>
                      {campaign.name}
                    </h4>
                    <div className="flex items-center space-x-2">
                      <span className="text-lg">{getPlatformIcon(campaign.platform)}</span>
                      <Badge className={getPlatformColor(campaign.platform)}>
                        {campaign.platform.charAt(0).toUpperCase() + campaign.platform.slice(1)}
                      </Badge>
                    </div>
                  </div>
                  <div className="grid grid-cols-2 gap-4 text-sm mb-3">
                    <div>
                      <p className="text-text-secondary">Platform</p>
                      <p className="font-medium text-text capitalize" data-testid={`campaign-${campaign.id}-platform`}>
                        {campaign.platform}
                      </p>
                    </div>
                    <div>
                      <p className="text-text-secondary">Spent</p>
                      <p className="font-medium text-text" data-testid={`campaign-${campaign.id}-spent`}>
                        ${Number(campaign.spent || 0).toLocaleString()} / ${Number(campaign.budget).toLocaleString()}
                      </p>
                    </div>
                    <div>
                      <p className="text-text-secondary">Reach</p>
                      <p className="font-medium text-text" data-testid={`campaign-${campaign.id}-reach`}>
                        {(campaign.reach || 0).toLocaleString()}
                      </p>
                    </div>
                    <div>
                      <p className="text-text-secondary">Conversions</p>
                      <p className="font-medium text-text" data-testid={`campaign-${campaign.id}-conversions`}>
                        {campaign.conversions || 0}
                      </p>
                    </div>
                  </div>
                  <div className="space-y-2">
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-text-secondary">Budget Progress</span>
                      <span className="font-medium">{progress.toFixed(0)}%</span>
                    </div>
                    <Progress value={progress} className="h-2" data-testid={`campaign-${campaign.id}-progress`} />
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
}
