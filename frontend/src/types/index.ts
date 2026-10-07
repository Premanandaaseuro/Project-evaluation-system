export type Role = 'ROLE_ADMIN' | 'ROLE_STUDENT' | 'ROLE_EVALUATOR';

export type ProjectStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'ASSIGNED'
  | 'EVALUATING'
  | 'EVALUATED'
  | 'APPROVED'
  | 'REJECTED';

export type TestStatus = 'PASS' | 'FAIL' | 'SKIPPED' | 'ERROR';

export type JobStatus =
  | 'QUEUED'
  | 'RUNNING'
  | 'BUILDING'
  | 'STARTING'
  | 'TESTING'
  | 'ANALYZING'
  | 'COMPLETED'
  | 'FAILED'
  | 'TIMEOUT';

export interface User {
  id: number;
  username: string;
  email: string;
  role: Role;
  fullName: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  id: number;
  username: string;
  email: string;
  role: Role;
  fullName: string;
}

export interface ProjectResponse {
  id: number;
  studentId: number;
  studentName: string;
  studentEmail: string;
  studentDepartment: string;
  title: string;
  description: string;
  repositoryUrl: string;
  projectType: string;
  technologyStack: string;
  documentationPath?: string;
  zipPath?: string;
  requirements?: string;
  status: ProjectStatus;
  submittedAt?: string;
  createdAt: string;
  updatedAt: string;
  assignedEvaluatorId?: number;
  assignedEvaluatorName?: string;
  automatedScore?: number;
  manualScore?: number;
  finalScore?: number;
  grade?: string;
  resultPublished?: boolean;
}

export interface AutomatedTestResultDto {
  id: number;
  testName: string;
  category: string;
  expectedResult: string;
  actualResult: string;
  status: TestStatus;
  executionTime: number;
  errorMessage?: string;
  marksAwarded: number;
  maxMarks: number;
}

export interface EvaluationDetailsResponse {
  project: ProjectResponse;
  automatedEvaluationId?: number;
  jobStatus?: JobStatus;
  buildStatus?: string;
  startupStatus?: string;
  apiStatus?: string;
  uiStatus?: string;
  databaseStatus?: string;
  securityStatus?: string;
  codeQualityStatus?: string;
  documentationStatus?: string;
  automatedScore?: number;
  buildLogs?: string;
  aiFeedback?: string;
  errorMessage?: string;
  startedAt?: string;
  completedAt?: string;
  categoryScores?: Record<string, number>;
  testResults?: AutomatedTestResultDto[];

  // Manual Evaluation
  manualEvaluationId?: number;
  evaluatorName?: string;
  innovationMarks?: number;
  technicalMarks?: number;
  documentationMarks?: number;
  presentationMarks?: number;
  outcomeMarks?: number;
  manualTotal?: number;
  comments?: string;
  manualSubmittedAt?: string;

  // Final Results
  finalScore?: number;
  grade?: string;
  remarks?: string;
  published?: boolean;
  publishedAt?: string;
}

export interface ManualEvaluationRequest {
  innovationMarks: number;
  technicalMarks: number;
  documentationMarks: number;
  presentationMarks: number;
  outcomeMarks: number;
  comments: string;
}

export interface AdminDashboardDto {
  totalStudents: number;
  totalEvaluators: number;
  totalProjects: number;
  submittedProjects: number;
  pendingEvaluations: number;
  completedEvaluations: number;
  averageScore: number;
  highestScore: number;
  lowestScore: number;
  statusDistribution: Record<string, number>;
  gradeDistribution: Record<string, number>;
  departmentAverages: Record<string, number>;
  scoreComparison: {
    avgAutomated: number;
    avgManual: number;
  };
  recentProjects: ProjectResponse[];
}

export interface StudentDashboardDto {
  totalProjects: number;
  submittedProjects: number;
  evaluatedProjects: number;
  averageScore?: number;
  projects: ProjectResponse[];
}

export interface EvaluatorDashboardDto {
  totalAssigned: number;
  pendingEvaluations: number;
  completedEvaluations: number;
  assignedProjects: ProjectResponse[];
}

export interface StudentDto {
  id: number;
  userId: number;
  username: string;
  email: string;
  studentCode: string;
  fullName: string;
  department: string;
  semester: number;
  phone: string;
  active: boolean;
  createdAt: string;
  projectCount: number;
}

export interface EvaluatorDto {
  id: number;
  userId: number;
  username: string;
  email: string;
  employeeCode: string;
  fullName: string;
  department: string;
  active: boolean;
  createdAt: string;
  assignedCount: number;
  completedCount: number;
}

export interface CriteriaDto {
  id: number;
  name: string;
  description: string;
  maxMarks: number;
  evaluationType: 'AUTOMATED' | 'MANUAL';
  criterionKey: string;
  active: boolean;
}

export interface AuditLog {
  id: number;
  userId?: number;
  username?: string;
  action: string;
  entityType?: string;
  entityId?: number;
  timestamp: string;
  details?: string;
}

export interface NotificationItem {
  id: number;
  title: string;
  message: string;
  type: string;
  read: boolean;
  createdAt: string;
}
