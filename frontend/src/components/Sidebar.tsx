import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard,
  FolderGit2,
  Users,
  GraduationCap,
  Sliders,
  History,
  PlusCircle,
  FileCheck2,
  ShieldAlert,
} from 'lucide-react';

export const Sidebar: React.FC = () => {
  const { user } = useAuth();

  const getLinks = () => {
    if (user?.role === 'ROLE_ADMIN') {
      return [
        { to: '/admin/dashboard', label: 'Admin Dashboard', icon: LayoutDashboard },
        { to: '/admin/projects', label: 'Manage Projects', icon: FolderGit2 },
        { to: '/admin/students', label: 'Students Directory', icon: GraduationCap },
        { to: '/admin/evaluators', label: 'Faculty Evaluators', icon: Users },
        { to: '/admin/settings', label: 'Rubrics & Criteria', icon: Sliders },
        { to: '/admin/audit-logs', label: 'Audit Trail Logs', icon: History },
      ];
    } else if (user?.role === 'ROLE_EVALUATOR') {
      return [
        { to: '/evaluator/dashboard', label: 'Evaluator Dashboard', icon: LayoutDashboard },
        { to: '/evaluator/projects', label: 'Assigned Projects', icon: FileCheck2 },
      ];
    } else {
      return [
        { to: '/student/dashboard', label: 'Student Dashboard', icon: LayoutDashboard },
        { to: '/student/submit', label: 'Submit New Project', icon: PlusCircle },
        { to: '/student/projects', label: 'My Submissions', icon: FolderGit2 },
      ];
    }
  };

  const links = getLinks();

  return (
    <aside className="w-64 bg-slate-900 border-r border-slate-800 min-h-[calc(100vh-4rem)] p-4 flex flex-col justify-between hidden md:flex">
      <div className="space-y-1">
        <div className="px-3 py-2 text-[11px] font-semibold tracking-wider text-slate-500 uppercase">
          Menu Navigation
        </div>
        {links.map((link) => {
          const Icon = link.icon;
          return (
            <NavLink
              key={link.to}
              to={link.to}
              className={({ isActive }) =>
                `flex items-center space-x-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all ${
                  isActive
                    ? 'bg-emerald-600/15 text-emerald-400 border border-emerald-500/20 font-semibold'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                }`
              }
            >
              <Icon className="w-4 h-4" />
              <span>{link.label}</span>
            </NavLink>
          );
        })}
      </div>

      <div className="p-3 bg-slate-800/40 border border-slate-800 rounded-xl text-xs text-slate-400">
        <div className="flex items-center space-x-2 text-emerald-400 font-semibold mb-1">
          <ShieldAlert className="w-4 h-4" />
          <span>Security Sandbox Active</span>
        </div>
        <p className="text-[11px] text-slate-500">
          Evaluations run in isolated Docker sandboxes with non-root resource limits.
        </p>
      </div>
    </aside>
  );
};
