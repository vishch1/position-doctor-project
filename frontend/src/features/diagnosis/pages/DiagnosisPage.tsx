import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { diagnosisApi } from '../../../api/diagnosisApi';
import { positionApi } from '../../../api/positionApi';
import { HealthGauge } from '../../../components/common/HealthGauge';
import { RiskBadge } from '../../../components/common/RiskBadge';
import { RecommendationBadge } from '../../../components/common/RecommendationBadge';
import { ActionExecutionModal } from '../../../components/common/ActionExecutionModal';
import { LoadingSkeleton } from '../../../components/common/LoadingSkeleton';
import { EmptyState } from '../../../components/common/EmptyState';
import {
  HeartPulse,
  Activity,
  FileText,
  ShieldCheck,
  ArrowLeft,
  Lock,
  TrendingUp,
  Sparkles,
  CheckCircle2,
  RefreshCw,
  PlusCircle,
  DollarSign,
  AlertOctagon,
  Sliders,
  Play,
  Calculator,
} from 'lucide-react';

export const DiagnosisPage: React.FC = () => {
  const { positionId } = useParams<{ positionId?: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [userNotification, setUserNotification] = useState<string | null>(null);
  const [executionModalState, setExecutionModalState] = useState<{
    isOpen: boolean;
    actionType: string;
    customTitle?: string;
  }>({ isOpen: false, actionType: '' });

  // Fetch list of positions
  const { data: positionsData, isLoading: isPositionsLoading } = useQuery({
    queryKey: ['positions'],
    queryFn: () => positionApi.getAll(),
  });

  const effectiveId = positionId || (positionsData?.data?.[0]?.id ?? '');

  const { data: positionData } = useQuery({
    queryKey: ['position', effectiveId],
    queryFn: () => positionApi.getById(effectiveId),
    enabled: !!effectiveId,
  });

  const { data: reportData, isLoading: isReportLoading } = useQuery({
    queryKey: ['diagnosisReport', effectiveId],
    queryFn: () => diagnosisApi.getDiagnosis(effectiveId),
    enabled: !!effectiveId,
    refetchInterval: 15000,
  });

  const { data: recommendationData } = useQuery({
    queryKey: ['recommendation', effectiveId],
    queryFn: () => diagnosisApi.getRecommendation(effectiveId),
    enabled: !!effectiveId,
  });

  if (isPositionsLoading || isReportLoading) {
    return <LoadingSkeleton rows={6} />;
  }

  if (!effectiveId || !reportData?.data) {
    return (
      <EmptyState
        title="No Position Selected for Diagnosis"
        description="Select a position from your portfolio to run the AI Medical Health Diagnosis."
        actionLabel="Go to Positions"
        onAction={() => navigate('/positions')}
      />
    );
  }

  const report = reportData.data;
  const recommendation = recommendationData?.data;
  const position = positionData?.data;

  const currentPrice = position?.currentPrice || position?.entryPrice || 0;
  const entryPrice = position?.entryPrice || 1;
  const quantity = position?.quantity || 0;
  const pnl = position?.unrealizedPnL || 0;
  const isGain = pnl >= 0;

  const stopLoss = position?.stopLossPrice;
  const takeProfit = position?.takeProfitPrice;

  const distToSL = stopLoss && currentPrice > 0 ? (((currentPrice - stopLoss) / currentPrice) * 100).toFixed(2) : null;
  const distToTP = takeProfit && currentPrice > 0 ? (((takeProfit - currentPrice) / currentPrice) * 100).toFixed(2) : null;

  // Derive clinical outcome based on recommendation type
  const getExpectedOutcome = (): string => {
    switch (report.recommendation) {
      case 'EXIT':
        return 'Eliminates downside tail-risk and preserves remaining principal capital.';
      case 'BOOK_PROFIT':
        return 'Locks in accrued unrealized gains while maintaining partial upside exposure.';
      case 'ADD':
        return 'Lowers average cost basis to optimize potential recovery upside.';
      case 'TIGHTEN_STOPLOSS':
        return 'Locks in tighter stop-loss threshold to guard against market volatility.';
      case 'HOLD':
      default:
        return 'Maintains steady risk-reward balance under current market trends.';
    }
  };

  // Requirement 2: Recommended Actions button groups by recommendation
  const getRecommendedActionButtons = () => {
    switch (report.recommendation) {
      case 'HOLD':
        return [
          { label: 'Refresh Analysis', actionType: 'REFRESH', icon: <RefreshCw className="w-3.5 h-3.5" />, color: 'bg-emerald-600 hover:bg-emerald-500' },
          { label: 'Add Position', actionType: 'ADD_POSITION', icon: <PlusCircle className="w-3.5 h-3.5" />, color: 'bg-blue-600 hover:bg-blue-500' },
        ];
      case 'BOOK_PROFIT':
        return [
          { label: 'Book 25%', actionType: 'BOOK_25', icon: <DollarSign className="w-3.5 h-3.5" />, color: 'bg-amber-600 hover:bg-amber-500' },
          { label: 'Book 50%', actionType: 'BOOK_50', icon: <DollarSign className="w-3.5 h-3.5" />, color: 'bg-amber-600 hover:bg-amber-500' },
          { label: 'Book Full', actionType: 'BOOK_FULL', icon: <DollarSign className="w-3.5 h-3.5" />, color: 'bg-amber-700 hover:bg-amber-600' },
        ];
      case 'ADD':
        return [
          { label: 'Buy More', actionType: 'BUY_MORE', icon: <PlusCircle className="w-3.5 h-3.5" />, color: 'bg-blue-600 hover:bg-blue-500' },
          { label: 'Calculate Average', actionType: 'CALCULATE_AVERAGE', icon: <Calculator className="w-3.5 h-3.5" />, color: 'bg-indigo-600 hover:bg-indigo-500' },
        ];
      case 'EXIT':
        return [
          { label: 'Exit Position', actionType: 'EXIT_FULL', icon: <AlertOctagon className="w-3.5 h-3.5" />, color: 'bg-rose-600 hover:bg-rose-500' },
          { label: 'Exit 50%', actionType: 'EXIT_50', icon: <AlertOctagon className="w-3.5 h-3.5" />, color: 'bg-rose-700 hover:bg-rose-600' },
        ];
      case 'TIGHTEN_STOPLOSS':
        return [
          { label: 'Edit Stop Loss', actionType: 'EDIT_STOP_LOSS', icon: <Sliders className="w-3.5 h-3.5" />, color: 'bg-orange-600 hover:bg-orange-500' },
          { label: 'Move SL to Break-even', actionType: 'MOVE_SL_BREAKEVEN', icon: <Lock className="w-3.5 h-3.5" />, color: 'bg-amber-600 hover:bg-amber-500' },
        ];
      default:
        return [
          { label: 'Refresh Analysis', actionType: 'REFRESH', icon: <RefreshCw className="w-3.5 h-3.5" />, color: 'bg-blue-600 hover:bg-blue-500' },
        ];
    }
  };

  const handleActionExecution = (actionType: string, label: string) => {
    if (actionType === 'REFRESH') {
      queryClient.invalidateQueries({ queryKey: ['diagnosisReport', effectiveId] });
      setUserNotification('AI Diagnosis refreshed successfully.');
      setTimeout(() => setUserNotification(null), 3000);
      return;
    }

    if (actionType === 'CALCULATE_AVERAGE') {
      setUserNotification(`DCA Average Entry Price for ${report.symbol}: $${((entryPrice + currentPrice) / 2).toFixed(2)}`);
      setTimeout(() => setUserNotification(null), 4000);
      return;
    }

    setExecutionModalState({
      isOpen: true,
      actionType,
      customTitle: `Execute Action: ${label}`,
    });
  };

  const recButtons = getRecommendedActionButtons();

  return (
    <div className="space-y-8 animate-fadeIn">
      {/* Navigation & Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div className="flex items-center space-x-3">
          <button
            onClick={() => navigate('/positions')}
            className="p-2.5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition cursor-pointer"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <div className="flex items-center space-x-2 text-xs font-bold text-blue-600 dark:text-blue-400 uppercase tracking-widest">
              <HeartPulse className="w-4 h-4 animate-pulse" />
              <span>AI Doctor Patient Report</span>
            </div>
            <h2 className="text-3xl font-black text-slate-900 dark:text-white tracking-tight">
              Position Clinical Diagnosis
            </h2>
          </div>
        </div>

        {/* Position Selector Dropdown if multiple positions */}
        {positionsData?.data && positionsData.data.length > 1 && (
          <select
            value={effectiveId}
            onChange={(e) => navigate(`/diagnosis/${e.target.value}`)}
            className="px-4 py-2.5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-xs font-bold text-slate-900 dark:text-white focus:outline-none cursor-pointer"
          >
            {positionsData.data.map((p) => (
              <option key={p.id} value={p.id}>
                {p.symbol} ({p.exchange}) - Health {p.riskLevel}
              </option>
            ))}
          </select>
        )}
      </div>

      {/* User Action Feedback Toast */}
      {userNotification && (
        <div className="p-4 rounded-2xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-600 dark:text-emerald-400 text-xs font-bold flex items-center space-x-2 animate-fadeIn">
          <CheckCircle2 className="w-4 h-4" />
          <span>{userNotification}</span>
        </div>
      )}

      {/* Main Medical Report Hero Banner */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left: Health Score Patient Gauge */}
        <div className="p-8 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col items-center justify-center text-center relative overflow-hidden">
          <div className="absolute top-3 left-4 flex items-center space-x-1.5 text-xs font-bold text-slate-400 uppercase tracking-widest">
            <Activity className="w-4 h-4 text-blue-500" />
            <span>Vital Health Meter</span>
          </div>

          <div className="scale-125 my-6">
            <HealthGauge score={report.healthScore} size="lg" />
          </div>

          <div className="mt-4 pt-4 border-t border-slate-100 dark:border-slate-800 w-full flex items-center justify-between text-xs">
            <span className="text-slate-500 font-semibold">Risk Assessment:</span>
            <RiskBadge level={report.riskLevel} />
          </div>
        </div>

        {/* Requirement 4: Redesigned AI Doctor Prescription Card */}
        <div className="lg:col-span-2 p-8 rounded-3xl bg-gradient-to-br from-slate-900 via-slate-900 to-slate-950 text-white border border-slate-800 shadow-xl flex flex-col justify-between relative overflow-hidden">
          <div className="absolute top-0 right-0 w-80 h-80 bg-blue-600/10 rounded-full blur-3xl pointer-events-none" />

          <div className="space-y-4 z-10">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <FileText className="w-5 h-5 text-blue-400" />
                <span className="text-xs font-extrabold uppercase tracking-widest text-blue-300">
                  AI Doctor Prescription Card
                </span>
              </div>
              <RecommendationBadge action={report.recommendation} />
            </div>

            {/* Medical Prescription Vitals Grid */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 p-3.5 rounded-2xl bg-slate-800/60 border border-slate-700/60 text-xs">
              <div>
                <span className="text-slate-400 font-medium block">Diagnosis</span>
                <strong className="text-slate-100 font-bold">{report.riskLevel} Risk</strong>
              </div>
              <div>
                <span className="text-slate-400 font-medium block">Recommendation</span>
                <strong className="text-slate-100 font-bold">{report.recommendation.replace('_', ' ')}</strong>
              </div>
              <div>
                <span className="text-slate-400 font-medium block">Confidence Score</span>
                <strong className="text-emerald-400 font-bold">{recommendation?.confidence || 95}%</strong>
              </div>
              <div>
                <span className="text-slate-400 font-medium block">Health Score</span>
                <strong className="text-blue-400 font-bold">{report.healthScore}/100</strong>
              </div>
            </div>

            {/* Clinical Reasoning & Expected Outcome */}
            <div className="p-4 rounded-2xl bg-slate-800/80 border border-slate-700/80 space-y-2">
              <div className="text-xs font-bold text-slate-400 uppercase tracking-wider">Clinical Reasoning</div>
              <p className="text-sm font-semibold text-slate-200 leading-relaxed">
                "{report.reason}"
              </p>
              <div className="pt-2 border-t border-slate-700/60 text-xs">
                <span className="text-blue-400 font-bold">Expected Outcome: </span>
                <span className="text-slate-300 font-medium">{getExpectedOutcome()}</span>
              </div>
            </div>
          </div>

          {/* Large Primary Execute Recommendation CTA */}
          <div className="pt-6 border-t border-slate-800/80 flex items-center justify-between gap-4 z-10">
            <div className="flex items-center space-x-2 text-xs font-medium text-slate-400">
              <ShieldCheck className="w-4 h-4 text-emerald-400" />
              <span>Real-time clinical order dispatch</span>
            </div>

            <button
              onClick={() =>
                handleActionExecution(report.recommendation, `Execute ${report.recommendation.replace('_', ' ')}`)
              }
              className="px-6 py-3 rounded-2xl bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white font-black text-xs shadow-lg shadow-blue-500/25 transition-all hover:scale-105 active:scale-95 flex items-center space-x-2 cursor-pointer"
            >
              <Play className="w-4 h-4 fill-white" />
              <span>Execute Recommendation</span>
            </button>
          </div>
        </div>
      </div>

      {/* Requirement 2: Recommended Actions Section */}
      <div className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <Sparkles className="w-5 h-5 text-blue-500" />
            <h3 className="text-sm font-bold text-slate-900 dark:text-white uppercase tracking-wider">
              Contextual Recommended Actions ({report.recommendation})
            </h3>
          </div>
          <span className="text-xs font-semibold text-slate-400">Click any action to execute</span>
        </div>

        <div className="flex flex-wrap items-center gap-3 pt-1">
          {recButtons.map((btn) => (
            <button
              key={btn.actionType}
              onClick={() => handleActionExecution(btn.actionType, btn.label)}
              className={`px-5 py-2.5 rounded-xl text-white font-bold text-xs shadow-md transition-all hover:scale-105 active:scale-95 flex items-center space-x-2 cursor-pointer ${btn.color}`}
            >
              {btn.icon}
              <span>{btn.label}</span>
            </button>
          ))}
        </div>
      </div>

      {/* Medical Vitals Breakdown */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-1">
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Stock Symbol</span>
          <h4 className="text-xl font-black text-slate-900 dark:text-white">{report.symbol}</h4>
          <span className="text-[11px] text-slate-500 font-medium">Exchange: {position?.exchange || 'NASDAQ'}</span>
        </div>

        <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-1">
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Unrealized P&L</span>
          <h4 className={`text-xl font-black ${isGain ? 'text-emerald-500' : 'text-rose-500'}`}>
            {isGain ? '+' : ''}${pnl.toFixed(2)}
          </h4>
          <span className="text-[11px] text-slate-500 font-medium">Qty: {quantity} @ ${entryPrice.toFixed(2)}</span>
        </div>

        <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-1">
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Distance to Stop-Loss</span>
          <h4 className="text-xl font-black text-slate-900 dark:text-white">
            {distToSL ? `${distToSL}%` : 'Not Set'}
          </h4>
          <span className="text-[11px] text-slate-500 font-medium">
            {stopLoss ? `SL Target: $${stopLoss.toFixed(2)}` : 'Trading without SL protection'}
          </span>
        </div>

        <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-1">
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Distance to Take-Profit</span>
          <h4 className="text-xl font-black text-slate-900 dark:text-white">
            {distToTP ? `${distToTP}%` : 'Not Set'}
          </h4>
          <span className="text-[11px] text-slate-500 font-medium">
            {takeProfit ? `TP Target: $${takeProfit.toFixed(2)}` : 'No profit target defined'}
          </span>
        </div>
      </div>

      {/* Action Execution Modal */}
      {position && (
        <ActionExecutionModal
          isOpen={executionModalState.isOpen}
          onClose={() => setExecutionModalState({ isOpen: false, actionType: '' })}
          position={position}
          actionType={executionModalState.actionType}
          recommendationAction={report.recommendation}
          customTitle={executionModalState.customTitle}
        />
      )}
    </div>
  );
};
