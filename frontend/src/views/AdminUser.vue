<template>
  <div class="page">
    <el-card>
      <div class="header">
        <h2>用户管理</h2>
        <div class="filter">
          <el-button type="danger" :disabled="selectedUsers.length === 0" @click="handleBatchBan">批量禁用</el-button>
          <el-button type="success" :disabled="selectedUsers.length === 0" @click="handleBatchUnban">批量启用</el-button>
          <el-button type="primary" @click="fetchList">刷新</el-button>
        </div>
      </div>
      
      <el-table :data="list" style="width: 100%" v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="role" label="角色">
          <template #default="{ row }">
             <el-tag v-if="row.role === 0">普通用户</el-tag>
             <el-tag v-else-if="row.role === 1" type="warning">传承人</el-tag>
             <el-tag v-else-if="row.role === 2" type="danger">管理员</el-tag>
             <el-tag v-else-if="row.role === 3" type="danger" effect="dark">超级管理员</el-tag>
             <el-tag v-else-if="row.role === 4" type="success">学徒</el-tag>
             <el-tag v-else type="info">未知角色</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
           <template #default="{ row }">
             <el-tag :type="row.status === 1 ? 'success' : 'danger'">
               {{ row.status === 1 ? '正常' : '禁用' }}
             </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button link :type="row.status === 1 ? 'danger' : 'success'" @click="toggleBan(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="primary" @click="openRoleDialog(row)">设置角色</el-button>
            <el-button link type="warning" @click="openPwdDialog(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Password Reset Dialog -->
    <el-dialog v-model="pwdDialogVisible" title="重置密码" width="400px">
        <el-form :model="pwdForm" label-width="80px">
            <el-form-item label="新密码">
                <el-input v-model="pwdForm.password" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="pwdDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitResetPwd">确定</el-button>
            </span>
        </template>
    </el-dialog>

    <!-- Role Setting Dialog -->
    <el-dialog v-model="roleDialogVisible" title="设置用户角色" width="400px">
        <el-form :model="roleForm" label-width="80px">
            <el-form-item label="角色">
                <el-select v-model="roleForm.role" placeholder="请选择角色">
                    <el-option label="普通用户" :value="0" />
                    <el-option label="传承人" :value="1" />
                    <el-option label="管理员" :value="2" />
                    <el-option label="超级管理员" :value="3" />
                    <el-option label="学徒" :value="4" />
                </el-select>
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="roleDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitRoleUpdate">确定</el-button>
            </span>
        </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const list = ref([])
const loading = ref(false)
const selectedUsers = ref([])

const pwdDialogVisible = ref(false)
const roleDialogVisible = ref(false)
const currentEditUser = ref(null)

const pwdForm = reactive({ password: '' })
const roleForm = reactive({ role: 0 })

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

const handleSelectionChange = (val) => {
    selectedUsers.value = val
}

const handleBatchBan = () => {
    batchUpdateStatus(0, '禁用')
}

const handleBatchUnban = () => {
    batchUpdateStatus(1, '启用')
}

const batchUpdateStatus = async (status, actionText) => {
    if (selectedUsers.value.length === 0) return
    
    try {
        await ElMessageBox.confirm(`确定要批量${actionText}选中的 ${selectedUsers.value.length} 个用户吗？`, '提示', {
            type: 'warning'
        })
        
        const userIds = selectedUsers.value.map(u => u.userId)
        const res = await request.post('/users/batch-status', { userIds, status })
        
        if (res.code === 200) {
            ElMessage.success('批量操作成功')
            fetchList()
        } else {
            ElMessage.error(res.message || '操作失败')
        }
    } catch (e) {
        if (e !== 'cancel') ElMessage.error('操作失败')
    }
}

const toggleBan = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  
  await ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示')
  
  try {
    await request.put(`/users/${row.userId}`, { status: newStatus })
    ElMessage.success('操作成功')
    row.status = newStatus
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const openPwdDialog = (row) => {
    currentEditUser.value = row
    pwdForm.password = ''
    pwdDialogVisible.value = true
}

const submitResetPwd = async () => {
    if (!pwdForm.password) {
        ElMessage.warning('请输入新密码')
        return
    }
    try {
        const res = await request.put(`/users/${currentEditUser.value.userId}/password`, { password: pwdForm.password })
        if (res.code === 200) {
            ElMessage.success('密码重置成功')
            pwdDialogVisible.value = false
        } else {
            ElMessage.error(res.message || '重置失败')
        }
    } catch (e) {
        ElMessage.error('重置失败')
    }
}

const openRoleDialog = (row) => {
    currentEditUser.value = row
    roleForm.role = row.role
    roleDialogVisible.value = true
}

const submitRoleUpdate = async () => {
    try {
        const res = await request.put(`/users/${currentEditUser.value.userId}/role`, { role: roleForm.role })
        if (res.code === 200) {
            ElMessage.success('角色设置成功')
            currentEditUser.value.role = roleForm.role
            roleDialogVisible.value = false
        } else {
            ElMessage.error(res.message || '设置失败')
        }
    } catch (e) {
        ElMessage.error('设置失败')
    }
}

onMounted(fetchList)
</script>

<style scoped>
.page { padding: 16px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.filter { display: flex; gap: 10px; }
</style>
