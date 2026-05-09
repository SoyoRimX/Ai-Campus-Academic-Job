import request from './request'

export function getStudents(params: { page: number; size: number; keyword?: string }) {
  return request.get('/academic/students', { params })
}

export function getStudent(id: number) {
  return request.get(`/academic/student/${id}`)
}

export function getWarnings(params: { page: number; size: number; studentId?: number }) {
  return request.get('/academic/warnings', { params })
}

export function markWarningRead(id: number) {
  return request.put(`/academic/warning/${id}/read`)
}

export function handleWarning(id: number, data: { remark: string }) {
  return request.put(`/academic/warning/${id}/handle`, data)
}

export function getStudyPlans(params: { page: number; size: number; studentId?: number }) {
  return request.get('/academic/study-plans', { params })
}

export function getStudyPlan(id: number) {
  return request.get(`/academic/study-plan/${id}`)
}

export function addStudyPlan(data: any) {
  return request.post('/academic/study-plan', data)
}

export function updateStudyPlan(data: any) {
  return request.put('/academic/study-plan', data)
}

export function deleteStudyPlan(id: number) {
  return request.delete(`/academic/study-plan/${id}`)
}
