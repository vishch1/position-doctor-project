import React from 'react';

interface StatCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  change?: string;
  isPositive?: boolean;
  icon?: React.ReactNode;
  accentColor?: 'blue' | 'green' | 'red' | 'amber' | 'purple';
}

export const StatCard: React.FC<StatCardProps> = ({
  title,
  value,
  subtitle,
  change,
  isPositive,
  icon,
  accentColor = 'blue',
}) => {
  const getAccent = () => {
    switch (accentColor) {
      case 'green':
        return 'from-emerald-500/10 via-transparent to-transparent text-emerald-500';
      case 'red':
        return 'from-rose-500/10 via-transparent to-transparent text-rose-500';
      case 'amber':
        return 'from-amber-500/10 via-transparent to-transparent text-amber-500';
      case 'purple':
        return 'from-purple-500/10 via-transparent to-transparent text-purple-500';
      default:
        return 'from-blue-500/10 via-transparent to-transparent text-blue-500';
    }
  };

  return (
    <div className={`relative p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm hover:shadow-md transition-all duration-200 overflow-hidden group`}>
      <div className={`absolute top-0 right-0 w-24 h-24 bg-gradient-to-bl ${getAccent()} opacity-50 blur-xl pointer-events-none`} />

      <div className="flex items-center justify-between mb-2">
        <span className="text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400">
          {title}
        </span>
        {icon && (
          <div className="p-2 rounded-xl bg-slate-100 dark:bg-slate-800/80 text-slate-700 dark:text-slate-200 group-hover:scale-110 transition-transform">
            {icon}
          </div>
        )}
      </div>

      <div className="flex items-baseline space-x-2">
        <h3 className="text-2xl font-extrabold tracking-tight text-slate-900 dark:text-white">
          {value}
        </h3>
        {change && (
          <span
            className={`text-xs font-bold px-2 py-0.5 rounded-full ${
              isPositive
                ? 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400'
                : 'bg-rose-500/10 text-rose-600 dark:text-rose-400'
            }`}
          >
            {isPositive ? '+' : ''}{change}
          </span>
        )}
      </div>

      {subtitle && (
        <p className="mt-1 text-xs text-slate-500 dark:text-slate-400 font-medium">
          {subtitle}
        </p>
      )}
    </div>
  );
};
