<template>
  <div class="admin-home-content">
    <div class="page-header">
      <h2>首页内容管理</h2>
      <el-button type="primary" @click="openCreateDialog">新增内容</el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="fetchData">
      <el-tab-pane label="首页轮播图" name="HOME_CAROUSEL">
        <el-table :data="tableData" border style="width: 100%">
          <el-table-column prop="sortOrder" label="排序" width="80" sortable />
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="subtitle" label="副标题" show-overflow-tooltip />
          <el-table-column label="图片" width="120">
            <template #default="{ row }">
              <el-image :src="row.imageUrl" :preview-src-list="[row.imageUrl]" style="width: 100px; height: 50px" fit="cover" />
            </template>
          </el-table-column>
          <el-table-column prop="linkUrl" label="跳转链接" show-overflow-tooltip />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">
                {{ row.status === 1 ? '显示' : '隐藏' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="最新戏曲资讯" name="NEWS_CAROUSEL">
        <el-table :data="tableData" border style="width: 100%">
          <el-table-column prop="sortOrder" label="排序" width="80" sortable />
          <el-table-column prop="title" label="资讯标题" />
          <el-table-column label="封面图" width="120">
            <template #default="{ row }">
              <el-image :src="row.imageUrl" :preview-src-list="[row.imageUrl]" style="width: 100px; height: 50px" fit="cover" />
            </template>
          </el-table-column>
          <el-table-column prop="linkUrl" label="链接" show-overflow-tooltip />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '显示' : '隐藏' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="公告与通知" name="NOTICE">
        <el-table :data="tableData" border style="width: 100%">
          <el-table-column prop="sortOrder" label="排序" width="80" sortable />
          <el-table-column prop="title" label="公告标题" />
          <el-table-column prop="content" label="内容详情" show-overflow-tooltip />
          <el-table-column prop="linkUrl" label="链接" show-overflow-tooltip />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '显示' : '隐藏' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="非遗风采" name="HIGHLIGHT">
        <el-table :data="tableData" border style="width: 100%">
          <el-table-column prop="sortOrder" label="排序" width="80" sortable />
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="content" label="描述" show-overflow-tooltip />
          <el-table-column label="图片" width="120">
            <template #default="{ row }">
              <el-image :src="row.imageUrl" :preview-src-list="[row.imageUrl]" style="width: 100px; height: 50px" fit="cover" />
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '显示' : '隐藏' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

      <!-- Edit/Create Dialog -->
      <el-dialog v-model="dialogVisible" :title="form.id ? '编辑内容' : '新增内容'" width="500px">
        <el-form :model="form" label-width="100px">
          <el-form-item label="标题" required>
            <el-input v-model="form.title" placeholder="请输入标题" />
          </el-form-item>
          
          <el-form-item label="副标题" v-if="activeTab === 'HOME_CAROUSEL'">
            <el-input v-model="form.subtitle" placeholder="请输入副标题" />
          </el-form-item>
          
          <el-form-item label="内容/描述" v-if="['NOTICE', 'HIGHLIGHT', 'NEWS_CAROUSEL'].includes(activeTab)">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入内容详情（资讯和公告将显示在详情页）" />
        </el-form-item>
          
          <el-form-item label="图片" v-if="activeTab !== 'NOTICE'">
           <el-upload
             class="avatar-uploader"
             action="#"
             :show-file-list="false"
             :http-request="customUpload"
           >
             <img v-if="form.imageUrl" :src="form.imageUrl" class="avatar" />
             <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
           </el-upload>
           <div class="el-upload__tip">只能上传jpg/png文件，且不超过2MB</div>
          </el-form-item>
          
          <el-form-item label="跳转链接" v-if="activeTab !== 'HIGHLIGHT'">
            <el-input v-model="form.linkUrl" placeholder="点击跳转的URL" />
          </el-form-item>
          
          <el-form-item label="排序">
            <el-input-number v-model="form.sortOrder" :min="0" />
          </el-form-item>
          
          <el-form-item label="状态">
            <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="显示" inactive-text="隐藏" />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSubmit">确定</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const activeTab = ref('HOME_CAROUSEL')
const tableData = ref([])
const dialogVisible = ref(false)
const form = reactive({
  id: null,
  type: '',
  title: '',
  subtitle: '',
  content: '',
  imageUrl: '',
  linkUrl: '',
  sortOrder: 0,
  status: 1
})

const fetchData = async () => {
  try {
    const res = await request.get('/admin/home/content/list', {
      params: { type: activeTab.value }
    })
    tableData.value = res.data
  } catch (e) {
    ElMessage.error('获取数据失败')
  }
}

const openCreateDialog = () => {
  Object.assign(form, {
    id: null,
    type: activeTab.value,
    title: '',
    subtitle: '',
    content: '',
    imageUrl: '',
    linkUrl: '',
    sortOrder: 0,
    status: 1
  })
  dialogVisible.value = true
}

const customUpload = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)
  try {
    const res = await request.post('/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.code === 200) {
      form.imageUrl = res.data
      options.onSuccess(res.data)
    } else {
      ElMessage.error(res.message || '上传失败')
      options.onError(new Error(res.message))
    }
  } catch (e) {
    ElMessage.error('上传出错')
    options.onError(e)
  }
}

const handleEdit = (row) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该条内容吗？', '提示', { type: 'warning' })
    .then(async () => {
      await request.delete(`/admin/home/content/${row.id}`)
      ElMessage.success('删除成功')
      fetchData()
    })
}

const handleSubmit = async () => {
  if (!form.title) {
    ElMessage.warning('标题不能为空')
    return
  }
  
  try {
    const method = form.id ? 'put' : 'post'
    const url = '/admin/home/content'
    
    await request({
      url: url,
      method: method,
      data: form
    })
    
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.admin-home-content {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}
.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatar {
  width: 178px;
  height: 178px;
  display: block;
}
</style>