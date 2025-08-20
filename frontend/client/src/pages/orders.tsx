import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ShoppingCart, Plus, Search, Filter, Eye, Truck, Clock, Check, X, DollarSign, Package } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { apiRequest } from "@/lib/queryClient";
import { useBusinessContext } from "@/hooks/use-business-context";

interface OrderItem {
  id: number;
  name: string;
  quantity: number;
  price: number;
  notes?: string;
}

interface Order {
  id: number;
  customerName: string;
  customerEmail: string;
  items: OrderItem[];
  total: number;
  status: 'pending' | 'preparing' | 'ready' | 'completed' | 'cancelled';
  orderDate: string;
  estimatedTime?: number;
  notes?: string;
  paymentMethod: string;
  deliveryMethod: 'pickup' | 'delivery';
  deliveryAddress?: string;
}

export default function Orders() {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const { currentBusiness } = useBusinessContext();
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);

  // Fetch orders for current business
  const { data: orders = [], isLoading } = useQuery<Order[]>({
    queryKey: ["/api", currentBusiness?.id, "orders"],
    queryFn: () => fetch(`/api/${currentBusiness?.id}/orders`).then(res => res.json()),
    refetchInterval: 10000, // Refresh every 10 seconds
    enabled: !!currentBusiness?.id // Only fetch if currentBusiness.id is available
  });

  // Order stats
  const { data: orderStats } = useQuery({
    queryKey: ["/api", currentBusiness?.id, "orders", "stats"],
    queryFn: () => fetch(`/api/${currentBusiness?.id}/orders/stats`).then(res => res.json()),
    enabled: !!currentBusiness?.id // Only fetch if currentBusiness.id is available
  });

  // Update order status mutation
  const updateOrderMutation = useMutation({
    mutationFn: async ({ orderId, status }: { orderId: number; status: string }) => {
      const response = await apiRequest("PATCH", `/api/${currentBusiness?.id}/orders/${orderId}`, { status });
      return response.json();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["/api", currentBusiness?.id, "orders"] });
      toast({
        title: "Order Updated",
        description: "Order status has been updated successfully.",
      });
    },
    enabled: !!currentBusiness?.id // Only enable mutation if currentBusiness.id is available
  });

  const filteredOrders = orders.filter(order => {
    const matchesSearch = 
      order.customerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      order.id.toString().includes(searchTerm) ||
      order.customerEmail.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesStatus = statusFilter === "all" || order.status === statusFilter;

    return matchesSearch && matchesStatus;
  });

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'pending': return 'bg-yellow-100 text-yellow-800';
      case 'preparing': return 'bg-blue-100 text-blue-800';
      case 'ready': return 'bg-green-100 text-green-800';
      case 'completed': return 'bg-gray-100 text-gray-800';
      case 'cancelled': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'pending': return Clock;
      case 'preparing': return Package;
      case 'ready': return Check;
      case 'completed': return Check;
      case 'cancelled': return X;
      default: return Clock;
    }
  };

  const todayOrders = orders.filter(order => 
    new Date(order.orderDate).toDateString() === new Date().toDateString()
  );

  const totalRevenue = orders.filter(order => order.status === 'completed')
    .reduce((sum, order) => sum + order.total, 0);

  return (
    <div className="flex-1 space-y-4 p-4 pt-6" data-testid="orders-page">
      <div className="flex items-center justify-between space-y-2">
        <h2 className="text-3xl font-bold tracking-tight">Order Management</h2>
        <div className="flex items-center space-x-2">
          <Button data-testid="button-new-order">
            <Plus className="w-4 h-4 mr-2" />
            New Order
          </Button>
        </div>
      </div>

      {/* Order Stats */}
      <div className="grid gap-4 md:grid-cols-4">
        <Card data-testid="stat-todays-orders">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Today's Orders</CardTitle>
            <ShoppingCart className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{todayOrders.length}</div>
            <p className="text-xs text-muted-foreground">
              {todayOrders.filter(o => o.status === 'pending').length} pending
            </p>
          </CardContent>
        </Card>

        <Card data-testid="stat-total-revenue">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Revenue</CardTitle>
            <DollarSign className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${totalRevenue.toFixed(2)}</div>
            <p className="text-xs text-muted-foreground">
              Completed orders
            </p>
          </CardContent>
        </Card>

        <Card data-testid="stat-avg-order-value">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Avg Order Value</CardTitle>
            <Package className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              ${orders.length > 0 ? (orders.reduce((sum, o) => sum + o.total, 0) / orders.length).toFixed(2) : '0.00'}
            </div>
            <p className="text-xs text-muted-foreground">
              All time average
            </p>
          </CardContent>
        </Card>

        <Card data-testid="stat-completion-rate">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Completion Rate</CardTitle>
            <Check className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {orders.length > 0 ? Math.round((orders.filter(o => o.status === 'completed').length / orders.length) * 100) : 0}%
            </div>
            <p className="text-xs text-muted-foreground">
              Orders completed
            </p>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="active" className="space-y-4">
        <TabsList>
          <TabsTrigger value="active" data-testid="tab-active-orders">Active Orders</TabsTrigger>
          <TabsTrigger value="completed" data-testid="tab-completed-orders">Completed</TabsTrigger>
          <TabsTrigger value="analytics" data-testid="tab-order-analytics">Analytics</TabsTrigger>
        </TabsList>

        <TabsContent value="active" className="space-y-4">
          {/* Search and Filters */}
          <div className="flex items-center space-x-4">
            <div className="flex items-center space-x-2 flex-1">
              <Search className="w-4 h-4 text-muted-foreground" />
              <Input
                placeholder="Search orders..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="max-w-sm"
                data-testid="input-search-orders"
              />
            </div>
            <Select value={statusFilter} onValueChange={setStatusFilter}>
              <SelectTrigger className="w-48" data-testid="select-status-filter">
                <Filter className="w-4 h-4 mr-2" />
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value="pending">Pending</SelectItem>
                <SelectItem value="preparing">Preparing</SelectItem>
                <SelectItem value="ready">Ready</SelectItem>
                <SelectItem value="completed">Completed</SelectItem>
                <SelectItem value="cancelled">Cancelled</SelectItem>
              </SelectContent>
            </Select>
          </div>

          {/* Orders List */}
          <Card data-testid="orders-list">
            <CardHeader>
              <CardTitle>Active Orders</CardTitle>
              <CardDescription>
                Manage {currentBusiness?.name || 'business'} orders in real-time
              </CardDescription>
            </CardHeader>
            <CardContent>
              {isLoading ? (
                <div className="space-y-4">
                  {[1, 2, 3].map((i) => (
                    <div key={i} className="animate-pulse">
                      <div className="h-20 bg-muted rounded"></div>
                    </div>
                  ))}
                </div>
              ) : filteredOrders.length > 0 ? (
                <div className="space-y-4">
                  {filteredOrders.map((order) => {
                    const StatusIcon = getStatusIcon(order.status);
                    return (
                      <div key={order.id} className="border rounded-lg p-4 hover:bg-muted/50 transition-colors" data-testid={`order-item-${order.id}`}>
                        <div className="flex items-center justify-between mb-3">
                          <div className="flex items-center space-x-3">
                            <div className="w-8 h-8 bg-primary/10 rounded-full flex items-center justify-center">
                              <StatusIcon className="w-4 h-4 text-primary" />
                            </div>
                            <div>
                              <h3 className="font-medium">Order #{order.id}</h3>
                              <p className="text-sm text-muted-foreground">{order.customerName}</p>
                            </div>
                          </div>

                          <div className="flex items-center space-x-3">
                            <Badge className={getStatusColor(order.status)}>
                              {order.status}
                            </Badge>
                            <span className="font-medium">${order.total.toFixed(2)}</span>
                            <Button variant="ghost" size="sm" onClick={() => setSelectedOrder(order)} data-testid={`button-view-order-${order.id}`}>
                              <Eye className="w-4 h-4" />
                            </Button>
                          </div>
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                          <div>
                            <p className="text-muted-foreground">Items:</p>
                            <p>{order.items.map(item => `${item.name} (${item.quantity})`).join(', ')}</p>
                          </div>
                          <div>
                            <p className="text-muted-foreground">Method:</p>
                            <p className="capitalize">{order.deliveryMethod}</p>
                          </div>
                          <div>
                            <p className="text-muted-foreground">Order Time:</p>
                            <p>{new Date(order.orderDate).toLocaleString()}</p>
                          </div>
                        </div>

                        {order.status !== 'completed' && order.status !== 'cancelled' && (
                          <div className="flex items-center space-x-2 mt-3">
                            <Button 
                              size="sm" 
                              variant="outline"
                              onClick={() => updateOrderMutation.mutate({ 
                                orderId: order.id, 
                                status: order.status === 'pending' ? 'preparing' : 
                                        order.status === 'preparing' ? 'ready' : 'completed'
                              })}
                              data-testid={`button-advance-order-${order.id}`}
                            >
                              {order.status === 'pending' ? 'Start Preparing' :
                               order.status === 'preparing' ? 'Mark Ready' : 'Complete Order'}
                            </Button>
                            <Button 
                              size="sm" 
                              variant="outline"
                              onClick={() => updateOrderMutation.mutate({ orderId: order.id, status: 'cancelled' })}
                              data-testid={`button-cancel-order-${order.id}`}
                            >
                              Cancel
                            </Button>
                          </div>
                        )}
                      </div>
                    );
                  })}
                </div>
              ) : (
                <div className="text-center py-8">
                  <ShoppingCart className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                  <h3 className="text-lg font-medium mb-2">No orders found</h3>
                  <p className="text-muted-foreground mb-4">
                    {searchTerm || statusFilter !== "all" 
                      ? "Try adjusting your search or filters."
                      : "Orders will appear here once customers place them."}
                  </p>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="completed">
          <Card>
            <CardHeader>
              <CardTitle>Completed Orders</CardTitle>
              <CardDescription>Order history and completed transactions</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="text-center py-8">
                <Check className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-medium mb-2">Order History</h3>
                <p className="text-muted-foreground">
                  Detailed order history and reporting coming soon.
                </p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="analytics">
          <Card>
            <CardHeader>
              <CardTitle>Order Analytics</CardTitle>
              <CardDescription>Sales trends and order insights</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="text-center py-8">
                <Package className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-medium mb-2">Order Analytics</h3>
                <p className="text-muted-foreground">
                  Advanced order analytics and reporting coming soon.
                </p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}