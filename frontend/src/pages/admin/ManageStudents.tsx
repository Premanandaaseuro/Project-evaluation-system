import React, { useState, useEffect } from 'react';
import { api } from '../../services/api';
import { StudentDto } from '../../types';
import { GraduationCap, Search, CheckCircle, XCircle } from 'lucide-react';

export const ManageStudents: React.FC = () => {
  const [students, setStudents] = useState<StudentDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  useEffect(() => {
    fetchStudents();
  }, []);

  const fetchStudents = async () => {
    try {
      const res = await api.get('/users/students');
      if (res.data.success) {
        setStudents(res.data.data);
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
        setStudents(prev => prev.map(s => s.userId === userId ? { ...s, active: res.data.data } : s));
      }
    } catch (e) {
      alert('Failed to update status');
    }
  };

  const filtered = students.filter(s =>
    s.fullName.toLowerCase().includes(search.toLowerCase()) ||
    s.studentCode.toLowerCase().includes(search.toLowerCase()) ||
    s.email.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-slate-800 gap-4">
        <div>
          <h1 className="text-2xl font-black text-white">Students Directory</h1>
          <p className="text-xs text-slate-400 mt-1">
            Manage student registrations, academic profile information, and submission status.
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
            placeholder="Search students by name, ID, or email..."
            className="w-full pl-9 pr-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-500 focus:ring-2 focus:ring-emerald-500 outline-none"
          />
        </div>
      </div>

      <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-800/60 text-slate-400 font-semibold uppercase text-[10px]">
              <tr>
                <th className="p-3">Student Name & Code</th>
                <th className="p-3">Email Address</th>
                <th className="p-3">Department</th>
                <th className="p-3">Semester</th>
                <th className="p-3">Submissions</th>
                <th className="p-3">Account Status</th>
                <th className="p-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800 text-slate-300">
              {filtered.map(s => (
                <tr key={s.id} className="hover:bg-slate-800/40 transition">
                  <td className="p-3">
                    <div className="font-bold text-white text-sm">{s.fullName}</div>
                    <div className="text-[11px] text-slate-400 font-mono">{s.studentCode}</div>
                  </td>
                  <td className="p-3 text-slate-300">{s.email}</td>
                  <td className="p-3 text-slate-400">{s.department}</td>
                  <td className="p-3 font-semibold text-slate-300">Sem {s.semester}</td>
                  <td className="p-3 font-bold text-emerald-400">{s.projectCount} projects</td>
                  <td className="p-3">
                    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold ${
                      s.active ? 'bg-emerald-950 text-emerald-400 border border-emerald-800' : 'bg-rose-950 text-rose-400 border border-rose-800'
                    }`}>
                      {s.active ? 'ACTIVE' : 'DISABLED'}
                    </span>
                  </td>
                  <td className="p-3 text-right">
                    <button
                      onClick={() => toggleStatus(s.userId)}
                      className="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 font-medium transition text-xs"
                    >
                      {s.active ? 'Disable' : 'Enable'}
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
