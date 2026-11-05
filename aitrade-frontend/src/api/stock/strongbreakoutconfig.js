import request from '@/utils/request'

// 查询主升浪策略回测参数配置列表
export function listStrongbreakoutconfig(query) {
  return request({
    url: '/stock/strongbreakoutconfig/list',
    method: 'get',
    params: query
  })
}

// 查询主升浪策略回测参数配置详细
export function getStrongbreakoutconfig(id) {
  return request({
    url: '/stock/strongbreakoutconfig/' + id,
    method: 'get'
  })
}

// 新增主升浪策略回测参数配置
export function addStrongbreakoutconfig(data) {
  return request({
    url: '/stock/strongbreakoutconfig',
    method: 'post',
    data: data
  })
}

// 修改主升浪策略回测参数配置
export function updateStrongbreakoutconfig(data) {
  return request({
    url: '/stock/strongbreakoutconfig',
    method: 'put',
    data: data
  })
}

// 删除主升浪策略回测参数配置
export function delStrongbreakoutconfig(id) {
  return request({
    url: '/stock/strongbreakoutconfig/' + id,
    method: 'delete'
  })
}
