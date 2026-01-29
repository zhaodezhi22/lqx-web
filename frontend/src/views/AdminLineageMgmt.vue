<template>
  <div class="admin-lineage-mgmt">
    <h2>师承关系管理</h2>
    
    <!-- <div class="actions">
      <el-button type="primary" @click="dialogVisible = true">添加师承关系</el-button>
    </div> -->

    <el-table :data="lineageList" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="masterName" label="师傅姓名" />
      <el-table-column prop="apprenticeName" label="徒弟姓名" />
      <el-table-column prop="heritageItem" label="非遗项目" />
      <el-table-column prop="startDate" label="拜师时间" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <!-- <el-button link type="primary" @click="editLineage(row)">编辑</el-button> -->
          <el-button link type="danger" @click="deleteLineage(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Dialog Removed -->
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import request from '../utils/request'

const loading = ref(false)
// const dialogVisible = ref(false)
const lineageList = ref([])

// const form = reactive({
//   id: null,
//   masterName: '',
//   apprenticeName: '',
//   heritageItem: '',
//   startDate: ''
// })

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

// const editLineage = (row) => {
//   Object.assign(form, row)
//   dialogVisible.value = true
// }

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

// const saveLineage = () => {
//   dialogVisible.value = false
//   ElMessage.info('暂不支持管理员直接添加，请由传承人发起')
// }

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
