import React, { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { ThemeToggle } from './ThemeToggle';
import { Bell, Clock, Search, ShieldCheck } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const TopNavbar: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [timeStr, setTimeStr] = useState<string>('');

  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      setTimeStr(now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }));
    };
    updateTime();
    const interval = setInterval(updateTime, 1000);
    return () => clearInterval(interval);
  }, []);

  return (
    <header className="h-20 bg-white/80 dark:bg-slate-900/80 backdrop-blur-md border-b border-slate-200 dark:border-slate-800 px-8 flex items-center justify-between sticky top-0 z-30 transition-colors">
      {/* Search Input */}
      <div className="relative w-80">
        <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
        <input
          type="text"
          placeholder="Search symbols (e.g. AAPL, RELIANCE, TSLA)..."
          className="w-full pl-10 pr-4 py-2 rounded-2xl bg-slate-100 dark:bg-slate-800/80 border border-transparent focus:border-blue-500 text-xs font-semibold text-slate-900 dark:text-white placeholder-slate-400 focus:outline-none transition"
        />
      </div>

      {/* Right Controls */}
      <div className="flex items-center space-x-4">
        {/* Live Feed Vital Badge */}
        <div className="hidden md:flex items-center space-x-2 px-3 py-1.5 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-600 dark:text-emerald-400 text-xs font-bold">
          <span className="w-2 h-2 rounded-full bg-emerald-500 animate-ping" />
          <ShieldCheck className="w-3.5 h-3.5" />
          <span>Finnhub Live Vitals</span>
        </div>

        {/* Live Clock */}
        <div className="hidden lg:flex items-center space-x-1.5 px-3 py-1.5 rounded-2xl bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300 text-xs font-mono font-bold">
          <Clock className="w-3.5 h-3.5 text-blue-500" />
          <span>{timeStr}</span>
        </div>

        {/* Notification Bell */}
        <button
          onClick={() => navigate('/alerts')}
          className="p-2.5 rounded-xl bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-200 hover:bg-slate-200 dark:hover:bg-slate-700 transition relative"
          title="Alerts & Medical Logs"
        >
          <Bell className="w-4 h-4" />
          <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-rose-500 animate-pulse" />
        </button>

        {/* Theme Toggle */}
        <ThemeToggle />

        {/* User Avatar */}
        <div className="flex items-center space-x-3 pl-2 border-l border-slate-200 dark:border-slate-800">
          <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-blue-600 to-indigo-600 flex items-center justify-center text-white font-bold text-sm shadow-md shadow-blue-500/20">
            {user?.firstName?.[0] || 'D'}{user?.lastName?.[0] || 'R'}
          </div>
          <div className="hidden sm:block text-left">
            <h4 className="text-xs font-bold text-slate-900 dark:text-white leading-tight">
              {user?.firstName ? `${user.firstName} ${user.lastName}` : 'Dr. Trader'}
            </h4>
            <span className="text-[10px] text-slate-400 font-semibold">{user?.email || 'trader@positiondoctor.com'}</span>
          </div>
        </div>
      </div>
    </header>
  );
};
