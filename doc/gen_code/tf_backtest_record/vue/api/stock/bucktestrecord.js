import request from '@/utils/request'

// 查询回测记录列表
export function listBucktestrecord(query) {
  return request({
    url: '/stock/bucktestrecord/list',
    method: 'get',
    params: query
  })
}

// 查询回测记录详细
export function getBucktestrecord(id) {
  return request({
    url: '/stock/bucktestrecord/' + id,
    method: 'get'
  })
}

// 新增回测记录
export function addBucktestrecord(data) {
  return request({
    url: '/stock/bucktestrecord',
    method: 'post',
    data: data
  })
}

// 修改回测记录
export function updateBucktestrecord(data) {
  return request({
    url: '/stock/bucktestrecord',
    method: 'put',
    data: data
  })
}

// 删除回测记录
export function delBucktestrecord(id) {
  return request({
    url: '/stock/bucktestrecord/' + id,
    method: 'delete'
  })
}
