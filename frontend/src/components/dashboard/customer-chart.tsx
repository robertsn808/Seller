import { useQuery } from "@tanstack/react-query";
import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from "recharts";

interface CustomerSegment {
  segment: string;
  count: number;
  percentage: number;
}

const COLORS = [
  'hsl(207, 75%, 44%)', // Primary
  'hsl(122, 40%, 39%)', // Secondary  
  'hsl(36, 100%, 49%)', // Accent
  'hsl(4, 86%, 58%)',   // Error
];

export default function CustomerChart() {
  const { data: segments, isLoading } = useQuery<CustomerSegment[]>({
    queryKey: ["/api/dashboard/customer-segments"],
    refetchInterval: 300000, // Refetch every 5 minutes
  });

  if (isLoading) {
    return (
      <div className="bg-surface rounded-xl shadow-sm border border-gray-200">
        <div className="p-6 border-b border-gray-200">
          <h3 className="font-roboto font-bold text-lg text-text">Customer Segments</h3>
        </div>
        <div className="p-6">
          <div className="chart-container bg-gray-100 rounded animate-pulse"></div>
        </div>
      </div>
    );
  }

  // Mock data if no real data available
  const mockData = [
    { segment: 'Locals', count: 45, percentage: 45 },
    { segment: 'Tourists', count: 25, percentage: 25 },
    { segment: 'Repeat Customers', count: 20, percentage: 20 },
    { segment: 'New Customers', count: 10, percentage: 10 }
  ];

  const displayData = segments && segments.length > 0 ? segments : mockData;

  return (
    <div className="bg-surface rounded-xl shadow-sm border border-gray-200" data-testid="customer-chart">
      <div className="p-6 border-b border-gray-200">
        <h3 className="font-roboto font-bold text-lg text-text">Customer Segments</h3>
      </div>
      <div className="p-6">
        <div className="chart-container">
          <ResponsiveContainer width="100%" height={200}>
            <PieChart>
              <Pie
                data={displayData}
                cx="50%"
                cy="50%"
                innerRadius={40}
                outerRadius={80}
                paddingAngle={2}
                dataKey="percentage"
              >
                {displayData.map((entry, index) => (
                  <Cell 
                    key={`cell-${index}`} 
                    fill={COLORS[index % COLORS.length]} 
                  />
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
            </PieChart>
          </ResponsiveContainer>
        </div>
        
        {/* Legend */}
        <div className="mt-4 grid grid-cols-2 gap-2">
          {displayData.map((segment, index) => (
            <div key={segment.segment} className="flex items-center space-x-2">
              <div 
                className="w-3 h-3 rounded-full" 
                style={{ backgroundColor: COLORS[index % COLORS.length] }}
              ></div>
              <span className="text-xs text-text-secondary">{segment.segment}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
