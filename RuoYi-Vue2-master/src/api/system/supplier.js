import request from '@/utils/request'

// 查询供应商信息列表
export function listSupplier(query) {
  return request({
    url: '/system/supplier/list',
    method: 'get',
    params: query
  })
}

// 查询供应商下拉选项
export function optionselectSupplier() {
  return request({
    url: '/system/supplier/optionselect',
    method: 'get'
  })
}

// 查询供应商信息详细
export function getSupplier(supplierId) {
  return request({
    url: '/system/supplier/' + supplierId,
    method: 'get'
  })
}

// 新增供应商信息
export function addSupplier(data) {
  return request({
    url: '/system/supplier',
    method: 'post',
    data: data
  })
}

// 修改供应商信息
export function updateSupplier(data) {
  return request({
    url: '/system/supplier',
    method: 'put',
    data: data
  })
}

// 删除供应商信息
export function delSupplier(supplierId) {
  return request({
    url: '/system/supplier/' + supplierId,
    method: 'delete'
  })
}
