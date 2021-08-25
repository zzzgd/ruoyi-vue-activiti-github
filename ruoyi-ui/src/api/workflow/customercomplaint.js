import request from '@/utils/request'

// 查询请假列表
export function listCustomercomplaint(query) {
  return request({
    url: '/workflow/customercomplaint/list',
    method: 'get',
    params: query
  })
}
// 查询请假列表
export function listCustomercomplaintAll(query) {
  return request({
    url: '/workflow/customercomplaint/listAll',
    method: 'get',
    params: query
  })
}

// 查询请假详细
export function getCustomercomplaint(id) {
  return request({
    url: '/workflow/customercomplaint/' + id,
    method: 'get'
  })
}


// 新增请假
export function addCustomercomplaint(data) {
  return request({
    url: '/workflow/customercomplaint',
    method: 'post',
    data: data
  })
}

// 修改请假
export function updateCustomercomplaint(data) {
  return request({
    url: '/workflow/customercomplaint',
    method: 'put',
    data: data
  })
}

// 删除请假
export function delCustomercomplaint(id) {
  return request({
    url: '/workflow/customercomplaint/' + id,
    method: 'delete'
  })
}

// 导出请假
export function exportCustomercomplaint(query) {
  return request({
    url: '/workflow/customercomplaint/export',
    method: 'get',
    params: query
  })
}
