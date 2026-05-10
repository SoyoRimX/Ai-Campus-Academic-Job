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

export function generateResume(studentId: number) {
  return request.post('/employ/resume/generate', { studentId })
}

export function getInterviews(params: { page: number; size: number }) {
  return request.get('/employ/interviews', { params })
}
