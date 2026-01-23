<template>
  <div class="page">
    <el-card>
      <h2>传承人申请审核</h2>
      <el-table :data="list" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="120" />
        <el-table-column prop="level" label="等级" />
        <el-table-column prop="genre" label="流派" />
        <el-table-column prop="masterName" label="师承" />
        <el-table-column prop="artisticCareer" label="演艺经历" />
        <el-table-column prop="awards" label="获奖情况" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="approve(row)">通过</el-button>
            <el-button type="danger" size="small" @click="reject(row)">驳回</el-button>
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

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/inheritor/pending')
    list.value = res.data || []
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const approve = async (row) => {
  await ElMessageBox.confirm('确定通过该申请？', '提示')
  try {
    await request.post(`/inheritor/approve/${row.id}`)
    ElMessage.success('已通过')
    fetchList()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  }
}

const reject = async (row) => {
  await ElMessageBox.confirm('确定驳回并删除该申请？', '提示')
  try {
    await request.post(`/inheritor/reject/${row.id}`)
    ElMessage.success('已驳回')
    fetchList()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  }
}

onMounted(fetchList)
</script>

<style scoped>
.page {
  padding: 16px;
}
</style>

