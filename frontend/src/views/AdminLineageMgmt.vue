<template>
  <div class="admin-lineage-mgmt">
    <h2>师承关系管理</h2>
    
    <div class="actions">
      <el-button type="primary" @click="dialogVisible = true">添加师承关系</el-button>
    </div>

    <el-table :data="lineageList" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="masterName" label="师傅姓名" />
      <el-table-column prop="apprenticeName" label="徒弟姓名" />
      <el-table-column prop="heritageItem" label="非遗项目" />
      <el-table-column prop="startDate" label="拜师时间" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button link type="primary" @click="editLineage(row)">编辑</el-button>
          <el-button link type="danger" @click="deleteLineage(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑关系' : '添加关系'">
      <el-form :model="form" label-width="100px">
        <el-form-item label="师傅姓名">
          <el-input v-model="form.masterName" placeholder="输入师傅姓名" />
        </el-form-item>
        <el-form-item label="徒弟姓名">
          <el-input v-model="form.apprenticeName" placeholder="输入徒弟姓名" />
        </el-form-item>
        <el-form-item label="非遗项目">
          <el-input v-model="form.heritageItem" placeholder="例如：剪纸" />
        </el-form-item>
        <el-form-item label="拜师时间">
          <el-date-picker v-model="form.startDate" type="date" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveLineage">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import request from '../utils/request'

const loading = ref(false)
const dialogVisible = ref(false)
const lineageList = ref([])

const form = reactive({
  id: null,
  masterName: '',
  apprenticeName: '',
  heritageItem: '',
  startDate: ''
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/apprenticeship')
    lineageList.value = res.data?.records || []
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const editLineage = (row) => {
  // Editing logic might be complex if changing IDs, so let's just show info or basic edit
  // For now just populate form
  Object.assign(form, row)
  dialogVisible.value = true
}

const deleteLineage = async (row) => {
  await ElMessageBox.confirm(`确定删除 ${row.masterName} - ${row.apprenticeName} 的关系吗？`, '提示')
  try {
    await request.delete(`/admin/apprenticeship/${row.id}`)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const saveLineage = () => {
  // Not implementing create/update for now as it requires selecting users
  dialogVisible.value = false
  ElMessage.info('暂不支持管理员直接添加，请由传承人发起')
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.admin-lineage-mgmt {
  padding: 20px;
}
.actions {
  margin-bottom: 20px;
}
</style>
