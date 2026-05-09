import request from './request'

export function getUsers(params: { page: number; size: number; keyword?: string }) {
  return request.get('/system/user', { params })
}

export function addUser(data: any) {
  return request.post('/system/user', data)
}

export function updateUser(data: any) {
  return request.put('/system/user', data)
}

export function deleteUser(id: number) {
  return request.delete(`/system/user/${id}`)
}

export function getRoles() {
  return request.get('/system/role/all')
}

export function getMenus() {
  return request.get('/system/menu/tree')
}
