<template>
  <div class="page">
    <el-card>
      <h2>传承人申请审核</h2>
      <el-table :data="list" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="120" />
        <el-table-column prop="level" label="等级" />
        <el-table-column label="流派" min-width="180">
          <template #default="{ row }">
            <div class="genre-cell">
              <span>{{ row.genre || '-' }}</span>
              <el-tag v-if="isCustomGenre(row)" size="small" type="warning">用户自填</el-tag>
              <el-tag v-else-if="row.genre" size="small" type="success">已收录</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="masterName" label="师承" />
        <el-table-column label="演艺经历" width="160">
          <template #default="{ row }">
            <el-button
              v-if="row.artisticCareer"
              link
              type="primary"
              @click="openDetailDialog('演艺经历', row.artisticCareer)"
            >
              点击查看
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="获奖情况" width="160">
          <template #default="{ row }">
            <el-button
              v-if="row.awards"
              link
              type="primary"
              @click="openDetailDialog('获奖情况', row.awards)"
            >
              点击查看
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="approve(row)">通过</el-button>
            <el-button
              v-if="canAddGenre(row)"
              type="primary"
              size="small"
              plain
              @click="approve(row, true)"
            >
              通过并收录流派
            </el-button>
            <el-button type="danger" size="small" @click="reject(row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card style="margin-top: 16px;">
      <h2>师承变更申请审核</h2>
      <el-table :data="masterChangeList" style="width: 100%" v-loading="masterChangeLoading">
        <el-table-column prop="studentName" label="申请人" width="140" />
        <el-table-column prop="currentMasterName" label="当前师父" width="160" />
        <el-table-column prop="targetMasterName" label="申请改为" width="160" />
        <el-table-column label="申请说明" min-width="180">
          <template #default="{ row }">
            <el-button
              v-if="row.reason"
              link
              type="primary"
              @click="openDetailDialog('师承变更申请说明', row.reason)"
            >
              点击查看
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="auditMasterChange(row, true)">通过</el-button>
            <el-button type="danger" size="small" @click="auditMasterChange(row, false)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailDialogVisible" :title="detailDialogTitle" width="640px">
      <div class="detail-content">{{ detailDialogContent || '暂无内容' }}</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const list = ref([])
const masterChangeList = ref([])
const genreLibrary = ref([])
const loading = ref(false)
const masterChangeLoading = ref(false)
const detailDialogVisible = ref(false)
const detailDialogTitle = ref('')
const detailDialogContent = ref('')

const normalizeGenre = (value) => (value || '').trim()

const isCustomGenre = (row) => {
  const genre = normalizeGenre(row?.genre)
  return !!genre && !genreLibrary.value.includes(genre)
}

const canAddGenre = (row) => isCustomGenre(row)

const loadGenreLibrary = async () => {
  try {
    const res = await request.get('/home/content', {
      params: { type: 'INHERITOR_GENRE' }
    })
    genreLibrary.value = Array.isArray(res.data)
      ? res.data.map(item => normalizeGenre(item.title)).filter(Boolean)
      : []
  } catch (e) {
    genreLibrary.value = []
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/inheritor/pending')
    list.value = res.data || []
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const fetchMasterChangeList = async () => {
  masterChangeLoading.value = true
  try {
    const res = await request.get('/admin/apprenticeship/change-apply')
    masterChangeList.value = res.data || []
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '加载师承变更申请失败')
  } finally {
    masterChangeLoading.value = false
  }
}

const saveGenreToLibrary = async (genre) => {
  const normalizedGenre = normalizeGenre(genre)
  if (!normalizedGenre || genreLibrary.value.includes(normalizedGenre)) {
    return false
  }
  await request.post('/admin/home/content', {
    type: 'INHERITOR_GENRE',
    title: normalizedGenre,
    subtitle: '',
    content: '审核员在传承人申请审核时收录',
    imageUrl: '',
    linkUrl: '',
    sortOrder: genreLibrary.value.length,
    status: 1
  })
  genreLibrary.value = [...genreLibrary.value, normalizedGenre]
  return true
}

const openDetailDialog = (title, content) => {
  detailDialogTitle.value = title
  detailDialogContent.value = content || ''
  detailDialogVisible.value = true
}

const approve = async (row, addGenre = false) => {
  const genre = normalizeGenre(row?.genre)
  const message = addGenre && genre
    ? `确定通过该申请，并将“${genre}”加入流派库吗？`
    : '确定通过该申请？'
  await ElMessageBox.confirm(message, '提示')
  try {
    await request.put('/admin/inheritor/audit', {
      id: row.id,
      status: 1,
      remark: '通过'
    })

    if (addGenre && genre) {
      try {
        const added = await saveGenreToLibrary(genre)
        ElMessage.success(added ? '已通过并收录流派' : '已通过，流派已存在无需重复收录')
      } catch (e) {
        ElMessage.warning('审核已通过，但收录流派失败，请稍后到“传承流派”中手动添加')
      }
    } else {
      ElMessage.success('已通过')
    }

    fetchList()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  }
}

const reject = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回理由', '驳回申请', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S/,
      inputErrorMessage: '理由不能为空'
    })
    
    await request.put('/admin/inheritor/audit', {
      id: row.id,
      status: 2,
      remark: value
    })
    ElMessage.success('已驳回')
    fetchList()
  } catch (e) {
    if (e !== 'cancel') {
       ElMessage.error(e?.response?.data?.message || '操作失败')
    }
  }
}

const auditMasterChange = async (row, pass) => {
  await ElMessageBox.confirm(
    pass
      ? `确定通过 ${row.studentName} 的师承变更申请吗？`
      : `确定驳回 ${row.studentName} 的师承变更申请吗？`,
    '提示'
  )
  try {
    await request.put(`/admin/apprenticeship/change-apply/${row.id}`, { pass })
    ElMessage.success(pass ? '已通过师承变更申请' : '已驳回师承变更申请')
    fetchMasterChangeList()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  }
}

onMounted(async () => {
  await loadGenreLibrary()
  fetchList()
  fetchMasterChangeList()
})
</script>

<style scoped>
.page {
  padding: 16px;
}

.genre-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-content {
  max-height: 60vh;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.7;
  color: #303133;
}
</style>
