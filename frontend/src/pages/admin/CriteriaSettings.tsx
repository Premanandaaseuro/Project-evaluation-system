import React, { useState, useEffect } from 'react';
import { api } from '../../services/api';
import { CriteriaDto } from '../../types';
import { Sliders, Award, Cpu, Sparkles, Check, Edit2 } from 'lucide-react';

export const CriteriaSettings: React.FC = () => {
  const [criteria, setCriteria] = useState<CriteriaDto[]>([]);
  const [gradingScale, setGradingScale] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(true);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editMaxMarks, setEditMaxMarks] = useState<number>(0);

  useEffect(() => {
    fetchConfig();
  }, []);

  const fetchConfig = async () => {
    try {
      const [critRes, gradeRes] = await Promise.all([
        api.get('/config/criteria'),
        api.get('/config/grading-scale'),
      ]);
      if (critRes.data.success) setCriteria(critRes.data.data);
      if (gradeRes.data.success) setGradingScale(gradeRes.data.data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const startEdit = (c: CriteriaDto) => {
    setEditingId(c.id);
    setEditMaxMarks(c.maxMarks);
  };

  const saveEdit = async (c: CriteriaDto) => {
    try {
      const res = await api.put(`/config/criteria/${c.id}`, {
        ...c,
        maxMarks: Number(editMaxMarks),
      });
      if (res.data.success) {
        setCriteria(prev => prev.map(item => item.id === c.id ? res.data.data : item));
        setEditingId(null);
      }
    } catch (e) {
      alert('Failed to update criteria');
    }
  };

  const automatedCriteria = criteria.filter(c => c.evaluationType === 'AUTOMATED');
  const manualCriteria = criteria.filter(c => c.evaluationType === 'MANUAL');

  const totalAuto = automatedCriteria.reduce((acc, c) => acc + (c.active ? c.maxMarks : 0), 0);
  const totalManual = manualCriteria.reduce((acc, c) => acc + (c.active ? c.maxMarks : 0), 0);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-slate-800 gap-4">
        <div>
          <h1 className="text-2xl font-black text-white">Evaluation Rubric & Grading Architecture</h1>
          <p className="text-xs text-slate-400 mt-1">
            Configure automated testing mark allocations, manual faculty criteria, and institutional grade boundaries.
          </p>
        </div>

        <div className="flex items-center space-x-3 text-xs font-semibold">
          <div className="px-3 py-1.5 rounded-lg bg-emerald-500/10 border border-emerald-500/30 text-emerald-400">
            Automated Pool: {totalAuto} / 85
          </div>
          <div className="px-3 py-1.5 rounded-lg bg-purple-500/10 border border-purple-500/30 text-purple-400">
            Manual Pool: {totalManual} / 15
          </div>
        </div>
      </div>

      {/* Grid of Automated vs Manual */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        
        {/* Automated Criteria */}
        <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800">
          <div className="flex items-center space-x-2 mb-4">
            <Cpu className="w-5 h-5 text-emerald-400" />
            <h2 className="text-base font-bold text-white">Level 1: Automated Criteria (85 Marks)</h2>
          </div>

          <div className="space-y-3">
            {automatedCriteria.map(c => (
              <div key={c.id} className="p-3 rounded-xl bg-slate-800/40 border border-slate-800 flex items-center justify-between">
                <div>
                  <div className="font-semibold text-slate-200 text-xs">{c.name}</div>
                  <div className="text-[11px] text-slate-400 mt-0.5">{c.description}</div>
                </div>

                <div className="flex items-center space-x-2 ml-4">
                  {editingId === c.id ? (
                    <div className="flex items-center space-x-1">
                      <input
                        type="number"
                        value={editMaxMarks}
                        onChange={(e) => setEditMaxMarks(Number(e.target.value))}
                        className="w-14 px-2 py-1 bg-slate-700 border border-slate-600 rounded text-xs text-white"
                      />
                      <button
                        onClick={() => saveEdit(c)}
                        className="p-1 rounded bg-emerald-600 text-white"
                      >
                        <Check className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  ) : (
                    <div className="flex items-center space-x-2">
                      <span className="text-xs font-bold text-emerald-400 px-2 py-0.5 rounded bg-slate-800 border border-slate-700">
                        {c.maxMarks} marks
                      </span>
                      <button
                        onClick={() => startEdit(c)}
                        className="p-1 text-slate-400 hover:text-slate-200"
                      >
                        <Edit2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Manual Faculty Criteria */}
        <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800">
          <div className="flex items-center space-x-2 mb-4">
            <Sparkles className="w-5 h-5 text-purple-400" />
            <h2 className="text-base font-bold text-white">Level 2: Faculty Manual Criteria (15 Marks)</h2>
          </div>

          <div className="space-y-3">
            {manualCriteria.map(c => (
              <div key={c.id} className="p-3 rounded-xl bg-slate-800/40 border border-slate-800 flex items-center justify-between">
                <div>
                  <div className="font-semibold text-slate-200 text-xs">{c.name}</div>
                  <div className="text-[11px] text-slate-400 mt-0.5">{c.description}</div>
                </div>

                <div className="flex items-center space-x-2 ml-4">
                  {editingId === c.id ? (
                    <div className="flex items-center space-x-1">
                      <input
                        type="number"
                        value={editMaxMarks}
                        onChange={(e) => setEditMaxMarks(Number(e.target.value))}
                        className="w-14 px-2 py-1 bg-slate-700 border border-slate-600 rounded text-xs text-white"
                      />
                      <button
                        onClick={() => saveEdit(c)}
                        className="p-1 rounded bg-purple-600 text-white"
                      >
                        <Check className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  ) : (
                    <div className="flex items-center space-x-2">
                      <span className="text-xs font-bold text-purple-400 px-2 py-0.5 rounded bg-slate-800 border border-slate-700">
                        {c.maxMarks} marks
                      </span>
                      <button
                        onClick={() => startEdit(c)}
                        className="p-1 text-slate-400 hover:text-slate-200"
                      >
                        <Edit2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>

      </div>

      {/* Institutional Grade Scale */}
      <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800">
        <div className="flex items-center space-x-2 mb-3">
          <Award className="w-5 h-5 text-amber-400" />
          <h2 className="text-sm font-bold text-white">Institutional Grade Boundaries</h2>
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
          {Object.entries(gradingScale).map(([range, label]) => (
            <div key={range} className="p-3 rounded-xl bg-slate-800/60 border border-slate-700/60 text-center">
              <div className="text-base font-black text-white">{range}</div>
              <div className="text-[11px] text-emerald-400 font-semibold mt-0.5">{label}</div>
            </div>
          ))}
        </div>
      </div>

    </div>
  );
};
