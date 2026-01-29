<template>
  <div class="page-container">
    <div class="header-section">
      <h2>非遗传承人</h2>
      <div class="filters">
         <!-- Placeholder for future filters -->
      </div>
    </div>

    <div class="inheritor-grid" v-loading="loading">
      <el-card 
        v-for="item in inheritors" 
        :key="item.userId" 
        class="inheritor-card" 
        shadow="hover"
        @click="goToProfile(item)"
      >
        <div class="card-content">
          <el-avatar :size="80" :src="item.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
          <h3 class="name">{{ item.name }}</h3>
          <el-tag effect="plain" type="danger" size="small">{{ item.level }}传承人</el-tag>
          <div class="desc">
            <!-- 暂无简介字段，预留 -->
            致力于柳琴戏的传承与发扬...
          </div>
          <div class="actions" style="margin-top: 15px;">
             <el-button type="default" size="small" @click.stop="goToGraph(item)">查看师承</el-button>
             <el-button type="primary" size="small" @click.stop="openApply(item)">拜师</el-button>
          </div>
        </div>
      </el-card>
    </div>
    
    <el-empty v-if="!loading && inheritors.length === 0" description="暂无传承人数据" />

    <!-- Apply Dialog -->
    <el-dialog v-model="applyDialogVisible" title="拜师申请" width="500px">
      <el-form :model="applyForm" label-width="80px">
        <el-form-item label="师父">
          <el-input v-model="applyForm.masterName" disabled />
        </el-form-item>
        <el-form-item label="申请书">
          <el-input 
            v-model="applyForm.content" 
            type="textarea" 
            :rows="4" 
            placeholder="请诚恳填写拜师帖，表达您的仰慕与决心..." 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="applyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitApply">提交申请</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const inheritors = ref([])
const loading = ref(false)

const applyDialogVisible = ref(false)
const applyForm = reactive({
  masterId: null,
  masterName: '',
  content: ''
})

const fetchInheritors = async () => {
  loading.value = true
  try {
    const res = await request.get('/inheritor/list')
    if (res.code === 200) {
      inheritors.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('加载传承人列表失败')
  } finally {
    loading.value = false
  }
}

const goToGraph = (item) => {
  // Pass the inheritor ID to focus on their lineage
  router.push({ path: '/inheritor/graph', query: { id: item.inheritorId } })
}

const goToProfile = (item) => {
  router.push({ name: 'UserPublicProfile', params: { id: item.userId } })
}

const openApply = (item) => {
  applyForm.masterId = item.userId
  applyForm.masterName = item.name
  applyForm.content = ''
  applyDialogVisible.value = true
}

const submitApply = async () => {
  if (!applyForm.content) {
    ElMessage.warning('请填写申请书')
    return
  }
  try {
    const res = await request.post('/master/apprentice/apply', {
      masterId: applyForm.masterId,
      content: applyForm.content
    })
    if (res.code === 200) {
      ElMessage.success('申请提交成功，请等待师父审核')
      applyDialogVisible.value = false
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '提交失败')
  }
}

onMounted(() => {
  fetchInheritors()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.header-section {
  margin-bottom: 24px;
}
.inheritor-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}
.inheritor-card {
  cursor: pointer;
  transition: transform 0.3s;
  text-align: center;
}
.inheritor-card:hover {
  transform: translateY(-5px);
}
.card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 0;
}
.name {
  margin: 15px 0 10px;
  font-size: 18px;
  color: #333;
}
.desc {
  margin-top: 15px;
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
