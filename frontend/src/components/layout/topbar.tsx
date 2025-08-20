import { Bell } from "lucide-react";
import { Button } from "@/components/ui/button";

interface TopBarProps {
  title: string;
  subtitle: string;
  notificationCount?: number;
  showLiveSync?: boolean;
}

export default function TopBar({ 
  title, 
  subtitle, 
  notificationCount = 0,
  showLiveSync = true 
}: TopBarProps) {
  return (
    <header className="bg-surface border-b border-gray-200 px-6 py-4">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="font-roboto font-bold text-2xl text-text">{title}</h2>
          <p className="text-text-secondary mt-1">{subtitle}</p>
        </div>
        <div className="flex items-center space-x-4">
          {/* Notification Bell */}
          <Button 
            variant="ghost" 
            size="icon"
            className="relative text-text-secondary hover:text-text"
            data-testid="button-notifications"
          >
            <Bell className="w-5 h-5" />
            {notificationCount > 0 && (
              <span 
                className="absolute -top-1 -right-1 w-5 h-5 bg-error text-white text-xs rounded-full flex items-center justify-center"
                data-testid="text-notification-count"
              >
                {notificationCount}
              </span>
            )}
          </Button>

          {/* Live Sync Status */}
          {showLiveSync && (
            <div className="flex items-center space-x-2 px-3 py-1 bg-success/10 text-success rounded-full">
              <div className="w-2 h-2 bg-success rounded-full animate-pulse-dot"></div>
              <span className="text-sm font-medium">Live Sync</span>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
