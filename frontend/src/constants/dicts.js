export const AUDIT_TYPE_OPTIONS = [
  { label: '全部类型', value: 'ALL' },
  { label: '资源审核', value: 'RESOURCE' },
  { label: '商品审核', value: 'PRODUCT' },
  { label: '活动审核', value: 'EVENT' }
]

export const AUDIT_STATUS_OPTIONS = [
  { label: '全部状态', value: '' },
  { label: '待审核', value: 0 },
  { label: '已通过/上架', value: 1 },
  { label: '已驳回/下架', value: 2 }
]

export const RESOURCE_TYPE_OPTIONS = [
  { label: '全部类型', value: '' },
  { label: '视频', value: 1 },
  { label: '音频', value: 2 },
  { label: '图文', value: 3 },
  { label: '剧本PDF', value: 4 }
]

export const INHERITOR_LEVEL_OPTIONS = [
  { label: '全部等级', value: '' },
  { label: '国家级', value: '国家级' },
  { label: '省级', value: '省级' },
  { label: '市级', value: '市级' },
  { label: '县/区级', value: '县/区级' }
]

export const REQUEST_METHOD_OPTIONS = [
  { label: '全部方法', value: '' },
  { label: 'GET', value: 'GET' },
  { label: 'POST', value: 'POST' },
  { label: 'PUT', value: 'PUT' },
  { label: 'DELETE', value: 'DELETE' }
]

export const getAuditStatusLabel = (status) => {
  if (status === 0) return '待审核'
  if (status === 1) return '已通过/上架'
  if (status === 2) return '已驳回/下架'
  return '未知'
}

export const getAuditStatusType = (status) => {
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'info'
}

export const getResourceTypeLabel = (type) => {
  const item = RESOURCE_TYPE_OPTIONS.find(option => option.value === type)
  return item?.label || '未知'
}
