import request from '@/utils/request'

// 查询底部反转策略回测参数配置列表
export function listBottombreakoutconfig(query) {
  return request({
    url: '/stock/bottombreakoutconfig/list',
    method: 'get',
    params: query
  })
}

// 查询底部反转策略回测参数配置详细
export function getBottombreakoutconfig(id) {
  return request({
    url: '/stock/bottombreakoutconfig/' + id,
    method: 'get'
  })
}

// 新增底部反转策略回测参数配置
export function addBottombreakoutconfig(data) {
  return request({
    url: '/stock/bottombreakoutconfig',
    method: 'post',
    data: data
  })
}

// 修改底部反转策略回测参数配置
export function updateBottombreakoutconfig(data) {
  return request({
    url: '/stock/bottombreakoutconfig',
    method: 'put',
    data: data
  })
}

// 删除底部反转策略回测参数配置
export function delBottombreakoutconfig(id) {
  return request({
    url: '/stock/bottombreakoutconfig/' + id,
    method: 'delete'
  })
}
