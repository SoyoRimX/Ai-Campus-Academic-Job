import request from './request'

export function getKnowledgeList(params: { page: number; size: number; category?: string }) {
  return request.get('/ai/knowledge', { params })
}

export function addKnowledge(data: any) {
  return request.post('/ai/knowledge', data)
}

export function updateKnowledge(data: any) {
  return request.put('/ai/knowledge', data)
}

export function deleteKnowledge(id: number) {
  return request.delete(`/ai/knowledge/${id}`)
}
