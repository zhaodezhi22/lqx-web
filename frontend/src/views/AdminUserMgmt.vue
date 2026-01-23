<template>
  <div class="page">
    <el-card>
      <div class="header">
        <h2>用户管理</h2>
        <el-input v-model="keyword" placeholder="用户名/手机号" style="width: 200px" />
      </div>
      
      <el-table :data="list" style="width: 100%" v-loading="loading">
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="role" label="角色">
          <template #default="{ row }">
             <el-tag v-if="row.role === 0">普通用户</el-tag>
             <el-tag v-else-if="row.role === 1" type="warning">传承人</el-tag>
             <el-tag v-else-if="row.role === 2" type="danger">审核员</el-tag>
             <el-tag v-else-if="row.role === 3" type="danger" effect="dark">管理员</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
           <template #default="{ row }">
             <el-tag :type="row.status === 1 ? 'success' : 'danger'">
               {{ row.status === 1 ? '正常' : '禁用' }}
             </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button link type="primary" @click="edit(row)">编辑</el-button>
            <el-button link type="warning" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger">重置密码</el-button>
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
const keyword = ref('')

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/users')
    list.value = res.data || []
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  
  await ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示')
  
  try {
    // We need an update API. Assuming PUT /users/{id} accepts partial updates or full object
    const updated = { ...row, status: newStatus }
    await request.put(`/users/${row.userId}`, updated)
    ElMessage.success('操作成功')
    row.status = newStatus
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const edit = (row) => {
  ElMessage.info('编辑功能待开发')
}

onMounted(fetchList)
</script>

<style scoped>
.page { padding: 16px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
</style>
