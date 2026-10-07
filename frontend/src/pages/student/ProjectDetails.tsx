import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api } from '../../services/api';
import { EvaluationDetailsResponse, AutomatedTestResultDto } from '../../types';
import { Scorecard } from '../../components/Scorecard';
import { TestStatusBadge } from '../../components/StatusBadge';
import { Play, Terminal, Sparkles, CheckCircle, ExternalLink, ArrowLeft, RefreshCw } from 'lucide-react';

export const ProjectDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [evaluation, setEvaluation] = useState<EvaluationDetailsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'scorecard' | 'tests' | 'logs' | 'ai'>('scorecard');
  const [reRunning, setReRunning] = useState(false);

  useEffect(() => {
    fetchEvaluationDetails();
  }, [id]);

  const fetchEvaluationDetails = async () => {
    try {
      const res = await api.get(`/evaluations/project/${id}`);
      if (res.data.success) {
        setEvaluation(res.data.data);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const handleReRun = async () => {
    setReRunning(true);
    try {
      await api.post(`/evaluations/project/${id}/run`);
      await fetchEvaluationDetails();
    } catch (e: any) {
      alert(e.response?.data?.message || 'Failed to re-run evaluation');
    } finally {
      setReRunning(false);
    }
  };

  if (loading || !evaluation) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-emerald-500"></div>
      </div>
    );
  }

  const { project, testResults, buildLogs, aiFeedback } = evaluation;

  return (
    <div className="space-y-6">
      
      {/* Top Navigation */}
      <div className="flex items-center justify-between pb-3 border-b border-slate-800">
        <Link
          to="/student/projects"
          className="inline-flex items-center space-x-1.5 text-xs font-semibold text-slate-400 hover:text-white transition"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to Projects</span>
        </Link>

        <button
          onClick={handleReRun}
          disabled={reRunning}
          className="inline-flex items-center space-x-1.5 px-3 py-1.5 rounded-lg bg-emerald-600/20 text-emerald-400 hover:bg-emerald-600/30 font-semibold text-xs transition disabled:opacity-50"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${reRunning ? 'animate-spin' : ''}`} />
          <span>{reRunning ? 'Executing Sandbox...' : 'Re-Run Automated Evaluation'}</span>
        </button>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-slate-800 space-x-4">
        {[
          { id: 'scorecard', label: 'Evaluation Scorecard' },
          { id: 'tests', label: `Detailed Automated Tests (${testResults?.length || 0})` },
          { id: 'logs', label: 'Terminal Build Logs' },
          { id: 'ai', label: 'AI Architectural Insights' },
        ].map(tab => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id as any)}
            className={`pb-3 text-xs font-bold transition border-b-2 ${
              activeTab === tab.id
                ? 'border-emerald-500 text-emerald-400'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Tab 1: Master Scorecard */}
      {activeTab === 'scorecard' && (
        <Scorecard evaluation={evaluation} />
      )}

      {/* Tab 2: Individual Test Results Breakdown */}
      {activeTab === 'tests' && (
        <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="font-bold text-white text-sm">Automated Test Execution Breakdown</h3>
            <span className="text-xs text-slate-400">{testResults?.length || 0} Test Items Executed</span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-800/60 text-slate-400 font-semibold uppercase text-[10px]">
                <tr>
                  <th className="p-3">Category</th>
                  <th className="p-3">Test Name & Spec</th>
                  <th className="p-3">Expected Result</th>
                  <th className="p-3">Actual Result</th>
                  <th className="p-3">Execution Time</th>
                  <th className="p-3">Status</th>
                  <th className="p-3 text-right">Marks</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800 text-slate-300">
                {testResults?.map((test) => (
                  <tr key={test.id} className="hover:bg-slate-800/40 transition">
                    <td className="p-3 font-semibold text-slate-400 whitespace-nowrap">
                      {test.category}
                    </td>
                    <td className="p-3">
                      <div className="font-bold text-white">{test.testName}</div>
                      {test.errorMessage && (
                        <div className="text-[11px] text-rose-400 mt-0.5">{test.errorMessage}</div>
                      )}
                    </td>
                    <td className="p-3 text-slate-400">{test.expectedResult}</td>
                    <td className="p-3 text-slate-300 font-mono text-[11px]">{test.actualResult}</td>
                    <td className="p-3 text-slate-500 whitespace-nowrap">{test.executionTime} ms</td>
                    <td className="p-3 whitespace-nowrap">
                      <TestStatusBadge status={test.status} />
                    </td>
                    <td className="p-3 text-right font-bold text-emerald-400 whitespace-nowrap">
                      {test.marksAwarded} / {test.maxMarks}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab 3: Terminal Build Logs */}
      {activeTab === 'logs' && (
        <div className="rounded-2xl bg-slate-950 border border-slate-800 overflow-hidden shadow-2xl">
          <div className="bg-slate-900 px-4 py-2.5 border-b border-slate-800 flex items-center space-x-2">
            <div className="flex space-x-1.5">
              <div className="w-3 h-3 rounded-full bg-rose-500/80"></div>
              <div className="w-3 h-3 rounded-full bg-amber-500/80"></div>
              <div className="w-3 h-3 rounded-full bg-emerald-500/80"></div>
            </div>
            <Terminal className="w-3.5 h-3.5 text-slate-400 ml-2" />
            <span className="text-xs font-mono text-slate-400">sandbox-build-execution.log</span>
          </div>

          <pre className="p-4 text-xs font-mono text-emerald-400 bg-slate-950/80 overflow-x-auto max-h-[500px] leading-relaxed">
            {buildLogs || 'No build logs captured for this project yet.'}
          </pre>
        </div>
      )}

      {/* Tab 4: AI Architectural Insights */}
      {activeTab === 'ai' && (
        <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 space-y-4">
          <div className="flex items-center space-x-2 text-purple-400 font-bold text-sm">
            <Sparkles className="w-5 h-5" />
            <span>AI-Assisted Architectural Evaluation Layer</span>
          </div>
          <p className="text-xs text-slate-400 leading-relaxed">
            AI recommendations act as an analytical advisory layer to support faculty reviews and student learning. Automated test evidence retains authoritative priority over scoring.
          </p>

          <div className="p-4 rounded-xl bg-slate-800/40 border border-slate-700/60 font-sans text-xs text-slate-300 whitespace-pre-line leading-relaxed">
            {aiFeedback || 'AI insights generated during sandbox analysis.'}
          </div>
        </div>
      )}

    </div>
  );
};
