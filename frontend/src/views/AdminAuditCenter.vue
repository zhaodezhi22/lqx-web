<template>
  <div class="page">
    <el-card>
      <div class="page-header">
        <div>
          <h2>统一审核中心</h2>
          <p class="page-tip">集中处理资源与商品审核，支持按类型、状态与关键字快速筛选。</p>
        </div>
        <div class="header-actions">
          <el-button @click="openBatchAudit(1)" :disabled="!multipleSelection.length">批量通过</el-button>
          <el-button type="danger" plain @click="openBatchAudit(2)" :disabled="!multipleSelection.length">批量驳回</el-button>
          <el-button type="primary" @click="fetchList">刷新</el-button>
        </div>
      </div>

      <PageFilterBar>
        <el-input
          v-model="keyword"
          placeholder="搜索标题、商品名"
          clearable
          class="filter-item keyword"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="bizType" class="filter-item" @change="handleSearch">
          <el-option v-for="item in AUDIT_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="status" class="filter-item" clearable @change="handleSearch">
          <el-option v-for="item in AUDIT_STATUS_OPTIONS" :key="String(item.value)" :label="item.label" :value="item.value" />
        </el-select>
        <el-input
          v-model="applicantName"
          placeholder="按提交人筛选"
          clearable
          class="filter-item"
          @keyup.enter="handleSearch"
        />
        <el-button @click="resetFilters">重置</el-button>
        <el-switch
          v-model="pendingOnly"
          inline-prompt
          active-text="只看待审"
          inactive-text="全部状态"
          @change="handlePendingToggle"
        />
      </PageFilterBar>

      <div class="stats-grid">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">当前列表</div>
          <div class="stat-value">{{ stats.total }}</div>
        </el-card>
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">待审核</div>
          <div class="stat-value warning">{{ stats.pending }}</div>
        </el-card>
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">资源条数</div>
          <div class="stat-value">{{ stats.resource }}</div>
        </el-card>
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">商品条数</div>
          <div class="stat-value success">{{ stats.product }}</div>
        </el-card>
      </div>

      <el-table :data="list" border stripe v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" :selectable="isRowSelectable" />
        <el-table-column prop="bizType" label="审核类型" width="110">
          <template #default="{ row }">
            <el-tag :type="row.bizType === 'RESOURCE' ? 'warning' : 'success'">
              {{ row.bizType === 'RESOURCE' ? '资源' : '商品' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题/名称" min-width="220" />
        <el-table-column prop="subTitle" label="补充信息" min-width="180">
          <template #default="{ row }">
            {{ row.subTitle || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="meta" label="业务信息" min-width="160">
          <template #default="{ row }">
            {{ row.meta || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="提交人" width="120" />
        <el-table-column prop="status" label="状态" width="130">
          <template #default="{ row }">
            <el-tag :type="getAuditStatusType(row.status)">{{ getAuditStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="auditRemark" label="审核备注" min-width="180">
          <template #default="{ row }">
            {{ row.auditRemark || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">查看详情</el-button>
            <template v-if="row.status === 0">
              <el-button link type="success" @click="openSingleAudit(row, 1)">通过</el-button>
              <el-button link type="danger" @click="openSingleAudit(row, 2)">驳回</el-button>
            </template>
            <span v-else class="muted">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="prev, pager, next, total"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <el-dialog v-model="auditDialogVisible" :title="auditDialogTitle" width="520px">
      <el-form label-width="90px">
        <el-form-item label="审核备注">
          <el-input
            v-model="auditRemark"
            type="textarea"
            :rows="4"
            :placeholder="auditStatus === 2 ? '请输入驳回原因或处理说明' : '可选填写通过说明'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resourceDetailVisible" title="资源详情" width="50%">
      <div v-if="resourceDetail" class="resource-detail">
        <h3>{{ resourceDetail.title }}</h3>
        <p class="detail-meta">提交人：{{ resourceDetail.applicantName || '-' }}</p>
        <p class="detail-meta">分类：{{ resourceDetail.subTitle || '-' }}</p>
        <p class="detail-meta">类型：{{ resourceDetail.meta || '-' }}</p>
        <img v-if="resourceDetail.coverImage" :src="resourceDetail.coverImage" class="detail-image" />
        <p class="detail-desc">{{ resourceDetail.description || '暂无描述' }}</p>
        <p class="detail-meta">资源链接：{{ resourceDetail.fileUrl || '-' }}</p>
      </div>
    </el-dialog>

    <ProductDetailModal
      v-model:visible="productDetailVisible"
      :product-id="detailProductId"
      :is-admin="true"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { useRoute } from 'vue-router'
import ProductDetailModal from '../components/ProductDetailModal.vue'
import PageFilterBar from '../components/common/PageFilterBar.vue'
import {
  AUDIT_STATUS_OPTIONS,
  AUDIT_TYPE_OPTIONS,
  getAuditStatusLabel,
  getAuditStatusType
} from '../constants/dicts'
import { clearRememberedState, loadRememberedState, saveRememberedState } from '../utils/viewState'

const route = useRoute()
const list = ref([])
const loading = ref(false)
const rememberedState = loadRememberedState('admin-audit-center-filters', {
  keyword: '',
  bizType: 'ALL',
  status: '',
  applicantName: '',
  pendingOnly: false
})
const keyword = ref(rememberedState.keyword)
const bizType = ref(rememberedState.bizType)
const status = ref(rememberedState.status)
const applicantName = ref(rememberedState.applicantName)
const pendingOnly = ref(rememberedState.pendingOnly)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const multipleSelection = ref([])
const auditDialogVisible = ref(false)
const auditMode = ref('single')
const auditStatus = ref(1)
const auditRemark = ref('')
const currentRow = ref(null)
const resourceDetailVisible = ref(false)
const resourceDetail = ref(null)
const productDetailVisible = ref(false)
const detailProductId = ref(null)

const formatDate = (value) => {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

const auditDialogTitle = computed(() => {
  const action = auditStatus.value === 1 ? '通过' : '驳回'
  return auditMode.value === 'batch' ? `批量${action}` : `审核${action}`
})

const stats = computed(() => {
  const source = list.value || []
  return {
    total: source.length,
    pending: source.filter(item => item.status === 0).length,
    resource: source.filter(item => item.bizType === 'RESOURCE').length,
    product: source.filter(item => item.bizType === 'PRODUCT').length
  }
})

const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      type: bizType.value
    }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    const effectiveStatus = pendingOnly.value ? 0 : status.value
    if (effectiveStatus !== '' && effectiveStatus !== null && effectiveStatus !== undefined) params.status = effectiveStatus
    if (applicantName.value.trim()) params.applicantName = applicantName.value.trim()
    saveRememberedState('admin-audit-center-filters', {
      keyword: keyword.value,
      bizType: bizType.value,
      status: status.value,
      applicantName: applicantName.value,
      pendingOnly: pendingOnly.value
    })
    const res = await request.get('/admin/audit-center/items', { params })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    ElMessage.error('加载审核中心数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchList()
}

const resetFilters = () => {
  keyword.value = ''
  bizType.value = 'ALL'
  status.value = ''
  applicantName.value = ''
  pendingOnly.value = false
  clearRememberedState('admin-audit-center-filters')
  handleSearch()
}

const handleSelectionChange = (rows) => {
  multipleSelection.value = rows
}

const isRowSelectable = (row) => row.status === 0

const handlePendingToggle = (value) => {
  if (value) {
    status.value = 0
  }
  handleSearch()
}

const syncRouteFilters = () => {
  if (route.query.type) {
    bizType.value = String(route.query.type).toUpperCase()
  }
  if (route.query.status !== undefined && route.query.status !== '') {
    status.value = Number(route.query.status)
    pendingOnly.value = status.value === 0
  }
}

const viewDetail = async (row) => {
  try {
    const res = await request.get(`/admin/audit-center/${row.bizType}/${row.recordId}`)
    if (row.bizType === 'RESOURCE') {
      resourceDetail.value = res.data
      resourceDetailVisible.value = true
      return
    }
    detailProductId.value = row.recordId
    productDetailVisible.value = true
  } catch (e) {
    ElMessage.error('加载详情失败')
  }
}

const openSingleAudit = (row, nextStatus) => {
  currentRow.value = row
  auditMode.value = 'single'
  auditStatus.value = nextStatus
  auditRemark.value = ''
  auditDialogVisible.value = true
}

const openBatchAudit = (nextStatus) => {
  if (!multipleSelection.value.length) {
    ElMessage.warning('请先选择待审核数据')
    return
  }
  auditMode.value = 'batch'
  auditStatus.value = nextStatus
  auditRemark.value = ''
  auditDialogVisible.value = true
}

const submitAudit = async () => {
  const actionText = auditStatus.value === 1 ? '通过' : '驳回'
  if (auditStatus.value === 2 && !auditRemark.value.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  try {
    await ElMessageBox.confirm(`确定${actionText}${auditMode.value === 'batch' ? '所选数据' : `该${currentRow.value?.bizType === 'RESOURCE' ? '资源' : '商品'}` }吗？`, '审核确认', {
      type: auditStatus.value === 1 ? 'success' : 'warning'
    })
    if (auditMode.value === 'batch') {
      await request.post('/admin/audit-center/batch', {
        status: auditStatus.value,
        remark: auditRemark.value.trim(),
        items: multipleSelection.value.map(item => ({
          bizType: item.bizType,
          recordId: item.recordId
        }))
      })
    } else {
      await request.put(`/admin/audit-center/${currentRow.value.bizType}/${currentRow.value.recordId}`, {
        status: auditStatus.value,
        remark: auditRemark.value.trim()
      })
    }
    auditDialogVisible.value = false
    multipleSelection.value = []
    currentRow.value = null
    ElMessage.success(`审核${actionText}成功`)
    fetchList()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e?.response?.data?.message || '审核操作失败')
    }
  }
}

onMounted(() => {
  syncRouteFilters()
  fetchList()
})
</script>

<style scoped>
.page {
  padding: 16px;
}
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}
.header-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.page-tip {
  margin: 8px 0 0;
  color: #666;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}
.stat-card {
  border-radius: 12px;
}
.stat-label {
  color: #909399;
  font-size: 13px;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}
.stat-value.warning {
  color: #e6a23c;
}
.stat-value.success {
  color: #67c23a;
}
.filter-item {
  width: 180px;
}
.filter-item.keyword {
  width: 260px;
}
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
.muted {
  color: #999;
}
.resource-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.detail-meta {
  color: #666;
  margin: 0;
}
.detail-desc {
  line-height: 1.7;
  margin: 0;
  color: #303133;
}
.detail-image {
  max-width: 100%;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}
</style>
