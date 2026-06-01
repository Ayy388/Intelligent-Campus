import request from '@/utils/request'

export function getRecharges(params: any) {
  return request.get('/life/card-recharge', { params })
}

export function recharge(data: any) {
  return request.post('/life/card-recharge', data)
}

export function getLostFound(params: any) {
  return request.get('/life/lost-found', { params })
}

export function addLostFound(data: any) {
  return request.post('/life/lost-found', data)
}

export function updateLostFoundStatus(id: number, status: number) {
  return request.put(`/life/lost-found/${id}`, null, { params: { status } })
}

export function getLostFoundById(id: number) {
  return request.get(`/life/lost-found/${id}`)
}

export function deleteLostFound(id: number) {
  return request.delete(`/life/lost-found/${id}`)
}
