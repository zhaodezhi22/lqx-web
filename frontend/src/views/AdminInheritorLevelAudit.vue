<template>
  <div class="page">
    <h2>传承人等级变更审核</h2>
    
    <div class="filter-container">
      <el-select v-model="filterStatus" placeholder="状态" @change="fetchList">
        <el-option label="全部" :value="null" />
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已拒绝" :value="2" />
      </el-select>
      <el-button type="primary" @click="fetchList" style="margin-left: 10px;">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" style="width: 100%; margin-top: 20px;">
      <el-table-column prop="userName" label="用户名" width="120" />
      <el-table-column prop="realName" label="真实姓名" width="120" />
      <el-table-column prop="currentLevel" label="当前等级" width="120" />
      <el-table-column prop="applyLevel" label="申请等级" width="120">
         <template #default="{ row }">
            <span style="color: #409EFF; font-weight: bold;">{{ row.applyLevel }}</span>
         </template>
      </el-table-column>
      <el-table-column prop="reason" label="申请理由" show-overflow-tooltip />
      <el-table-column prop="createTime" label="申请时间" width="180" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="primary" size="small" @click="openAudit(row)">审核</el-button>
          <el-button v-else type="info" size="small" @click="openAudit(row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top: 20px; text-align: right;">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        @current-change="handlePageChange"
      />
    </div>

    <el-dialog v-model="auditDialogVisible" title="审核详情" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="申请人">{{ currentItem.realName || currentItem.userName }}</el-descriptions-item>
        <el-descriptions-item label="当前等级">{{ currentItem.currentLevel }}</el-descriptions-item>
        <el-descriptions-item label="申请等级">{{ currentItem.applyLevel }}</el-descriptions-item>
        <el-descriptions-item label="申请理由">{{ currentItem.reason }}</el-descriptions-item>
        <el-descriptions-item label="佐证材料">
           <div v-if="currentItem.proofMaterials && currentItem.proofMaterials.length > 0">
              <div v-for="(url, idx) in parseMaterials(currentItem.proofMaterials)" :key="idx" style="margin-bottom: 5px;">
                 <el-link type="primary" :href="url" target="_blank">查看附件 {{ idx + 1 }}</el-link>
              </div>
           </div>
           <span v-else>无</span>
        </el-descriptions-item>
      </el-descriptions>

      <div v-if="currentItem.status === 0" style="margin-top: 20px;">
         <el-input v-model="auditRemark" type="textarea" :rows="2" placeholder="请输入审核意见（拒绝时必填）" />
      </div>
      <div v-else-if="currentItem.auditRemark" style="margin-top: 20px;">
         <p><strong>审核意见：</strong> {{ currentItem.auditRemark }}</p>
      </div>

      <template #footer>
        <el-button @click="auditDialogVisible = false">关闭</el-button>
        <template v-if="currentItem.status === 0">
           <el-button type="danger" @click="submitAudit(2)">拒绝</el-button>
           <el-button type="success" @click="submitAudit(1)">通过</el-button>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const filterStatus = ref(0) // Default pending

const auditDialogVisible = ref(false)
const currentItem = ref({})
const auditRemark = ref('')

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/inheritor/level/list', {
      params: {
        page: page.value,
        size: pageSize.value,
        status: filterStatus.value
      }
    })
    if (res.code === 200) {
      list.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (val) => {
  page.value = val
  fetchList()
}

const statusText = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return map[status] || '未知'
}

const statusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const parseMaterials = (jsonStr) => {
    if (!jsonStr) return []
    try {
        const parsed = JSON.parse(jsonStr)
        return Array.isArray(parsed) ? parsed : []
    } catch (e) {
        return []
    }
}

const openAudit = (row) => {
  currentItem.value = { ...row }
  auditRemark.value = ''
  auditDialogVisible.value = true
}

const submitAudit = async (status) => {
  if (status === 2 && !auditRemark.value) {
    ElMessage.warning('拒绝时必须填写审核意见')
    return
  }
  
  try {
    await request.put('/admin/inheritor/level/audit', {
      id: currentItem.value.id,
      status: status,
      remark: auditRemark.value
    })
    ElMessage.success('操作成功')
    auditDialogVisible.value = false
    fetchList()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.page {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}
.filter-container {
  margin-bottom: 20px;
}
</style>
