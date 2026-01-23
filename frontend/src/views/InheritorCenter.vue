<template>
  <div class="page">
    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="我的档案" name="profile">
        <div class="form-container">
          <h3>传承人档案</h3>
          <el-form :model="profileForm" label-width="100px">
            <el-form-item label="等级">
              <el-input v-model="profileForm.level" disabled />
            </el-form-item>
            <el-form-item label="师承">
              <el-input v-model="profileForm.masterName" />
            </el-form-item>
            <el-form-item label="流派">
              <el-input v-model="profileForm.genre" />
            </el-form-item>
            <el-form-item label="获奖情况">
              <el-input v-model="profileForm.awards" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="演艺经历">
              <el-input v-model="profileForm.artisticCareer" type="textarea" :rows="4" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="profileLoading" @click="updateProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <el-tab-pane label="资源上传" name="upload">
        <div class="form-container">
          <h3>上传非遗资源</h3>
          <el-form :model="resourceForm" label-width="100px">
            <el-form-item label="标题">
              <el-input v-model="resourceForm.title" />
            </el-form-item>
            <el-form-item label="类型">
              <el-select v-model="resourceForm.type">
                <el-option label="视频" :value="1" />
                <el-option label="音频" :value="2" />
                <el-option label="图文" :value="3" />
                <el-option label="剧本PDF" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item label="分类">
              <el-input v-model="resourceForm.category" placeholder="如：经典剧目、拉魂腔" />
            </el-form-item>
            <el-form-item label="封面URL">
              <el-input v-model="resourceForm.coverImg" />
            </el-form-item>
            <el-form-item label="文件URL">
              <el-input v-model="resourceForm.fileUrl" />
            </el-form-item>
            <el-form-item label="简介">
              <el-input v-model="resourceForm.description" type="textarea" :rows="4" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="resourceLoading" @click="submitResource">提交审核</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="活动管理" name="activity">
        <div class="toolbar">
           <el-button type="primary" @click="showActivityDialog">发布新活动</el-button>
        </div>
        <el-table :data="activities" style="width: 100%; margin-top: 20px;" v-loading="activityLoading">
          <el-table-column prop="title" label="活动标题" />
          <el-table-column prop="showTime" label="时间" />
          <el-table-column prop="venue" label="地点" />
          <el-table-column prop="status" label="状态">
             <template #default="scope">
               <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
                 {{ scope.row.status === 1 ? '已发布' : '审核中' }}
               </el-tag>
             </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      
      <el-tab-pane label="文创销售数据" name="sales">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card shadow="hover">
              <template #header>总销售额</template>
              <div class="stat-num">¥ {{ salesStats.totalSales || '0.00' }}</div>
            </el-card>
          </el-col>
           <el-col :span="8">
            <el-card shadow="hover">
              <template #header>订单总数</template>
              <div class="stat-num">{{ salesStats.orderCount || 0 }}</div>
            </el-card>
          </el-col>
           <el-col :span="8">
            <el-card shadow="hover">
              <template #header>在售商品</template>
              <div class="stat-num">{{ salesStats.productCount || 0 }}</div>
            </el-card>
          </el-col>
        </el-row>
        
        <div class="sales-tips" style="margin-top: 20px; color: #666;">
          <p>* 数据实时更新，仅统计已完成支付的订单。</p>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Activity Dialog -->
    <el-dialog v-model="dialogVisible" title="发布活动">
      <el-form :model="activityForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="activityForm.title" />
        </el-form-item>
        <el-form-item label="时间">
           <el-date-picker v-model="activityForm.showTime" type="datetime" placeholder="选择日期时间" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="activityForm.venue" />
        </el-form-item>
        <el-form-item label="票价">
          <el-input-number v-model="activityForm.ticketPrice" :min="0" />
        </el-form-item>
        <el-form-item label="总座位">
          <el-input-number v-model="activityForm.totalSeats" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="activitySubmitting" @click="submitActivity">确认发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const activeTab = ref('profile')

// Profile
const profileForm = reactive({
  id: null,
  level: '',
  masterName: '',
  genre: '',
  awards: '',
  artisticCareer: ''
})
const profileLoading = ref(false)

const fetchProfile = async () => {
  try {
    const res = await request.get('/inheritor/profile')
    if (res.data) {
      Object.assign(profileForm, res.data)
    }
  } catch (e) {
    // Silent fail or default
  }
}

const updateProfile = async () => {
  profileLoading.value = true
  try {
    await request.put('/inheritor/profile', profileForm)
    ElMessage.success('档案更新成功')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '更新失败')
  } finally {
    profileLoading.value = false
  }
}

// Resource
const resourceForm = reactive({
  title: '',
  type: 1,
  category: '',
  coverImg: '',
  fileUrl: '',
  description: '',
})
const resourceLoading = ref(false)

const submitResource = async () => {
  resourceLoading.value = true
  try {
    await request.post('/resources', resourceForm)
    ElMessage.success('上传成功，请等待管理员审核')
    resourceForm.title = ''
    resourceForm.description = ''
    resourceForm.fileUrl = ''
    resourceForm.coverImg = ''
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '上传失败')
  } finally {
    resourceLoading.value = false
  }
}

// Activity
const activities = ref([])
const activityLoading = ref(false)
const dialogVisible = ref(false)
const activitySubmitting = ref(false)
const activityForm = reactive({
  title: '',
  showTime: '',
  venue: '',
  ticketPrice: 0,
  totalSeats: 100
})

const fetchActivities = async () => {
  activityLoading.value = true
  try {
    const res = await request.get('/events/my-events')
    activities.value = res.data || []
  } catch (e) {
    // ignore
  } finally {
    activityLoading.value = false
  }
}

const showActivityDialog = () => {
  activityForm.title = ''
  activityForm.showTime = ''
  activityForm.venue = ''
  activityForm.ticketPrice = 0
  activityForm.totalSeats = 100
  dialogVisible.value = true
}

const submitActivity = async () => {
  if (!activityForm.title || !activityForm.showTime) {
    ElMessage.warning('请填写必要信息')
    return
  }
  activitySubmitting.value = true
  try {
    await request.post('/events/create', activityForm)
    ElMessage.success('活动发布成功')
    dialogVisible.value = false
    fetchActivities()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '发布失败')
  } finally {
    activitySubmitting.value = false
  }
}

// Sales
const salesStats = ref({
  totalSales: 0,
  orderCount: 0,
  productCount: 0
})

const fetchSalesStats = async () => {
  try {
    const res = await request.get('/mall/sales-stats')
    salesStats.value = res.data || {}
  } catch (e) {
    // ignore
  }
}

onMounted(() => {
  fetchProfile()
  // Load others lazy or now? Let's load all if role is inheritor.
  // We can assume user is inheritor if they are here, or API will fail safely.
  // But maybe better to load when tab changes? Simple is better.
  fetchActivities()
  fetchSalesStats()
})
</script>

<style scoped>
.page {
  padding: 20px;
}
.form-container {
  max-width: 600px;
}
.stat-num {
  font-size: 24px;
  font-weight: bold;
  color: #aa1d1d;
}
</style>
