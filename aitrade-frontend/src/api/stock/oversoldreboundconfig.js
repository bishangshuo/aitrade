import request from '@/utils/request'

// 查询超跌反弹策略回测参数配置列表
export function listOversoldreboundconfig(query) {
  return request({
    url: '/stock/oversoldreboundconfig/list',
    method: 'get',
    params: query
  })
}

// 查询超跌反弹策略回测参数配置详细
export function getOversoldreboundconfig(id) {
  return request({
    url: '/stock/oversoldreboundconfig/' + id,
    method: 'get'
  })
}

// 新增超跌反弹策略回测参数配置
export function addOversoldreboundconfig(data) {
  return request({
    url: '/stock/oversoldreboundconfig',
    method: 'post',
    data: data
  })
}

// 修改超跌反弹策略回测参数配置
export function updateOversoldreboundconfig(data) {
  return request({
    url: '/stock/oversoldreboundconfig',
    method: 'put',
    data: data
  })
}

// 删除超跌反弹策略回测参数配置
export function delOversoldreboundconfig(id) {
  return request({
    url: '/stock/oversoldreboundconfig/' + id,
    method: 'delete'
  })
}
