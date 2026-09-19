import request from '@/utils/request'

// 查询库存盘点列表
export function listCheck(query) {
  return request({
    url: '/system/check/list',
    method: 'get',
    params: query
  })
}

// 查询待盘点的账面明细
export function listBookItems(query) {
  return request({
    url: '/system/check/bookItems',
    method: 'get',
    params: query
  })
}

// 查询库存盘点详细
export function getCheck(checkId) {
  return request({
    url: '/system/check/' + checkId,
    method: 'get'
  })
}

// 新增库存盘点
export function addCheck(data) {
  return request({
    url: '/system/check',
    method: 'post',
    data: data
  })
}

// 修改库存盘点
export function updateCheck(data) {
  return request({
    url: '/system/check',
    method: 'put',
    data: data
  })
}

// 盘点审核
export function auditCheck(checkId) {
  return request({
    url: '/system/check/audit/' + checkId,
    method: 'put'
  })
}

// 删除库存盘点
export function delCheck(checkId) {
  return request({
    url: '/system/check/' + checkId,
    method: 'delete'
  })
}
