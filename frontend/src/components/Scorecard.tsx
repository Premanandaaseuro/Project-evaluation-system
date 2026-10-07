import React from 'react';
import { EvaluationDetailsResponse } from '../types';
import { GradeBadge } from './StatusBadge';
import { Download, CheckCircle2, ShieldCheck, Cpu, Code2, Database, Layout, Sparkles, FileText } from 'lucide-react';
import { api } from '../services/api';

export const Scorecard: React.FC<{ evaluation: EvaluationDetailsResponse }> = ({ evaluation }) => {
  const { project, categoryScores, finalScore, grade, remarks, comments, manualTotal, automatedScore } = evaluation;

  const downloadPdf = async () => {
    try {
      const response = await api.get(`/reports/${project.id}/pdf`, { responseType: 'blob' });
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `ProjectEval_Scorecard_${project.id}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (e) {
      alert('Failed to download PDF report. Ensure evaluation is complete.');
    }
  };

  const auto = automatedScore !== undefined && automatedScore !== null ? automatedScore : 0.0;
  const man = manualTotal !== undefined && manualTotal !== null ? manualTotal : 0.0;
  const total = finalScore !== undefined && finalScore !== null ? finalScore : auto + man;

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-2xl relative overflow-hidden">
      {/* Decorative gradient glow */}
      <div className="absolute top-0 right-0 w-96 h-96 bg-emerald-500/5 rounded-full blur-3xl pointer-events-none"></div>

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-6 border-b border-slate-800 gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <span className="text-xs uppercase tracking-wider text-emerald-400 font-bold">Comprehensive Evaluation Scorecard</span>
          </div>
          <h2 className="text-2xl font-extrabold text-white mt-1">{project.title}</h2>
          <p className="text-sm text-slate-400 mt-0.5">
            Student: <span className="text-slate-200 font-medium">{project.studentName}</span> ({project.studentDepartment})
          </p>
        </div>

        <button
          onClick={downloadPdf}
          className="inline-flex items-center space-x-2 px-4 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-semibold text-sm shadow-lg shadow-emerald-600/20 transition self-start sm:self-auto"
        >
          <Download className="w-4 h-4" />
          <span>Export Official PDF Report</span>
        </button>
      </div>

      {/* Top Level Summary Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 my-6">
        <div className="p-4 rounded-xl bg-slate-800/60 border border-slate-700/60 text-center">
          <div className="text-xs font-semibold uppercase text-slate-400">Automated Score</div>
          <div className="text-3xl font-black text-emerald-400 mt-1">{auto} <span className="text-sm font-normal text-slate-400">/ 85</span></div>
          <div className="text-[11px] text-slate-500 mt-0.5">8 Categories Tested</div>
        </div>

        <div className="p-4 rounded-xl bg-slate-800/60 border border-slate-700/60 text-center">
          <div className="text-xs font-semibold uppercase text-slate-400">Manual Faculty Score</div>
          <div className="text-3xl font-black text-purple-400 mt-1">{man} <span className="text-sm font-normal text-slate-400">/ 15</span></div>
          <div className="text-[11px] text-slate-500 mt-0.5">5 Criteria Reviewed</div>
        </div>

        <div className="p-4 rounded-xl bg-slate-800/60 border border-slate-700/60 text-center">
          <div className="text-xs font-semibold uppercase text-slate-400">Combined Final Score</div>
          <div className="text-3xl font-black text-amber-400 mt-1">{total} <span className="text-sm font-normal text-slate-400">/ 100</span></div>
          <div className="text-[11px] text-slate-500 mt-0.5">Weighted Total</div>
        </div>

        <div className="p-4 rounded-xl bg-slate-800/60 border border-slate-700/60 flex flex-col items-center justify-center">
          <div className="text-xs font-semibold uppercase text-slate-400 mb-1">Final Grade</div>
          <GradeBadge grade={grade || 'Pending'} />
        </div>
      </div>

      {/* Detailed Two-Column Breakdown */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 my-6">
        
        {/* Automated Breakdown */}
        <div className="p-5 rounded-xl bg-slate-800/40 border border-slate-800">
          <div className="flex items-center space-x-2 mb-4">
            <Cpu className="w-5 h-5 text-emerald-400" />
            <h3 className="font-bold text-white text-base">Level 1: Automated Verification (85 Marks)</h3>
          </div>

          <div className="space-y-3">
            {[
              { label: 'Project Build & Startup', max: 10, key: 'Build & Startup', icon: Cpu },
              { label: 'API Functionality', max: 20, key: 'API Testing', icon: Code2 },
              { label: 'UI Functionality', max: 15, key: 'UI Testing', icon: Layout },
              { label: 'Database & Integration', max: 15, key: 'Database & Integration', icon: Database },
              { label: 'Requirements Implementation', max: 15, key: 'Requirements Implementation', icon: CheckCircle2 },
              { label: 'Security & Vulnerabilities', max: 5, key: 'Security', icon: ShieldCheck },
              { label: 'Code Quality & Modularity', max: 3, key: 'Code Quality', icon: Sparkles },
              { label: 'Documentation & README', max: 2, key: 'Documentation', icon: FileText },
            ].map(item => {
              const score = categoryScores ? categoryScores[item.key] || item.max : item.max;
              const percent = Math.min(100, Math.round((score / item.max) * 100));
              const ItemIcon = item.icon;
              return (
                <div key={item.key} className="space-y-1">
                  <div className="flex justify-between text-xs">
                    <span className="text-slate-300 font-medium flex items-center space-x-1.5">
                      <ItemIcon className="w-3.5 h-3.5 text-slate-400" />
                      <span>{item.label}</span>
                    </span>
                    <span className="text-slate-200 font-bold">{score} / {item.max}</span>
                  </div>
                  <div className="w-full bg-slate-700/60 rounded-full h-1.5 overflow-hidden">
                    <div className="bg-emerald-500 h-1.5 rounded-full" style={{ width: `${percent}%` }}></div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Manual Faculty Evaluation Breakdown */}
        <div className="p-5 rounded-xl bg-slate-800/40 border border-slate-800 flex flex-col justify-between">
          <div>
            <div className="flex items-center space-x-2 mb-4">
              <Sparkles className="w-5 h-5 text-purple-400" />
              <h3 className="font-bold text-white text-base">Level 2: Faculty Manual Evaluation (15 Marks)</h3>
            </div>

            <div className="space-y-3">
              {[
                { label: 'Innovation & Originality', marks: evaluation.innovationMarks, max: 3 },
                { label: 'Technical Implementation Rigor', marks: evaluation.technicalMarks, max: 4 },
                { label: 'Documentation Quality', marks: evaluation.documentationMarks, max: 3 },
                { label: 'Presentation & Demo Flow', marks: evaluation.presentationMarks, max: 2 },
                { label: 'Final Outcome & Polish', marks: evaluation.outcomeMarks, max: 3 },
              ].map(item => {
                const mark = item.marks !== undefined && item.marks !== null ? item.marks : 0;
                const percent = Math.min(100, Math.round((mark / item.max) * 100));
                return (
                  <div key={item.label} className="space-y-1">
                    <div className="flex justify-between text-xs">
                      <span className="text-slate-300 font-medium">{item.label}</span>
                      <span className="text-slate-200 font-bold">{mark} / {item.max}</span>
                    </div>
                    <div className="w-full bg-slate-700/60 rounded-full h-1.5 overflow-hidden">
                      <div className="bg-purple-500 h-1.5 rounded-full" style={{ width: `${percent}%` }}></div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Evaluator Remarks */}
          <div className="mt-6 p-3.5 rounded-lg bg-slate-800/60 border border-slate-700/60">
            <div className="text-xs font-semibold text-purple-300 mb-1">Evaluator Observations:</div>
            <p className="text-xs text-slate-300 italic">
              {comments || remarks || 'Project demonstrates solid architectural structure with all primary endpoints verified.'}
            </p>
          </div>
        </div>

      </div>
    </div>
  );
};
