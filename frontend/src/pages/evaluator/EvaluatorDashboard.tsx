import React, { useState, useEffect } from 'react';
import { api } from '../../services/api';
import { EvaluatorDashboardDto } from '../../types';
import { StatusBadge } from '../../components/StatusBadge';
import { Link } from 'react-router-dom';
import { FolderGit2, CheckCircle, Clock, FileCheck, ArrowRight } from 'lucide-react';

export const EvaluatorDashboard: React.FC = () => {
  const [data, setData] = useState<EvaluatorDashboardDto | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboard();
  }, []);

  const fetchDashboard = async () => {
    try {
      const res = await api.get('/dashboard/evaluator');
      if (res.data.success) {
        setData(res.data.data);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  if (loading || !data) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-purple-500"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-slate-800 gap-4">
        <div>
          <h1 className="text-2xl font-black text-white">Faculty Evaluator Workspace</h1>
          <p className="text-xs text-slate-400 mt-1">
            Review assigned student projects, verify automated test evidence, and award manual evaluation marks.
          </p>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold">
            <span>Assigned Projects</span>
            <FolderGit2 className="w-4 h-4 text-purple-400" />
          </div>
          <div className="text-2xl font-bold text-white mt-2">{data.totalAssigned}</div>
          <div className="text-[11px] text-slate-500 mt-1">Total assigned portfolio</div>
        </div>

        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold">
            <span>Pending Reviews</span>
            <Clock className="w-4 h-4 text-amber-400" />
          </div>
          <div className="text-2xl font-bold text-amber-400 mt-2">{data.pendingEvaluations}</div>
          <div className="text-[11px] text-slate-500 mt-1">Awaiting manual marks</div>
        </div>

        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold">
            <span>Completed Reviews</span>
            <CheckCircle className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-bold text-emerald-400 mt-2">{data.completedEvaluations}</div>
          <div className="text-[11px] text-slate-500 mt-1">Evaluated & graded</div>
        </div>
      </div>

      {/* Assigned Projects Table */}
      <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800">
        <div className="flex items-center justify-between mb-4">
          <h3 className="font-bold text-white text-sm">Assigned Projects Queue</h3>
          <span className="text-xs text-slate-400">{data.assignedProjects.length} Projects</span>
        </div>

        {data.assignedProjects.length === 0 ? (
          <div className="p-8 text-center text-slate-400 text-xs">
            No projects currently assigned to you.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-800/60 text-slate-400 font-semibold uppercase text-[10px]">
                <tr>
                  <th className="p-3">Project Title</th>
                  <th className="p-3">Student</th>
                  <th className="p-3">Type</th>
                  <th className="p-3">Status</th>
                  <th className="p-3">Automated (85)</th>
                  <th className="p-3">Manual (15)</th>
                  <th className="p-3">Final (100)</th>
                  <th className="p-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800 text-slate-300">
                {data.assignedProjects.map((p) => (
                  <tr key={p.id} className="hover:bg-slate-800/40 transition">
                    <td className="p-3">
                      <div className="font-bold text-white text-sm">{p.title}</div>
                      <div className="text-[11px] text-slate-400">{p.technologyStack || p.projectType}</div>
                    </td>
                    <td className="p-3">
                      <div className="font-medium text-slate-200">{p.studentName}</div>
                      <div className="text-[10px] text-slate-500">{p.studentDepartment}</div>
                    </td>
                    <td className="p-3 text-slate-400">{p.projectType}</td>
                    <td className="p-3"><StatusBadge status={p.status} /></td>
                    <td className="p-3 font-semibold text-emerald-400">
                      {p.automatedScore !== null && p.automatedScore !== undefined ? `${p.automatedScore} / 85` : '—'}
                    </td>
                    <td className="p-3 font-semibold text-purple-400">
                      {p.manualScore !== null && p.manualScore !== undefined ? `${p.manualScore} / 15` : (
                        <span className="text-amber-400 font-medium">Pending Review</span>
                      )}
                    </td>
                    <td className="p-3 font-black text-amber-400">
                      {p.finalScore !== null && p.finalScore !== undefined ? `${p.finalScore} (${p.grade})` : '—'}
                    </td>
                    <td className="p-3 text-right">
                      <Link
                        to={`/evaluator/projects/${p.id}/evaluate`}
                        className="inline-flex items-center space-x-1.5 px-3 py-1.5 rounded-lg bg-purple-600 hover:bg-purple-500 text-white font-semibold transition text-xs shadow-md shadow-purple-600/20"
                      >
                        <FileCheck className="w-3.5 h-3.5" />
                        <span>{p.manualScore !== null && p.manualScore !== undefined ? 'Review / Edit' : 'Evaluate'}</span>
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

    </div>
  );
};
