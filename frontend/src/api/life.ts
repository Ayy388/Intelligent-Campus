import request from '@/utils/request'

export function getCanteens() { return request.get('/life/canteens') }

export function getCanteenReviews(params: any) {
  return request.get('/life/canteen-reviews', { params })
}

export function addCanteenReview(data: any) {
  return request.post('/life/canteen-reviews', data)
}

export function saveCanteen(data: any) { return request.post('/life/canteens', data) }
export function updateCanteen(id: number, data: any) { return request.put(`/life/canteens/${id}`, data) }
export function deleteCanteen(id: number) { return request.delete(`/life/canteens/${id}`) }

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
