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

// 维护批次效期信息（仅生产日期、有效期、供应商、备注）
export function updateBatchExpiry(data) {
  return request({
    url: '/system/batch/expiry',
    method: 'put',
    data: data
  })
}

// 按批次剩余数量重算库存总量
export function recalcBatchStock() {
  return request({
    url: '/system/batch/recalcStock',
    method: 'post'
  })
}

// 刷新批次效期状态
export function refreshBatch() {
  return request({
    url: '/system/batch/refresh',
    method: 'put'
  })
}
