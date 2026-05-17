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

      <div class="category-panel">
        <div class="category-panel-header">
          <span class="category-title">资源分类维护</span>
          <div class="category-actions" v-if="canManageCategories">
            <el-input v-model="newCategoryName" placeholder="输入新分类名称" style="width: 220px" />
            <el-button type="primary" @click="addCategory">新增分类</el-button>
          </div>
        </div>
        <div class="category-tags">
          <el-tag
            v-for="item in categories"
            :key="item.categoryId"
            :closable="canManageCategories"
            class="category-tag"
            @close="removeCategory(item)"
          >
            {{ item.name }}
          </el-tag>
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
          <el-select v-model="editForm.category" placeholder="请选择资源分类" style="width: 100%">
            <el-option
              v-for="item in categories"
              :key="item.categoryId"
              :label="item.name"
              :value="item.name"
            />
          </el-select>
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
import { computed, onMounted, ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { hasAnyRole } from '../utils/permission'

const list = ref([])
const categories = ref([])
const loading = ref(false)
const keyword = ref('')
const newCategoryName = ref('')
const editVisible = ref(false)
const editForm = reactive({})
const canManageCategories = computed(() => hasAnyRole([2, 3]))

const getTypeLabel = (type) => {
  const map = { 1: '视频', 2: '音频', 3: '图文', 4: '剧本' }
  return map[type] || '未知'
}

const fetchList = async () => {
  loading.value = true
  try {
    // Admin API allows seeing all statuses. 
    // If we want to mimic "management" of existing resources, we can list all.
    const res = await request.get('/admin/resources', { 
      params: { 
        keyword: keyword.value 
        // status: 1 // Optional: Uncomment to only show Active resources
      } 
    })
    // Admin API returns Page object (res.data), records is the list
    list.value = res.data?.records || []
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const res = await request.get('/resource-categories')
    categories.value = res.data || []
  } catch (e) {
    categories.value = []
  }
}

const edit = (row) => {
  Object.assign(editForm, row)
  editVisible.value = true
}

const saveEdit = async () => {
  try {
    if (!editForm.category) {
      ElMessage.warning('请选择资源分类')
      return
    }
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

const addCategory = async () => {
  const name = newCategoryName.value.trim()
  if (!name) {
    ElMessage.warning('请输入分类名称')
    return
  }
  try {
    await request.post('/resource-categories', { name })
    ElMessage.success('分类新增成功')
    newCategoryName.value = ''
    fetchCategories()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '新增分类失败')
  }
}

const removeCategory = async (item) => {
  try {
    await ElMessageBox.confirm(`确定删除分类“${item.name}”吗？`, '提示', { type: 'warning' })
    await request.delete(`/resource-categories/${item.categoryId}`)
    ElMessage.success('分类删除成功')
    fetchCategories()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e?.response?.data?.message || '删除分类失败')
    }
  }
}

onMounted(() => {
  fetchList()
  fetchCategories()
})
</script>

<style scoped>
.page { padding: 16px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.category-panel {
  margin-bottom: 20px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  background: #fafafa;
}
.category-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.category-title {
  font-weight: 600;
  color: #303133;
}
.category-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.category-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}
.category-tag {
  margin-right: 0;
}
</style>
