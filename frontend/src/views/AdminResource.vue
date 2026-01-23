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
            <el-button link type="primary" @click="edit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="editVisible" title="编辑资源" width="50%">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="editForm.category" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="editForm.description" type="textarea" />
        </el-form-item>
        <el-form-item label="封面URL">
          <el-input v-model="editForm.coverImg" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
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
const keyword = ref('')
const editVisible = ref(false)
const editForm = reactive({})

const getTypeLabel = (type) => {
  const map = { 1: '视频', 2: '音频', 3: '图文', 4: '剧本' }
  return map[type] || '未知'
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/resources', { params: { category: keyword.value } })
    list.value = res.data || []
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const edit = (row) => {
  Object.assign(editForm, row)
  editVisible.value = true
}

const saveEdit = async () => {
  try {
    await request.put(`/resources/${editForm.resourceId}`, editForm)
    ElMessage.success('更新成功')
    editVisible.value = false
    fetchList()
  } catch (e) {
    ElMessage.error('更新失败')
  }
}

const remove = async (row) => {
  await ElMessageBox.confirm('确定删除该资源？', '提示')
  try {
    await request.delete(`/resources/${row.resourceId}`)
    ElMessage.success('已删除')
    fetchList()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(fetchList)
</script>

<style scoped>
.page { padding: 16px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
</style>
