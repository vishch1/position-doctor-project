import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { dashboardApi } from '../../../api/dashboardApi';
import { StatCard } from '../../../components/common/StatCard';
import { HealthGauge } from '../../../components/common/HealthGauge';
import { RiskBadge } from '../../../components/common/RiskBadge';
import { RecommendationBadge } from '../../../components/common/RecommendationBadge';
import { LoadingSkeleton } from '../../../components/common/LoadingSkeleton';
import { EmptyState } from '../../../components/common/EmptyState';
import { useNavigate } from 'react-router-dom';
import {
  TrendingUp,
  DollarSign,
  Activity,
  HeartPulse,
  ArrowUpRight,
  ShieldCheck,
  AlertTriangle,
  ChevronRight,
  Sparkles,
} from 'lucide-react';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  PieChart,
  Pie,
  Cell,
} from 'recharts';

export const DashboardPage: React.FC = () => {
  const navigate = useNavigate();

  const { data: dashboardData, isLoading, error } = useQuery({
    queryKey: ['dashboardSummary'],
    queryFn: () => dashboardApi.getSummary(),
    refetchInterval: 15000, // Live refresh every 15s
  });

  if (isLoading) {
    return <LoadingSkeleton rows={6} />;
  }

  if (error || !dashboardData?.data) {
    return (
      <EmptyState
        title="Unable to Load Dashboard Data"
        description="Could not connect to Position Doctor backend services. Ensure backend is running."
      />
    );
  }

  const { portfolioSummary, healthSummary, recentAlerts, recommendations, openPositions } =
    dashboardData.data;

  // Mock trend data for area chart
  const trendData = [
    { name: 'Mon', value: (portfolioSummary?.totalPortfolioValue || 10000) * 0.92 },
    { name: 'Tue', value: (portfolioSummary?.totalPortfolioValue || 10000) * 0.95 },
    { name: 'Wed', value: (portfolioSummary?.totalPortfolioValue || 10000) * 0.94 },
    { name: 'Thu', value: (portfolioSummary?.totalPortfolioValue || 10000) * 0.98 },
    { name: 'Fri', value: portfolioSummary?.totalPortfolioValue || 10000 },
  ];

  const pieData = [
    { name: 'Healthy', value: healthSummary?.healthyPositions || 1, color: '#22c55e' },
    { name: 'Warning', value: healthSummary?.warningPositions || 0, color: '#f59e0b' },
    { name: 'Critical', value: healthSummary?.criticalPositions || 0, color: '#ef4444' },
  ];

  const isPositivePnL = (portfolioSummary?.totalUnrealizedPnL || 0) >= 0;

  return (
    <div className="space-y-8 animate-fadeIn">
      {/* Top Banner & Quick Vitals */}
      <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2 text-xs font-bold text-blue-600 dark:text-blue-400 uppercase tracking-widest mb-1">
            <Sparkles className="w-4 h-4" />
            <span>AI Doctor Executive Summary</span>
          </div>
          <h2 className="text-3xl font-black text-slate-900 dark:text-white tracking-tight">
            Portfolio Health Vitals
          </h2>
        </div>

        <button
          onClick={() => navigate('/positions')}
          className="px-5 py-2.5 rounded-2xl bg-blue-600 hover:bg-blue-700 text-white font-bold text-sm shadow-lg shadow-blue-500/25 transition-all hover:scale-105 active:scale-95 flex items-center space-x-2"
        >
          <span>Examine Positions</span>
          <ArrowUpRight className="w-4 h-4" />
        </button>
      </div>

      {/* Hero Stats & Health Gauge Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Large Gradient Portfolio Value Card */}
        <div className="lg:col-span-2 p-8 rounded-3xl bg-gradient-to-br from-blue-600 via-indigo-600 to-slate-900 text-white shadow-xl shadow-blue-500/20 relative overflow-hidden flex flex-col justify-between min-h-[220px]">
          <div className="absolute top-0 right-0 w-80 h-80 bg-white/10 rounded-full blur-3xl pointer-events-none" />

          <div className="flex items-center justify-between z-10">
            <span className="text-xs font-bold uppercase tracking-widest text-blue-200">
              Total Portfolio Value
            </span>
            <div className="p-2 rounded-xl bg-white/10 backdrop-blur-md">
              <DollarSign className="w-5 h-5 text-blue-200" />
            </div>
          </div>

          <div className="z-10 my-4">
            <h1 className="text-4xl sm:text-5xl font-black tracking-tight">
              ${portfolioSummary?.totalPortfolioValue?.toLocaleString('en-US', { minimumFractionDigits: 2 }) || '0.00'}
            </h1>
            <div className="flex items-center space-x-3 mt-3">
              <span
                className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-extrabold ${
                  isPositivePnL ? 'bg-emerald-500/20 text-emerald-300' : 'bg-rose-500/20 text-rose-300'
                }`}
              >
                {isPositivePnL ? '+' : ''}${portfolioSummary?.totalUnrealizedPnL?.toFixed(2)} Total P&L
              </span>
              <span className="text-xs text-blue-200 font-semibold">
                Today P&L: ${portfolioSummary?.todayPnL?.toFixed(2)}
              </span>
            </div>
          </div>

          <div className="z-10 flex items-center justify-between text-xs text-blue-200/80 pt-4 border-t border-white/10">
            <span>Active Positions: {openPositions?.length || 0}</span>
            <span>Finnhub Live Stream Enabled</span>
          </div>
        </div>

        {/* AI Health Score Card */}
        <div className="p-8 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col items-center justify-center text-center relative overflow-hidden">
          <h3 className="text-xs font-extrabold text-slate-400 uppercase tracking-widest mb-4">
            Overall Health Score
          </h3>
          <div className="scale-125 my-2">
            <HealthGauge score={healthSummary?.overallHealthScore || 100} size="lg" />
          </div>

          <div className="grid grid-cols-3 gap-2 w-full mt-6 pt-4 border-t border-slate-100 dark:border-slate-800 text-center">
            <div>
              <span className="block text-xs font-bold text-emerald-500">
                {healthSummary?.healthyPositions || 0}
              </span>
              <span className="text-[10px] text-slate-400 font-medium uppercase">Healthy</span>
            </div>
            <div>
              <span className="block text-xs font-bold text-amber-500">
                {healthSummary?.warningPositions || 0}
              </span>
              <span className="text-[10px] text-slate-400 font-medium uppercase">Warning</span>
            </div>
            <div>
              <span className="block text-xs font-bold text-rose-500">
                {healthSummary?.criticalPositions || 0}
              </span>
              <span className="text-[10px] text-slate-400 font-medium uppercase">Critical</span>
            </div>
          </div>
        </div>
      </div>

      {/* Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Performance Area Chart */}
        <div className="lg:col-span-2 p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-bold text-slate-900 dark:text-white uppercase tracking-wider">
              Portfolio Growth Trend
            </h3>
            <span className="text-xs font-semibold text-blue-500">Last 7 Days</span>
          </div>

          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={trendData}>
                <defs>
                  <linearGradient id="colorVal" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#2563eb" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="#2563eb" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <XAxis dataKey="name" stroke="#64748b" fontSize={11} tickLine={false} axisLine={false} />
                <YAxis stroke="#64748b" fontSize={11} tickLine={false} axisLine={false} />
                <Tooltip />
                <Area type="monotone" dataKey="value" stroke="#2563eb" strokeWidth={3} fillOpacity={1} fill="url(#colorVal)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Risk Allocation Pie Chart */}
        <div className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col justify-between">
          <h3 className="text-sm font-bold text-slate-900 dark:text-white uppercase tracking-wider mb-2">
            Health Distribution
          </h3>

          <div className="h-48 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={pieData} innerRadius={50} outerRadius={70} paddingAngle={4} dataKey="value">
                  {pieData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>

          <div className="space-y-2 pt-4 border-t border-slate-100 dark:border-slate-800">
            {pieData.map((item) => (
              <div key={item.name} className="flex items-center justify-between text-xs">
                <div className="flex items-center space-x-2">
                  <span className="w-3 h-3 rounded-full" style={{ backgroundColor: item.color }} />
                  <span className="text-slate-600 dark:text-slate-400 font-medium">{item.name}</span>
                </div>
                <span className="font-bold text-slate-900 dark:text-white">{item.value} Positions</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Recent AI Prescriptions & Open Positions Table */}
      <div className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <HeartPulse className="w-5 h-5 text-blue-600 dark:text-blue-400" />
            <h3 className="text-sm font-bold text-slate-900 dark:text-white uppercase tracking-wider">
              Diagnosed Positions & Actionable Advice
            </h3>
          </div>
          <button
            onClick={() => navigate('/positions')}
            className="text-xs font-bold text-blue-600 dark:text-blue-400 hover:underline flex items-center"
          >
            <span>View All</span>
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>

        {openPositions && openPositions.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="border-b border-slate-200 dark:border-slate-800 text-slate-400 uppercase tracking-wider">
                  <th className="py-3 px-4 font-bold">Symbol</th>
                  <th className="py-3 px-4 font-bold">Exchange</th>
                  <th className="py-3 px-4 font-bold">Price</th>
                  <th className="py-3 px-4 font-bold">Unrealized P&L</th>
                  <th className="py-3 px-4 font-bold">Risk Level</th>
                  <th className="py-3 px-4 font-bold text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-slate-800 font-semibold">
                {openPositions.map((pos) => {
                  const isGain = (pos.unrealizedPnL || 0) >= 0;
                  return (
                    <tr key={pos.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/50 transition">
                      <td className="py-3.5 px-4 font-black text-slate-900 dark:text-white">{pos.symbol}</td>
                      <td className="py-3.5 px-4 text-slate-500">{pos.exchange}</td>
                      <td className="py-3.5 px-4">${pos.currentPrice?.toFixed(2) || pos.entryPrice?.toFixed(2)}</td>
                      <td className={`py-3.5 px-4 ${isGain ? 'text-emerald-500' : 'text-rose-500'}`}>
                        {isGain ? '+' : ''}${pos.unrealizedPnL?.toFixed(2)}
                      </td>
                      <td className="py-3.5 px-4">
                        <RiskBadge level={pos.riskLevel} />
                      </td>
                      <td className="py-3.5 px-4 text-right">
                        <button
                          onClick={() => navigate(`/diagnosis/${pos.id}`)}
                          className="px-3 py-1.5 rounded-xl bg-blue-500/10 hover:bg-blue-500/20 text-blue-600 dark:text-blue-400 font-bold transition"
                        >
                          Diagnose
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        ) : (
          <EmptyState
            title="No Open Positions"
            description="You have no diagnosed open positions currently. Create a position to activate AI monitoring."
            actionLabel="Add Position"
            onAction={() => navigate('/positions')}
          />
        )}
      </div>
    </div>
  );
};
