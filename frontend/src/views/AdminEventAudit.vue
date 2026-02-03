<template>
  <div class="page">
    <el-card>
      <div class="header">
        <h2>活动审核</h2>
        <div class="filter">
           <el-button type="primary" @click="fetchList">刷新</el-button>
        </div>
      </div>
      
      <el-table :data="list" style="width: 100%" v-loading="loading">
        <el-table-column prop="eventId" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="venue" label="地点" width="150" />
        <el-table-column prop="showTime" label="时间" width="180">
             <template #default="{ row }">
                 {{ row.showTime?.replace('T', ' ') }}
             </template>
        </el-table-column>
        <el-table-column prop="ticketPrice" label="票价" width="100" />
        <el-table-column prop="publisherId" label="发布者ID" width="100" />
        <el-table-column prop="status" label="状态" width="100">
           <template #default="{ row }">
             <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <template v-if="row.status === 0">
                <el-button link type="success" @click="audit(row, 1)">通过</el-button>
                <el-button link type="danger" @click="audit(row, 2)">驳回</el-button>
            </template>
            <span v-else class="text-gray">已处理</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const list = ref([])
const loading = ref(false)

const getStatusLabel = (status) => {
  if (status === 0) return '待审核'
  if (status === 1) return '已发布'
  if (status === 2) return '已驳回'
  return '未知'
}

const getStatusType = (status) => {
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'info'
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/events', {
      params: { all: true }
    })
    // Sort: Pending (0) first
    let data = res.data || []
    data.sort((a, b) => {
        if (a.status === 0 && b.status !== 0) return -1
        if (a.status !== 0 && b.status === 0) return 1
        return 0
    })
    list.value = data
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const audit = async (row, status) => {
  try {
    await request.put(`/admin/event/audit`, { 
        id: row.eventId, 
        status: status 
    })
    ElMessage.success('操作成功')
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
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.text-gray {
    color: #999;
    font-size: 12px;
}
</style>