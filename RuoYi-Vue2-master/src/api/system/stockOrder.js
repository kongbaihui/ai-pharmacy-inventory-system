import request from '@/utils/request'

export function listStockOrder(query) {
  return request({ url: '/system/stockOrder/list', method: 'get', params: query })
}

export function getStockOrderTrend() {
  return request({ url: '/system/stockOrder/trend', method: 'get' })
}

export function listStockOrderBatchOptions(medId, orderType) {
  return request({ url: '/system/stockOrder/batch/options', method: 'get', params: { medId, orderType } })
}

export function getStockOrder(orderId) {
  return request({ url: '/system/stockOrder/' + orderId, method: 'get' })
}

export function addStockOrder(data) {
  return request({ url: '/system/stockOrder', method: 'post', data: data })
}

export function addAndConfirmStockOrder(data) {
  return request({ url: '/system/stockOrder/confirm', method: 'post', data: data })
}

export function updateStockOrder(data) {
  return request({ url: '/system/stockOrder', method: 'put', data: data })
}

export function updateAndConfirmStockOrder(data) {
  return request({ url: '/system/stockOrder/confirm', method: 'put', data: data })
}

export function confirmStockOrder(orderId) {
  return request({ url: '/system/stockOrder/confirm/' + orderId, method: 'put' })
}

export function delStockOrder(orderIds) {
  return request({ url: '/system/stockOrder/' + orderIds, method: 'delete' })
}

export function listStockFlow(query) {
  return request({ url: '/system/stockOrder/flow/list', method: 'get', params: query })
}
