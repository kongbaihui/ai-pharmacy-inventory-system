import request from '@/utils/request'

// 查询药品库存列表
export function listStock(query) {
  return request({
    url: '/system/stock/list',
    method: 'get',
    params: query
  })
}

// 查询药品库存详细
export function getStock(stockId) {
  return request({
    url: '/system/stock/' + stockId,
    method: 'get'
  })
}

// 新增药品库存
export function addStock(data) {
  return request({
    url: '/system/stock',
    method: 'post',
    data: data
  })
}

// 修改药品库存
export function updateStock(data) {
  return request({
    url: '/system/stock',
    method: 'put',
    data: data
  })
}

// 删除药品库存
export function delStock(stockId) {
  return request({
    url: '/system/stock/' + stockId,
    method: 'delete'
  })
}
