<template>
  <div class="page">
    <el-card>
      <div class="header">
        <h2>评论审核</h2>
        <div class="filter">
           <el-radio-group v-model="status" @change="fetchList">
             <el-radio-button label="pending">待审核</el-radio-button>
             <el-radio-button label="reported">被举报</el-radio-button>
           </el-radio-group>
        </div>
      </div>
      
      <el-table :data="list" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="content" label="评论内容" />
        <el-table-column prop="user" label="发布用户" width="150" />
        <el-table-column prop="time" label="发布时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="handle(row, 'pass')">通过</el-button>
            <el-button type="danger" size="small" @click="handle(row, 'reject')">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

import request from '../utils/request'

const status = ref('pending')
const list = ref([])
const loading = ref(false)

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/comments', { params: { status: status.value } })
    list.value = res.data || []
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handle = async (row, action) => {
  try {
    await request.put(`/admin/comments/${row.id}/audit`, { action })
    ElMessage.success(action === 'pass' ? '已通过' : '已拒绝')
    fetchList()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// Initial fetch
fetchList()
</script>

<style scoped>
.page { padding: 16px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
</style>
