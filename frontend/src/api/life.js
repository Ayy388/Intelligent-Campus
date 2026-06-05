import request from '@/utils/request';
export function getRecharges(params) {
    return request.get('/life/card-recharge', { params });
}
export function recharge(data) {
    return request.post('/life/card-recharge', data);
}
export function getLostFound(params) {
    return request.get('/life/lost-found', { params });
}
export function addLostFound(data) {
    return request.post('/life/lost-found', data);
}
export function updateLostFoundStatus(id, status) {
    return request.put(`/life/lost-found/${id}`, null, { params: { status } });
}
export function getLostFoundById(id) {
    return request.get(`/life/lost-found/${id}`);
}
export function deleteLostFound(id) {
    return request.delete(`/life/lost-found/${id}`);
}
