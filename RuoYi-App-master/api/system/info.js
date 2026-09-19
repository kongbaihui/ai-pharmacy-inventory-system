import request from '@/utils/request'

// 查询药品信息列表
export function listInfo(query) {
  return request({
    url: '/system/info/list',
    method: 'get',
    params: query
  })
}

// 查询药品信息详细
export function getInfo(medId) {
  return request({
    url: '/system/info/' + medId,
    method: 'get'
  })
}

// 新增药品信息
export function addInfo(data) {
  return request({
    url: '/system/info',
    method: 'post',
    data: data
  })
}

// 修改药品信息
export function updateInfo(data) {
  return request({
    url: '/system/info',
    method: 'put',
    data: data
  })
}

// 删除药品信息
export function delInfo(medId) {
  return request({
    url: '/system/info/' + medId,
    method: 'delete'
  })
}
