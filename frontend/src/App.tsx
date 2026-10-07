import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { AppLayout } from './components/AppLayout';
import { ProtectedRoute } from './components/ProtectedRoute';

// Pages
import { Login } from './pages/auth/Login';
import { Register } from './pages/auth/Register';
import { AdminDashboard } from './pages/admin/AdminDashboard';
import { ManageProjects } from './pages/admin/ManageProjects';
import { ManageStudents } from './pages/admin/ManageStudents';
import { ManageEvaluators } from './pages/admin/ManageEvaluators';
import { CriteriaSettings } from './pages/admin/CriteriaSettings';
import { AuditLogs } from './pages/admin/AuditLogs';
import { StudentDashboard } from './pages/student/StudentDashboard';
import { SubmitProject } from './pages/student/SubmitProject';
import { MyProjects } from './pages/student/MyProjects';
import { ProjectDetails } from './pages/student/ProjectDetails';
import { EvaluatorDashboard } from './pages/evaluator/EvaluatorDashboard';
import { EvaluateWorkspace } from './pages/evaluator/EvaluateWorkspace';

const RootRedirect: React.FC = () => {
  const { user, isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-emerald-500"></div>
      </div>
    );
  }

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />;
  }

  if (user.role === 'ROLE_ADMIN') return <Navigate to="/admin/dashboard" replace />;
  if (user.role === 'ROLE_EVALUATOR') return <Navigate to="/evaluator/dashboard" replace />;
  return <Navigate to="/student/dashboard" replace />;
};

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Authentication Routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Root Role-Based Router */}
          <Route path="/" element={<RootRedirect />} />

          {/* Protected Routes inside AppLayout */}
          <Route element={<AppLayout />}>
            
            {/* Admin Routes */}
            <Route element={<ProtectedRoute allowedRoles={['ROLE_ADMIN']} />}>
              <Route path="/admin/dashboard" element={<AdminDashboard />} />
              <Route path="/admin/projects" element={<ManageProjects />} />
              <Route path="/admin/students" element={<ManageStudents />} />
              <Route path="/admin/evaluators" element={<ManageEvaluators />} />
              <Route path="/admin/settings" element={<CriteriaSettings />} />
              <Route path="/admin/audit-logs" element={<AuditLogs />} />
            </Route>

            {/* Student Routes */}
            <Route element={<ProtectedRoute allowedRoles={['ROLE_STUDENT', 'ROLE_ADMIN']} />}>
              <Route path="/student/dashboard" element={<StudentDashboard />} />
              <Route path="/student/submit" element={<SubmitProject />} />
              <Route path="/student/projects" element={<MyProjects />} />
              <Route path="/student/projects/:id" element={<ProjectDetails />} />
            </Route>

            {/* Evaluator Routes */}
            <Route element={<ProtectedRoute allowedRoles={['ROLE_EVALUATOR', 'ROLE_ADMIN']} />}>
              <Route path="/evaluator/dashboard" element={<EvaluatorDashboard />} />
              <Route path="/evaluator/projects" element={<EvaluatorDashboard />} />
              <Route path="/evaluator/projects/:id/evaluate" element={<EvaluateWorkspace />} />
            </Route>

          </Route>

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
};

export default App;
