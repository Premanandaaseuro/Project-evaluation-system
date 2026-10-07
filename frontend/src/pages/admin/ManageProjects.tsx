import React, { useState, useEffect } from 'react';
import { api } from '../../services/api';
import { ProjectResponse, EvaluatorDto } from '../../types';
import { StatusBadge } from '../../components/StatusBadge';
import { Link } from 'react-router-dom';
import { FolderGit2, Play, UserCheck, Search, Filter, Eye, Download } from 'lucide-react';

export const ManageProjects: React.FC = () => {
  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [evaluators, setEvaluators] = useState<EvaluatorDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  
  // Assignment modal state
  const [selectedProject, setSelectedProject] = useState<ProjectResponse | null>(null);
  const [selectedEvaluatorId, setSelectedEvaluatorId] = useState<number | ''>('');
  const [assigning, setAssigning] = useState(false);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const [projRes, evalRes] = await Promise.all([
        api.get('/projects'),
        api.get('/users/evaluators')
      ]);
      if (projRes.data.success) setProjects(projRes.data.data);
      if (evalRes.data.success) setEvaluators(evalRes.data.data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const handleAssign = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedProject || !selectedEvaluatorId) return;
    setAssigning(true);
    try {
      const res = await api.post(`/projects/${selectedProject.id}/assign`, {
        evaluatorId: Number(selectedEvaluatorId)
      });
      if (res.data.success) {
        setProjects(prev => prev.map(p => p.id === selectedProject.id ? res.data.data : p));
        setSelectedProject(null);
        setSelectedEvaluatorId('');
      }
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to assign evaluator');
    } finally {
      setAssigning(false);
    }
  };

  const runEvaluation = async (projectId: number) => {
    try {
      alert('Triggering automated evaluation pipeline. This runs in sandbox...');
      const res = await api.post(`/evaluations/project/${projectId}/run`);
      if (res.data.success) {
        fetchData();
      }
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to run evaluation');
    }
  };

  const filtered = projects.filter(p => {
    const matchesSearch = p.title.toLowerCase().includes(search.toLowerCase()) ||
                          p.studentName.toLowerCase().includes(search.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || p.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  return (
    <div className="space-y-6">
      
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-slate-800 gap-4">
        <div>
          <h1 className="text-2xl font-black text-white">Project Submissions Directory</h1>
          <p className="text-xs text-slate-400 mt-1">
            Review all submissions, assign faculty evaluators, and inspect live evaluation pipelines.
          </p>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4 p-4 rounded-xl bg-slate-900 border border-slate-800">
        <div className="relative w-full sm:w-80">
          <Search className="w-4 h-4 text-slate-500 absolute left-3 top-3" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search by project or student..."
            className="w-full pl-9 pr-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-500 focus:ring-2 focus:ring-emerald-500 outline-none"
          />
        </div>

        <div className="flex items-center space-x-2 w-full sm:w-auto">
          <Filter className="w-4 h-4 text-slate-400" />
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white outline-none"
          >
            <option value="ALL">All Statuses</option>
            <option value="DRAFT">DRAFT</option>
            <option value="SUBMITTED">SUBMITTED</option>
            <option value="ASSIGNED">ASSIGNED</option>
            <option value="EVALUATING">EVALUATING</option>
            <option value="EVALUATED">EVALUATED</option>
          </select>
        </div>
      </div>

      {/* Projects Table */}
      <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-800/60 text-slate-400 font-semibold uppercase text-[10px]">
              <tr>
                <th className="p-3">Title & Stack</th>
                <th className="p-3">Student</th>
                <th className="p-3">Status</th>
                <th className="p-3">Automated (85)</th>
                <th className="p-3">Manual (15)</th>
                <th className="p-3">Final (100)</th>
                <th className="p-3">Assigned Evaluator</th>
                <th className="p-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800 text-slate-300">
              {filtered.map((p) => (
                <tr key={p.id} className="hover:bg-slate-800/40 transition">
                  <td className="p-3">
                    <div className="font-bold text-white text-sm">{p.title}</div>
                    <div className="text-[11px] text-slate-400">{p.projectType} • {p.technologyStack || 'Polyglot'}</div>
                  </td>
                  <td className="p-3">
                    <div className="font-medium text-slate-200">{p.studentName}</div>
                    <div className="text-[10px] text-slate-500">{p.studentDepartment}</div>
                  </td>
                  <td className="p-3"><StatusBadge status={p.status} /></td>
                  <td className="p-3 font-semibold text-emerald-400">
                    {p.automatedScore !== null && p.automatedScore !== undefined ? `${p.automatedScore}/85` : '—'}
                  </td>
                  <td className="p-3 font-semibold text-purple-400">
                    {p.manualScore !== null && p.manualScore !== undefined ? `${p.manualScore}/15` : '—'}
                  </td>
                  <td className="p-3 font-black text-amber-400">
                    {p.finalScore !== null && p.finalScore !== undefined ? `${p.finalScore} (${p.grade})` : '—'}
                  </td>
                  <td className="p-3">
                    {p.assignedEvaluatorName ? (
                      <span className="text-slate-200 font-medium">{p.assignedEvaluatorName}</span>
                    ) : (
                      <button
                        onClick={() => setSelectedProject(p)}
                        className="text-xs text-purple-400 hover:text-purple-300 font-semibold underline"
                      >
                        Assign Evaluator
                      </button>
                    )}
                  </td>
                  <td className="p-3 text-right">
                    <div className="flex items-center justify-end space-x-1.5">
                      <button
                        onClick={() => runEvaluation(p.id)}
                        className="p-1.5 rounded bg-emerald-600/20 text-emerald-400 hover:bg-emerald-600/30 transition"
                        title="Re-run Automated Evaluation"
                      >
                        <Play className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => setSelectedProject(p)}
                        className="p-1.5 rounded bg-purple-600/20 text-purple-400 hover:bg-purple-600/30 transition"
                        title="Assign / Reassign Evaluator"
                      >
                        <UserCheck className="w-3.5 h-3.5" />
                      </button>
                      <Link
                        to={`/student/projects/${p.id}`}
                        className="p-1.5 rounded bg-slate-800 text-slate-200 hover:bg-slate-700 transition"
                        title="View Scorecard"
                      >
                        <Eye className="w-3.5 h-3.5" />
                      </Link>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Assign Evaluator Modal */}
      {selectedProject && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 shadow-2xl">
            <h3 className="text-lg font-bold text-white mb-2">Assign Faculty Evaluator</h3>
            <p className="text-xs text-slate-400 mb-4">
              Select an evaluator for project: <strong className="text-slate-200">{selectedProject.title}</strong>
            </p>

            <form onSubmit={handleAssign} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">Select Evaluator</label>
                <select
                  required
                  value={selectedEvaluatorId}
                  onChange={(e) => setSelectedEvaluatorId(Number(e.target.value))}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-xl text-white text-xs outline-none"
                >
                  <option value="">-- Choose Evaluator --</option>
                  {evaluators.map((ev) => (
                    <option key={ev.id} value={ev.id}>
                      {ev.fullName} ({ev.department} - {ev.employeeCode})
                    </option>
                  ))}
                </select>
              </div>

              <div className="flex justify-end space-x-2 pt-2">
                <button
                  type="button"
                  onClick={() => setSelectedProject(null)}
                  className="px-4 py-2 rounded-xl bg-slate-800 text-slate-300 hover:bg-slate-700 text-xs font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={assigning || !selectedEvaluatorId}
                  className="px-4 py-2 rounded-xl bg-purple-600 hover:bg-purple-500 text-white text-xs font-semibold shadow-lg shadow-purple-600/20 disabled:opacity-50"
                >
                  {assigning ? 'Assigning...' : 'Confirm Assignment'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
};
