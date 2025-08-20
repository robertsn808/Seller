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
import { Package, Plus, AlertTriangle, TrendingDown, Search, Filter, Edit, BarChart3, Truck, Clock } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { apiRequest } from "@/lib/queryClient";
import { useBusinessContext } from "@/hooks/use-business-context";

interface InventoryItem {
  id: number;
  name: string;
  category: string;
  currentStock: number;
  minStock: number;
  maxStock: number;
  unit: string;
  costPerUnit: number;
  supplier: string;
  lastRestocked: string;
  expiryDate?: string;
  location: string;
  status: 'in-stock' | 'low-stock' | 'out-of-stock' | 'expired';
}

export default function Inventory() {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const { currentBusiness } = useBusinessContext();
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [showAddItem, setShowAddItem] = useState(false);

  // Fetch inventory items for current business
  const { data: inventoryItems = [], isLoading } = useQuery<InventoryItem[]>({
    queryKey: ["/api", currentBusiness.id, "inventory"],
    queryFn: () => apiRequest("GET", `/api/${currentBusiness.id}/inventory`).then(res => res.json()),
  });

  // Update inventory mutation
  const updateInventoryMutation = useMutation({
    mutationFn: async (itemData: Partial<InventoryItem>) => {
      const response = await apiRequest("PATCH", `/api/${currentBusiness.id}/inventory/${itemData.id}`, itemData);
      return response.json();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["/api", currentBusiness.id, "inventory"] });
      toast({
        title: "Inventory Updated",
        description: "Item has been updated successfully.",
      });
    },
  });

  const filteredItems = inventoryItems.filter(item => {
    const matchesSearch = item.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         item.supplier.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = statusFilter === "all" || item.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'in-stock': return 'bg-green-100 text-green-800';
      case 'low-stock': return 'bg-yellow-100 text-yellow-800';
      case 'out-of-stock': return 'bg-red-100 text-red-800';
      case 'expired': return 'bg-purple-100 text-purple-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getStockLevel = (item: InventoryItem) => {
    return (item.currentStock / item.maxStock) * 100;
  };

  const lowStockItems = inventoryItems.filter(item => item.status === 'low-stock' || item.status === 'out-of-stock');
  const totalValue = inventoryItems.reduce((sum, item) => sum + (item.currentStock * item.costPerUnit), 0);

  return (
    <div className="flex-1 space-y-4 p-4 pt-6" data-testid="inventory-page">
      <div className="flex items-center justify-between space-y-2">
        <h2 className="text-3xl font-bold tracking-tight">Inventory Management</h2>
        <div className="flex items-center space-x-2">
          <Button onClick={() => setShowAddItem(true)} data-testid="button-add-inventory">
            <Plus className="w-4 h-4 mr-2" />
            Add Item
          </Button>
        </div>
      </div>

      {/* Inventory Stats */}
      <div className="grid gap-4 md:grid-cols-4">
        <Card data-testid="stat-total-items">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Items</CardTitle>
            <Package className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{inventoryItems.length}</div>
            <p className="text-xs text-muted-foreground">
              Across all categories
            </p>
          </CardContent>
        </Card>
        
        <Card data-testid="stat-low-stock">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Low Stock Alerts</CardTitle>
            <AlertTriangle className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-orange-600">{lowStockItems.length}</div>
            <p className="text-xs text-muted-foreground">
              Items need restocking
            </p>
          </CardContent>
        </Card>
        
        <Card data-testid="stat-inventory-value">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Value</CardTitle>
            <BarChart3 className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${totalValue.toFixed(2)}</div>
            <p className="text-xs text-muted-foreground">
              Current inventory value
            </p>
          </CardContent>
        </Card>
        
        <Card data-testid="stat-categories">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Categories</CardTitle>
            <Filter className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {new Set(inventoryItems.map(item => item.category)).size}
            </div>
            <p className="text-xs text-muted-foreground">
              Item categories
            </p>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="overview" className="space-y-4">
        <TabsList>
          <TabsTrigger value="overview" data-testid="tab-inventory-overview">Overview</TabsTrigger>
          <TabsTrigger value="alerts" data-testid="tab-inventory-alerts">Alerts</TabsTrigger>
          <TabsTrigger value="reports" data-testid="tab-inventory-reports">Reports</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-4">
          {/* Search and Filters */}
          <div className="flex items-center space-x-4">
            <div className="flex items-center space-x-2 flex-1">
              <Search className="w-4 h-4 text-muted-foreground" />
              <Input
                placeholder="Search inventory..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="max-w-sm"
                data-testid="input-search-inventory"
              />
            </div>
            <Select value={statusFilter} onValueChange={setStatusFilter}>
              <SelectTrigger className="w-48" data-testid="select-status-filter">
                <Filter className="w-4 h-4 mr-2" />
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value="in-stock">In Stock</SelectItem>
                <SelectItem value="low-stock">Low Stock</SelectItem>
                <SelectItem value="out-of-stock">Out of Stock</SelectItem>
                <SelectItem value="expired">Expired</SelectItem>
              </SelectContent>
            </Select>
          </div>

          {/* Inventory List */}
          <Card data-testid="inventory-list">
            <CardHeader>
              <CardTitle>Inventory Items</CardTitle>
              <CardDescription>
                Manage {currentBusiness.name} inventory and stock levels
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
              ) : filteredItems.length > 0 ? (
                <div className="space-y-4">
                  {filteredItems.map((item) => (
                    <div key={item.id} className="border rounded-lg p-4 hover:bg-muted/50 transition-colors" data-testid={`inventory-item-${item.id}`}>
                      <div className="flex items-center justify-between mb-3">
                        <div className="flex items-center space-x-4">
                          <div className="w-12 h-12 bg-primary/10 rounded-lg flex items-center justify-center">
                            <Package className="w-6 h-6 text-primary" />
                          </div>
                          <div>
                            <h3 className="font-medium">{item.name}</h3>
                            <p className="text-sm text-muted-foreground">{item.category} • {item.supplier}</p>
                          </div>
                        </div>
                        
                        <div className="flex items-center space-x-4">
                          <Badge className={getStatusColor(item.status)}>
                            {item.status.replace('-', ' ')}
                          </Badge>
                          <div className="text-right">
                            <p className="font-medium">{item.currentStock} {item.unit}</p>
                            <p className="text-sm text-muted-foreground">${(item.currentStock * item.costPerUnit).toFixed(2)}</p>
                          </div>
                          <Button variant="ghost" size="sm" data-testid={`button-edit-inventory-${item.id}`}>
                            <Edit className="w-4 h-4" />
                          </Button>
                        </div>
                      </div>
                      
                      <div className="space-y-2">
                        <div className="flex items-center justify-between text-sm">
                          <span>Stock Level</span>
                          <span>{Math.round(getStockLevel(item))}%</span>
                        </div>
                        <Progress value={getStockLevel(item)} className="h-2" />
                        
                        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm mt-3">
                          <div>
                            <p className="text-muted-foreground">Min Stock:</p>
                            <p>{item.minStock} {item.unit}</p>
                          </div>
                          <div>
                            <p className="text-muted-foreground">Max Stock:</p>
                            <p>{item.maxStock} {item.unit}</p>
                          </div>
                          <div>
                            <p className="text-muted-foreground">Location:</p>
                            <p>{item.location}</p>
                          </div>
                          <div>
                            <p className="text-muted-foreground">Last Restocked:</p>
                            <p>{new Date(item.lastRestocked).toLocaleDateString()}</p>
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-8">
                  <Package className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                  <h3 className="text-lg font-medium mb-2">No inventory found</h3>
                  <p className="text-muted-foreground mb-4">
                    {searchTerm || statusFilter !== "all" 
                      ? "Try adjusting your search or filters."
                      : "Start tracking your inventory."}
                  </p>
                  <Button onClick={() => setShowAddItem(true)} data-testid="button-add-first-inventory">
                    <Plus className="w-4 h-4 mr-2" />
                    Add Your First Item
                  </Button>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="alerts">
          <Card>
            <CardHeader>
              <CardTitle>Inventory Alerts</CardTitle>
              <CardDescription>Items requiring immediate attention</CardDescription>
            </CardHeader>
            <CardContent>
              {lowStockItems.length > 0 ? (
                <div className="space-y-4">
                  {lowStockItems.map((item) => (
                    <div key={item.id} className="flex items-center justify-between p-4 border-l-4 border-orange-500 bg-orange-50 rounded-r-lg" data-testid={`alert-item-${item.id}`}>
                      <div className="flex items-center space-x-3">
                        <AlertTriangle className="w-5 h-5 text-orange-600" />
                        <div>
                          <h4 className="font-medium">{item.name}</h4>
                          <p className="text-sm text-muted-foreground">
                            {item.status === 'out-of-stock' ? 'Out of stock' : `Only ${item.currentStock} ${item.unit} left`}
                          </p>
                        </div>
                      </div>
                      <Button size="sm" data-testid={`button-restock-${item.id}`}>
                        <Truck className="w-4 h-4 mr-2" />
                        Restock
                      </Button>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-8">
                  <Package className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                  <h3 className="text-lg font-medium mb-2">No Alerts</h3>
                  <p className="text-muted-foreground">
                    All inventory levels are adequate.
                  </p>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="reports">
          <Card>
            <CardHeader>
              <CardTitle>Inventory Reports</CardTitle>
              <CardDescription>Analytics and reporting for inventory management</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="text-center py-8">
                <BarChart3 className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-medium mb-2">Reports Coming Soon</h3>
                <p className="text-muted-foreground">
                  Detailed inventory analytics and reporting features will be available soon.
                </p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}