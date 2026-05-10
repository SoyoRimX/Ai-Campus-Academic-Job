import request from './request'

export function getJobs(params: { page: number; size: number; keyword?: string }) {
  return request.get('/employ/jobs', { params })
}

export function getJob(id: number) {
  return request.get(`/employ/job/${id}`)
}

export function addJob(data: any) {
  return request.post('/employ/job', data)
}

export function updateJob(data: any) {
  return request.put('/employ/job', data)
}

export function deleteJob(id: number) {
  return request.delete(`/employ/job/${id}`)
}

export function getResumes(params?: { page: number; size: number }) {
  return request.get('/employ/resumes', { params })
}

export function getResume(studentId: number) {
  return request.get(`/employ/resume/${studentId}`)
}

export function generateResume(studentNo: string) {
  return request.post('/employ/resume/generate', { studentNo })
}

export function getInterviews(params: { page: number; size: number }) {
  return request.get('/employ/interviews', { params })
}

export function getStudentInterviews(studentId: number) {
  return request.get(`/employ/interviews/${studentId}`)
}

export function startInterview(data: { studentId: number; jobId: number; interviewType: number; jobTitle?: string; requiredSkills?: string }) {
  return request.post('/employ/interview/start', data)
}

export function submitInterview(data: { interviewId: number; answers: string }) {
  return request.post('/employ/interview/submit', data)
}

export function saveResume(data: any) {
  return request.post('/employ/resume', data)
}

export function createMatch(data: { resumeId: number; jobId: number }) {
  return request.post('/employ/match', data)
}

export function getMatches(resumeId: number) {
  return request.get(`/employ/matches/${resumeId}`)
}
