import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Switch } from "@/components/ui/switch";
import { Utensils, Plus, Edit, Trash2, Star, DollarSign, Clock, ChefHat, TrendingUp, Eye, Search } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { apiRequest } from "@/lib/queryClient";
import { useBusinessContext } from "@/hooks/use-business-context";

interface MenuItem {
  id: number;
  name: string;
  description: string;
  price: number;
  category: string;
  isAvailable: boolean;
  isPopular: boolean;
  prepTime: number;
  ingredients: string[];
  allergens: string[];
  calories?: number;
  image?: string;
  customizations?: string[];
}

export default function Menu() {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const { currentBusiness, getMenuItems, getSpecialties } = useBusinessContext();
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("all");
  const [showAddItem, setShowAddItem] = useState(false);
  const [editingItem, setEditingItem] = useState<MenuItem | null>(null);

  // Fetch menu items for current business
  const { data: menuItems = [], isLoading } = useQuery<MenuItem[]>({
    queryKey: ["/api", currentBusiness.id, "menu"],
    queryFn: () => apiRequest("GET", `/api/${currentBusiness.id}/menu`).then(res => res.json()),
  });

  // Add/update menu item mutation
  const menuItemMutation = useMutation({
    mutationFn: async (itemData: Partial<MenuItem>) => {
      const method = itemData.id ? "PATCH" : "POST";
      const url = itemData.id ? `/api/${currentBusiness.id}/menu/${itemData.id}` : `/api/${currentBusiness.id}/menu`;
      const response = await apiRequest(method, url, itemData);
      return response.json();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["/api", currentBusiness.id, "menu"] });
      setShowAddItem(false);
      setEditingItem(null);
      toast({
        title: "Menu Updated",
        description: "Menu item has been saved successfully.",
      });
    },
  });

  // Delete menu item mutation
  const deleteItemMutation = useMutation({
    mutationFn: async (itemId: number) => {
      const response = await apiRequest("DELETE", `/api/${currentBusiness.id}/menu/${itemId}`);
      return response.json();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["/api", currentBusiness.id, "menu"] });
      toast({
        title: "Item Deleted",
        description: "Menu item has been removed.",
      });
    },
  });

  const categories = [
    { value: "all", label: "All Items" },
    ...(currentBusiness.id === 'allii-fish-market' ? [
      { value: "poke-bowls", label: "Poke Bowls" },
      { value: "sides", label: "Sides" },
      { value: "smoked-meats", label: "Smoked Meats" },
      { value: "specials", label: "Daily Specials" }
    ] : [
      { value: "beverages", label: "Coconut Water" },
      { value: "flavored", label: "Flavored" },
      { value: "organic", label: "Organic" },
      { value: "sparkling", label: "Sparkling" }
    ])
  ];

  const filteredItems = menuItems.filter(item => {
    const matchesSearch = item.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         item.description.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = selectedCategory === "all" || item.category === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  const toggleAvailability = (itemId: number, isAvailable: boolean) => {
    menuItemMutation.mutate({ id: itemId, isAvailable: !isAvailable });
  };

  return (
    <div className="flex-1 space-y-4 p-4 pt-6" data-testid="menu-page">
      <div className="flex items-center justify-between space-y-2">
        <h2 className="text-3xl font-bold tracking-tight">Menu Management</h2>
        <div className="flex items-center space-x-2">
          <Button onClick={() => setShowAddItem(true)} data-testid="button-add-menu-item">
            <Plus className="w-4 h-4 mr-2" />
            Add Item
          </Button>
        </div>
      </div>

      {/* Menu Stats */}
      <div className="grid gap-4 md:grid-cols-4">
        <Card data-testid="stat-total-items">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Items</CardTitle>
            <Utensils className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{menuItems.length}</div>
            <p className="text-xs text-muted-foreground">
              {menuItems.filter(i => i.isAvailable).length} available
            </p>
          </CardContent>
        </Card>
        
        <Card data-testid="stat-popular-items">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Popular Items</CardTitle>
            <Star className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{menuItems.filter(i => i.isPopular).length}</div>
            <p className="text-xs text-muted-foreground">
              Customer favorites
            </p>
          </CardContent>
        </Card>
        
        <Card data-testid="stat-avg-price">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Avg Price</CardTitle>
            <DollarSign className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              ${menuItems.length > 0 ? (menuItems.reduce((sum, i) => sum + Number(i.price), 0) / menuItems.length).toFixed(2) : '0.00'}
            </div>
            <p className="text-xs text-muted-foreground">
              Across all categories
            </p>
          </CardContent>
        </Card>
        
        <Card data-testid="stat-avg-prep-time">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Avg Prep Time</CardTitle>
            <Clock className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {menuItems.length > 0 ? Math.round(menuItems.reduce((sum, i) => sum + i.prepTime, 0) / menuItems.length) : 0} min
            </div>
            <p className="text-xs text-muted-foreground">
              Kitchen preparation
            </p>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="items" className="space-y-4">
        <TabsList>
          <TabsTrigger value="items" data-testid="tab-menu-items">Menu Items</TabsTrigger>
          <TabsTrigger value="categories" data-testid="tab-categories">Categories</TabsTrigger>
          <TabsTrigger value="analytics" data-testid="tab-menu-analytics">Analytics</TabsTrigger>
        </TabsList>

        <TabsContent value="items" className="space-y-4">
          {/* Search and Filters */}
          <div className="flex items-center space-x-4">
            <div className="flex items-center space-x-2 flex-1">
              <Search className="w-4 h-4 text-muted-foreground" />
              <Input
                placeholder="Search menu items..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="max-w-sm"
                data-testid="input-search-menu"
              />
            </div>
            <Select value={selectedCategory} onValueChange={setSelectedCategory}>
              <SelectTrigger className="w-48" data-testid="select-category-filter">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {categories.map(category => (
                  <SelectItem key={category.value} value={category.value}>
                    {category.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {/* Menu Items Grid */}
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3" data-testid="menu-items-grid">
            {isLoading ? (
              [1, 2, 3, 4, 5, 6].map((i) => (
                <Card key={i} className="animate-pulse">
                  <CardHeader>
                    <div className="h-4 bg-muted rounded w-3/4"></div>
                    <div className="h-3 bg-muted rounded w-1/2"></div>
                  </CardHeader>
                  <CardContent>
                    <div className="h-16 bg-muted rounded"></div>
                  </CardContent>
                </Card>
              ))
            ) : filteredItems.length > 0 ? (
              filteredItems.map((item) => (
                <Card key={item.id} className={`transition-all hover:shadow-md ${!item.isAvailable ? 'opacity-60' : ''}`} data-testid={`menu-item-${item.id}`}>
                  <CardHeader className="pb-3">
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <CardTitle className="text-lg flex items-center gap-2">
                          {item.name}
                          {item.isPopular && (
                            <Badge variant="secondary" className="bg-yellow-100 text-yellow-800">
                              <Star className="w-3 h-3 mr-1" />
                              Popular
                            </Badge>
                          )}
                        </CardTitle>
                        <div className="flex items-center gap-2 mt-1">
                          <Badge variant="outline">{item.category}</Badge>
                          <span className="text-sm text-muted-foreground">{item.prepTime} min</span>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className="text-lg font-bold text-primary">${Number(item.price).toFixed(2)}</p>
                        <div className="flex items-center space-x-1 mt-1">
                          <Switch
                            checked={item.isAvailable}
                            onCheckedChange={() => toggleAvailability(item.id, item.isAvailable)}
                            size="sm"
                            data-testid={`switch-availability-${item.id}`}
                          />
                          <span className="text-xs text-muted-foreground">
                            {item.isAvailable ? 'Available' : 'Unavailable'}
                          </span>
                        </div>
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent className="pt-0">
                    <p className="text-sm text-muted-foreground mb-3">{item.description}</p>
                    
                    <div className="space-y-2 text-xs">
                      <div>
                        <span className="font-medium">Ingredients: </span>
                        <span className="text-muted-foreground">{item.ingredients.join(', ')}</span>
                      </div>
                      {item.allergens.length > 0 && (
                        <div>
                          <span className="font-medium">Allergens: </span>
                          <span className="text-muted-foreground">{item.allergens.join(', ')}</span>
                        </div>
                      )}
                      {item.calories && (
                        <div>
                          <span className="font-medium">Calories: </span>
                          <span className="text-muted-foreground">{item.calories}</span>
                        </div>
                      )}
                    </div>
                    
                    <div className="flex items-center justify-between mt-4">
                      <div className="flex space-x-2">
                        <Button variant="outline" size="sm" onClick={() => setEditingItem(item)} data-testid={`button-edit-item-${item.id}`}>
                          <Edit className="w-3 h-3 mr-1" />
                          Edit
                        </Button>
                        <Button variant="outline" size="sm" data-testid={`button-view-item-${item.id}`}>
                          <Eye className="w-3 h-3 mr-1" />
                          View
                        </Button>
                      </div>
                      <Button 
                        variant="ghost" 
                        size="sm" 
                        onClick={() => deleteItemMutation.mutate(item.id)}
                        className="text-red-600 hover:text-red-700 hover:bg-red-50"
                        data-testid={`button-delete-item-${item.id}`}
                      >
                        <Trash2 className="w-3 h-3" />
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              ))
            ) : (
              <div className="col-span-full text-center py-8">
                <Utensils className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-medium mb-2">No menu items found</h3>
                <p className="text-muted-foreground mb-4">
                  {searchTerm || selectedCategory !== "all" 
                    ? "Try adjusting your search or filters."
                    : `Start building your ${currentBusiness.name} menu.`}
                </p>
                <Button onClick={() => setShowAddItem(true)} data-testid="button-add-first-item">
                  <Plus className="w-4 h-4 mr-2" />
                  Add Your First Item
                </Button>
              </div>
            )}
          </div>
        </TabsContent>

        <TabsContent value="categories">
          <Card>
            <CardHeader>
              <CardTitle>Menu Categories</CardTitle>
              <CardDescription>Organize your menu items into categories</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid gap-4 md:grid-cols-2">
                {categories.slice(1).map(category => (
                  <Card key={category.value} className="p-4" data-testid={`category-${category.value}`}>
                    <div className="flex items-center justify-between">
                      <div>
                        <h3 className="font-medium">{category.label}</h3>
                        <p className="text-sm text-muted-foreground">
                          {menuItems.filter(item => item.category === category.value).length} items
                        </p>
                      </div>
                      <Button variant="ghost" size="sm">
                        <Edit className="w-4 h-4" />
                      </Button>
                    </div>
                  </Card>
                ))}
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="analytics">
          <Card>
            <CardHeader>
              <CardTitle>Menu Analytics</CardTitle>
              <CardDescription>Performance insights for your menu items</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="text-center py-8">
                <TrendingUp className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-medium mb-2">Menu Analytics</h3>
                <p className="text-muted-foreground">
                  Detailed menu performance analytics coming soon.
                </p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}