import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { api } from '../../services/api';
import { EvaluationDetailsResponse, ManualEvaluationRequest } from '../../types';
import { Scorecard } from '../../components/Scorecard';
import { TestStatusBadge, StatusBadge } from '../../components/StatusBadge';
import {
  ArrowLeft,
  Sparkles,
  CheckCircle,
  FileCheck2,
  Terminal,
  ShieldCheck,
  Github,
  Play,
  Save,
} from 'lucide-react';

export const EvaluateWorkspace: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [evaluation, setEvaluation] = useState<EvaluationDetailsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  // Manual evaluation form state
  const [innovationMarks, setInnovationMarks] = useState<number>(3.0);
  const [technicalMarks, setTechnicalMarks] = useState<number>(4.0);
  const [documentationMarks, setDocumentationMarks] = useState<number>(3.0);
  const [presentationMarks, setPresentationMarks] = useState<number>(2.0);
  const [outcomeMarks, setOutcomeMarks] = useState<number>(3.0);
  const [comments, setComments] = useState<string>('');

  useEffect(() => {
    fetchEvaluation();
  }, [id]);

  const fetchEvaluation = async () => {
    try {
      const res = await api.get(`/evaluations/project/${id}`);
      if (res.data.success) {
        const data: EvaluationDetailsResponse = res.data.data;
        setEvaluation(data);
        if (data.innovationMarks !== undefined && data.innovationMarks !== null) {
          setInnovationMarks(data.innovationMarks);
          setTechnicalMarks(data.technicalMarks || 0);
          setDocumentationMarks(data.documentationMarks || 0);
          setPresentationMarks(data.presentationMarks || 0);
          setOutcomeMarks(data.outcomeMarks || 0);
          setComments(data.comments || '');
        }
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const currentManualTotal = Math.min(
    15.0,
    Number(innovationMarks) +
      Number(technicalMarks) +
      Number(documentationMarks) +
      Number(presentationMarks) +
      Number(outcomeMarks)
  );

  const autoScore = evaluation?.automatedScore || 0;
  const projectedFinalScore = Math.min(100.0, autoScore + currentManualTotal);

  const handleSubmitEvaluation = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const payload: ManualEvaluationRequest = {
        innovationMarks: Number(innovationMarks),
        technicalMarks: Number(technicalMarks),
        documentationMarks: Number(documentationMarks),
        presentationMarks: Number(presentationMarks),
        outcomeMarks: Number(outcomeMarks),
        comments,
      };

      const res = await api.post(`/evaluations/project/${id}/manual`, payload);
      if (res.data.success) {
        alert('Manual evaluation submitted successfully! Final score & grade updated.');
        await fetchEvaluation();
      }
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to submit manual marks');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading || !evaluation) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-purple-500"></div>
      </div>
    );
  }

  const { project, testResults, buildLogs } = evaluation;

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-slate-800 gap-4">
        <div>
          <Link
            to="/evaluator/dashboard"
            className="inline-flex items-center space-x-1.5 text-xs font-semibold text-slate-400 hover:text-white transition mb-2"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Back to Assigned Projects</span>
          </Link>
          <h1 className="text-2xl font-black text-white">{project.title}</h1>
          <p className="text-xs text-slate-400 mt-1">
            Student: <strong className="text-slate-200">{project.studentName}</strong> ({project.studentDepartment}) • {project.projectType}
          </p>
        </div>

        <div className="flex items-center space-x-2">
          {project.repositoryUrl && (
            <a
              href={project.repositoryUrl}
              target="_blank"
              rel="noreferrer"
              className="inline-flex items-center space-x-1.5 px-3 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold border border-slate-700 transition"
            >
              <Github className="w-3.5 h-3.5" />
              <span>Inspect Repository</span>
            </a>
          )}
        </div>
      </div>

      {/* Top Cards: Automated Summary vs Projected Total */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800 text-center">
          <div className="text-xs font-semibold uppercase text-slate-400">Level 1: Automated Score</div>
          <div className="text-2xl font-bold text-emerald-400 mt-1">{autoScore} / 85</div>
          <div className="text-[11px] text-slate-500 mt-0.5">Calculated by testing engines</div>
        </div>

        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800 text-center">
          <div className="text-xs font-semibold uppercase text-slate-400">Level 2: Faculty Manual Score</div>
          <div className="text-2xl font-bold text-purple-400 mt-1">{currentManualTotal.toFixed(1)} / 15</div>
          <div className="text-[11px] text-slate-500 mt-0.5">Calculated dynamically below</div>
        </div>

        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800 text-center">
          <div className="text-xs font-semibold uppercase text-slate-400">Projected Final Score</div>
          <div className="text-2xl font-bold text-amber-400 mt-1">{projectedFinalScore.toFixed(1)} / 100</div>
          <div className="text-[11px] text-slate-500 mt-0.5">Grade: {projectedFinalScore >= 90 ? 'A+' : projectedFinalScore >= 80 ? 'A' : projectedFinalScore >= 70 ? 'B' : projectedFinalScore >= 60 ? 'C' : projectedFinalScore >= 50 ? 'D' : 'Needs Improvement'}</div>
        </div>
      </div>

      {/* Manual Scoring Form Card */}
      <div className="p-6 rounded-2xl bg-slate-900 border border-purple-500/30 shadow-2xl space-y-6">
        <div className="flex items-center space-x-2 border-b border-slate-800 pb-3">
          <Sparkles className="w-5 h-5 text-purple-400" />
          <h2 className="text-base font-bold text-white">Level 2: Faculty Manual Evaluation Rubric (15 Marks)</h2>
        </div>

        <form onSubmit={handleSubmitEvaluation} className="space-y-5">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            
            {/* Criterion 1: Innovation */}
            <div className="p-4 rounded-xl bg-slate-800/60 border border-slate-700/60 space-y-2">
              <div className="flex justify-between items-center">
                <label className="text-xs font-bold text-slate-200">1. Innovation & Novelty</label>
                <span className="text-xs font-extrabold text-purple-400">{innovationMarks} / 3.0</span>
              </div>
              <p className="text-[11px] text-slate-400">Originality, creative problem solving, and novel approach.</p>
              <input
                type="range"
                min="0"
                max="3"
                step="0.5"
                value={innovationMarks}
                onChange={(e) => setInnovationMarks(parseFloat(e.target.value))}
                className="w-full accent-purple-500 cursor-pointer"
              />
            </div>

            {/* Criterion 2: Technical Implementation */}
            <div className="p-4 rounded-xl bg-slate-800/60 border border-slate-700/60 space-y-2">
              <div className="flex justify-between items-center">
                <label className="text-xs font-bold text-slate-200">2. Technical Rigor</label>
                <span className="text-xs font-extrabold text-purple-400">{technicalMarks} / 4.0</span>
              </div>
              <p className="text-[11px] text-slate-400">Complexity, architecture patterns, and engineering quality.</p>
              <input
                type="range"
                min="0"
                max="4"
                step="0.5"
                value={technicalMarks}
                onChange={(e) => setTechnicalMarks(parseFloat(e.target.value))}
                className="w-full accent-purple-500 cursor-pointer"
              />
            </div>

            {/* Criterion 3: Documentation */}
            <div className="p-4 rounded-xl bg-slate-800/60 border border-slate-700/60 space-y-2">
              <div className="flex justify-between items-center">
                <label className="text-xs font-bold text-slate-200">3. Documentation</label>
                <span className="text-xs font-extrabold text-purple-400">{documentationMarks} / 3.0</span>
              </div>
              <p className="text-[11px] text-slate-400">Reports, architecture diagrams, clarity of explanations.</p>
              <input
                type="range"
                min="0"
                max="3"
                step="0.5"
                value={documentationMarks}
                onChange={(e) => setDocumentationMarks(parseFloat(e.target.value))}
                className="w-full accent-purple-500 cursor-pointer"
              />
            </div>

            {/* Criterion 4: Presentation */}
            <div className="p-4 rounded-xl bg-slate-800/60 border border-slate-700/60 space-y-2">
              <div className="flex justify-between items-center">
                <label className="text-xs font-bold text-slate-200">4. Presentation & Cleanliness</label>
                <span className="text-xs font-extrabold text-purple-400">{presentationMarks} / 2.0</span>
              </div>
              <p className="text-[11px] text-slate-400">Code formatting, style conventions, clarity of demonstration.</p>
              <input
                type="range"
                min="0"
                max="2"
                step="0.5"
                value={presentationMarks}
                onChange={(e) => setPresentationMarks(parseFloat(e.target.value))}
                className="w-full accent-purple-500 cursor-pointer"
              />
            </div>

            {/* Criterion 5: Final Outcome */}
            <div className="p-4 rounded-xl bg-slate-800/60 border border-slate-700/60 space-y-2 sm:col-span-2">
              <div className="flex justify-between items-center">
                <label className="text-xs font-bold text-slate-200">5. Final Outcome & Working Product</label>
                <span className="text-xs font-extrabold text-purple-400">{outcomeMarks} / 3.0</span>
              </div>
              <p className="text-[11px] text-slate-400">Overall deliverable completeness, polish, and functional viability.</p>
              <input
                type="range"
                min="0"
                max="3"
                step="0.5"
                value={outcomeMarks}
                onChange={(e) => setOutcomeMarks(parseFloat(e.target.value))}
                className="w-full accent-purple-500 cursor-pointer"
              />
            </div>

          </div>

          <div>
            <label className="block text-xs font-bold text-slate-200 mb-1">
              Evaluator Comments & Qualitative Feedback
            </label>
            <textarea
              rows={3}
              required
              value={comments}
              onChange={(e) => setComments(e.target.value)}
              placeholder="Provide constructive feedback highlighting architectural strengths and recommended technical improvements..."
              className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:ring-2 focus:ring-purple-500 outline-none"
            />
          </div>

          <div className="flex justify-end space-x-3 pt-2 border-t border-slate-800">
            <button
              type="submit"
              disabled={submitting}
              className="inline-flex items-center space-x-2 px-6 py-2.5 rounded-xl bg-purple-600 hover:bg-purple-500 text-white font-bold text-xs shadow-lg shadow-purple-600/25 transition disabled:opacity-50"
            >
              <Save className="w-4 h-4" />
              <span>{submitting ? 'Calculating Final Score...' : 'Submit Evaluation & Publish Grade'}</span>
            </button>
          </div>
        </form>
      </div>

      {/* Detailed Automated Test Summary */}
      <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="font-bold text-white text-sm">Automated Test Evidence ({testResults?.length || 0} checks)</h3>
          <span className="text-xs text-emerald-400 font-semibold">{autoScore} / 85 awarded</span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-800/60 text-slate-400 font-semibold uppercase text-[10px]">
              <tr>
                <th className="p-3">Category</th>
                <th className="p-3">Test Name</th>
                <th className="p-3">Actual Result</th>
                <th className="p-3">Status</th>
                <th className="p-3 text-right">Marks</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800 text-slate-300">
              {testResults?.map((t) => (
                <tr key={t.id} className="hover:bg-slate-800/40 transition">
                  <td className="p-3 text-slate-400">{t.category}</td>
                  <td className="p-3 font-semibold text-white">{t.testName}</td>
                  <td className="p-3 text-slate-300 font-mono text-[11px]">{t.actualResult}</td>
                  <td className="p-3"><TestStatusBadge status={t.status} /></td>
                  <td className="p-3 text-right font-bold text-emerald-400">{t.marksAwarded} / {t.maxMarks}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

    </div>
  );
};
