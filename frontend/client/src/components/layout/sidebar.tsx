import { Link, useLocation } from "wouter";
import { Fish, ChartPie, Bot, Users, ShoppingCart, Utensils, Package, Megaphone, BarChart, User, LogOut, PenTool } from "lucide-react";
import BusinessSwitcher from "./business-switcher";

const navigationItems = [
  { href: "/dashboard", icon: ChartPie, label: "Dashboard", testId: "nav-dashboard" },
  { href: "/grok-ai", icon: Bot, label: "Grok AI Insights", testId: "nav-grok-ai" },
  { href: "/content-creator", icon: PenTool, label: "Content Creator", testId: "nav-content-creator" },
  { href: "/customers", icon: Users, label: "Customer Management", testId: "nav-customers" },
  { href: "/orders", icon: ShoppingCart, label: "Order Management", testId: "nav-orders" },
  { href: "/menu", icon: Utensils, label: "Menu Management", testId: "nav-menu" },
  { href: "/inventory", icon: Package, label: "Inventory System", testId: "nav-inventory" },
  { href: "/marketing", icon: Megaphone, label: "Marketing Campaigns", testId: "nav-marketing" },
  { href: "/analytics", icon: BarChart, label: "Analytics & Reports", testId: "nav-analytics" },
];

export default function Sidebar() {
  const [location] = useLocation();

  const isActive = (href: string) => {
    if (href === "/dashboard") {
      return location === "/" || location === "/dashboard";
    }
    return location === href;
  };

  return (
    <aside className="w-64 bg-surface shadow-lg border-r border-gray-200 flex flex-col" data-testid="sidebar">
      {/* Header */}
      <div className="p-4 border-b border-gray-200 space-y-4">
        <div className="flex items-center space-x-3">
          <div className="w-10 h-10 bg-primary rounded-lg flex items-center justify-center">
            <Fish className="text-white text-lg" />
          </div>
          <div>
            <h1 className="font-roboto font-bold text-lg text-text">Allii Businesses</h1>
            <p className="text-sm text-text-secondary">Grok AI Marketing</p>
          </div>
        </div>
        
        {/* Business Switcher */}
        <BusinessSwitcher />
      </div>

      {/* Navigation */}
      <nav className="flex-1 p-4 space-y-2">
        {navigationItems.map((item) => (
          <Link
            key={item.href}
            href={item.href}
            data-testid={item.testId}
            className={`nav-item ${
              isActive(item.href) ? "nav-item-active" : "nav-item-inactive"
            }`}
          >
            <item.icon className="w-5 h-5" />
            <span className="font-medium">{item.label}</span>
          </Link>
        ))}
      </nav>

      {/* User Profile */}
      <div className="p-4 border-t border-gray-200">
        <div className="flex items-center space-x-3">
          <div className="w-8 h-8 bg-primary rounded-full flex items-center justify-center">
            <User className="text-white text-sm" />
          </div>
          <div className="flex-1">
            <p className="font-medium text-sm text-text">Manager Admin</p>
            <p className="text-xs text-text-secondary">Operations Manager</p>
          </div>
          <button 
            className="text-text-secondary hover:text-text"
            data-testid="button-logout"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </div>
    </aside>
  );
}
