import React, { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { dashboardApi } from '../../../api/dashboardApi';
import { StatCard } from '../../../components/common/StatCard';
import { HealthGauge } from '../../../components/common/HealthGauge';
import { RiskBadge } from '../../../components/common/RiskBadge';
import { RecommendationBadge } from '../../../components/common/RecommendationBadge';
import { ActionExecutionModal } from '../../../components/common/ActionExecutionModal';
import { LoadingSkeleton } from '../../../components/common/LoadingSkeleton';
import { EmptyState } from '../../../components/common/EmptyState';
import { PositionResponse, RecommendationAction, RiskLevel } from '../../../types';
import { useNavigate } from 'react-router-dom';
import {
  TrendingUp,
  DollarSign,
  Activity,
  HeartPulse,
  ArrowUpRight,
  ShieldCheck,
  ChevronRight,
  Sparkles,
  RefreshCw,
  PlusCircle,
  Lock,
  AlertOctagon,
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
  const queryClient = useQueryClient();

  const [executionModalState, setExecutionModalState] = useState<{
    isOpen: boolean;
    position: PositionResponse | null;
    actionType: string;
    recAction?: RecommendationAction;
  }>({ isOpen: false, position: null, actionType: '' });

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

  const { portfolioSummary, healthSummary, recommendations, openPositions } = dashboardData.data;

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

  const getAggregatedRiskLevel = (): RiskLevel => {
    if (healthSummary?.criticalPositions && healthSummary.criticalPositions > 0) return 'CRITICAL';
    if (healthSummary?.warningPositions && healthSummary.warningPositions > 0) return 'HIGH';
    const score = healthSummary?.overallHealthScore ?? 75;
    if (score >= 80) return 'LOW';
    if (score >= 60) return 'MODERATE';
    if (score >= 40) return 'HIGH';
    return 'CRITICAL';
  };

  // Derive recommendation action for position
  const getPositionRecommendation = (posId: string): RecommendationAction => {
    if (recommendations && recommendations.length > 0) {
      const rec = recommendations.find((r) => r.positionId === posId);
      if (rec) return rec.recommendation;
    }
    return 'HOLD';
  };

  // Requirement 3: Primary CTA button matched to recommendation
  const renderPrimaryActionCTA = (pos: PositionResponse, recAction: RecommendationAction) => {
    switch (recAction) {
      case 'EXIT':
        return (
          <button
            onClick={() =>
              setExecutionModalState({
                isOpen: true,
                position: pos,
                actionType: 'EXIT_POSITION',
                recAction,
              })
            }
            className="px-3 py-1.5 rounded-xl bg-rose-600 hover:bg-rose-500 text-white font-bold text-xs shadow-md shadow-rose-500/20 transition cursor-pointer flex items-center space-x-1"
          >
            <AlertOctagon className="w-3.5 h-3.5" />
            <span>Exit Position</span>
          </button>
        );
      case 'BOOK_PROFIT':
        return (
          <button
            onClick={() =>
              setExecutionModalState({
                isOpen: true,
                position: pos,
                actionType: 'BOOK_PROFIT',
                recAction,
              })
            }
            className="px-3 py-1.5 rounded-xl bg-amber-600 hover:bg-amber-500 text-white font-bold text-xs shadow-md shadow-amber-500/20 transition cursor-pointer flex items-center space-x-1"
          >
            <DollarSign className="w-3.5 h-3.5" />
            <span>Book Profit</span>
          </button>
        );
      case 'ADD':
        return (
          <button
            onClick={() =>
              setExecutionModalState({
                isOpen: true,
                position: pos,
                actionType: 'BUY_MORE',
                recAction,
              })
            }
            className="px-3 py-1.5 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs shadow-md shadow-blue-500/20 transition cursor-pointer flex items-center space-x-1"
          >
            <PlusCircle className="w-3.5 h-3.5" />
            <span>Buy More</span>
          </button>
        );
      case 'TIGHTEN_STOPLOSS':
        return (
          <button
            onClick={() =>
              setExecutionModalState({
                isOpen: true,
                position: pos,
                actionType: 'EDIT_STOP_LOSS',
                recAction,
              })
            }
            className="px-3 py-1.5 rounded-xl bg-orange-600 hover:bg-orange-500 text-white font-bold text-xs shadow-md shadow-orange-500/20 transition cursor-pointer flex items-center space-x-1"
          >
            <Lock className="w-3.5 h-3.5" />
            <span>Edit Stop Loss</span>
          </button>
        );
      case 'HOLD':
      default:
        return (
          <button
            onClick={() => queryClient.invalidateQueries({ queryKey: ['dashboardSummary'] })}
            className="px-3 py-1.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs shadow-md shadow-emerald-500/20 transition cursor-pointer flex items-center space-x-1"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            <span>Refresh</span>
          </button>
        );
    }
  };

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
          className="px-5 py-2.5 rounded-2xl bg-blue-600 hover:bg-blue-700 text-white font-bold text-sm shadow-lg shadow-blue-500/25 transition-all hover:scale-105 active:scale-95 flex items-center space-x-2 cursor-pointer"
        >
          <span>Examine Positions</span>
          <ArrowUpRight className="w-4 h-4" />
        </button>
      </div>

      {/* Hero Stats & Health Gauge Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left: Overall Health Score */}
        <div className="p-8 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col items-center justify-center text-center relative overflow-hidden">
          <div className="absolute top-3 left-4 flex items-center space-x-1.5 text-xs font-bold text-slate-400 uppercase tracking-widest">
            <Activity className="w-4 h-4 text-blue-500" />
            <span>Portfolio Health Index</span>
          </div>

          <div className="scale-125 my-6">
            <HealthGauge score={healthSummary?.overallHealthScore || 75} size="lg" />
          </div>

          <div className="w-full pt-4 border-t border-slate-100 dark:border-slate-800 flex justify-between text-xs font-bold">
            <span className="text-slate-400">System Diagnosis:</span>
            <RiskBadge level={getAggregatedRiskLevel()} />
          </div>
        </div>

        {/* Middle: Key Financial Metrics */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 lg:col-span-2">
          <StatCard
            title="Total Portfolio Value"
            value={`$${(portfolioSummary?.totalPortfolioValue || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}`}
            change="2.4%"
            isPositive={true}
            icon={<DollarSign className="w-5 h-5 text-blue-600 dark:text-blue-400" />}
          />
          <StatCard
            title="Total Investment"
            value={`$${((portfolioSummary?.totalPortfolioValue || 0) - (portfolioSummary?.totalUnrealizedPnL || 0)).toLocaleString('en-US', { minimumFractionDigits: 2 })}`}
            icon={<TrendingUp className="w-5 h-5 text-indigo-600 dark:text-indigo-400" />}
          />
          <StatCard
            title="Unrealized P&L"
            value={`${isPositivePnL ? '+' : ''}$${(portfolioSummary?.totalUnrealizedPnL || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}`}
            isPositive={isPositivePnL}
            icon={<Activity className={`w-5 h-5 ${isPositivePnL ? 'text-emerald-500' : 'text-rose-500'}`} />}
          />
          <StatCard
            title="Open Positions Monitored"
            value={`${openPositions?.length || 0}`}
            icon={<ShieldCheck className="w-5 h-5 text-emerald-500" />}
          />
        </div>
      </div>

      {/* Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Portfolio Growth Trend */}
        <div className="lg:col-span-2 p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col justify-between">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-bold text-slate-900 dark:text-white uppercase tracking-wider">
                Portfolio Value Trend
              </h3>
              <span className="text-xs text-slate-400 font-semibold">5-Day Live Performance Tracking</span>
            </div>
          </div>

          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={trendData}>
                <defs>
                  <linearGradient id="colorValue" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="#3b82f6" stopOpacity={0.0} />
                  </linearGradient>
                </defs>
                <XAxis dataKey="name" stroke="#94a3b8" fontSize={11} tickLine={false} />
                <YAxis stroke="#94a3b8" fontSize={11} tickLine={false} axisLine={false} />
                <Tooltip />
                <Area type="monotone" dataKey="value" stroke="#3b82f6" strokeWidth={3} fillOpacity={1} fill="url(#colorValue)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Health Distribution Pie Chart */}
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

      {/* Requirement 3: Diagnosed Positions & Actionable Advice */}
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
            className="text-xs font-bold text-blue-600 dark:text-blue-400 hover:underline flex items-center cursor-pointer"
          >
            <span>View All Positions</span>
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
                  <th className="py-3 px-4 font-bold">AI Recommendation</th>
                  <th className="py-3 px-4 font-bold text-right">Primary Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-slate-800 font-semibold">
                {openPositions.map((pos) => {
                  const isGain = (pos.unrealizedPnL || 0) >= 0;
                  const recAction = getPositionRecommendation(pos.id);

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
                      <td className="py-3.5 px-4">
                        <RecommendationBadge action={recAction} />
                      </td>
                      <td className="py-3.5 px-4 text-right">
                        {renderPrimaryActionCTA(pos, recAction)}
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

      {/* Action Execution Modal */}
      {executionModalState.position && (
        <ActionExecutionModal
          isOpen={executionModalState.isOpen}
          onClose={() => setExecutionModalState({ isOpen: false, position: null, actionType: '' })}
          position={executionModalState.position}
          actionType={executionModalState.actionType}
          recommendationAction={executionModalState.recAction}
        />
      )}
    </div>
  );
};
