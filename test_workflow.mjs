// Complete End-to-End Workflow Verification Script
const BASE_URL = 'http://localhost:8080/api';

async function request(path, options = {}) {
  const url = `${BASE_URL}${path}`;
  const res = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });
  if (options.binary) {
    return { ok: res.ok, status: res.status, buffer: await res.arrayBuffer() };
  }
  const text = await res.text();
  try {
    return { ok: res.ok, status: res.status, json: JSON.parse(text) };
  } catch {
    return { ok: res.ok, status: res.status, raw: text };
  }
}

async function main() {
  console.log('====================================================');
  console.log('   PROJECTEVAL COMPLETE END-TO-END VERIFICATION     ');
  console.log('====================================================\n');

  // 1. Health Check
  console.log('[1/10] Verifying System Health API (/api/health)...');
  const health = await request('/health');
  if (!health.ok || health.json.data.status !== 'UP') {
    throw new Error('Health check failed: ' + JSON.stringify(health));
  }
  console.log('✓ Health status:', health.json.data.status, 'Version:', health.json.data.version);

  // 2. Admin Authentication
  console.log('\n[2/10] Authenticating Administrator (admin@projecteval.com)...');
  const adminLogin = await request('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ usernameOrEmail: 'admin@projecteval.com', password: 'Admin@123' }),
  });
  if (!adminLogin.ok) throw new Error('Admin login failed: ' + JSON.stringify(adminLogin));
  const adminToken = adminLogin.json.data.accessToken;
  console.log('✓ Admin authenticated. Role:', adminLogin.json.data.role);

  // 3. Admin Dashboard & Analytics
  console.log('\n[3/10] Verifying Admin Dashboard & Metric Aggregations...');
  const adminDash = await request('/dashboard/admin', {
    headers: { Authorization: `Bearer ${adminToken}` },
  });
  if (!adminDash.ok) throw new Error('Admin dashboard failed: ' + JSON.stringify(adminDash));
  console.log('✓ Total Projects:', adminDash.json.data.totalProjects);
  console.log('✓ Total Students:', adminDash.json.data.totalStudents);
  console.log('✓ Average Performance Score:', adminDash.json.data.averageScore);
  console.log('✓ Status Distribution:', adminDash.json.data.statusDistribution);

  // 4. Student Authentication
  console.log('\n[4/10] Authenticating Student Developer (student@projecteval.com)...');
  const studentLogin = await request('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ usernameOrEmail: 'student@projecteval.com', password: 'Student@123' }),
  });
  if (!studentLogin.ok) throw new Error('Student login failed: ' + JSON.stringify(studentLogin));
  const studentToken = studentLogin.json.data.accessToken;
  const studentId = studentLogin.json.data.id;
  console.log('✓ Student authenticated. Name:', studentLogin.json.data.fullName);

  // 5. Student Project Creation
  console.log('\n[5/10] Creating New Project: "Cloud Microservices API Gateway"...');
  const newProj = await request('/projects', {
    method: 'POST',
    headers: { Authorization: `Bearer ${studentToken}` },
    body: JSON.stringify({
      title: 'Cloud Microservices API Gateway',
      description: 'High-throughput reactive edge proxy with rate limiting, JWT validation, and circuit breakers.',
      projectType: 'Spring Boot',
      technologyStack: 'Java 21, Spring Cloud Gateway, Redis, PostgreSQL, Docker',
      repositoryUrl: 'https://github.com/example/api-gateway',
      requirements: '1. Reactive routing and dynamic reverse proxy\n2. Token-bucket distributed rate limiting\n3. Centralized JWT claim verification\n4. Circuit breaker fallback handlers\n5. Real-time metric export to Prometheus',
    }),
  });
  if (!newProj.ok) throw new Error('Project creation failed: ' + JSON.stringify(newProj));
  const projectId = newProj.json.data.id;
  console.log('✓ Project created with ID:', projectId, 'Status:', newProj.json.data.status);

  // 6. Student Project Submission
  console.log('\n[6/10] Submitting Project for Evaluation...');
  const submitProj = await request(`/projects/${projectId}/submit`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${studentToken}` },
  });
  if (!submitProj.ok) throw new Error('Project submit failed: ' + JSON.stringify(submitProj));
  console.log('✓ Project submitted. Status:', submitProj.json.data.status);

  // 7. Admin Evaluator Assignment
  console.log('\n[7/10] Admin assigning Faculty Evaluator to Project...');
  const assignProj = await request(`/projects/${projectId}/assign`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${adminToken}` },
    body: JSON.stringify({ evaluatorId: 1 }),
  });
  if (!assignProj.ok) throw new Error('Assignment failed: ' + JSON.stringify(assignProj));
  console.log('✓ Evaluator assigned:', assignProj.json.data.assignedEvaluatorName);

  // 8. Automated Evaluation Pipeline Execution
  console.log('\n[8/10] Running Level 1 Automated Sandbox Pipeline...');
  const runEval = await request(`/evaluations/project/${projectId}/run`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${adminToken}` },
  });
  if (!runEval.ok) throw new Error('Evaluation pipeline failed: ' + JSON.stringify(runEval));
  console.log('✓ Automated pipeline completed. Score:', runEval.json.data.automatedScore, '/ 85');
  console.log('✓ Job status:', runEval.json.data.jobStatus);

  // 9. Evaluator Manual Marks Submission
  console.log('\n[9/10] Evaluator Submitting Level 2 Manual Evaluation...');
  const evalLogin = await request('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ usernameOrEmail: 'evaluator@projecteval.com', password: 'Evaluator@123' }),
  });
  if (!evalLogin.ok) throw new Error('Evaluator login failed: ' + JSON.stringify(evalLogin));
  const evalToken = evalLogin.json.data.accessToken;

  const manualSubmit = await request(`/evaluations/project/${projectId}/manual`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${evalToken}` },
    body: JSON.stringify({
      innovationMarks: 3.0,
      technicalMarks: 4.0,
      documentationMarks: 3.0,
      presentationMarks: 2.0,
      outcomeMarks: 3.0,
      comments: 'Outstanding architecture and reactive patterns. Exemplary documentation and automated test coverage.',
    }),
  });
  if (!manualSubmit.ok) throw new Error('Manual submission failed: ' + JSON.stringify(manualSubmit));
  console.log('✓ Manual evaluation submitted. Total Manual:', manualSubmit.json.data.manualScore, '/ 15');
  console.log('✓ Combined Final Score:', manualSubmit.json.data.finalScore, '/ 100');
  console.log('✓ Official Letter Grade:', manualSubmit.json.data.grade);

  // 10. PDF Report Download
  console.log('\n[10/10] Downloading Official Scorecard PDF Report...');
  const pdfRes = await request(`/reports/${projectId}/pdf`, {
    binary: true,
    headers: { Authorization: `Bearer ${studentToken}` },
  });
  if (!pdfRes.ok || pdfRes.buffer.byteLength < 1000) {
    throw new Error('PDF download failed. Byte length: ' + pdfRes.buffer?.byteLength);
  }
  console.log('✓ Official PDF Scorecard generated successfully. Size:', pdfRes.buffer.byteLength, 'bytes');

  console.log('\n====================================================');
  console.log('   ALL 10/10 VERIFICATION CHECKS PASSED PERFECTLY!  ');
  console.log('====================================================');
}

main().catch(err => {
  console.error('\n❌ Verification Failed:', err);
  process.exit(1);
});
