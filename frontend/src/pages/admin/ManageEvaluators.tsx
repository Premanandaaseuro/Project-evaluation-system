import React, { useState, useEffect } from 'react';
import { api } from '../../services/api';
import { EvaluatorDto } from '../../types';
import { Users, Search } from 'lucide-react';

export const ManageEvaluators: React.FC = () => {
  const [evaluators, setEvaluators] = useState<EvaluatorDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  useEffect(() => {
    fetchEvaluators();
  }, []);

  const fetchEvaluators = async () => {
    try {
      const res = await api.get('/users/evaluators');
      if (res.data.success) {
        setEvaluators(res.data.data);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const toggleStatus = async (userId: number) => {
    try {
      const res = await api.put(`/users/${userId}/toggle-status`);
      if (res.data.success) {
        setEvaluators(prev => prev.map(e => e.userId === userId ? { ...e, active: res.data.data } : e));
      }
    } catch (e) {
      alert('Failed to update status');
    }
  };

  const filtered = evaluators.filter(e =>
    e.fullName.toLowerCase().includes(search.toLowerCase()) ||
    e.employeeCode.toLowerCase().includes(search.toLowerCase()) ||
    e.email.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-slate-800 gap-4">
        <div>
          <h1 className="text-2xl font-black text-white">Faculty Evaluators Directory</h1>
          <p className="text-xs text-slate-400 mt-1">
            Overview of department evaluators, assigned project workloads, and evaluation completion.
          </p>
        </div>
      </div>

      <div className="p-4 rounded-xl bg-slate-900 border border-slate-800">
        <div className="relative w-full sm:w-80">
          <Search className="w-4 h-4 text-slate-500 absolute left-3 top-3" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search faculty by name or code..."
            className="w-full pl-9 pr-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-500 focus:ring-2 focus:ring-emerald-500 outline-none"
          />
        </div>
      </div>

      <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-800/60 text-slate-400 font-semibold uppercase text-[10px]">
              <tr>
                <th className="p-3">Faculty Name & Code</th>
                <th className="p-3">Email Address</th>
                <th className="p-3">Department</th>
                <th className="p-3">Assigned Projects</th>
                <th className="p-3">Completed Reviews</th>
                <th className="p-3">Account Status</th>
                <th className="p-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800 text-slate-300">
              {filtered.map(e => (
                <tr key={e.id} className="hover:bg-slate-800/40 transition">
                  <td className="p-3">
                    <div className="font-bold text-white text-sm">{e.fullName}</div>
                    <div className="text-[11px] text-purple-400 font-mono">{e.employeeCode}</div>
                  </td>
                  <td className="p-3 text-slate-300">{e.email}</td>
                  <td className="p-3 text-slate-400">{e.department}</td>
                  <td className="p-3 font-semibold text-slate-200">{e.assignedCount} assigned</td>
                  <td className="p-3 font-bold text-emerald-400">{e.completedCount} completed</td>
                  <td className="p-3">
                    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold ${
                      e.active ? 'bg-emerald-950 text-emerald-400 border border-emerald-800' : 'bg-rose-950 text-rose-400 border border-rose-800'
                    }`}>
                      {e.active ? 'ACTIVE' : 'DISABLED'}
                    </span>
                  </td>
                  <td className="p-3 text-right">
                    <button
                      onClick={() => toggleStatus(e.userId)}
                      className="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 font-medium transition text-xs"
                    >
                      {e.active ? 'Disable' : 'Enable'}
                    </button>
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
