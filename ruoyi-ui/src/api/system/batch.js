import request from '@/utils/request'

// 查询药品批次（有效期）列表
export function listBatch(query) {
  return request({
    url: '/system/batch/list',
    method: 'get',
    params: query
  })
}

// 查询临期与过期药品统计
export function getExpirySummary() {
  return request({
    url: '/system/batch/expirySummary',
    method: 'get'
  })
}

// 查询药品批次详细
export function getBatch(batchId) {
  return request({
    url: '/system/batch/' + batchId,
    method: 'get'
  })
}

// 新增药品批次
export function addBatch(data) {
  return request({
    url: '/system/batch',
    method: 'post',
    data: data
  })
}

// 修改药品批次
export function updateBatch(data) {
  return request({
    url: '/system/batch',
    method: 'put',
    data: data
  })
}

// 刷新批次效期状态
export function refreshBatch() {
  return request({
    url: '/system/batch/refresh',
    method: 'put'
  })
}

// 删除药品批次
export function delBatch(batchId) {
  return request({
    url: '/system/batch/' + batchId,
    method: 'delete'
  })
}
