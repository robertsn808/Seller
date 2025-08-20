import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis, CartesianGrid } from "recharts";

interface SalesData {
  date: string;
  revenue: number;
  orderCount: number;
}

export default function SalesChart() {
  const [timeframe, setTimeframe] = useState("7");
  
  const { data: salesData, isLoading } = useQuery<SalesData[]>({
    queryKey: ["/api/dashboard/sales-data", { timeframe }],
    refetchInterval: 60000, // Refetch every minute
  });

  if (isLoading) {
    return (
      <div className="bg-surface rounded-xl shadow-sm border border-gray-200">
        <div className="p-6 border-b border-gray-200">
          <div className="flex items-center justify-between">
            <h3 className="font-roboto font-bold text-lg text-text">Sales Performance</h3>
            <div className="w-32 h-8 bg-gray-200 rounded animate-pulse"></div>
          </div>
        </div>
        <div className="p-6">
          <div className="chart-container bg-gray-100 rounded animate-pulse"></div>
        </div>
      </div>
    );
  }

  // Format data for chart
  const chartData = salesData?.map(item => ({
    date: new Date(item.date).toLocaleDateString('en-US', { 
      month: 'short', 
      day: 'numeric' 
    }),
    revenue: Number(item.revenue),
    orders: item.orderCount
  })) || [];

  // Generate mock data if no real data
  const mockData = [
    { date: 'Mon', revenue: 2100, orders: 85 },
    { date: 'Tue', revenue: 2300, orders: 92 },
    { date: 'Wed', revenue: 2800, orders: 110 },
    { date: 'Thu', revenue: 2650, orders: 105 },
    { date: 'Fri', revenue: 3100, orders: 125 },
    { date: 'Sat', revenue: 3400, orders: 135 },
    { date: 'Sun', revenue: 2850, orders: 115 },
  ];

  const displayData = chartData.length > 0 ? chartData : mockData;

  return (
    <div className="bg-surface rounded-xl shadow-sm border border-gray-200" data-testid="sales-chart">
      <div className="p-6 border-b border-gray-200">
        <div className="flex items-center justify-between">
          <h3 className="font-roboto font-bold text-lg text-text">Sales Performance</h3>
          <Select value={timeframe} onValueChange={setTimeframe}>
            <SelectTrigger className="w-32" data-testid="select-timeframe">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="7">Last 7 days</SelectItem>
              <SelectItem value="30">Last 30 days</SelectItem>
              <SelectItem value="90">Last 3 months</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>
      <div className="p-6">
        <div className="chart-container">
          <ResponsiveContainer width="100%" height={200}>
            <LineChart data={displayData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
              <XAxis 
                dataKey="date" 
                axisLine={false}
                tickLine={false}
                tick={{ fontSize: 12, fill: '#6b7280' }}
              />
              <YAxis 
                axisLine={false}
                tickLine={false}
                tick={{ fontSize: 12, fill: '#6b7280' }}
                tickFormatter={(value) => `$${value}`}
              />
              <Tooltip 
                formatter={(value: number, name: string) => [
                  name === 'revenue' ? `$${value.toLocaleString()}` : value,
                  name === 'revenue' ? 'Revenue' : 'Orders'
                ]}
                labelStyle={{ color: '#374151' }}
                contentStyle={{ 
                  backgroundColor: 'white', 
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px'
                }}
              />
              <Line 
                type="monotone" 
                dataKey="revenue" 
                stroke="hsl(207, 75%, 44%)" 
                strokeWidth={3}
                dot={{ fill: "hsl(207, 75%, 44%)", strokeWidth: 2, r: 4 }}
                activeDot={{ r: 6, stroke: "hsl(207, 75%, 44%)", strokeWidth: 2 }}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}
