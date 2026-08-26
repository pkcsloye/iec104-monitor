import request from '@/utils/request'

// 查询映射配置列表
export function listMapping(query) {
  return request({
    url: '/iec104/mapping/list',
    method: 'get',
    params: query
  })
}

// 获取映射配置详细信息
export function getMapping(mappingId) {
  return request({
    url: '/iec104/mapping/' + mappingId,
    method: 'get'
  })
}

// 查询所有启用的映射配置
export function listAllMapping() {
  return request({
    url: '/iec104/mapping/all',
    method: 'get'
  })
}

// 新增映射配置
export function addMapping(data) {
  return request({
    url: '/iec104/mapping',
    method: 'post',
    data: data
  })
}

// 修改映射配置
export function updateMapping(data) {
  return request({
    url: '/iec104/mapping',
    method: 'put',
    data: data
  })
}

// 删除映射配置
export function delMapping(mappingIds) {
  return request({
    url: '/iec104/mapping/' + mappingIds,
    method: 'delete'
  })
}
