<template>
  <div class="page">
    <el-card>
      <div class="page-header">
        <div>
          <h2>后台操作日志</h2>
          <p class="page-tip">记录审核员与管理员在后台发起的接口操作，便于回溯和排查。</p>
        </div>
        <el-button type="primary" @click="fetchList">刷新</el-button>
      </div>

      <div class="filter-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索操作人或动作名"
          clearable
          class="filter-item keyword"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="method" clearable class="filter-item" @change="handleSearch">
          <el-option v-for="item in REQUEST_METHOD_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-input
          v-model="path"
          placeholder="按接口路径筛选"
          clearable
          class="filter-item"
          @keyup.enter="handleSearch"
        />
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="createdTime" label="时间" width="180">
          <template #default="{ row }">{{ formatDate(row.createdTime) }}</template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="operatorRole" label="角色" width="100">
          <template #default="{ row }">{{ getRoleLabel(row.operatorRole) }}</template>
        </el-table-column>
        <el-table-column prop="requestMethod" label="方法" width="100">
          <template #default="{ row }">
            <el-tag :type="getMethodTagType(row.requestMethod)">{{ row.requestMethod }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestPath" label="接口路径" min-width="220" />
        <el-table-column prop="actionName" label="动作" min-width="220" />
        <el-table-column prop="responseStatus" label="响应" width="90" />
        <el-table-column prop="successFlag" label="结果" width="90">
          <template #default="{ row }">
            <el-tag :type="row.successFlag === 1 ? 'success' : 'danger'">
              {{ row.successFlag === 1 ? '成功' : '失败' }}
            </el-tag>
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
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { REQUEST_METHOD_OPTIONS } from '../constants/dicts'

const list = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const keyword = ref('')
const method = ref('')
const path = ref('')

const formatDate = (value) => {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

const getRoleLabel = (role) => {
  if (role === 3) return 'root'
  if (role === 2) return '审核员'
  if (role === 1) return '传承人'
  return '用户'
}

const getMethodTagType = (value) => {
  if (value === 'GET') return 'info'
  if (value === 'POST') return 'success'
  if (value === 'PUT') return 'warning'
  if (value === 'DELETE') return 'danger'
  return ''
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (method.value) params.method = method.value
    if (path.value.trim()) params.path = path.value.trim()
    const res = await request.get('/admin/logs', { params })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    ElMessage.error('加载操作日志失败')
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
  method.value = ''
  path.value = ''
  handleSearch()
}

onMounted(fetchList)
</script>

<style scoped>
.page {
  padding: 16px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}
.page-tip {
  margin: 8px 0 0;
  color: #666;
}
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}
.filter-item {
  width: 220px;
}
.filter-item.keyword {
  width: 280px;
}
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
