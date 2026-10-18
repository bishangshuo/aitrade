import request from '@/utils/request'

// 查询任务规划列表
export function listTatask(query) {
  return request({
    url: '/stock/tatask/list',
    method: 'get',
    params: query
  })
}

// 查询任务规划详细
export function getTatask(id) {
  return request({
    url: '/stock/tatask/' + id,
    method: 'get'
  })
}

// 新增任务规划
export function addTatask(data) {
  return request({
    url: '/stock/tatask',
    method: 'post',
    data: data
  })
}

// 修改任务规划
export function updateTatask(data) {
  return request({
    url: '/stock/tatask',
    method: 'put',
    data: data
  })
}

// 删除任务规划
export function delTatask(id) {
  return request({
    url: '/stock/tatask/' + id,
    method: 'delete'
  })
}
