import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import TopBar from "@/components/layout/topbar";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { 
  LineChart, 
  Line, 
  AreaChart,
  Area,
  BarChart, 
  Bar, 
  PieChart, 
  Pie, 
  Cell,
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer,
  Legend
} from "recharts";
import { 
  BarChart3, 
  TrendingUp, 
  TrendingDown, 
  DollarSign, 
  Users, 
  ShoppingCart, 
  Star,
  Calendar,
  Download,
  Filter,
  RefreshCw,
  Target,
  Clock,
  Percent
} from "lucide-react";
import { useBusinessContext } from "@/hooks/use-business-context";
import { apiRequest } from "@/lib/queryClient";

const COLORS = [
  'hsl(207, 75%, 44%)', // Primary
  'hsl(122, 40%, 39%)', // Secondary  
  'hsl(36, 100%, 49%)', // Accent
  'hsl(4, 86%, 58%)',   // Error
  'hsl(122, 45%, 49%)', // Success
];

export default function Analytics() {
  const [timeframe, setTimeframe] = useState("30");
  const [metric, setMetric] = useState("revenue");
  const { currentBusiness } = useBusinessContext();

  const { data: dashboardMetrics } = useQuery({
    queryKey: ["/api/dashboard/metrics"],
    refetchInterval: 30000,
  });

  const { data: salesData } = useQuery({
    queryKey: ["/api/dashboard/sales-data", { timeframe }],
    refetchInterval: 60000,
  });

  const { data: customerSegments } = useQuery({
    queryKey: ["/api/dashboard/customer-segments"],
    refetchInterval: 300000,
  });

  // Fetch analytics data for current business
  const { data: hourlyTraffic = [] } = useQuery({
    queryKey: ["/api", currentBusiness.id, "analytics", "hourly-traffic"],
    queryFn: () => apiRequest("GET", `/api/${currentBusiness.id}/analytics/hourly-traffic`).then(res => res.json()),
  });

  const { data: channelPerformance = [] } = useQuery({
    queryKey: ["/api", currentBusiness.id, "analytics", "channel-performance"],
    queryFn: () => apiRequest("GET", `/api/${currentBusiness.id}/analytics/channel-performance`).then(res => res.json()),
  });

  const { data: menuPerformance = [] } = useQuery({
    queryKey: ["/api", currentBusiness.id, "analytics", "menu-performance"],
    queryFn: () => apiRequest("GET", `/api/${currentBusiness.id}/analytics/menu-performance`).then(res => res.json()),
  });

  const { data: cohortData = [] } = useQuery({
    queryKey: ["/api", currentBusiness.id, "analytics", "cohort-data"],
    queryFn: () => apiRequest("GET", `/api/${currentBusiness.id}/analytics/cohort-data`).then(res => res.json()),
  });

  const { data: marketingAttribution = [] } = useQuery({
    queryKey: ["/api", currentBusiness.id, "analytics", "marketing-attribution"],
    queryFn: () => apiRequest("GET", `/api/${currentBusiness.id}/analytics/marketing-attribution`).then(res => res.json()),
  });

  const formatCurrency = (value: number) => `$${value.toLocaleString()}`;
  const formatPercent = (value: number) => `${value.toFixed(1)}%`;

  return (
    <>
      <TopBar
        title="Analytics & Reports"
        subtitle="Comprehensive business intelligence and performance insights"
        notificationCount={3}
      />
      
      <div className="flex-1 overflow-auto p-6 bg-background">
        <Tabs defaultValue="overview" className="space-y-6">
          <div className="flex items-center justify-between">
            <TabsList className="grid w-full max-w-md grid-cols-4">
              <TabsTrigger value="overview" data-testid="tab-overview">Overview</TabsTrigger>
              <TabsTrigger value="sales" data-testid="tab-sales">Sales</TabsTrigger>
              <TabsTrigger value="customers" data-testid="tab-customers">Customers</TabsTrigger>
              <TabsTrigger value="marketing" data-testid="tab-marketing">Marketing</TabsTrigger>
            </TabsList>
            <div className="flex items-center space-x-2">
              <Select value={timeframe} onValueChange={setTimeframe}>
                <SelectTrigger className="w-32" data-testid="select-timeframe">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="7">Last 7 days</SelectItem>
                  <SelectItem value="30">Last 30 days</SelectItem>
                  <SelectItem value="90">Last 3 months</SelectItem>
                  <SelectItem value="365">Last year</SelectItem>
                </SelectContent>
              </Select>
              <Button variant="outline" size="sm" data-testid="button-refresh">
                <RefreshCw className="w-4 h-4 mr-2" />
                Refresh
              </Button>
              <Button variant="outline" size="sm" data-testid="button-export">
                <Download className="w-4 h-4 mr-2" />
                Export
              </Button>
            </div>
          </div>

          <TabsContent value="overview" className="space-y-6">
            {/* Key Performance Indicators */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              <Card>
                <CardContent className="p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-text-secondary text-sm font-medium">Total Revenue</p>
                      <p className="text-2xl font-bold text-text mt-1">
                        {dashboardMetrics ? formatCurrency(dashboardMetrics.dailyRevenue * 30) : '$0'}
                      </p>
                      <p className="text-success text-sm mt-1 flex items-center">
                        <TrendingUp className="w-4 h-4 mr-1" />
                        +12.5% vs last period
                      </p>
                    </div>
                    <DollarSign className="w-8 h-8 text-primary" />
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-text-secondary text-sm font-medium">Total Orders</p>
                      <p className="text-2xl font-bold text-text mt-1">
                        {dashboardMetrics ? (dashboardMetrics.ordersToday * 30).toLocaleString() : '0'}
                      </p>
                      <p className="text-success text-sm mt-1 flex items-center">
                        <TrendingUp className="w-4 h-4 mr-1" />
                        +8.3% vs last period
                      </p>
                    </div>
                    <ShoppingCart className="w-8 h-8 text-success" />
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-text-secondary text-sm font-medium">Avg Order Value</p>
                      <p className="text-2xl font-bold text-text mt-1">$24.50</p>
                      <p className="text-warning text-sm mt-1 flex items-center">
                        <TrendingDown className="w-4 h-4 mr-1" />
                        -2.1% vs last period
                      </p>
                    </div>
                    <Target className="w-8 h-8 text-accent" />
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-text-secondary text-sm font-medium">Customer LTV</p>
                      <p className="text-2xl font-bold text-text mt-1">$187.50</p>
                      <p className="text-success text-sm mt-1 flex items-center">
                        <TrendingUp className="w-4 h-4 mr-1" />
                        +15.2% vs last period
                      </p>
                    </div>
                    <Star className="w-8 h-8 text-primary" />
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* Traffic and Orders by Hour */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <Card>
                <CardHeader>
                  <CardTitle className="font-roboto">Daily Traffic Patterns</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="h-80">
                    <ResponsiveContainer width="100%" height="100%">
                      <AreaChart data={hourlyTraffic}>
                        <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                        <XAxis 
                          dataKey="hour" 
                          tick={{ fontSize: 12, fill: '#6b7280' }}
                          axisLine={false}
                          tickLine={false}
                        />
                        <YAxis 
                          tick={{ fontSize: 12, fill: '#6b7280' }}
                          axisLine={false}
                          tickLine={false}
                        />
                        <Tooltip 
                          contentStyle={{ 
                            backgroundColor: 'white', 
                            border: '1px solid #e5e7eb',
                            borderRadius: '8px'
                          }}
                        />
                        <Area 
                          type="monotone" 
                          dataKey="visitors" 
                          stackId="1"
                          stroke="hsl(207, 75%, 44%)" 
                          fill="hsl(207, 75%, 44%)"
                          fillOpacity={0.3}
                        />
                        <Area 
                          type="monotone" 
                          dataKey="orders" 
                          stackId="2"
                          stroke="hsl(122, 40%, 39%)" 
                          fill="hsl(122, 40%, 39%)"
                          fillOpacity={0.6}
                        />
                      </AreaChart>
                    </ResponsiveContainer>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle className="font-roboto">Order Channel Performance</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {channelPerformance.map((channel, index) => {
                      const maxRevenue = Math.max(...channelPerformance.map(c => c.revenue));
                      const progress = (channel.revenue / maxRevenue) * 100;
                      
                      return (
                        <div key={channel.channel} className="space-y-2">
                          <div className="flex items-center justify-between">
                            <span className="font-medium">{channel.channel}</span>
                            <div className="text-right">
                              <p className="font-bold">{formatCurrency(channel.revenue)}</p>
                              <p className="text-sm text-text-secondary">{channel.orders} orders</p>
                            </div>
                          </div>
                          <Progress value={progress} className="h-2" />
                          <div className="flex justify-between text-sm text-text-secondary">
                            <span>Avg Order: {formatCurrency(channel.avgOrder)}</span>
                            <span>Market Share: {((channel.revenue / channelPerformance.reduce((sum, c) => sum + c.revenue, 0)) * 100).toFixed(1)}%</span>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </CardContent>
              </Card>
            </div>
          </TabsContent>

          <TabsContent value="sales" className="space-y-6">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {/* Revenue Trend */}
              <Card>
                <CardHeader>
                  <CardTitle className="font-roboto">Revenue Trend</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="h-80">
                    <ResponsiveContainer width="100%" height="100%">
                      <LineChart data={salesData || []}>
                        <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                        <XAxis 
                          dataKey="date" 
                          tick={{ fontSize: 12, fill: '#6b7280' }}
                          axisLine={false}
                          tickLine={false}
                        />
                        <YAxis 
                          tick={{ fontSize: 12, fill: '#6b7280' }}
                          axisLine={false}
                          tickLine={false}
                          tickFormatter={(value) => `$${value}`}
                        />
                        <Tooltip 
                          formatter={(value: number) => [`$${value.toLocaleString()}`, 'Revenue']}
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
                </CardContent>
              </Card>

              {/* Top Menu Items */}
              <Card>
                <CardHeader>
                  <CardTitle className="font-roboto">Top Selling Items</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {menuPerformance.map((item, index) => (
                      <div key={item.item} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                        <div className="flex items-center space-x-3">
                          <div className="w-8 h-8 bg-primary rounded-full flex items-center justify-center text-white font-bold text-sm">
                            {index + 1}
                          </div>
                          <div>
                            <p className="font-medium">{item.item}</p>
                            <p className="text-sm text-text-secondary">{item.orders} orders</p>
                          </div>
                        </div>
                        <div className="text-right">
                          <p className="font-bold">{formatCurrency(item.revenue)}</p>
                          <div className="flex items-center">
                            <Progress value={item.popularity} className="h-1 w-16 mr-2" />
                            <span className="text-xs text-text-secondary">{item.popularity}%</span>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* Sales by Time Period */}
            <Card>
              <CardHeader>
                <CardTitle className="font-roboto">Sales Performance by Day</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="h-80">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={salesData || []}>
                      <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                      <XAxis 
                        dataKey="date" 
                        tick={{ fontSize: 12, fill: '#6b7280' }}
                        axisLine={false}
                        tickLine={false}
                      />
                      <YAxis 
                        yAxisId="revenue"
                        orientation="left"
                        tick={{ fontSize: 12, fill: '#6b7280' }}
                        axisLine={false}
                        tickLine={false}
                        tickFormatter={(value) => `$${value}`}
                      />
                      <YAxis 
                        yAxisId="orders"
                        orientation="right"
                        tick={{ fontSize: 12, fill: '#6b7280' }}
                        axisLine={false}
                        tickLine={false}
                      />
                      <Tooltip 
                        formatter={(value: number, name: string) => [
                          name === 'revenue' ? `$${value.toLocaleString()}` : value,
                          name === 'revenue' ? 'Revenue' : 'Orders'
                        ]}
                        contentStyle={{ 
                          backgroundColor: 'white', 
                          border: '1px solid #e5e7eb',
                          borderRadius: '8px'
                        }}
                      />
                      <Bar 
                        yAxisId="revenue"
                        dataKey="revenue" 
                        fill="hsl(207, 75%, 44%)" 
                        radius={[4, 4, 0, 0]}
                      />
                      <Bar 
                        yAxisId="orders"
                        dataKey="orderCount" 
                        fill="hsl(122, 40%, 39%)" 
                        radius={[4, 4, 0, 0]}
                      />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="customers" className="space-y-6">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {/* Customer Segments */}
              <Card>
                <CardHeader>
                  <CardTitle className="font-roboto">Customer Segments</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="h-80">
                    <ResponsiveContainer width="100%" height="100%">
                      <PieChart>
                        <Pie
                          data={customerSegments || []}
                          cx="50%"
                          cy="50%"
                          innerRadius={60}
                          outerRadius={120}
                          paddingAngle={2}
                          dataKey="percentage"
                        >
                          {(customerSegments || []).map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                          ))}
                        </Pie>
                        <Tooltip 
                          formatter={(value: number, name: string, props: any) => [
                            `${value}% (${props.payload.count})`,
                            props.payload.segment
                          ]}
                          contentStyle={{ 
                            backgroundColor: 'white', 
                            border: '1px solid #e5e7eb',
                            borderRadius: '8px'
                          }}
                        />
                        <Legend />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>
                </CardContent>
              </Card>

              {/* Customer Retention */}
              <Card>
                <CardHeader>
                  <CardTitle className="font-roboto">Customer Retention</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="h-80">
                    <ResponsiveContainer width="100%" height="100%">
                      <LineChart data={cohortData}>
                        <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                        <XAxis 
                          dataKey="month" 
                          tick={{ fontSize: 12, fill: '#6b7280' }}
                          axisLine={false}
                          tickLine={false}
                        />
                        <YAxis 
                          tick={{ fontSize: 12, fill: '#6b7280' }}
                          axisLine={false}
                          tickLine={false}
                          tickFormatter={(value) => `${value}%`}
                        />
                        <Tooltip 
                          formatter={(value: number) => [`${value.toFixed(1)}%`, 'Retention Rate']}
                          contentStyle={{ 
                            backgroundColor: 'white', 
                            border: '1px solid #e5e7eb',
                            borderRadius: '8px'
                          }}
                        />
                        <Line 
                          type="monotone" 
                          dataKey="retention" 
                          stroke="hsl(122, 45%, 49%)" 
                          strokeWidth={3}
                          dot={{ fill: "hsl(122, 45%, 49%)", strokeWidth: 2, r: 4 }}
                          activeDot={{ r: 6, stroke: "hsl(122, 45%, 49%)", strokeWidth: 2 }}
                        />
                      </LineChart>
                    </ResponsiveContainer>
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* Customer Acquisition */}
            <Card>
              <CardHeader>
                <CardTitle className="font-roboto">Customer Acquisition Trends</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="h-80">
                  <ResponsiveContainer width="100%" height="100%">
                    <AreaChart data={cohortData}>
                      <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                      <XAxis 
                        dataKey="month" 
                        tick={{ fontSize: 12, fill: '#6b7280' }}
                        axisLine={false}
                        tickLine={false}
                      />
                      <YAxis 
                        tick={{ fontSize: 12, fill: '#6b7280' }}
                        axisLine={false}
                        tickLine={false}
                      />
                      <Tooltip 
                        contentStyle={{ 
                          backgroundColor: 'white', 
                          border: '1px solid #e5e7eb',
                          borderRadius: '8px'
                        }}
                      />
                      <Area 
                        type="monotone" 
                        dataKey="newCustomers" 
                        stackId="1"
                        stroke="hsl(207, 75%, 44%)" 
                        fill="hsl(207, 75%, 44%)"
                        fillOpacity={0.6}
                      />
                      <Area 
                        type="monotone" 
                        dataKey="returning" 
                        stackId="1"
                        stroke="hsl(122, 40%, 39%)" 
                        fill="hsl(122, 40%, 39%)"
                        fillOpacity={0.6}
                      />
                    </AreaChart>
                  </ResponsiveContainer>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="marketing" className="space-y-6">
            {/* Marketing Attribution */}
            <Card>
              <CardHeader>
                <CardTitle className="font-roboto">Marketing Attribution & ROI</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {marketingAttribution.map((source, index) => {
                    const roi = source.roi === Infinity ? 'N/A' : `${source.roi.toFixed(0)}%`;
                    const roiColor = source.roi >= 200 ? 'text-success' : source.roi >= 100 ? 'text-warning' : 'text-error';
                    
                    return (
                      <div key={source.source} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                        <div className="flex items-center space-x-3">
                          <div className={`w-3 h-3 rounded-full`} style={{ backgroundColor: COLORS[index % COLORS.length] }}></div>
                          <div>
                            <p className="font-medium">{source.source}</p>
                            <p className="text-sm text-text-secondary">{source.conversions} conversions</p>
                          </div>
                        </div>
                        <div className="text-right space-y-1">
                          <div className="flex items-center space-x-4">
                            <div className="text-center">
                              <p className="text-sm text-text-secondary">Revenue</p>
                              <p className="font-bold">{formatCurrency(source.revenue)}</p>
                            </div>
                            <div className="text-center">
                              <p className="text-sm text-text-secondary">Cost</p>
                              <p className="font-medium">{formatCurrency(source.cost)}</p>
                            </div>
                            <div className="text-center">
                              <p className="text-sm text-text-secondary">ROI</p>
                              <p className={`font-bold ${roiColor}`}>{roi}</p>
                            </div>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </CardContent>
            </Card>

            {/* Campaign Performance Chart */}
            <Card>
              <CardHeader>
                <CardTitle className="font-roboto">Campaign Performance Over Time</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="h-80">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={marketingAttribution}>
                      <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                      <XAxis 
                        dataKey="source" 
                        tick={{ fontSize: 12, fill: '#6b7280' }}
                        axisLine={false}
                        tickLine={false}
                        angle={-45}
                        textAnchor="end"
                        height={80}
                      />
                      <YAxis 
                        yAxisId="conversions"
                        orientation="left"
                        tick={{ fontSize: 12, fill: '#6b7280' }}
                        axisLine={false}
                        tickLine={false}
                      />
                      <YAxis 
                        yAxisId="revenue"
                        orientation="right"
                        tick={{ fontSize: 12, fill: '#6b7280' }}
                        axisLine={false}
                        tickLine={false}
                        tickFormatter={(value) => `$${value}`}
                      />
                      <Tooltip 
                        formatter={(value: number, name: string) => [
                          name === 'revenue' ? `$${value.toLocaleString()}` : value,
                          name === 'revenue' ? 'Revenue' : 'Conversions'
                        ]}
                        contentStyle={{ 
                          backgroundColor: 'white', 
                          border: '1px solid #e5e7eb',
                          borderRadius: '8px'
                        }}
                      />
                      <Bar 
                        yAxisId="conversions"
                        dataKey="conversions" 
                        fill="hsl(207, 75%, 44%)" 
                        radius={[4, 4, 0, 0]}
                      />
                      <Bar 
                        yAxisId="revenue"
                        dataKey="revenue" 
                        fill="hsl(122, 40%, 39%)" 
                        radius={[4, 4, 0, 0]}
                      />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </>
  );
}
