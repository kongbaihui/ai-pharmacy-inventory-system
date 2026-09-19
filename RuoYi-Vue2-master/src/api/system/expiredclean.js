import request from '@/utils/request'

// 查询过期药品清理列表
export function listExpiredclean(query) {
  return request({
    url: '/system/expiredclean/list',
    method: 'get',
    params: query
  })
}

// 查询过期药品清理详细
export function getExpiredclean(cleanId) {
  return request({
    url: '/system/expiredclean/' + cleanId,
    method: 'get'
  })
}

// 新增过期药品清理
export function addExpiredclean(data) {
  return request({
    url: '/system/expiredclean',
    method: 'post',
    data: data
  })
}

// 修改过期药品清理
export function updateExpiredclean(data) {
  return request({
    url: '/system/expiredclean',
    method: 'put',
    data: data
  })
}

// 确认清理（扣减库存）
export function confirmExpiredclean(cleanId, remark) {
  return request({
    url: '/system/expiredclean/confirm/' + cleanId,
    method: 'put',
    params: { remark: remark }
  })
}

// 驳回清理申请
export function rejectExpiredclean(cleanId, remark) {
  return request({
    url: '/system/expiredclean/reject/' + cleanId,
    method: 'put',
    params: { remark: remark }
  })
}

// 删除过期药品清理
export function delExpiredclean(cleanId) {
  return request({
    url: '/system/expiredclean/' + cleanId,
    method: 'delete'
  })
}
