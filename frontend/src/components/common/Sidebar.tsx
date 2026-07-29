import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Briefcase, TrendingUp, Bell, HeartPulse, LogOut } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export const Sidebar: React.FC = () => {
  const { logout } = useAuth();

  const navItems = [
    { label: 'Executive Dashboard', path: '/dashboard', icon: <LayoutDashboard className="w-5 h-5" /> },
    { label: 'Portfolios', path: '/portfolios', icon: <Briefcase className="w-5 h-5" /> },
    { label: 'Position Doctor', path: '/positions', icon: <TrendingUp className="w-5 h-5" /> },
    { label: 'AI Diagnosis Center', path: '/diagnosis', icon: <HeartPulse className="w-5 h-5" /> },
    { label: 'Alerts & Vitals', path: '/alerts', icon: <Bell className="w-5 h-5" /> },
  ];

  return (
    <aside className="w-64 bg-white dark:bg-slate-900 border-r border-slate-200 dark:border-slate-800 flex flex-col justify-between h-screen sticky top-0 z-40 transition-colors">
      <div>
        {/* Medical-Fintech Brand Header */}
        <div className="p-6 flex items-center space-x-3 border-b border-slate-100 dark:border-slate-800/80">
          <div className="p-2.5 rounded-2xl bg-gradient-to-tr from-blue-600 to-indigo-600 text-white shadow-lg shadow-blue-500/25">
            <HeartPulse className="w-6 h-6 animate-pulse" />
          </div>
          <div>
            <h1 className="text-lg font-black tracking-tight text-slate-900 dark:text-white leading-none">
              Position<span className="text-blue-600 dark:text-blue-400">Doctor</span>
            </h1>
            <span className="text-[10px] font-bold tracking-widest text-slate-400 dark:text-slate-500 uppercase">
              AI Portfolio Vitals
            </span>
          </div>
        </div>

        {/* Navigation Links */}
        <nav className="p-4 space-y-1.5">
          {navItems.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `flex items-center space-x-3 px-4 py-3 rounded-2xl font-bold text-sm transition-all duration-200 ${
                  isActive
                    ? 'bg-blue-600 text-white shadow-md shadow-blue-500/25'
                    : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800/70 hover:text-slate-900 dark:hover:text-white'
                }`
              }
            >
              {item.icon}
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>
      </div>

      {/* Footer & Logout */}
      <div className="p-4 border-t border-slate-100 dark:border-slate-800/80">
        <button
          onClick={logout}
          className="w-full flex items-center space-x-3 px-4 py-3 rounded-2xl font-bold text-sm text-rose-600 dark:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-500/10 transition-colors"
        >
          <LogOut className="w-5 h-5" />
          <span>Sign Out</span>
        </button>
      </div>
    </aside>
  );
};
