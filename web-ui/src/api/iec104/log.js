import request from '@/utils/request'

// 查询日志文件列表
export function listLogFile(query) {
  return request({
    url: '/iec104/log/list',
    method: 'get',
    params: query
  })
}

// 获取日志文件详细信息
export function getLogFile(fileId) {
  return request({
    url: '/iec104/log/' + fileId,
    method: 'get'
  })
}

// 上传并解析IEC104日志文件
export function uploadLogFile(data) {
  return request({
    url: '/iec104/log/upload',
    method: 'post',
    headers: { 'Content-Type': 'multipart/form-data' },
    data: data
  })
}

// 删除日志文件
export function delLogFile(fileIds) {
  return request({
    url: '/iec104/log/' + fileIds,
    method: 'delete'
  })
}

// 获取文件中的IOA列表
export function getIoaList(fileId) {
  return request({
    url: '/iec104/log/ioas/' + fileId,
    method: 'get'
  })
}

// 获取图表数据
export function getChartData(fileId, ioas) {
  return request({
    url: '/iec104/log/chart/' + fileId,
    method: 'get',
    params: { ioas: ioas }
  })
}

// 查询数据点列表
export function listDataPoint(query) {
  return request({
    url: '/iec104/log/data',
    method: 'get',
    params: query
  })
}
