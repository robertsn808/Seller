import { Switch, Route } from "wouter";
import { queryClient } from "./lib/queryClient";
import { QueryClientProvider } from "@tanstack/react-query";
import { Toaster } from "@/components/ui/toaster";
import { TooltipProvider } from "@/components/ui/tooltip";
import { useEffect } from "react";
import { wsManager } from "./lib/websocket";
import { BusinessProvider } from "@/hooks/use-business-context";

// Pages
import Dashboard from "@/pages/dashboard";
import GrokAI from "@/pages/grok-ai";
import Customers from "@/pages/customers";
import Orders from "@/pages/orders";
import Menu from "@/pages/menu";
import Inventory from "@/pages/inventory";
import Marketing from "@/pages/marketing";
import Analytics from "@/pages/analytics";
import ContentCreator from "@/pages/content-creator";
import NotFound from "@/pages/not-found";

// Layout
import Sidebar from "@/components/layout/sidebar";

function Router() {
  return (
    <div className="flex h-screen bg-background">
      <Sidebar />
      <main className="flex-1 flex flex-col overflow-hidden">
        <Switch>
          <Route path="/" component={Dashboard} />
          <Route path="/dashboard" component={Dashboard} />
          <Route path="/grok-ai" component={GrokAI} />
          <Route path="/customers" component={Customers} />
          <Route path="/orders" component={Orders} />
          <Route path="/menu" component={Menu} />
          <Route path="/inventory" component={Inventory} />
          <Route path="/marketing" component={Marketing} />
          <Route path="/analytics" component={Analytics} />
          <Route path="/content-creator" component={ContentCreator} />
          <Route component={NotFound} />
        </Switch>
      </main>
    </div>
  );
}

function App() {
  useEffect(() => {
    wsManager.connect();
    
    return () => {
      wsManager.disconnect();
    };
  }, []);

  return (
    <QueryClientProvider client={queryClient}>
      <BusinessProvider>
        <TooltipProvider>
          <Toaster />
          <Router />
        </TooltipProvider>
      </BusinessProvider>
    </QueryClientProvider>
  );
}

export default App;
