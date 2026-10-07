import React from 'react';
import { ProjectStatus, TestStatus } from '../types';

export const StatusBadge: React.FC<{ status: ProjectStatus | string }> = ({ status }) => {
  const getColors = () => {
    switch (status) {
      case 'DRAFT':
        return 'bg-slate-800 text-slate-300 border-slate-700';
      case 'SUBMITTED':
        return 'bg-blue-900/60 text-blue-300 border-blue-700/50';
      case 'ASSIGNED':
        return 'bg-purple-900/60 text-purple-300 border-purple-700/50';
      case 'EVALUATING':
        return 'bg-amber-900/60 text-amber-300 border-amber-700/50 animate-pulse';
      case 'EVALUATED':
      case 'APPROVED':
        return 'bg-emerald-900/60 text-emerald-300 border-emerald-700/50';
      case 'REJECTED':
        return 'bg-rose-900/60 text-rose-300 border-rose-700/50';
      default:
        return 'bg-slate-800 text-slate-300 border-slate-700';
    }
  };

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${getColors()}`}>
      {status}
    </span>
  );
};

export const TestStatusBadge: React.FC<{ status: TestStatus | string }> = ({ status }) => {
  if (status === 'PASS') {
    return (
      <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-bold bg-emerald-950 text-emerald-400 border border-emerald-800/60">
        PASS ✓
      </span>
    );
  }
  return (
    <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-bold bg-rose-950 text-rose-400 border border-rose-800/60">
      FAIL ✗
    </span>
  );
};

export const GradeBadge: React.FC<{ grade: string }> = ({ grade }) => {
  const getStyle = () => {
    if (grade === 'A+' || grade === 'A') return 'bg-emerald-500/20 text-emerald-400 border-emerald-500/40 text-lg';
    if (grade === 'B') return 'bg-blue-500/20 text-blue-400 border-blue-500/40 text-lg';
    if (grade === 'C') return 'bg-amber-500/20 text-amber-400 border-amber-500/40 text-lg';
    return 'bg-rose-500/20 text-rose-400 border-rose-500/40 text-base';
  };

  return (
    <span className={`inline-flex items-center justify-center font-extrabold px-3 py-1 rounded-lg border shadow-sm ${getStyle()}`}>
      {grade}
    </span>
  );
};
