import request from '@/utils/request'

// 查询回测账户快照列表
export function listTfaccountdaily(query) {
  return request({
    url: '/stock/tfaccountdaily/list',
    method: 'get',
    params: query
  })
}

// 查询回测账户快照详细
export function getTfaccountdaily(id) {
  return request({
    url: '/stock/tfaccountdaily/' + id,
    method: 'get'
  })
}

// 新增回测账户快照
export function addTfaccountdaily(data) {
  return request({
    url: '/stock/tfaccountdaily',
    method: 'post',
    data: data
  })
}

// 修改回测账户快照
export function updateTfaccountdaily(data) {
  return request({
    url: '/stock/tfaccountdaily',
    method: 'put',
    data: data
  })
}

// 删除回测账户快照
export function delTfaccountdaily(id) {
  return request({
    url: '/stock/tfaccountdaily/' + id,
    method: 'delete'
  })
}
