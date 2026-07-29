import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { alertApi } from '../../../api/alertApi';
import { AlertResponse, AlertSeverity } from '../../../types';
import { LoadingSkeleton } from '../../../components/common/LoadingSkeleton';
import { EmptyState } from '../../../components/common/EmptyState';
import {
  Bell,
  CheckCircle,
  AlertTriangle,
  Info,
  ShieldAlert,
  Search,
  Filter,
  CheckCheck,
} from 'lucide-react';

export const AlertPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');
  const [severityFilter, setSeverityFilter] = useState<string>('ALL');
  const [showUnreadOnly, setShowUnreadOnly] = useState(false);

  const { data: alertsData, isLoading } = useQuery({
    queryKey: ['alerts'],
    queryFn: () => alertApi.getAll(),
    refetchInterval: 15000,
  });

  const markReadMutation = useMutation({
    mutationFn: alertApi.markAsRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['alerts'] });
      queryClient.invalidateQueries({ queryKey: ['dashboardSummary'] });
    },
  });

  const alerts = alertsData?.data || [];

  const filteredAlerts = alerts.filter((alert) => {
    const matchesSearch =
      alert.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      alert.message.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesSeverity = severityFilter === 'ALL' || alert.severity === severityFilter;
    const matchesUnread = !showUnreadOnly || !alert.read;

    return matchesSearch && matchesSeverity && matchesUnread;
  });

  if (isLoading) return <LoadingSkeleton rows={5} />;

  const getSeverityBadge = (severity: AlertSeverity) => {
    switch (severity) {
      case 'CRITICAL':
        return {
          bg: 'bg-rose-500/10 text-rose-500 border-rose-500/20',
          icon: <ShieldAlert className="w-4 h-4 mr-1.5" />,
          label: 'CRITICAL',
        };
      case 'WARNING':
        return {
          bg: 'bg-amber-500/10 text-amber-500 border-amber-500/20',
          icon: <AlertTriangle className="w-4 h-4 mr-1.5" />,
          label: 'WARNING',
        };
      case 'INFO':
      default:
        return {
          bg: 'bg-blue-500/10 text-blue-500 border-blue-500/20',
          icon: <Info className="w-4 h-4 mr-1.5" />,
          label: 'INFO',
        };
    }
  };

  return (
    <div className="space-y-6 animate-fadeIn">
      {/* Header Bar */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2 text-xs font-bold text-blue-600 dark:text-blue-400 uppercase tracking-widest mb-1">
            <Bell className="w-4 h-4" />
            <span>AI Doctor Vital Notifications</span>
          </div>
          <h2 className="text-2xl font-black text-slate-900 dark:text-white tracking-tight">
            Medical Alerts & Health Logs
          </h2>
        </div>

        <button
          onClick={() => {
            alerts.filter((a) => !a.read).forEach((a) => markReadMutation.mutate(a.id));
          }}
          className="px-4 py-2 rounded-2xl bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300 font-bold text-xs transition flex items-center space-x-1.5"
        >
          <CheckCheck className="w-4 h-4 text-blue-500" />
          <span>Mark All as Read</span>
        </button>
      </div>

      {/* Filter & Search Controls */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-4">
        <div className="flex flex-wrap items-center gap-3">
          <div className="relative w-64">
            <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Search alert notifications..."
              className="w-full pl-10 pr-4 py-2 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-xs font-semibold text-slate-900 dark:text-white placeholder-slate-400 focus:outline-none focus:border-blue-500"
            />
          </div>

          <div className="flex items-center space-x-1.5 px-3 py-1.5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-xs font-semibold text-slate-600 dark:text-slate-300">
            <Filter className="w-3.5 h-3.5 text-slate-400" />
            <select
              value={severityFilter}
              onChange={(e) => setSeverityFilter(e.target.value)}
              className="bg-transparent focus:outline-none cursor-pointer"
            >
              <option value="ALL">All Severities</option>
              <option value="CRITICAL">Critical</option>
              <option value="WARNING">Warning</option>
              <option value="INFO">Info</option>
            </select>
          </div>

          <label className="flex items-center space-x-2 text-xs font-bold text-slate-600 dark:text-slate-400 cursor-pointer">
            <input
              type="checkbox"
              checked={showUnreadOnly}
              onChange={(e) => setShowUnreadOnly(e.target.checked)}
              className="rounded text-blue-600 focus:ring-blue-500"
            />
            <span>Unread Only</span>
          </label>
        </div>

        <span className="text-xs font-semibold text-slate-400">
          Showing {filteredAlerts.length} Alerts
        </span>
      </div>

      {/* Alerts List */}
      {filteredAlerts.length > 0 ? (
        <div className="space-y-3">
          {filteredAlerts.map((alert) => {
            const badge = getSeverityBadge(alert.severity);

            return (
              <div
                key={alert.id}
                className={`p-5 rounded-2xl border transition-all duration-200 flex items-start justify-between gap-4 ${
                  alert.read
                    ? 'bg-white dark:bg-slate-900/60 border-slate-200 dark:border-slate-800/80 opacity-80'
                    : 'bg-white dark:bg-slate-900 border-blue-500/30 shadow-md'
                }`}
              >
                <div className="flex items-start space-x-4">
                  <div className={`p-2.5 rounded-xl border ${badge.bg} mt-0.5`}>
                    {badge.icon}
                  </div>

                  <div className="space-y-1">
                    <div className="flex items-center space-x-2">
                      <h4 className="text-sm font-extrabold text-slate-900 dark:text-white">
                        {alert.title}
                      </h4>
                      {!alert.read && (
                        <span className="w-2 h-2 rounded-full bg-blue-500 animate-pulse" />
                      )}
                    </div>
                    <p className="text-xs text-slate-600 dark:text-slate-300 leading-relaxed">
                      {alert.message}
                    </p>
                    <span className="text-[10px] text-slate-400 font-medium block pt-1">
                      {new Date(alert.createdAt || Date.now()).toLocaleString()}
                    </span>
                  </div>
                </div>

                {!alert.read && (
                  <button
                    onClick={() => markReadMutation.mutate(alert.id)}
                    className="px-3 py-1.5 rounded-xl bg-blue-500/10 hover:bg-blue-500/20 text-blue-600 dark:text-blue-400 font-bold text-xs transition"
                  >
                    Mark Read
                  </button>
                )}
              </div>
            );
          })}
        </div>
      ) : (
        <EmptyState
          title="No Alerts Triggered"
          description="Your position vitals are currently stable with no health score shifts or recommendation changes."
        />
      )}
    </div>
  );
};
