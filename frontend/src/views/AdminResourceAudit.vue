<template>
  <div class="page">
    <el-card>
      <div class="header">
        <h2>资源库管理</h2>
        <div class="filter">
           <el-input v-model="keyword" placeholder="搜索资源标题" style="width: 200px; margin-right: 10px" />
           <el-button type="primary" @click="fetchList">搜索</el-button>
        </div>
      </div>
      
      <el-table :data="list" style="width: 100%" v-loading="loading">
        <el-table-column prop="resourceId" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            {{ getTypeLabel(row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="uploaderId" label="上传者ID" width="100" />
        <el-table-column prop="createdTime" label="上传时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="preview(row)">预览</el-button>
            <el-button link type="danger" @click="remove(row)">下架</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="previewVisible" title="资源预览" width="50%">
      <div v-if="currentResource">
        <h3>{{ currentResource.title }}</h3>
        <p>{{ currentResource.description }}</p>
        <img v-if="currentResource.coverImg" :src="currentResource.coverImg" style="max-width: 100%" />
        <p>资源链接: {{ currentResource.fileUrl }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const list = ref([])
const loading = ref(false)
const keyword = ref('')
const previewVisible = ref(false)
const currentResource = ref(null)

const getTypeLabel = (type) => {
  const map = { 1: '视频', 2: '音频', 3: '图文', 4: '剧本' }
  return map[type] || '未知'
}

const fetchList = async () => {
  loading.value = true
  try {
    // In a real app, pass keyword as param
    const res = await request.get('/resources')
    list.value = res.data || []
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const preview = (row) => {
  currentResource.value = row
  previewVisible.value = true
}

const remove = async (row) => {
  await ElMessageBox.confirm('确定下架删除该资源？', '提示')
  try {
    // Mock delete
    // await request.delete(`/resources/${row.resourceId}`)
    ElMessage.success('已下架 (Mock)')
    fetchList()
  } catch (e) {
    // ElMessage.error('操作失败')
  }
}

onMounted(fetchList)
</script>

<style scoped>
.page { padding: 16px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
</style>
