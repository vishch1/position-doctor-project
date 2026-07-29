import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { positionApi } from '../../../api/positionApi';
import { portfolioApi } from '../../../api/portfolioApi';
import { Exchange, PositionType } from '../../../types';
import { RiskBadge } from '../../../components/common/RiskBadge';
import { LoadingSkeleton } from '../../../components/common/LoadingSkeleton';
import { EmptyState } from '../../../components/common/EmptyState';
import { ConfirmationDialog } from '../../../components/common/ConfirmationDialog';
import { useNavigate } from 'react-router-dom';
import {
  TrendingUp,
  Plus,
  Search,
  Trash2,
  HeartPulse,
  Filter,
  ArrowUpDown,
  X,
  ExternalLink,
} from 'lucide-react';

export const PositionPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [searchTerm, setSearchTerm] = useState('');
  const [exchangeFilter, setExchangeFilter] = useState<string>('ALL');
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  // Form states for new position
  const [portfolioId, setPortfolioId] = useState('');
  const [symbol, setSymbol] = useState('');
  const [exchange, setExchange] = useState<Exchange>('NASDAQ');
  const [positionType, setPositionType] = useState<PositionType>('LONG');
  const [quantity, setQuantity] = useState<number>(10);
  const [entryPrice, setEntryPrice] = useState<number>(100);
  const [stopLossPrice, setStopLossPrice] = useState<number | undefined>(90);
  const [takeProfitPrice, setTakeProfitPrice] = useState<number | undefined>(130);

  const { data: positionsData, isLoading: isPositionsLoading } = useQuery({
    queryKey: ['positions'],
    queryFn: () => positionApi.getAll(),
    refetchInterval: 30000, // Live Finnhub update fetch every 30s
  });

  const { data: portfoliosData } = useQuery({
    queryKey: ['portfolios'],
    queryFn: () => portfolioApi.getAll(),
  });

  const createMutation = useMutation({
    mutationFn: positionApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['positions'] });
      queryClient.invalidateQueries({ queryKey: ['dashboardSummary'] });
      setIsCreateOpen(false);
      resetForm();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: positionApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['positions'] });
      queryClient.invalidateQueries({ queryKey: ['dashboardSummary'] });
      setDeletingId(null);
    },
  });

  const resetForm = () => {
    setSymbol('');
    setQuantity(10);
    setEntryPrice(100);
    setStopLossPrice(90);
    setTakeProfitPrice(130);
  };

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault();
    if (!portfolioId || !symbol.trim()) return;
    createMutation.mutate({
      portfolioId,
      symbol: symbol.toUpperCase(),
      exchange,
      positionType,
      quantity,
      entryPrice,
      stopLossPrice,
      takeProfitPrice,
    });
  };

  const positions = positionsData?.data || [];
  const portfolios = portfoliosData?.data || [];

  const filteredPositions = positions.filter((pos) => {
    const matchesSearch = pos.symbol.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesExchange = exchangeFilter === 'ALL' || pos.exchange === exchangeFilter;
    return matchesSearch && matchesExchange;
  });

  if (isPositionsLoading) return <LoadingSkeleton rows={6} />;

  return (
    <div className="space-y-6 animate-fadeIn">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2 text-xs font-bold text-blue-600 dark:text-blue-400 uppercase tracking-widest mb-1">
            <TrendingUp className="w-4 h-4" />
            <span>Zerodha-Style Trading Monitor</span>
          </div>
          <h2 className="text-2xl font-black text-slate-900 dark:text-white tracking-tight">
            Stock Positions & Vitals
          </h2>
        </div>

        <button
          onClick={() => {
            if (portfolios.length > 0) setPortfolioId(portfolios[0].id);
            resetForm();
            setIsCreateOpen(true);
          }}
          className="px-4 py-2.5 rounded-2xl bg-blue-600 hover:bg-blue-700 text-white font-bold text-sm shadow-md shadow-blue-500/25 transition-all hover:scale-105 active:scale-95 flex items-center space-x-2"
        >
          <Plus className="w-4 h-4" />
          <span>Add Position</span>
        </button>
      </div>

      {/* Filter and Search Bar */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-4">
        <div className="flex items-center space-x-3">
          <div className="relative w-72">
            <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Search symbol (AAPL, NVDA)..."
              className="w-full pl-10 pr-4 py-2 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-xs font-semibold text-slate-900 dark:text-white placeholder-slate-400 focus:outline-none focus:border-blue-500"
            />
          </div>

          <div className="flex items-center space-x-1.5 px-3 py-1.5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-xs font-semibold text-slate-600 dark:text-slate-300">
            <Filter className="w-3.5 h-3.5 text-slate-400" />
            <select
              value={exchangeFilter}
              onChange={(e) => setExchangeFilter(e.target.value)}
              className="bg-transparent focus:outline-none cursor-pointer"
            >
              <option value="ALL">All Exchanges</option>
              <option value="NSE">NSE</option>
              <option value="BSE">BSE</option>
              <option value="NASDAQ">NASDAQ</option>
              <option value="NYSE">NYSE</option>
            </select>
          </div>
        </div>

        <span className="text-xs font-semibold text-slate-400">
          Showing {filteredPositions.length} Positions
        </span>
      </div>

      {/* Trading Datatable */}
      {filteredPositions.length > 0 ? (
        <div className="rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="border-b border-slate-200 dark:border-slate-800 text-slate-400 uppercase tracking-wider bg-slate-50/50 dark:bg-slate-800/30 sticky top-0">
                  <th className="py-4 px-5 font-bold">Stock</th>
                  <th className="py-4 px-5 font-bold">Exchange</th>
                  <th className="py-4 px-5 font-bold">Type</th>
                  <th className="py-4 px-5 font-bold">Qty</th>
                  <th className="py-4 px-5 font-bold">Entry Price</th>
                  <th className="py-4 px-5 font-bold">Current Price</th>
                  <th className="py-4 px-5 font-bold">Unrealized P&L</th>
                  <th className="py-4 px-5 font-bold">Risk Level</th>
                  <th className="py-4 px-5 font-bold text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-slate-800 font-semibold">
                {filteredPositions.map((pos) => {
                  const isGain = (pos.unrealizedPnL || 0) >= 0;
                  const currentPrice = pos.currentPrice || pos.entryPrice;

                  return (
                    <tr
                      key={pos.id}
                      className="hover:bg-slate-50/80 dark:hover:bg-slate-800/50 transition group"
                    >
                      <td className="py-4 px-5">
                        <div className="flex items-center space-x-2">
                          <span className="font-black text-sm text-slate-900 dark:text-white">
                            {pos.symbol}
                          </span>
                        </div>
                      </td>
                      <td className="py-4 px-5 text-slate-500 font-bold">{pos.exchange}</td>
                      <td className="py-4 px-5">
                        <span
                          className={`px-2 py-0.5 rounded-md text-[10px] font-black uppercase ${
                            pos.positionType === 'LONG'
                              ? 'bg-blue-500/10 text-blue-600 dark:text-blue-400'
                              : 'bg-purple-500/10 text-purple-600 dark:text-purple-400'
                          }`}
                        >
                          {pos.positionType}
                        </span>
                      </td>
                      <td className="py-4 px-5 text-slate-900 dark:text-slate-200">{pos.quantity}</td>
                      <td className="py-4 px-5 text-slate-600 dark:text-slate-400">${pos.entryPrice?.toFixed(2)}</td>
                      <td className="py-4 px-5 font-bold text-slate-900 dark:text-white">
                        ${currentPrice?.toFixed(2)}
                      </td>
                      <td className={`py-4 px-5 font-extrabold ${isGain ? 'text-emerald-500' : 'text-rose-500'}`}>
                        {isGain ? '+' : ''}${pos.unrealizedPnL?.toFixed(2)}
                      </td>
                      <td className="py-4 px-5">
                        <RiskBadge level={pos.riskLevel} />
                      </td>
                      <td className="py-4 px-5 text-right space-x-2">
                        <button
                          onClick={() => navigate(`/diagnosis/${pos.id}`)}
                          className="px-3 py-1.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-bold transition inline-flex items-center space-x-1 shadow-sm"
                        >
                          <HeartPulse className="w-3.5 h-3.5" />
                          <span>Diagnose</span>
                        </button>
                        <button
                          onClick={() => setDeletingId(pos.id)}
                          className="p-1.5 rounded-xl bg-rose-500/10 hover:bg-rose-500/20 text-rose-500 transition"
                          title="Delete Position"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        <EmptyState
          title="No Positions Added"
          description="Create your first stock position to activate real-time market tracking and AI diagnostics."
          actionLabel="Add Position"
          onAction={() => {
            if (portfolios.length > 0) setPortfolioId(portfolios[0].id);
            resetForm();
            setIsCreateOpen(true);
          }}
        />
      )}

      {/* Create Position Modal */}
      {isCreateOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-sm">
          <div className="w-full max-w-lg p-6 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-3xl shadow-2xl space-y-4 relative">
            <button
              onClick={() => setIsCreateOpen(false)}
              className="absolute top-4 right-4 text-slate-400 hover:text-slate-600 dark:hover:text-white"
            >
              <X className="w-5 h-5" />
            </button>
            <h3 className="text-lg font-extrabold text-slate-900 dark:text-white">Add Stock Position</h3>

            {portfolios.length === 0 ? (
              <div className="p-4 rounded-2xl bg-amber-500/10 text-amber-500 text-xs font-semibold">
                You must create a portfolio first before adding positions.
              </div>
            ) : (
              <form onSubmit={handleCreate} className="space-y-4">
                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1">
                    Select Portfolio
                  </label>
                  <select
                    value={portfolioId}
                    onChange={(e) => setPortfolioId(e.target.value)}
                    className="w-full px-4 py-2.5 rounded-2xl bg-slate-100 dark:bg-slate-800 border border-transparent focus:border-blue-500 text-sm font-semibold text-slate-900 dark:text-white focus:outline-none"
                  >
                    {portfolios.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.name} ({p.currency})
                      </option>
                    ))}
                  </select>
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1">
                      Stock Symbol
                    </label>
                    <input
                      type="text"
                      required
                      value={symbol}
                      onChange={(e) => setSymbol(e.target.value.toUpperCase())}
                      placeholder="AAPL, RELIANCE, TSLA"
                      className="w-full px-4 py-2.5 rounded-2xl bg-slate-100 dark:bg-slate-800 border border-transparent focus:border-blue-500 text-sm font-semibold text-slate-900 dark:text-white focus:outline-none"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1">
                      Exchange
                    </label>
                    <select
                      value={exchange}
                      onChange={(e) => setExchange(e.target.value as Exchange)}
                      className="w-full px-4 py-2.5 rounded-2xl bg-slate-100 dark:bg-slate-800 border border-transparent focus:border-blue-500 text-sm font-semibold text-slate-900 dark:text-white focus:outline-none"
                    >
                      <option value="NASDAQ">NASDAQ</option>
                      <option value="NYSE">NYSE</option>
                      <option value="NSE">NSE</option>
                      <option value="BSE">BSE</option>
                    </select>
                  </div>
                </div>

                <div className="grid grid-cols-3 gap-3">
                  <div>
                    <label className="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1">
                      Type
                    </label>
                    <select
                      value={positionType}
                      onChange={(e) => setPositionType(e.target.value as PositionType)}
                      className="w-full px-4 py-2.5 rounded-2xl bg-slate-100 dark:bg-slate-800 border border-transparent focus:border-blue-500 text-sm font-semibold text-slate-900 dark:text-white focus:outline-none"
                    >
                      <option value="LONG">LONG</option>
                      <option value="SHORT">SHORT</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1">
                      Quantity
                    </label>
                    <input
                      type="number"
                      required
                      min={1}
                      value={quantity}
                      onChange={(e) => setQuantity(Number(e.target.value))}
                      className="w-full px-4 py-2.5 rounded-2xl bg-slate-100 dark:bg-slate-800 border border-transparent focus:border-blue-500 text-sm font-semibold text-slate-900 dark:text-white focus:outline-none"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1">
                      Entry Price ($)
                    </label>
                    <input
                      type="number"
                      required
                      step="0.01"
                      min={0.01}
                      value={entryPrice}
                      onChange={(e) => setEntryPrice(Number(e.target.value))}
                      className="w-full px-4 py-2.5 rounded-2xl bg-slate-100 dark:bg-slate-800 border border-transparent focus:border-blue-500 text-sm font-semibold text-slate-900 dark:text-white focus:outline-none"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1">
                      Stop-Loss ($)
                    </label>
                    <input
                      type="number"
                      step="0.01"
                      value={stopLossPrice || ''}
                      onChange={(e) => setStopLossPrice(e.target.value ? Number(e.target.value) : undefined)}
                      placeholder="Optional"
                      className="w-full px-4 py-2.5 rounded-2xl bg-slate-100 dark:bg-slate-800 border border-transparent focus:border-blue-500 text-sm font-semibold text-slate-900 dark:text-white focus:outline-none"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1">
                      Take-Profit ($)
                    </label>
                    <input
                      type="number"
                      step="0.01"
                      value={takeProfitPrice || ''}
                      onChange={(e) => setTakeProfitPrice(e.target.value ? Number(e.target.value) : undefined)}
                      placeholder="Optional"
                      className="w-full px-4 py-2.5 rounded-2xl bg-slate-100 dark:bg-slate-800 border border-transparent focus:border-blue-500 text-sm font-semibold text-slate-900 dark:text-white focus:outline-none"
                    />
                  </div>
                </div>

                <div className="flex justify-end space-x-3 pt-2">
                  <button
                    type="button"
                    onClick={() => setIsCreateOpen(false)}
                    className="px-4 py-2 text-sm font-semibold text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={createMutation.isPending}
                    className="px-5 py-2 text-sm font-bold text-white bg-blue-600 hover:bg-blue-700 rounded-xl shadow-md shadow-blue-500/20"
                  >
                    {createMutation.isPending ? 'Creating...' : 'Save Position'}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}

      {/* Delete Confirmation */}
      <ConfirmationDialog
        isOpen={!!deletingId}
        title="Delete Position?"
        message="Are you sure you want to delete this position record?"
        confirmText="Delete"
        onConfirm={() => deletingId && deleteMutation.mutate(deletingId)}
        onCancel={() => setDeletingId(null)}
      />
    </div>
  );
};
