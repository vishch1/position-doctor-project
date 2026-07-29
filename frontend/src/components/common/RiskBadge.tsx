import React from 'react';
import { RiskLevel } from '../../types';
import { ShieldCheck, ShieldAlert, AlertTriangle, Activity } from 'lucide-react';

interface RiskBadgeProps {
  level?: RiskLevel;
}

export const RiskBadge: React.FC<RiskBadgeProps> = ({ level = 'MODERATE' }) => {
  const getBadgeStyle = () => {
    switch (level) {
      case 'LOW':
        return {
          bg: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20',
          icon: <ShieldCheck className="w-3.5 h-3.5 mr-1" />,
          label: 'LOW RISK',
        };
      case 'MODERATE':
        return {
          bg: 'bg-blue-500/10 text-blue-600 dark:text-blue-400 border-blue-500/20',
          icon: <Activity className="w-3.5 h-3.5 mr-1" />,
          label: 'MODERATE',
        };
      case 'HIGH':
        return {
          bg: 'bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/20',
          icon: <AlertTriangle className="w-3.5 h-3.5 mr-1" />,
          label: 'HIGH RISK',
        };
      case 'CRITICAL':
        return {
          bg: 'bg-rose-500/15 text-rose-600 dark:text-rose-400 border-rose-500/30 animate-pulse',
          icon: <ShieldAlert className="w-3.5 h-3.5 mr-1" />,
          label: 'CRITICAL',
        };
    }
  };

  const style = getBadgeStyle();

  return (
    <span
      className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold border ${style.bg} transition-colors`}
    >
      {style.icon}
      {style.label}
    </span>
  );
};
