import { useQuery } from "@tanstack/react-query";
import { AlertCircle, AlertTriangle, CheckCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useToast } from "@/hooks/use-toast";
import type { Inventory } from "@shared/schema";

export default function InventoryAlerts() {
  const { toast } = useToast();
  
  const { data: lowStockItems, isLoading } = useQuery<Inventory[]>({
    queryKey: ["/api/inventory/low-stock"],
    refetchInterval: 30000, // Refetch every 30 seconds
  });

  const { data: allInventory } = useQuery<Inventory[]>({
    queryKey: ["/api/inventory"],
  });

  const handleReorder = async (itemId: number, itemName: string) => {
    try {
      // TODO: Implement reorder logic
      toast({
        title: "Reorder Initiated",
        description: `Reorder request sent for ${itemName}`,
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to initiate reorder. Please try again.",
        variant: "destructive",
      });
    }
  };

  const getAlertType = (item: Inventory) => {
    const stockRatio = Number(item.stockQuantity) / Number(item.reorderLevel);
    if (stockRatio <= 0.5) return "critical";
    if (stockRatio <= 1) return "warning";
    return "good";
  };

  if (isLoading) {
    return (
      <div className="bg-surface rounded-xl shadow-sm border border-gray-200">
        <div className="p-6 border-b border-gray-200">
          <h3 className="font-roboto font-bold text-lg text-text">Inventory Alerts</h3>
        </div>
        <div className="p-6 space-y-4">
          {[...Array(3)].map((_, i) => (
            <div key={i} className="p-3 rounded-lg border animate-pulse">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <div className="w-5 h-5 bg-gray-200 rounded"></div>
                  <div className="space-y-1">
                    <div className="h-4 bg-gray-200 rounded w-20"></div>
                    <div className="h-3 bg-gray-200 rounded w-16"></div>
                  </div>
                </div>
                <div className="h-6 bg-gray-200 rounded w-16"></div>
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  // Show mix of low stock and some good stock items
  const alertItems = [
    ...(lowStockItems || []),
    ...(allInventory?.filter(item => 
      !lowStockItems?.find(low => low.id === item.id) &&
      Number(item.stockQuantity) > Number(item.reorderLevel)
    ).slice(0, 2) || [])
  ].slice(0, 5);

  return (
    <div className="bg-surface rounded-xl shadow-sm border border-gray-200" data-testid="inventory-alerts">
      <div className="p-6 border-b border-gray-200">
        <h3 className="font-roboto font-bold text-lg text-text">Inventory Alerts</h3>
      </div>
      <div className="p-6 space-y-4">
        {alertItems.length === 0 ? (
          <div className="text-center py-8">
            <CheckCircle className="w-12 h-12 text-success mx-auto mb-4" />
            <p className="text-text-secondary">All inventory levels are good</p>
          </div>
        ) : (
          alertItems.map((item) => {
            const alertType = getAlertType(item);
            const icon = alertType === "critical" ? AlertCircle : 
                        alertType === "warning" ? AlertTriangle : CheckCircle;
            const alertClass = alertType === "critical" ? "inventory-alert-critical" :
                             alertType === "warning" ? "inventory-alert-warning" : "inventory-alert-good";
            const iconColor = alertType === "critical" ? "text-error" :
                             alertType === "warning" ? "text-warning" : "text-success";
            
            const Icon = icon;
            
            return (
              <div 
                key={item.id} 
                className={`flex items-center justify-between p-3 ${alertClass}`}
                data-testid={`inventory-alert-${item.id}`}
              >
                <div className="flex items-center space-x-3">
                  <Icon className={iconColor} />
                  <div>
                    <p className="font-medium text-sm text-text" data-testid={`inventory-${item.id}-name`}>
                      {item.ingredientName}
                    </p>
                    <p className="text-xs text-text-secondary" data-testid={`inventory-${item.id}-quantity`}>
                      {alertType === "good" ? "Well stocked" : 
                       `Only ${Number(item.stockQuantity)} ${item.unit} remaining`}
                    </p>
                  </div>
                </div>
                {alertType === "good" ? (
                  <span className="text-success text-xs font-medium">Good</span>
                ) : (
                  <Button
                    size="sm"
                    variant={alertType === "critical" ? "destructive" : "default"}
                    className={alertType === "critical" ? "bg-error hover:bg-error/90" : "bg-warning hover:bg-warning/90"}
                    onClick={() => handleReorder(item.id, item.ingredientName)}
                    data-testid={`button-reorder-${item.id}`}
                  >
                    Reorder
                  </Button>
                )}
              </div>
            );
          })
        )}

        <Button 
          className="w-full mt-4 bg-primary text-white hover:bg-primary/90"
          data-testid="button-view-all-inventory"
        >
          View All Inventory
        </Button>
      </div>
    </div>
  );
}
