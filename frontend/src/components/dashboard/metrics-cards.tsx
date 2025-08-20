import { DollarSign, Users, ShoppingCart, Brain, TrendingUp, TrendingDown } from "lucide-react";
import { useQuery } from "@tanstack/react-query";

interface MetricsData {
  dailyRevenue: number;
  yesterdayRevenue: number;
  activeCustomers: number;
  ordersToday: number;
  aiInsights: number;
}

export default function MetricsCards() {
  const { data: metrics, isLoading } = useQuery<MetricsData>({
    queryKey: ["/api/dashboard/metrics"],
    refetchInterval: 30000, // Refetch every 30 seconds
  });

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        {[...Array(4)].map((_, i) => (
          <div key={i} className="metric-card animate-pulse">
            <div className="h-6 bg-gray-200 rounded mb-2"></div>
            <div className="h-8 bg-gray-200 rounded mb-2"></div>
            <div className="h-4 bg-gray-200 rounded"></div>
          </div>
        ))}
      </div>
    );
  }

  if (!metrics) return null;

  const dailyRev = Number(metrics.dailyRevenue) || 0;
  const yesterdayRev = Number(metrics.yesterdayRevenue) || 0;
  const revenueChange = yesterdayRev > 0 
    ? ((dailyRev - yesterdayRev) / yesterdayRev * 100)
    : 0;

  const metricCards = [
    {
      title: "Daily Revenue",
      value: `$${Number(metrics.dailyRevenue).toLocaleString()}`,
      change: `${revenueChange >= 0 ? '+' : ''}${revenueChange.toFixed(1)}%`,
      changeLabel: "vs yesterday",
      icon: DollarSign,
      iconBg: "bg-primary/10",
      iconColor: "text-primary",
      trend: revenueChange >= 0 ? "up" : "down",
      testId: "metric-revenue"
    },
    {
      title: "Active Customers",
      value: Number(metrics.activeCustomers).toLocaleString(),
      change: "+8.3%",
      changeLabel: "this week",
      icon: Users,
      iconBg: "bg-secondary/10",
      iconColor: "text-secondary",
      trend: "up",
      testId: "metric-customers"
    },
    {
      title: "Orders Today",
      value: Number(metrics.ordersToday).toString(),
      change: "-2.1%",
      changeLabel: "vs yesterday",
      icon: ShoppingCart,
      iconBg: "bg-accent/10",
      iconColor: "text-accent",
      trend: "down",
      testId: "metric-orders"
    },
    {
      title: "AI Insights",
      value: Number(metrics.aiInsights).toString(),
      change: "5 new",
      changeLabel: "today",
      icon: Brain,
      iconBg: "bg-primary/10",
      iconColor: "text-primary",
      trend: "up",
      testId: "metric-insights"
    }
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      {metricCards.map((metric) => (
        <div key={metric.title} className="metric-card" data-testid={metric.testId}>
          <div className="flex items-center justify-between">
            <div>
              <p className="text-text-secondary text-sm font-medium">{metric.title}</p>
              <p className="text-2xl font-bold text-text mt-1" data-testid={`${metric.testId}-value`}>
                {metric.value}
              </p>
              <p className={`text-sm mt-1 flex items-center ${
                metric.trend === "up" ? "text-success" : metric.trend === "down" ? "text-warning" : "text-primary"
              }`}>
                {metric.trend === "up" ? (
                  <TrendingUp className="w-4 h-4 mr-1" />
                ) : metric.trend === "down" ? (
                  <TrendingDown className="w-4 h-4 mr-1" />
                ) : (
                  <Brain className="w-4 h-4 mr-1" />
                )}
                <span data-testid={`${metric.testId}-change`}>{metric.change}</span> {metric.changeLabel}
              </p>
            </div>
            <div className={`w-12 h-12 ${metric.iconBg} rounded-lg flex items-center justify-center`}>
              <metric.icon className={`${metric.iconColor} text-xl`} />
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
