import React from 'react';
import { HeartPulse } from 'lucide-react';

interface HealthGaugeProps {
  score: number; // 0 to 100
  size?: 'sm' | 'md' | 'lg';
  showLabel?: boolean;
}

export const HealthGauge: React.FC<HealthGaugeProps> = ({
  score,
  size = 'md',
  showLabel = true,
}) => {
  const normalizedScore = Math.max(0, Math.min(100, score));

  const getStatusColor = () => {
    if (normalizedScore >= 75) return { stroke: '#22c55e', text: 'text-emerald-500', label: 'HEALTHY' };
    if (normalizedScore >= 40) return { stroke: '#f59e0b', text: 'text-amber-500', label: 'WARNING' };
    return { stroke: '#ef4444', text: 'text-rose-500', label: 'CRITICAL' };
  };

  const status = getStatusColor();

  const radius = size === 'lg' ? 45 : size === 'md' ? 36 : 24;
  const strokeWidth = size === 'lg' ? 8 : size === 'md' ? 6 : 4;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - (normalizedScore / 100) * circumference;

  const svgSize = (radius + strokeWidth) * 2;

  return (
    <div className="flex flex-col items-center justify-center relative">
      <div className="relative flex items-center justify-center">
        <svg width={svgSize} height={svgSize} className="transform -rotate-90">
          <circle
            cx={svgSize / 2}
            cy={svgSize / 2}
            r={radius}
            stroke="currentColor"
            strokeWidth={strokeWidth}
            fill="transparent"
            className="text-slate-200 dark:text-slate-800"
          />
          <circle
            cx={svgSize / 2}
            cy={svgSize / 2}
            r={radius}
            stroke={status.stroke}
            strokeWidth={strokeWidth}
            strokeDasharray={circumference}
            strokeDashoffset={strokeDashoffset}
            strokeLinecap="round"
            fill="transparent"
            className="transition-all duration-1000 ease-out"
          />
        </svg>

        <div className="absolute inset-0 flex flex-col items-center justify-center text-center">
          <span
            className={`font-black tracking-tight ${status.text} ${
              size === 'lg' ? 'text-2xl' : size === 'md' ? 'text-lg' : 'text-xs'
            }`}
          >
            {normalizedScore}
          </span>
        </div>
      </div>

      {showLabel && (
        <div className="flex items-center mt-1.5 space-x-1">
          <HeartPulse className={`w-3.5 h-3.5 ${status.text} animate-pulse`} />
          <span className={`text-[10px] font-bold tracking-widest uppercase ${status.text}`}>
            {status.label}
          </span>
        </div>
      )}
    </div>
  );
};
