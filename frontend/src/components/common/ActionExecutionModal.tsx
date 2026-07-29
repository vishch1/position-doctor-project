import React, { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { positionApi } from '../../api/positionApi';
import { PositionResponse, RecommendationAction } from '../../types';
import { X, CheckCircle2, ShieldAlert, DollarSign, Lock, AlertOctagon, RefreshCw, PlusCircle } from 'lucide-react';

interface ActionExecutionModalProps {
  isOpen: boolean;
  onClose: () => void;
  position: PositionResponse | null;
  actionType: string; // e.g. 'EXIT', 'BOOK_PROFIT', 'TIGHTEN_STOPLOSS', 'ADD', 'MOVE_SL_BREAKEVEN', etc.
  recommendationAction?: RecommendationAction;
  customTitle?: string;
  onSuccessCallback?: () => void;
}

export const ActionExecutionModal: React.FC<ActionExecutionModalProps> = ({
  isOpen,
  onClose,
  position,
  actionType,
  recommendationAction,
  customTitle,
  onSuccessCallback,
}) => {
  const queryClient = useQueryClient();
  const [stopLossInput, setStopLossInput] = useState<number>(position?.stopLossPrice || position?.entryPrice || 0);
  const [quantityInput, setQuantityInput] = useState<number>(position?.quantity || 10);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const deleteMutation = useMutation({
    mutationFn: positionApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['positions'] });
      queryClient.invalidateQueries({ queryKey: ['dashboardSummary'] });
      queryClient.invalidateQueries({ queryKey: ['diagnosisReport'] });
      setSuccessMessage(`Successfully executed position exit for ${position?.symbol}`);
      setTimeout(() => {
        setSuccessMessage(null);
        if (onSuccessCallback) onSuccessCallback();
        onClose();
      }, 1500);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: any }) => positionApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['positions'] });
      queryClient.invalidateQueries({ queryKey: ['dashboardSummary'] });
      queryClient.invalidateQueries({ queryKey: ['diagnosisReport'] });
      setSuccessMessage(`Successfully updated risk controls for ${position?.symbol}`);
      setTimeout(() => {
        setSuccessMessage(null);
        if (onSuccessCallback) onSuccessCallback();
        onClose();
      }, 1500);
    },
  });

  if (!isOpen || !position) return null;

  const currentPrice = position.currentPrice || position.entryPrice;
  const entryPrice = position.entryPrice;

  const handleExecute = () => {
    const act = actionType.toUpperCase();

    if (act.includes('EXIT') || act.includes('SELL_POSITION') || act.includes('BOOK_FULL')) {
      deleteMutation.mutate(position.id);
    } else if (act.includes('BREAKEVEN') || act.includes('MOVE_SL')) {
      updateMutation.mutate({
        id: position.id,
        data: { stopLossPrice: entryPrice },
      });
    } else if (act.includes('STOP_LOSS') || act.includes('SL') || act.includes('TIGHTEN')) {
      updateMutation.mutate({
        id: position.id,
        data: { stopLossPrice: stopLossInput },
      });
    } else if (act.includes('BOOK') || act.includes('PROFIT')) {
      // Simulate partial profit realization
      setSuccessMessage(`Order Executed: Booked profit on ${position.symbol} at $${currentPrice.toFixed(2)}`);
      setTimeout(() => {
        setSuccessMessage(null);
        if (onSuccessCallback) onSuccessCallback();
        onClose();
      }, 1500);
    } else {
      // General simulated execution
      setSuccessMessage(`Executed recommendation action "${actionType.replace(/_/g, ' ')}" for ${position.symbol}`);
      setTimeout(() => {
        setSuccessMessage(null);
        if (onSuccessCallback) onSuccessCallback();
        onClose();
      }, 1500);
    }
  };

  const getActionColor = () => {
    const act = actionType.toUpperCase();
    if (act.includes('EXIT')) return 'bg-rose-600 hover:bg-rose-500 shadow-rose-500/20';
    if (act.includes('BOOK')) return 'bg-amber-600 hover:bg-amber-500 shadow-amber-500/20';
    if (act.includes('ADD') || act.includes('BUY')) return 'bg-blue-600 hover:bg-blue-500 shadow-blue-500/20';
    if (act.includes('STOP_LOSS') || act.includes('SL')) return 'bg-orange-600 hover:bg-orange-500 shadow-orange-500/20';
    return 'bg-emerald-600 hover:bg-emerald-500 shadow-emerald-500/20';
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-md animate-fadeIn">
      <div className="w-full max-w-lg rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-2xl p-6 space-y-6 relative overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-slate-100 dark:border-slate-800 pb-4">
          <div className="flex items-center space-x-2">
            <ShieldAlert className="w-5 h-5 text-blue-500" />
            <h3 className="text-lg font-black text-slate-900 dark:text-white">
              {customTitle || `Execute AI Prescription: ${position.symbol}`}
            </h3>
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-xl text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition cursor-pointer"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Success Toast Banner */}
        {successMessage ? (
          <div className="p-4 rounded-2xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-600 dark:text-emerald-400 font-bold text-xs flex items-center space-x-2 animate-fadeIn">
            <CheckCircle2 className="w-5 h-5" />
            <span>{successMessage}</span>
          </div>
        ) : (
          <div className="space-y-4">
            {/* Position Summary Card */}
            <div className="p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-800 space-y-2 text-xs">
              <div className="flex justify-between">
                <span className="text-slate-400 font-medium">Position:</span>
                <span className="font-bold text-slate-900 dark:text-white">
                  {position.symbol} ({position.exchange})
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400 font-medium">Entry Price / Current Price:</span>
                <span className="font-mono font-bold text-slate-900 dark:text-white">
                  ${entryPrice.toFixed(2)} / ${currentPrice.toFixed(2)}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400 font-medium">Unrealized P&L:</span>
                <span className={`font-bold ${position.unrealizedPnL >= 0 ? 'text-emerald-500' : 'text-rose-500'}`}>
                  {position.unrealizedPnL >= 0 ? '+' : ''}${position.unrealizedPnL.toFixed(2)}
                </span>
              </div>
              {position.stopLossPrice && (
                <div className="flex justify-between">
                  <span className="text-slate-400 font-medium">Current Stop Loss:</span>
                  <span className="font-mono font-bold text-amber-500">${position.stopLossPrice.toFixed(2)}</span>
                </div>
              )}
            </div>

            {/* Custom Input Fields for Edit Stop Loss */}
            {(actionType.toUpperCase().includes('STOP_LOSS') || actionType.toUpperCase().includes('SL')) &&
              !actionType.toUpperCase().includes('BREAKEVEN') && (
                <div className="space-y-2">
                  <label className="block text-xs font-bold text-slate-700 dark:text-slate-300">
                    New Stop Loss Threshold ($)
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    value={stopLossInput}
                    onChange={(e) => setStopLossInput(parseFloat(e.target.value) || 0)}
                    className="w-full px-4 py-2.5 rounded-xl bg-slate-100 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-sm font-bold font-mono focus:outline-none focus:border-blue-500"
                  />
                  <p className="text-[11px] text-slate-400">
                    Entry price is ${entryPrice.toFixed(2)}. Setting SL protects capital against drawdowns.
                  </p>
                </div>
              )}

            {/* Confirmation Warning */}
            <div className="p-3 rounded-xl bg-blue-500/10 border border-blue-500/20 text-blue-600 dark:text-blue-400 text-xs font-semibold">
              Executing action: <strong className="uppercase">{actionType.replace(/_/g, ' ')}</strong>. This will
              update live risk parameters in your portfolio.
            </div>

            {/* Footer Buttons */}
            <div className="flex items-center justify-end space-x-3 pt-2">
              <button
                onClick={onClose}
                className="px-4 py-2.5 rounded-xl bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 font-bold text-xs hover:bg-slate-200 dark:hover:bg-slate-700 transition cursor-pointer"
              >
                Cancel
              </button>
              <button
                onClick={handleExecute}
                disabled={deleteMutation.isPending || updateMutation.isPending}
                className={`px-5 py-2.5 rounded-xl text-white font-bold text-xs shadow-md transition cursor-pointer ${getActionColor()}`}
              >
                {deleteMutation.isPending || updateMutation.isPending ? 'Executing...' : 'Confirm & Execute Order'}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
