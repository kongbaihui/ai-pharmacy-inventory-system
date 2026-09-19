import request from '@/utils/request'

// 查询库存流水列表
export function listFlow(query) {
  return request({
    url: '/system/flow/list',
    method: 'get',
    params: query
  })
}

// 查询库存流水详细
export function getFlow(flowId) {
  return request({
    url: '/system/flow/' + flowId,
    method: 'get'
  })
}

// 新增库存流水
export function addFlow(data) {
  return request({
    url: '/system/flow',
    method: 'post',
    data: data
  })
}

// 修改库存流水
export function updateFlow(data) {
  return request({
    url: '/system/flow',
    method: 'put',
    data: data
  })
}

// 删除库存流水
export function delFlow(flowId) {
  return request({
    url: '/system/flow/' + flowId,
    method: 'delete'
  })
}
