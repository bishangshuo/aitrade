import request from '@/utils/request'

// 查询回測當前持倉列表
export function listTfposition(query) {
  return request({
    url: '/stock/tfposition/list',
    method: 'get',
    params: query
  })
}

// 查询回測當前持倉详细
export function getTfposition(id) {
  return request({
    url: '/stock/tfposition/' + id,
    method: 'get'
  })
}

// 新增回測當前持倉
export function addTfposition(data) {
  return request({
    url: '/stock/tfposition',
    method: 'post',
    data: data
  })
}

// 修改回測當前持倉
export function updateTfposition(data) {
  return request({
    url: '/stock/tfposition',
    method: 'put',
    data: data
  })
}

// 删除回測當前持倉
export function delTfposition(id) {
  return request({
    url: '/stock/tfposition/' + id,
    method: 'delete'
  })
}
