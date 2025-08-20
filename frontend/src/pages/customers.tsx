import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Progress } from "@/components/ui/progress";
import { Users, UserPlus, Mail, Phone, MapPin, Calendar, TrendingUp, Star, Filter, Search, MoreHorizontal } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { apiRequest } from "@/lib/queryClient";
import { useBusinessContext } from "@/hooks/use-business-context";

interface Customer {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  city?: string;
  state?: string;
  orderCount: number;
  totalSpent: number;
  lastOrderDate?: string;
  loyaltyTier: 'bronze' | 'silver' | 'gold' | 'platinum';
  customerSince: string;
  preferences?: string[];
}

export default function Customers() {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const { currentBusiness } = useBusinessContext();
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedFilter, setSelectedFilter] = useState("all");
  const [showAddCustomer, setShowAddCustomer] = useState(false);

  // Fetch analytics data for current business
  const { data: hourlyTraffic = [] } = useQuery({
    queryKey: ["/api", currentBusiness.id, "analytics", "hourly-traffic"],
    queryFn: () => apiRequest("GET", `/api/${currentBusiness.id}/analytics/hourly-traffic`).then(res => res.json()),
  });

  // Fetch customers for current business
  const { data: customers = [], isLoading } = useQuery<Customer[]>({
    queryKey: ["/api", currentBusiness.id, "customers"],
    queryFn: () => apiRequest("GET", `/api/${currentBusiness.id}/customers`).then(res => res.json()),
    refetchInterval: 30000,
  });

  // Customer stats
  const { data: customerStats } = useQuery({
    queryKey: ["/api", currentBusiness.id, "customers", "stats"],
    queryFn: () => apiRequest("GET", `/api/${currentBusiness.id}/customers/stats`).then(res => res.json()),
  });

  // Add customer mutation
  const addCustomerMutation = useMutation({
    mutationFn: async (customerData: Partial<Customer>) => {
      const response = await apiRequest("POST", `/api/${currentBusiness.id}/customers`, customerData);
      return response.json();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["/api", currentBusiness.id, "customers"] });
      setShowAddCustomer(false);
      toast({
        title: "Customer Added",
        description: "New customer has been successfully added.",
      });
    },
  });

  const filteredCustomers = customers.filter(customer => {
    const matchesSearch = 
      customer.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      customer.lastName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      customer.email.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesFilter = selectedFilter === "all" || 
      (selectedFilter === "high-value" && customer.totalSpent > 500) ||
      (selectedFilter === "recent" && customer.lastOrderDate && new Date(customer.lastOrderDate) > new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)) ||
      (selectedFilter === customer.loyaltyTier);

    return matchesSearch && matchesFilter;
  });

  const getTierColor = (tier: string) => {
    switch (tier) {
      case 'platinum': return 'bg-purple-100 text-purple-800';
      case 'gold': return 'bg-yellow-100 text-yellow-800';
      case 'silver': return 'bg-gray-100 text-gray-800';
      default: return 'bg-orange-100 text-orange-800';
    }
  };

  return (
    <div className="flex-1 space-y-4 p-4 pt-6" data-testid="customers-page">
      <div className="flex items-center justify-between space-y-2">
        <h2 className="text-3xl font-bold tracking-tight">Customer Management</h2>
        <div className="flex items-center space-x-2">
          <Button onClick={() => setShowAddCustomer(true)} data-testid="button-add-customer">
            <UserPlus className="w-4 h-4 mr-2" />
            Add Customer
          </Button>
        </div>
      </div>

      {/* Customer Stats */}
      <div className="grid gap-4 md:grid-cols-4">
        <Card data-testid="stat-total-customers">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Customers</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{customers.length}</div>
            <p className="text-xs text-muted-foreground">
              +12% from last month
            </p>
          </CardContent>
        </Card>

        <Card data-testid="stat-avg-order-value">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Avg Order Value</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              ${customers.length > 0 ? (customers.reduce((sum, c) => sum + c.totalSpent, 0) / customers.reduce((sum, c) => sum + c.orderCount, 0) || 0).toFixed(2) : '0.00'}
            </div>
            <p className="text-xs text-muted-foreground">
              +8% from last month
            </p>
          </CardContent>
        </Card>

        <Card data-testid="stat-repeat-customers">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Repeat Customers</CardTitle>
            <Star className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {customers.filter(c => c.orderCount > 1).length}
            </div>
            <p className="text-xs text-muted-foreground">
              {customers.length > 0 ? Math.round((customers.filter(c => c.orderCount > 1).length / customers.length) * 100) : 0}% retention rate
            </p>
          </CardContent>
        </Card>

        <Card data-testid="stat-loyalty-members">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Loyalty Members</CardTitle>
            <Badge className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {customers.filter(c => c.loyaltyTier !== 'bronze').length}
            </div>
            <p className="text-xs text-muted-foreground">
              Premium tier customers
            </p>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="overview" className="space-y-4">
        <TabsList>
          <TabsTrigger value="overview" data-testid="tab-overview">Overview</TabsTrigger>
          <TabsTrigger value="analytics" data-testid="tab-analytics">Analytics</TabsTrigger>
          <TabsTrigger value="segments" data-testid="tab-segments">Segments</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-4">
          {/* Search and Filters */}
          <div className="flex items-center space-x-4">
            <div className="flex items-center space-x-2 flex-1">
              <Search className="w-4 h-4 text-muted-foreground" />
              <Input
                placeholder="Search customers..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="max-w-sm"
                data-testid="input-search-customers"
              />
            </div>
            <Select value={selectedFilter} onValueChange={setSelectedFilter}>
              <SelectTrigger className="w-48" data-testid="select-customer-filter">
                <Filter className="w-4 h-4 mr-2" />
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Customers</SelectItem>
                <SelectItem value="high-value">High Value ($500+)</SelectItem>
                <SelectItem value="recent">Recent Orders</SelectItem>
                <SelectItem value="platinum">Platinum Tier</SelectItem>
                <SelectItem value="gold">Gold Tier</SelectItem>
                <SelectItem value="silver">Silver Tier</SelectItem>
                <SelectItem value="bronze">Bronze Tier</SelectItem>
              </SelectContent>
            </Select>
          </div>

          {/* Customer List */}
          <Card data-testid="customers-list">
            <CardHeader>
              <CardTitle>Customer Directory</CardTitle>
              <CardDescription>
                Manage your {currentBusiness.name} customers and their preferences
              </CardDescription>
            </CardHeader>
            <CardContent>
              {isLoading ? (
                <div className="space-y-4">
                  {[1, 2, 3].map((i) => (
                    <div key={i} className="animate-pulse">
                      <div className="h-16 bg-muted rounded"></div>
                    </div>
                  ))}
                </div>
              ) : filteredCustomers.length > 0 ? (
                <div className="space-y-4">
                  {filteredCustomers.map((customer) => (
                    <div key={customer.id} className="flex items-center justify-between p-4 border rounded-lg hover:bg-muted/50 transition-colors" data-testid={`customer-item-${customer.id}`}>
                      <div className="flex items-center space-x-4">
                        <div className="w-12 h-12 bg-primary/10 rounded-full flex items-center justify-center">
                          <span className="text-primary font-semibold">
                            {customer.firstName[0]}{customer.lastName[0]}
                          </span>
                        </div>
                        <div>
                          <h3 className="font-medium">{customer.firstName} {customer.lastName}</h3>
                          <div className="flex items-center space-x-4 text-sm text-muted-foreground">
                            <span className="flex items-center">
                              <Mail className="w-3 h-3 mr-1" />
                              {customer.email}
                            </span>
                            {customer.phone && (
                              <span className="flex items-center">
                                <Phone className="w-3 h-3 mr-1" />
                                {customer.phone}
                              </span>
                            )}
                            {customer.city && (
                              <span className="flex items-center">
                                <MapPin className="w-3 h-3 mr-1" />
                                {customer.city}, {customer.state}
                              </span>
                            )}
                          </div>
                        </div>
                      </div>

                      <div className="flex items-center space-x-4">
                        <div className="text-right">
                          <p className="text-lg font-bold">${Number(customer.totalSpent).toFixed(2)}</p>
                          <p className="text-sm text-muted-foreground">{customer.orderCount} orders</p>
                        </div>
                        <Badge className={getTierColor(customer.loyaltyTier)}>
                          {customer.loyaltyTier}
                        </Badge>
                        <Button variant="ghost" size="sm" data-testid={`button-customer-actions-${customer.id}`}>
                          <MoreHorizontal className="w-4 h-4" />
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-8">
                  <Users className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                  <h3 className="text-lg font-medium mb-2">No customers found</h3>
                  <p className="text-muted-foreground mb-4">
                    {searchTerm || selectedFilter !== "all" 
                      ? "Try adjusting your search or filters."
                      : "Start building your customer base."}
                  </p>
                  <Button onClick={() => setShowAddCustomer(true)} data-testid="button-add-first-customer">
                    <UserPlus className="w-4 h-4 mr-2" />
                    Add Your First Customer
                  </Button>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="analytics">
          <Card>
            <CardHeader>
              <CardTitle>Customer Analytics</CardTitle>
              <CardDescription>Insights into customer behavior and trends</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="text-center py-8">
                <TrendingUp className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-medium mb-2">Analytics Dashboard</h3>
                <p className="text-muted-foreground">
                  Advanced customer analytics and insights coming soon.
                </p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="segments">
          <Card>
            <CardHeader>
              <CardTitle>Customer Segments</CardTitle>
              <CardDescription>Organize customers into targeted groups</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="text-center py-8">
                <Users className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-medium mb-2">Customer Segmentation</h3>
                <p className="text-muted-foreground">
                  Advanced segmentation and targeting features coming soon.
                </p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}