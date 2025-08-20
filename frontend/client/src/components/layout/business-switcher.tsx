import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { 
  DropdownMenu, 
  DropdownMenuContent, 
  DropdownMenuItem, 
  DropdownMenuTrigger,
  DropdownMenuSeparator,
  DropdownMenuLabel
} from "@/components/ui/dropdown-menu";
import { Badge } from "@/components/ui/badge";
import { Fish, Apple, ChevronDown, Building2, MapPin, Users, TrendingUp } from "lucide-react";

interface Business {
  id: string;
  name: string;
  type: string;
  icon: any;
  location: string;
  description: string;
  metrics: {
    revenue: string;
    customers: string;
    growth: string;
  };
  color: string;
}

const businesses: Business[] = [
  {
    id: "allii-fish-market",
    name: "Allii Fish Market",
    type: "Hawaiian Poke Shop",
    icon: Fish,
    location: "Honolulu, Hawaii",
    description: "Premium poke bowls, smoked meats, and authentic Hawaiian cuisine",
    metrics: {
      revenue: "$42.3K",
      customers: "1.2K",
      growth: "+18%"
    },
    color: "blue"
  },
  {
    id: "allii-coconut-water",
    name: "Allii Coconut Water",
    type: "Beverage Company",
    icon: Apple,
    location: "Hawaii & Mainland",
    description: "Organic coconut water and tropical beverages",
    metrics: {
      revenue: "$127.8K",
      customers: "3.8K",
      growth: "+24%"
    },
    color: "green"
  }
];

export default function BusinessSwitcher() {
  const [selectedBusiness, setSelectedBusiness] = useState<Business>(businesses[0]);
  const [isOpen, setIsOpen] = useState(false);

  const handleBusinessSwitch = (business: Business) => {
    setSelectedBusiness(business);
    setIsOpen(false);
    
    // Store in localStorage for persistence
    localStorage.setItem('selectedBusiness', business.id);
    
    // Trigger a custom event to notify other components
    window.dispatchEvent(new CustomEvent('businessChanged', { 
      detail: business 
    }));
  };

  const BusinessCard = ({ business, isSelected = false }: { business: Business; isSelected?: boolean }) => {
    const IconComponent = business.icon;
    
    return (
      <Card className={`cursor-pointer transition-all hover:shadow-md ${isSelected ? 'ring-2 ring-primary' : ''}`}>
        <CardContent className="p-4">
          <div className="flex items-start space-x-3">
            <div className={`w-12 h-12 rounded-lg flex items-center justify-center ${
              business.color === 'blue' ? 'bg-blue-100 text-blue-600' : 'bg-green-100 text-green-600'
            }`}>
              <IconComponent className="w-6 h-6" />
            </div>
            
            <div className="flex-1 min-w-0">
              <div className="flex items-center justify-between">
                <h3 className="font-semibold text-sm truncate">{business.name}</h3>
                {isSelected && (
                  <Badge variant="secondary" className="text-xs">Active</Badge>
                )}
              </div>
              
              <p className="text-xs text-muted-foreground mb-2">{business.type}</p>
              
              <div className="flex items-center text-xs text-muted-foreground mb-3">
                <MapPin className="w-3 h-3 mr-1" />
                {business.location}
              </div>
              
              <p className="text-xs text-muted-foreground mb-3 line-clamp-2">
                {business.description}
              </p>
              
              <div className="grid grid-cols-3 gap-2 text-xs">
                <div className="text-center">
                  <p className="font-medium">{business.metrics.revenue}</p>
                  <p className="text-muted-foreground">Revenue</p>
                </div>
                <div className="text-center">
                  <p className="font-medium">{business.metrics.customers}</p>
                  <p className="text-muted-foreground">Customers</p>
                </div>
                <div className="text-center">
                  <p className={`font-medium ${business.metrics.growth.startsWith('+') ? 'text-green-600' : 'text-red-600'}`}>
                    {business.metrics.growth}
                  </p>
                  <p className="text-muted-foreground">Growth</p>
                </div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    );
  };

  const IconComponent = selectedBusiness.icon;

  return (
    <DropdownMenu open={isOpen} onOpenChange={setIsOpen}>
      <DropdownMenuTrigger asChild>
        <Button 
          variant="outline" 
          className="flex items-center justify-between w-full max-w-sm h-auto p-3"
          data-testid="business-switcher-trigger"
        >
          <div className="flex items-center space-x-3">
            <div className={`w-8 h-8 rounded-lg flex items-center justify-center ${
              selectedBusiness.color === 'blue' ? 'bg-blue-100 text-blue-600' : 'bg-green-100 text-green-600'
            }`}>
              <IconComponent className="w-4 h-4" />
            </div>
            <div className="text-left">
              <p className="font-medium text-sm">{selectedBusiness.name}</p>
              <p className="text-xs text-muted-foreground">{selectedBusiness.type}</p>
            </div>
          </div>
          <ChevronDown className="w-4 h-4 text-muted-foreground" />
        </Button>
      </DropdownMenuTrigger>
      
      <DropdownMenuContent className="w-96 p-2" align="start" data-testid="business-switcher-content">
        <DropdownMenuLabel className="flex items-center gap-2 px-2 py-2">
          <Building2 className="w-4 h-4" />
          Switch Business Context
        </DropdownMenuLabel>
        <DropdownMenuSeparator />
        
        <div className="space-y-2 p-2">
          {businesses.map((business) => (
            <div
              key={business.id}
              onClick={() => handleBusinessSwitch(business)}
              data-testid={`business-option-${business.id}`}
            >
              <BusinessCard 
                business={business} 
                isSelected={business.id === selectedBusiness.id}
              />
            </div>
          ))}
        </div>
        
        <DropdownMenuSeparator />
        
        <DropdownMenuItem className="flex items-center gap-2 text-xs text-muted-foreground">
          <TrendingUp className="w-3 h-3" />
          All businesses powered by Grok AI Marketing
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}