import request from '@/utils/request'

// 查询回測虛擬賬戶列表
export function listTfaccount(query) {
  return request({
    url: '/tfstock/tfaccount/list',
    method: 'get',
    params: query
  })
}

// 查询回測虛擬賬戶详细
export function getTfaccount(id) {
  return request({
    url: '/tfstock/tfaccount/' + id,
    method: 'get'
  })
}

// 新增回測虛擬賬戶
export function addTfaccount(data) {
  return request({
    url: '/tfstock/tfaccount',
    method: 'post',
    data: data
  })
}

// 修改回測虛擬賬戶
export function updateTfaccount(data) {
  return request({
    url: '/tfstock/tfaccount',
    method: 'put',
    data: data
  })
}

// 删除回測虛擬賬戶
export function delTfaccount(id) {
  return request({
    url: '/tfstock/tfaccount/' + id,
    method: 'delete'
  })
}
