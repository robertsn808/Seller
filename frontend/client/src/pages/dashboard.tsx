import TopBar from "@/components/layout/topbar";
import MetricsCards from "@/components/dashboard/metrics-cards";
import GrokInsights from "@/components/dashboard/grok-insights";
import InventoryAlerts from "@/components/dashboard/inventory-alerts";
import SalesChart from "@/components/dashboard/sales-chart";
import CustomerChart from "@/components/dashboard/customer-chart";
import RecentOrders from "@/components/dashboard/recent-orders";
import ActiveCampaigns from "@/components/dashboard/active-campaigns";
import { useWebSocket } from "@/hooks/use-websocket";
import { useEffect } from "react";
import { useToast } from "@/hooks/use-toast";
import { queryClient } from "@/lib/queryClient";

export default function Dashboard() {
  const { toast } = useToast();
  const { isConnected, lastMessage } = useWebSocket();

  useEffect(() => {
    if (lastMessage) {
      // Handle real-time updates
      switch (lastMessage.type) {
        case 'order_created':
          queryClient.invalidateQueries({ queryKey: ["/api/dashboard/metrics"] });
          queryClient.invalidateQueries({ queryKey: ["/api/dashboard/recent-orders"] });
          toast({
            title: "New Order",
            description: `Order #${lastMessage.data.id} has been created`,
          });
          break;
        case 'inventory_updated':
          queryClient.invalidateQueries({ queryKey: ["/api/inventory/low-stock"] });
          break;
        case 'ai_insight_created':
          queryClient.invalidateQueries({ queryKey: ["/api/ai/insights"] });
          toast({
            title: "New AI Insight",
            description: lastMessage.data.title,
          });
          break;
        case 'campaign_created':
          queryClient.invalidateQueries({ queryKey: ["/api/campaigns"] });
          break;
      }
    }
  }, [lastMessage, toast]);

  return (
    <>
      <TopBar
        title="Dashboard Overview"
        subtitle="Real-time business intelligence and AI insights"
        notificationCount={3}
        showLiveSync={isConnected}
      />
      
      <div className="flex-1 overflow-auto p-6 bg-background">
        {/* Metrics Cards */}
        <MetricsCards />

        {/* Main Dashboard Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
          <GrokInsights />
          <InventoryAlerts />
        </div>

        {/* Charts and Analytics */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
          <SalesChart />
          <CustomerChart />
        </div>

        {/* Recent Orders and Campaigns */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <RecentOrders />
          <ActiveCampaigns />
        </div>
      </div>
    </>
  );
}
