import React, { useState, useEffect } from 'react';
import { api } from '../../services/api';
import { AdminDashboardDto } from '../../types';
import { StatusBadge } from '../../components/StatusBadge';
import { Link } from 'react-router-dom';
import {
  Users,
  FolderGit2,
  Clock,
  CheckCircle,
  Award,
  TrendingUp,
  BarChart3,
  Sliders,
  ArrowUpRight,
} from 'lucide-react';

export const AdminDashboard: React.FC = () => {
  const [data, setData] = useState<AdminDashboardDto | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboard();
  }, []);

  const fetchDashboard = async () => {
    try {
      const res = await api.get('/dashboard/admin');
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
        <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-emerald-500"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-slate-800 gap-4">
        <div>
          <h1 className="text-2xl font-black text-white">System Administration Dashboard</h1>
          <p className="text-xs text-slate-400 mt-1">
            Global evaluation metrics, grading analytics, and faculty assignment controls.
          </p>
        </div>

        <div className="flex items-center space-x-2">
          <Link
            to="/admin/projects"
            className="px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-slate-200 border border-slate-700 transition"
          >
            Manage Projects
          </Link>
          <Link
            to="/admin/settings"
            className="px-3.5 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-xs font-semibold text-white transition shadow-lg shadow-emerald-600/20"
          >
            Configure Rubric
          </Link>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400">Total Projects</span>
            <FolderGit2 className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-bold text-white mt-2">{data.totalProjects}</div>
          <div className="text-[11px] text-slate-500 mt-1">{data.submittedProjects} submitted</div>
        </div>

        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400">Students Enrolled</span>
            <Users className="w-4 h-4 text-blue-400" />
          </div>
          <div className="text-2xl font-bold text-white mt-2">{data.totalStudents}</div>
          <div className="text-[11px] text-slate-500 mt-1">{data.totalEvaluators} evaluators assigned</div>
        </div>

        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400">Evaluation Progress</span>
            <Clock className="w-4 h-4 text-amber-400" />
          </div>
          <div className="text-2xl font-bold text-white mt-2">{data.completedEvaluations} <span className="text-sm font-normal text-slate-400">done</span></div>
          <div className="text-[11px] text-slate-500 mt-1">{data.pendingEvaluations} pending reviews</div>
        </div>

        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400">Average Performance</span>
            <Award className="w-4 h-4 text-purple-400" />
          </div>
          <div className="text-2xl font-bold text-purple-400 mt-2">{data.averageScore} <span className="text-sm font-normal text-slate-400">/ 100</span></div>
          <div className="text-[11px] text-slate-500 mt-1">High: {data.highestScore} | Low: {data.lowestScore}</div>
        </div>
      </div>

      {/* Analytics Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Project Status Breakdown */}
        <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800">
          <div className="flex items-center space-x-2 mb-4">
            <BarChart3 className="w-4 h-4 text-emerald-400" />
            <h3 className="font-bold text-white text-sm">Projects by Status</h3>
          </div>
          <div className="space-y-3">
            {Object.entries(data.statusDistribution || {}).map(([status, count]) => {
              const total = data.totalProjects > 0 ? data.totalProjects : 1;
              const percent = Math.round((Number(count) / total) * 100);
              return (
                <div key={status} className="space-y-1">
                  <div className="flex justify-between text-xs">
                    <span className="text-slate-300 font-medium">{status}</span>
                    <span className="text-slate-400">{count} ({percent}%)</span>
                  </div>
                  <div className="w-full bg-slate-800 rounded-full h-2 overflow-hidden">
                    <div className="bg-emerald-500 h-2 rounded-full" style={{ width: `${percent}%` }}></div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Grade Distribution */}
        <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800">
          <div className="flex items-center space-x-2 mb-4">
            <Award className="w-4 h-4 text-amber-400" />
            <h3 className="font-bold text-white text-sm">Grade Distribution</h3>
          </div>
          <div className="grid grid-cols-3 gap-2">
            {Object.entries(data.gradeDistribution || {}).map(([grade, count]) => (
              <div key={grade} className="p-3 rounded-xl bg-slate-800/60 border border-slate-700/60 text-center">
                <div className="text-lg font-black text-white">{grade}</div>
                <div className="text-xs text-emerald-400 font-semibold">{count} students</div>
              </div>
            ))}
          </div>
          <div className="mt-4 p-3 bg-slate-800/40 rounded-xl border border-slate-800 text-[11px] text-slate-400">
            Automated evaluation accounts for 85% of total score; manual evaluation accounts for 15%.
          </div>
        </div>

        {/* Department Averages & Comparison */}
        <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800">
          <div className="flex items-center space-x-2 mb-4">
            <TrendingUp className="w-4 h-4 text-purple-400" />
            <h3 className="font-bold text-white text-sm">Department Performance</h3>
          </div>
          <div className="space-y-3">
            {Object.entries(data.departmentAverages || {}).map(([dept, avg]) => (
              <div key={dept} className="flex justify-between items-center p-2.5 rounded-lg bg-slate-800/40 border border-slate-800">
                <span className="text-xs text-slate-300 font-medium">{dept}</span>
                <span className="text-xs font-bold text-emerald-400">{avg} / 100</span>
              </div>
            ))}
          </div>

          <div className="mt-4 pt-3 border-t border-slate-800 flex justify-between text-xs">
            <span className="text-slate-400">Avg Automated: <strong className="text-white">{data.scoreComparison?.avgAutomated || 0}</strong></span>
            <span className="text-slate-400">Avg Manual: <strong className="text-white">{data.scoreComparison?.avgManual || 0}</strong></span>
          </div>
        </div>

      </div>

      {/* Recent Submissions Table */}
      <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800">
        <div className="flex items-center justify-between mb-4">
          <h3 className="font-bold text-white text-sm">Recent Project Submissions</h3>
          <Link to="/admin/projects" className="text-xs text-emerald-400 hover:underline flex items-center space-x-1">
            <span>View all projects</span>
            <ArrowUpRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-800/60 text-slate-400 font-semibold uppercase text-[10px]">
              <tr>
                <th className="p-3">Project Title</th>
                <th className="p-3">Student</th>
                <th className="p-3">Type</th>
                <th className="p-3">Status</th>
                <th className="p-3">Score</th>
                <th className="p-3">Assigned Evaluator</th>
                <th className="p-3 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800 text-slate-300">
              {data.recentProjects?.map((p) => (
                <tr key={p.id} className="hover:bg-slate-800/40 transition">
                  <td className="p-3 font-semibold text-white">{p.title}</td>
                  <td className="p-3">{p.studentName}</td>
                  <td className="p-3 text-slate-400">{p.projectType}</td>
                  <td className="p-3"><StatusBadge status={p.status} /></td>
                  <td className="p-3 font-bold text-emerald-400">
                    {p.finalScore !== null && p.finalScore !== undefined ? `${p.finalScore} (${p.grade})` : '—'}
                  </td>
                  <td className="p-3 text-slate-300">
                    {p.assignedEvaluatorName || <span className="text-slate-500 italic">Unassigned</span>}
                  </td>
                  <td className="p-3 text-right">
                    <Link
                      to={`/student/projects/${p.id}`}
                      className="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-200 font-medium transition"
                    >
                      View
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

    </div>
  );
};
