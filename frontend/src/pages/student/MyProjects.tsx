import React, { useState, useEffect } from 'react';
import { api } from '../../services/api';
import { ProjectResponse } from '../../types';
import { StatusBadge } from '../../components/StatusBadge';
import { Link } from 'react-router-dom';
import { FolderGit2, PlusCircle, Eye, Download, Play } from 'lucide-react';

export const MyProjects: React.FC = () => {
  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchProjects();
  }, []);

  const fetchProjects = async () => {
    try {
      const res = await api.get('/projects');
      if (res.data.success) {
        setProjects(res.data.data);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const downloadPdf = async (projectId: number) => {
    try {
      const response = await api.get(`/reports/${projectId}/pdf`, { responseType: 'blob' });
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `ProjectEval_Scorecard_${projectId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (e) {
      alert('PDF generation requires completed evaluation.');
    }
  };

  return (
    <div className="space-y-6">
      
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-slate-800 gap-4">
        <div>
          <h1 className="text-2xl font-black text-white">My Submitted Projects</h1>
          <p className="text-xs text-slate-400 mt-1">
            Track automated evaluation status, faculty comments, and download official performance reports.
          </p>
        </div>

        <Link
          to="/student/submit"
          className="inline-flex items-center space-x-2 px-4 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-semibold text-xs shadow-lg shadow-emerald-600/25 transition self-start sm:self-auto"
        >
          <PlusCircle className="w-4 h-4" />
          <span>New Submission</span>
        </Link>
      </div>

      <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800">
        {projects.length === 0 ? (
          <div className="p-8 text-center text-slate-400 text-xs">
            No projects found. Click "New Submission" above to get started.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-800/60 text-slate-400 font-semibold uppercase text-[10px]">
                <tr>
                  <th className="p-3">Title & Stack</th>
                  <th className="p-3">Submission Type</th>
                  <th className="p-3">Status</th>
                  <th className="p-3">Automated (85)</th>
                  <th className="p-3">Manual (15)</th>
                  <th className="p-3">Final (100)</th>
                  <th className="p-3">Grade</th>
                  <th className="p-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800 text-slate-300">
                {projects.map((p) => (
                  <tr key={p.id} className="hover:bg-slate-800/40 transition">
                    <td className="p-3">
                      <div className="font-bold text-white text-sm">{p.title}</div>
                      <div className="text-[11px] text-slate-400">{p.projectType} • {p.technologyStack || 'Polyglot'}</div>
                    </td>
                    <td className="p-3 text-slate-400">
                      {p.repositoryUrl ? 'GitHub' : 'ZIP File'}
                    </td>
                    <td className="p-3"><StatusBadge status={p.status} /></td>
                    <td className="p-3 font-semibold text-emerald-400">
                      {p.automatedScore !== null && p.automatedScore !== undefined ? `${p.automatedScore} / 85` : '—'}
                    </td>
                    <td className="p-3 font-semibold text-purple-400">
                      {p.manualScore !== null && p.manualScore !== undefined ? `${p.manualScore} / 15` : '—'}
                    </td>
                    <td className="p-3 font-bold text-amber-400">
                      {p.finalScore !== null && p.finalScore !== undefined ? `${p.finalScore} / 100` : '—'}
                    </td>
                    <td className="p-3 font-extrabold text-white">
                      {p.grade || '—'}
                    </td>
                    <td className="p-3 text-right">
                      <div className="flex items-center justify-end space-x-2">
                        <Link
                          to={`/student/projects/${p.id}`}
                          className="px-2.5 py-1.5 rounded-lg bg-emerald-600/20 text-emerald-400 hover:bg-emerald-600/30 font-semibold transition text-xs flex items-center space-x-1"
                        >
                          <Eye className="w-3.5 h-3.5" />
                          <span>Details</span>
                        </Link>
                        <button
                          onClick={() => downloadPdf(p.id)}
                          className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 transition"
                          title="Download PDF Scorecard"
                        >
                          <Download className="w-3.5 h-3.5" />
                        </button>
                      </div>
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
