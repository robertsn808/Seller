import { useQuery } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Hash } from "lucide-react";

interface RecentOrder {
  id: number;
  customerName: string;
  total: string;
  status: string;
  orderDate: string;
  orderChannel: string;
}

export default function RecentOrders() {
  const { data: orders, isLoading } = useQuery<RecentOrder[]>({
    queryKey: ["/api/dashboard/recent-orders"],
    refetchInterval: 30000, // Refetch every 30 seconds
  });

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'completed':
        return 'status-completed';
      case 'pending':
        return 'status-pending';
      case 'preparing':
        return 'bg-warning text-white';
      case 'delivery':
        return 'status-active';
      default:
        return 'bg-gray-500 text-white';
    }
  };

  const formatTime = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / (1000 * 60));
    
    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins} min ago`;
    if (diffMins < 1440) return `${Math.floor(diffMins / 60)} hr ago`;
    return date.toLocaleDateString();
  };

  if (isLoading) {
    return (
      <div className="bg-surface rounded-xl shadow-sm border border-gray-200">
        <div className="p-6 border-b border-gray-200">
          <div className="flex items-center justify-between">
            <h3 className="font-roboto font-bold text-lg text-text">Recent Orders</h3>
            <div className="w-16 h-6 bg-gray-200 rounded animate-pulse"></div>
          </div>
        </div>
        <div className="p-6">
          <div className="space-y-4">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="flex items-center justify-between p-3 border border-gray-200 rounded-lg animate-pulse">
                <div className="flex items-center space-x-3">
                  <div className="w-8 h-8 bg-gray-200 rounded-full"></div>
                  <div className="space-y-1">
                    <div className="h-4 bg-gray-200 rounded w-20"></div>
                    <div className="h-3 bg-gray-200 rounded w-16"></div>
                  </div>
                </div>
                <div className="space-y-1">
                  <div className="h-4 bg-gray-200 rounded w-12"></div>
                  <div className="h-3 bg-gray-200 rounded w-10"></div>
                </div>
                <div className="w-16 h-6 bg-gray-200 rounded"></div>
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-surface rounded-xl shadow-sm border border-gray-200" data-testid="recent-orders">
      <div className="p-6 border-b border-gray-200">
        <div className="flex items-center justify-between">
          <h3 className="font-roboto font-bold text-lg text-text">Recent Orders</h3>
          <Button 
            variant="ghost" 
            className="text-primary hover:text-primary/80 text-sm font-medium"
            data-testid="button-view-all-orders"
          >
            View All
          </Button>
        </div>
      </div>
      <div className="p-6">
        <div className="space-y-4">
          {!orders || orders.length === 0 ? (
            <div className="text-center py-8">
              <Hash className="w-12 h-12 text-gray-400 mx-auto mb-4" />
              <p className="text-text-secondary">No recent orders</p>
            </div>
          ) : (
            orders.slice(0, 5).map((order) => (
              <div 
                key={order.id} 
                className="flex items-center justify-between p-3 border border-gray-200 rounded-lg"
                data-testid={`order-${order.id}`}
              >
                <div className="flex items-center space-x-3">
                  <div className="w-8 h-8 bg-primary/10 rounded-full flex items-center justify-center">
                    <Hash className="text-primary text-sm" />
                  </div>
                  <div>
                    <p className="font-medium text-sm text-text" data-testid={`order-${order.id}-id`}>
                      Order #{order.id}
                    </p>
                    <p className="text-xs text-text-secondary" data-testid={`order-${order.id}-customer`}>
                      {order.customerName || 'Walk-in Customer'}
                    </p>
                  </div>
                </div>
                <div className="text-right">
                  <p className="font-medium text-sm text-text" data-testid={`order-${order.id}-total`}>
                    ${typeof order.total === 'string' ? parseFloat(order.total).toFixed(2) : Number(order.total).toFixed(2)}
                  </p>
                  <p className="text-xs text-text-secondary" data-testid={`order-${order.id}-time`}>
                    {formatTime(order.orderDate)}
                  </p>
                </div>
                <span 
                  className={`status-badge ${getStatusColor(order.status)}`}
                  data-testid={`order-${order.id}-status`}
                >
                  {order.status.charAt(0).toUpperCase() + order.status.slice(1)}
                </span>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
