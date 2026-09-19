import request from '@/utils/request'

// 查询过期药品清理列表
export function listClean(query) {
  return request({
    url: '/system/clean/list',
    method: 'get',
    params: query
  })
}

// 查询过期药品清理详细
export function getClean(cleanId) {
  return request({
    url: '/system/clean/' + cleanId,
    method: 'get'
  })
}

// 新增过期药品清理
export function addClean(data) {
  return request({
    url: '/system/clean',
    method: 'post',
    data: data
  })
}

// 修改过期药品清理
export function updateClean(data) {
  return request({
    url: '/system/clean',
    method: 'put',
    data: data
  })
}

// 删除过期药品清理
export function delClean(cleanId) {
  return request({
    url: '/system/clean/' + cleanId,
    method: 'delete'
  })
}
