import request from '@/utils/request'

// 查询回测持仓日历列表
export function listTfpositiondaily(query) {
  return request({
    url: '/stock/tfpositiondaily/list',
    method: 'get',
    params: query
  })
}

// 查询回测持仓日历详细
export function getTfpositiondaily(id) {
  return request({
    url: '/stock/tfpositiondaily/' + id,
    method: 'get'
  })
}

// 新增回测持仓日历
export function addTfpositiondaily(data) {
  return request({
    url: '/stock/tfpositiondaily',
    method: 'post',
    data: data
  })
}

// 修改回测持仓日历
export function updateTfpositiondaily(data) {
  return request({
    url: '/stock/tfpositiondaily',
    method: 'put',
    data: data
  })
}

// 删除回测持仓日历
export function delTfpositiondaily(id) {
  return request({
    url: '/stock/tfpositiondaily/' + id,
    method: 'delete'
  })
}
