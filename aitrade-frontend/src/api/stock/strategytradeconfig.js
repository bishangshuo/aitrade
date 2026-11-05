import request from '@/utils/request'

// 查询交易策略参数配置列表
export function listStrategytradeconfig(query) {
  return request({
    url: '/stock/strategytradeconfig/list',
    method: 'get',
    params: query
  })
}

// 查询交易策略参数配置详细
export function getStrategytradeconfig(id) {
  return request({
    url: '/stock/strategytradeconfig/' + id,
    method: 'get'
  })
}

// 新增交易策略参数配置
export function addStrategytradeconfig(data) {
  return request({
    url: '/stock/strategytradeconfig',
    method: 'post',
    data: data
  })
}

// 修改交易策略参数配置
export function updateStrategytradeconfig(data) {
  return request({
    url: '/stock/strategytradeconfig',
    method: 'put',
    data: data
  })
}

// 删除交易策略参数配置
export function delStrategytradeconfig(id) {
  return request({
    url: '/stock/strategytradeconfig/' + id,
    method: 'delete'
  })
}
