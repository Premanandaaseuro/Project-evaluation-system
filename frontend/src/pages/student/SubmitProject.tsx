import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../../services/api';
import { PlusCircle, Github, Upload, FileText, CheckCircle2, ArrowRight } from 'lucide-react';

export const SubmitProject: React.FC = () => {
  const navigate = useNavigate();

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [projectType, setProjectType] = useState('React');
  const [technologyStack, setTechnologyStack] = useState('React, TypeScript, Tailwind CSS, Vite');
  const [repositoryUrl, setRepositoryUrl] = useState('');
  const [requirements, setRequirements] = useState(
`1. User registration and secure JWT authentication
2. Responsive dashboard with analytics and charts
3. Full CRUD operations with REST API integration
4. Form validation and centralized error handling
5. Database relational persistence and schema integrity`
  );
  
  const [zipFile, setZipFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      // Step 1: Create project record
      const createRes = await api.post('/projects', {
        title,
        description,
        projectType,
        technologyStack,
        repositoryUrl,
        requirements,
      });

      if (!createRes.data.success) {
        throw new Error(createRes.data.message || 'Failed to create project');
      }

      const createdProject = createRes.data.data;
      const projectId = createdProject.id;

      // Step 2: Upload ZIP file if provided
      if (zipFile) {
        const formData = new FormData();
        formData.append('file', zipFile);
        await api.post(`/projects/${projectId}/upload-zip`, formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        });
      }

      // Step 3: Submit project to move status from DRAFT to SUBMITTED
      await api.post(`/projects/${projectId}/submit`);

      // Step 4: Automatically trigger initial automated testing pipeline!
      api.post(`/evaluations/project/${projectId}/run`).catch(() => {});

      navigate(`/student/projects/${projectId}`);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Submission failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      
      {/* Header */}
      <div className="pb-4 border-b border-slate-800">
        <h1 className="text-2xl font-black text-white">Submit Project for Evaluation</h1>
        <p className="text-xs text-slate-400 mt-1">
          Provide your project metadata, GitHub URL or ZIP archive, and requirement checklist for Level 1 automated evaluation.
        </p>
      </div>

      {error && (
        <div className="p-3.5 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-400 text-xs font-semibold">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        
        {/* Section 1: Core Details */}
        <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 space-y-4">
          <h2 className="text-sm font-bold text-white uppercase tracking-wider flex items-center space-x-2">
            <span>1. Project Information</span>
          </h2>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="sm:col-span-2">
              <label className="block text-xs font-semibold text-slate-300 mb-1">Project Title *</label>
              <input
                type="text"
                required
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="e.g. Distributed Task Orchestrator & Dashboard"
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700 rounded-xl text-white text-xs focus:ring-2 focus:ring-emerald-500 outline-none"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Project Type *</label>
              <select
                value={projectType}
                onChange={(e) => setProjectType(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700 rounded-xl text-white text-xs focus:ring-2 focus:ring-emerald-500 outline-none"
              >
                <option value="React">React (SPA / Vite)</option>
                <option value="Node.js">Node.js / Express API</option>
                <option value="Java">Java / Maven</option>
                <option value="Spring Boot">Spring Boot 3.x</option>
                <option value="Python">Python</option>
                <option value="Flask">Flask Web Application</option>
                <option value="Django">Django Full-Stack</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Technology Stack Tags</label>
              <input
                type="text"
                value={technologyStack}
                onChange={(e) => setTechnologyStack(e.target.value)}
                placeholder="React, TypeScript, Spring Boot, PostgreSQL"
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700 rounded-xl text-white text-xs focus:ring-2 focus:ring-emerald-500 outline-none"
              />
            </div>

            <div className="sm:col-span-2">
              <label className="block text-xs font-semibold text-slate-300 mb-1">Project Description & Architecture</label>
              <textarea
                rows={3}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Brief summary of the system architecture, business logic, and problem solved..."
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700 rounded-xl text-white text-xs focus:ring-2 focus:ring-emerald-500 outline-none"
              />
            </div>
          </div>
        </div>

        {/* Section 2: Code Delivery (GitHub or ZIP) */}
        <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 space-y-4">
          <h2 className="text-sm font-bold text-white uppercase tracking-wider flex items-center space-x-2">
            <span>2. Code Artifacts Delivery</span>
          </h2>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1 flex items-center space-x-1.5">
                <Github className="w-3.5 h-3.5" />
                <span>GitHub Repository URL</span>
              </label>
              <input
                type="url"
                value={repositoryUrl}
                onChange={(e) => setRepositoryUrl(e.target.value)}
                placeholder="https://github.com/username/project"
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700 rounded-xl text-white text-xs focus:ring-2 focus:ring-emerald-500 outline-none"
              />
              <span className="text-[11px] text-slate-500 mt-1 block">
                Repository will be cloned into an isolated workspace sandbox.
              </span>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1 flex items-center space-x-1.5">
                <Upload className="w-3.5 h-3.5" />
                <span>Upload Project ZIP File</span>
              </label>
              <input
                type="file"
                accept=".zip"
                onChange={(e) => setZipFile(e.target.files ? e.target.files[0] : null)}
                className="w-full px-3.5 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-slate-400 text-xs file:mr-3 file:py-1 file:px-2.5 file:rounded-lg file:border-0 file:text-xs file:font-semibold file:bg-emerald-600 file:text-white hover:file:bg-emerald-500"
              />
              <span className="text-[11px] text-slate-500 mt-1 block">
                Max 50MB. ZIP slip traversal protection enforced.
              </span>
            </div>
          </div>
        </div>

        {/* Section 3: Requirements Implementation Checklist */}
        <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 space-y-4">
          <h2 className="text-sm font-bold text-white uppercase tracking-wider flex items-center space-x-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-400" />
            <span>3. Requirements Implementation List (15 Marks)</span>
          </h2>
          <p className="text-xs text-slate-400">
            Enter each requirement on a separate line. The automated evaluator inspects each numbered item against endpoints, UI elements, and data models to calculate requirement coverage marks.
          </p>

          <textarea
            rows={5}
            required
            value={requirements}
            onChange={(e) => setRequirements(e.target.value)}
            className="w-full font-mono text-xs px-3.5 py-2.5 bg-slate-800/80 border border-slate-700 rounded-xl text-white focus:ring-2 focus:ring-emerald-500 outline-none"
          />
        </div>

        {/* Submit Action */}
        <div className="flex justify-end space-x-3">
          <button
            type="button"
            onClick={() => navigate('/student/dashboard')}
            className="px-5 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 font-semibold text-xs transition"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={loading}
            className="inline-flex items-center space-x-2 px-6 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs shadow-lg shadow-emerald-600/25 transition disabled:opacity-50"
          >
            <span>{loading ? 'Initiating Sandbox Evaluation...' : 'Submit & Start Evaluation'}</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </div>

      </form>
    </div>
  );
};
