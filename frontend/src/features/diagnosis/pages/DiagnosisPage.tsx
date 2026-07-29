import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { diagnosisApi } from '../../../api/diagnosisApi';
import { positionApi } from '../../../api/positionApi';
import { HealthGauge } from '../../../components/common/HealthGauge';
import { RiskBadge } from '../../../components/common/RiskBadge';
import { RecommendationBadge } from '../../../components/common/RecommendationBadge';
import { LoadingSkeleton } from '../../../components/common/LoadingSkeleton';
import { EmptyState } from '../../../components/common/EmptyState';
import {
  HeartPulse,
  Activity,
  FileText,
  ShieldCheck,
  AlertTriangle,
  ArrowLeft,
  Lock,
  TrendingUp,
  DollarSign,
  Clock,
  Sparkles,
} from 'lucide-react';

export const DiagnosisPage: React.FC = () => {
  const { positionId } = useParams<{ positionId?: string }>();
  const navigate = useNavigate();

  // If no positionId in URL, fetch list of positions and pick first available
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

  // Calculate Distance to SL & TP %
  const stopLoss = position?.stopLossPrice;
  const takeProfit = position?.takeProfitPrice;

  const distToSL = stopLoss && currentPrice > 0 ? (((currentPrice - stopLoss) / currentPrice) * 100).toFixed(2) : null;
  const distToTP = takeProfit && currentPrice > 0 ? (((takeProfit - currentPrice) / currentPrice) * 100).toFixed(2) : null;

  return (
    <div className="space-y-8 animate-fadeIn">
      {/* Navigation & Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div className="flex items-center space-x-3">
          <button
            onClick={() => navigate('/positions')}
            className="p-2.5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition"
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
            <span className="text-slate-500">Risk Assessment:</span>
            <RiskBadge level={report.riskLevel} />
          </div>
        </div>

        {/* Right: AI Prescription Card */}
        <div className="lg:col-span-2 p-8 rounded-3xl bg-gradient-to-br from-slate-900 via-slate-900 to-slate-950 text-white border border-slate-800 shadow-xl flex flex-col justify-between relative overflow-hidden">
          <div className="absolute top-0 right-0 w-80 h-80 bg-blue-600/10 rounded-full blur-3xl pointer-events-none" />

          <div className="space-y-4 z-10">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <FileText className="w-5 h-5 text-blue-400" />
                <span className="text-xs font-extrabold uppercase tracking-widest text-blue-300">
                  AI Doctor Prescription & Action
                </span>
              </div>
              <RecommendationBadge action={report.recommendation} />
            </div>

            <div className="p-4 rounded-2xl bg-slate-800/80 border border-slate-700/80 space-y-2">
              <div className="flex items-center justify-between text-xs font-bold text-slate-400">
                <span>Diagnostic Reason & Medical Clinical Analysis</span>
                <span>Confidence: {recommendation?.confidence || 90}%</span>
              </div>
              <p className="text-sm font-semibold text-slate-200 leading-relaxed">
                "{report.reason}"
              </p>
            </div>
          </div>

          {/* Quick Doctor Action Bar */}
          <div className="pt-6 border-t border-slate-800/80 flex flex-wrap items-center justify-between gap-4 z-10">
            <div className="flex items-center space-x-2 text-xs font-medium text-slate-400">
              <ShieldCheck className="w-4 h-4 text-emerald-400" />
              <span>Diagnostic updates continuously every 15 seconds</span>
            </div>

            <div className="flex items-center space-x-3">
              <button
                onClick={() => navigate('/positions')}
                className="px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs shadow-md shadow-blue-500/20 transition"
              >
                Execute Doctor Prescription
              </button>
            </div>
          </div>
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

      {/* Patient Clinical Diagnosis Logs Timeline */}
      <div className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
        <div className="flex items-center space-x-2">
          <Sparkles className="w-5 h-5 text-blue-500" />
          <h3 className="text-sm font-bold text-slate-900 dark:text-white uppercase tracking-wider">
            Diagnostic Timeline & Health Factor Evaluation
          </h3>
        </div>

        <div className="space-y-4 pt-2">
          <div className="flex items-start space-x-3 p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800">
            <div className="p-2 rounded-xl bg-blue-500/10 text-blue-500 mt-0.5">
              <TrendingUp className="w-4 h-4" />
            </div>
            <div>
              <h5 className="text-xs font-bold text-slate-900 dark:text-white">
                Unrealized Return Factor Evaluated
              </h5>
              <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                Current price (${currentPrice.toFixed(2)}) compared to entry (${entryPrice.toFixed(2)}) indicates a {isGain ? 'positive' : 'negative'} return trend.
              </p>
            </div>
          </div>

          <div className="flex items-start space-x-3 p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800">
            <div className="p-2 rounded-xl bg-amber-500/10 text-amber-500 mt-0.5">
              <Lock className="w-4 h-4" />
            </div>
            <div>
              <h5 className="text-xs font-bold text-slate-900 dark:text-white">
                Stop-Loss Buffer Health Test
              </h5>
              <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                {stopLoss
                  ? `Position maintains a ${distToSL}% buffer above the defined stop-loss of $${stopLoss.toFixed(2)}.`
                  : 'Warning: Position lacks stop-loss protection, incurring health score deduction.'}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
