import request from '@/utils/request'

// 查询回測資金流水明細列表
export function listTffundflow(query) {
  return request({
    url: '/stock/tffundflow/list',
    method: 'get',
    params: query
  })
}

// 查询回測資金流水明細详细
export function getTffundflow(id) {
  return request({
    url: '/stock/tffundflow/' + id,
    method: 'get'
  })
}

// 新增回測資金流水明細
export function addTffundflow(data) {
  return request({
    url: '/stock/tffundflow',
    method: 'post',
    data: data
  })
}

// 修改回測資金流水明細
export function updateTffundflow(data) {
  return request({
    url: '/stock/tffundflow',
    method: 'put',
    data: data
  })
}

// 删除回測資金流水明細
export function delTffundflow(id) {
  return request({
    url: '/stock/tffundflow/' + id,
    method: 'delete'
  })
}
