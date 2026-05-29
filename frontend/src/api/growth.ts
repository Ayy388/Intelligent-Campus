import request from '@/utils/request'

export function getProfile() { return request.get('/growth/profile') }
export function saveProfile(data: any) { return request.put('/growth/profile', data) }
export function addEvaluation(studentId: number, content: string) {
  return request.post('/growth/evaluation', null, { params: { studentId, content } })
}

export function getCheckIns(params: any) { return request.get('/growth/checkin', { params }) }
export function createCheckIn(data: any) { return request.post('/growth/checkin', data) }
export function doCheckIn(checkinId: number) {
  return request.post('/growth/checkin/do', null, { params: { checkinId } })
}
export function getCheckInRecords(id: number) { return request.get(`/growth/checkin/${id}/records`) }
export function getCheckInStatus(id: number) { return request.get(`/growth/checkin/${id}/status`) }
