import React from 'react';
import { RecommendationAction } from '../../types';
import { CheckCircle2, PlusCircle, DollarSign, Lock, AlertOctagon } from 'lucide-react';

interface RecommendationBadgeProps {
  action?: RecommendationAction;
  showIcon?: boolean;
}

export const RecommendationBadge: React.FC<RecommendationBadgeProps> = ({
  action = 'HOLD',
  showIcon = true,
}) => {
  const getBadgeStyle = () => {
    switch (action) {
      case 'HOLD':
        return {
          bg: 'bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border-emerald-500/30',
          icon: <CheckCircle2 className="w-3.5 h-3.5 mr-1" />,
          label: 'HOLD',
        };
      case 'ADD':
        return {
          bg: 'bg-blue-500/15 text-blue-600 dark:text-blue-400 border-blue-500/30',
          icon: <PlusCircle className="w-3.5 h-3.5 mr-1" />,
          label: 'BUY MORE',
        };
      case 'BOOK_PROFIT':
        return {
          bg: 'bg-amber-500/15 text-amber-600 dark:text-amber-400 border-amber-500/30',
          icon: <DollarSign className="w-3.5 h-3.5 mr-1" />,
          label: 'BOOK PROFIT',
        };
      case 'TIGHTEN_STOPLOSS':
        return {
          bg: 'bg-orange-500/15 text-orange-600 dark:text-orange-400 border-orange-500/30',
          icon: <Lock className="w-3.5 h-3.5 mr-1" />,
          label: 'TIGHTEN STOP-LOSS',
        };
      case 'EXIT':
        return {
          bg: 'bg-rose-500/20 text-rose-600 dark:text-rose-400 border-rose-500/40 animate-pulse',
          icon: <AlertOctagon className="w-3.5 h-3.5 mr-1" />,
          label: 'EXIT POSITION',
        };
      default:
        return {
          bg: 'bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border-emerald-500/30',
          icon: <CheckCircle2 className="w-3.5 h-3.5 mr-1" />,
          label: 'HOLD',
        };
    }
  };

  const style = getBadgeStyle();

  return (
    <span
      className={`inline-flex items-center px-2.5 py-1 rounded-md text-xs font-bold border tracking-wider uppercase ${style.bg}`}
    >
      {showIcon && style.icon}
      {style.label}
    </span>
  );
};
