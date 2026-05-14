import request from '@/utils/request'

// 查询币种管理列表
export function listInstid(query) {
  return request({
    url: '/exchange/instid/list',
    method: 'get',
    params: query
  })
}

// 查询币种管理详细
export function getInstid(id) {
  return request({
    url: '/exchange/instid/' + id,
    method: 'get'
  })
}

// 新增币种管理
export function addInstid(data) {
  return request({
    url: '/exchange/instid',
    method: 'post',
    data: data
  })
}

// 修改币种管理
export function updateInstid(data) {
  return request({
    url: '/exchange/instid',
    method: 'put',
    data: data
  })
}

// 删除币种管理
export function delInstid(id) {
  return request({
    url: '/exchange/instid/' + id,
    method: 'delete'
  })
}
