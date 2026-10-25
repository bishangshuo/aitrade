import request from '@/utils/request'

// 查询交易日历列表
export function listTacal(query) {
  return request({
    url: '/stock/tacal/list',
    method: 'get',
    params: query
  })
}

// 查询交易日历详细
export function getTacal(id) {
  return request({
    url: '/stock/tacal/' + id,
    method: 'get'
  })
}

// 新增交易日历
export function addTacal(data) {
  return request({
    url: '/stock/tacal',
    method: 'post',
    data: data
  })
}

// 修改交易日历
export function updateTacal(data) {
  return request({
    url: '/stock/tacal',
    method: 'put',
    data: data
  })
}

// 删除交易日历
export function delTacal(id) {
  return request({
    url: '/stock/tacal/' + id,
    method: 'delete'
  })
}
