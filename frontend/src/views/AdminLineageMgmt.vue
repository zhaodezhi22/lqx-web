<template>
  <div class="admin-lineage-mgmt">
    <h2>师承关系管理</h2>

    <el-card class="section-card" shadow="never">
      <template #header>待审核师承变更申请</template>
      <el-table :data="changeApplyList" style="width: 100%" v-loading="changeApplyLoading">
        <el-table-column prop="studentName" label="申请人" />
        <el-table-column prop="currentMasterName" label="当前师父" />
        <el-table-column prop="targetMasterName" label="申请改为" />
        <el-table-column prop="reason" label="申请说明" show-overflow-tooltip />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="success" @click="auditChangeApply(row, true)">通过</el-button>
            <el-button link type="danger" @click="auditChangeApply(row, false)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!changeApplyLoading && changeApplyList.length === 0" description="暂无待审核的师承变更申请" />
    </el-card>

    <el-card class="section-card" shadow="never">
      <template #header>已生效师承关系</template>
      <el-table :data="lineageList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="masterName" label="师傅姓名" />
        <el-table-column prop="apprenticeName" label="徒弟姓名" />
        <el-table-column prop="heritageItem" label="非遗项目" />
        <el-table-column prop="startDate" label="拜师时间" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="danger" @click="deleteLineage(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Dialog Removed -->
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import request from '../utils/request'

const loading = ref(false)
const lineageList = ref([])
const changeApplyLoading = ref(false)
const changeApplyList = ref([])

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

const fetchChangeApplyList = async () => {
  changeApplyLoading.value = true
  try {
    const res = await request.get('/admin/apprenticeship/change-apply')
    changeApplyList.value = res.data || []
  } catch (e) {
    ElMessage.error('加载师承变更申请失败')
  } finally {
    changeApplyLoading.value = false
  }
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

const auditChangeApply = async (row, pass) => {
  await ElMessageBox.confirm(
    pass
      ? `确定通过 ${row.studentName} 的师承变更申请吗？`
      : `确定驳回 ${row.studentName} 的师承变更申请吗？`,
    '提示'
  )
  try {
    await request.put(`/admin/apprenticeship/change-apply/${row.id}`, { pass })
    ElMessage.success(pass ? '已通过师承变更申请' : '已驳回师承变更申请')
    fetchChangeApplyList()
    fetchList()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  }
}

onMounted(() => {
  fetchList()
  fetchChangeApplyList()
})
</script>

<style scoped>
.admin-lineage-mgmt {
  padding: 20px;
}
.section-card {
  margin-bottom: 20px;
}
</style>
