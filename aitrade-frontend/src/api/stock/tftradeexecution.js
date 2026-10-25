import request from '@/utils/request'

// 查询回測歷史成交交割單列表
export function listTftradeexecution(query) {
  return request({
    url: '/stock/tftradeexecution/list',
    method: 'get',
    params: query
  })
}

// 查询回測歷史成交交割單详细
export function getTftradeexecution(id) {
  return request({
    url: '/stock/tftradeexecution/' + id,
    method: 'get'
  })
}

// 新增回測歷史成交交割單
export function addTftradeexecution(data) {
  return request({
    url: '/stock/tftradeexecution',
    method: 'post',
    data: data
  })
}

// 修改回測歷史成交交割單
export function updateTftradeexecution(data) {
  return request({
    url: '/stock/tftradeexecution',
    method: 'put',
    data: data
  })
}

// 删除回測歷史成交交割單
export function delTftradeexecution(id) {
  return request({
    url: '/stock/tftradeexecution/' + id,
    method: 'delete'
  })
}
