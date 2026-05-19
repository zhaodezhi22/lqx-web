<template>
  <div class="page">
    <el-row :gutter="16">
      <el-col :xs="24" :lg="15">
        <el-card>
          <div class="section-header">
            <div>
              <h2>资源上传</h2>
              <p class="section-tip">管理员可直接在这里新增资源，提交后会进入统一审核中心，便于集中处理。</p>
            </div>
            <div class="header-actions">
              <el-button @click="goToResourceLibrary">资源库管理</el-button>
              <el-button type="primary" @click="goToAuditCenter">查看资源待审</el-button>
            </div>
          </div>

          <el-form :model="form" label-width="96px">
            <el-form-item label="资源标题">
              <el-input v-model="form.title" maxlength="80" show-word-limit placeholder="请输入资源标题" />
            </el-form-item>
            <el-form-item label="资源分类">
              <el-select v-model="form.category" placeholder="请选择资源分类" style="width: 100%">
                <el-option
                  v-for="item in categories"
                  :key="item.categoryId"
                  :label="item.name"
                  :value="item.name"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="资源类型">
              <el-select v-model="form.type" style="width: 100%">
                <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="封面图片">
              <el-upload
                class="cover-uploader"
                action="/api/file/upload"
                :show-file-list="false"
                :before-upload="beforeCoverUpload"
                :on-success="handleCoverSuccess"
              >
                <img v-if="form.coverImg" :src="form.coverImg" class="cover-image" />
                <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">支持 JPG/PNG/GIF/WEBP，大小不超过 5MB。</div>
            </el-form-item>
            <el-form-item label="资源文件">
              <el-upload
                action="/api/file/upload"
                :show-file-list="false"
                :on-success="handleFileSuccess"
              >
                <el-button type="primary" plain>上传资源文件</el-button>
              </el-upload>
              <div class="file-link" v-if="form.fileUrl">{{ form.fileUrl }}</div>
              <div class="upload-tip">支持视频、音频、图文附件、PDF 等资源文件，不限制大小。</div>
            </el-form-item>
            <el-form-item label="资源简介">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="5"
                maxlength="500"
                show-word-limit
                placeholder="请输入资源简介或使用说明"
              />
            </el-form-item>
            <el-form-item>
              <el-button @click="resetForm">重置</el-button>
              <el-button type="primary" :loading="submitting" @click="submitResource">提交资源</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="9">
        <el-card class="side-card">
          <template #header>
            <div class="card-title">使用说明</div>
          </template>
          <div class="guide-list">
            <p>1. 资源审核与商品审核已统一收口到审核中心。</p>
            <p>2. 当前页面保留“资源上传”能力，解决管理员无法发布资源的问题。</p>
            <p>3. 上传完成后可点击“查看资源待审”，直接进入资源待审列表。</p>
          </div>
        </el-card>

        <el-card class="side-card">
          <template #header>
            <div class="card-title">最近资源</div>
          </template>
          <el-table :data="recentResources" size="small" v-loading="recentLoading" empty-text="暂无资源数据">
            <el-table-column prop="title" label="标题" min-width="140" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdTime" label="提交时间" width="170">
              <template #default="{ row }">{{ formatDate(row.createdTime) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'

const router = useRouter()
const categories = ref([])
const recentResources = ref([])
const recentLoading = ref(false)
const submitting = ref(false)
const typeOptions = [
  { label: '视频', value: 1 },
  { label: '音频', value: 2 },
  { label: '图文', value: 3 },
  { label: '剧本PDF', value: 4 }
]

const form = reactive({
  title: '',
  type: 1,
  category: '',
  coverImg: '',
  fileUrl: '',
  description: ''
})

const formatDate = (value) => {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

const getStatusType = (status) => {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'warning'
}

const getStatusLabel = (status) => {
  if (status === 1) return '已通过'
  if (status === 2) return '已驳回'
  return '待审核'
}

const fetchCategories = async () => {
  try {
    const res = await request.get('/resource-categories')
    categories.value = res.data || []
  } catch (e) {
    categories.value = []
  }
}

const fetchRecentResources = async () => {
  recentLoading.value = true
  try {
    const res = await request.get('/admin/resources', { params: { page: 1, size: 6 } })
    recentResources.value = res.data?.records || []
  } catch (e) {
    recentResources.value = []
  } finally {
    recentLoading.value = false
  }
}

const resetForm = () => {
  form.title = ''
  form.type = 1
  form.category = ''
  form.coverImg = ''
  form.fileUrl = ''
  form.description = ''
}

const beforeCoverUpload = (file) => {
  const isImage = file.type?.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('封面必须为图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('封面图片大小不能超过 5MB')
    return false
  }
  return true
}

const handleCoverSuccess = (res) => {
  if (res.code === 200) {
    form.coverImg = res.data
    ElMessage.success('封面上传成功')
    return
  }
  ElMessage.error(res.message || '封面上传失败')
}

const handleFileSuccess = (res) => {
  if (res.code === 200) {
    form.fileUrl = res.data
    ElMessage.success('资源文件上传成功')
    return
  }
  ElMessage.error(res.message || '资源文件上传失败')
}

const submitResource = async () => {
  if (!form.title.trim()) {
    ElMessage.warning('请输入资源标题')
    return
  }
  if (!form.category) {
    ElMessage.warning('请选择资源分类')
    return
  }
  if (!form.fileUrl) {
    ElMessage.warning('请先上传资源文件')
    return
  }
  submitting.value = true
  try {
    await request.post('/resources', {
      title: form.title.trim(),
      type: form.type,
      category: form.category,
      coverImg: form.coverImg,
      fileUrl: form.fileUrl,
      description: form.description.trim()
    })
    ElMessage.success('资源提交成功，已进入审核流程')
    resetForm()
    fetchRecentResources()
    goToAuditCenter()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '资源提交失败')
  } finally {
    submitting.value = false
  }
}

const goToAuditCenter = () => {
  router.push({ path: '/admin/audit-center', query: { type: 'RESOURCE', status: '0' } })
}

const goToResourceLibrary = () => {
  router.push('/admin/resources')
}

onMounted(() => {
  fetchCategories()
  fetchRecentResources()
})
</script>

<style scoped>
.page {
  padding: 16px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}
.section-header h2 {
  margin: 0;
}
.section-tip {
  margin: 8px 0 0;
  color: #666;
}
.header-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.side-card + .side-card {
  margin-top: 16px;
}
.card-title {
  font-weight: 600;
}
.guide-list {
  color: #606266;
  line-height: 1.8;
}
.guide-list p {
  margin: 0 0 10px;
}
.cover-uploader {
  display: inline-flex;
}
.cover-uploader :deep(.el-upload) {
  width: 160px;
  height: 160px;
  border: 1px dashed var(--el-border-color);
  border-radius: 12px;
  background: #fafafa;
  overflow: hidden;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}
.cover-image {
  width: 160px;
  height: 160px;
  display: block;
  object-fit: cover;
}
.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}
.upload-tip {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}
.file-link {
  margin-top: 8px;
  color: #409eff;
  word-break: break-all;
}
</style>
