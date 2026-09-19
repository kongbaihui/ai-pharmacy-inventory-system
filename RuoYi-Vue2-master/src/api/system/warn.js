import request from '@/utils/request'

// 查询库存预警列表
export function listWarn(query) {
  return request({
    url: '/system/warn/list',
    method: 'get',
    params: query
  })
}

// 查询库存预警统计
export function getWarnSummary() {
  return request({
    url: '/system/warn/summary',
    method: 'get'
  })
}

// 查询库存预警详细
export function getWarn(warnId) {
  return request({
    url: '/system/warn/' + warnId,
    method: 'get'
  })
}

// 扫描生成库存预警
export function scanWarn() {
  return request({
    url: '/system/warn/scan',
    method: 'post'
  })
}

// 新增库存预警
export function addWarn(data) {
  return request({
    url: '/system/warn',
    method: 'post',
    data: data
  })
}

// 修改库存预警
export function updateWarn(data) {
  return request({
    url: '/system/warn',
    method: 'put',
    data: data
  })
}

// 处理库存预警
export function handleWarn(data) {
  return request({
    url: '/system/warn/handle',
    method: 'put',
    data: data
  })
}

// 删除库存预警
export function delWarn(warnId) {
  return request({
    url: '/system/warn/' + warnId,
    method: 'delete'
  })
}
