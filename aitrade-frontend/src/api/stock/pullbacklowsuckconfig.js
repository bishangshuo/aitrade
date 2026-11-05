import request from '@/utils/request'

// 查询震荡攀升低吸策略回测参数配置列表
export function listPullbacklowsuckconfig(query) {
  return request({
    url: '/stock/pullbacklowsuckconfig/list',
    method: 'get',
    params: query
  })
}

// 查询震荡攀升低吸策略回测参数配置详细
export function getPullbacklowsuckconfig(id) {
  return request({
    url: '/stock/pullbacklowsuckconfig/' + id,
    method: 'get'
  })
}

// 新增震荡攀升低吸策略回测参数配置
export function addPullbacklowsuckconfig(data) {
  return request({
    url: '/stock/pullbacklowsuckconfig',
    method: 'post',
    data: data
  })
}

// 修改震荡攀升低吸策略回测参数配置
export function updatePullbacklowsuckconfig(data) {
  return request({
    url: '/stock/pullbacklowsuckconfig',
    method: 'put',
    data: data
  })
}

// 删除震荡攀升低吸策略回测参数配置
export function delPullbacklowsuckconfig(id) {
  return request({
    url: '/stock/pullbacklowsuckconfig/' + id,
    method: 'delete'
  })
}
