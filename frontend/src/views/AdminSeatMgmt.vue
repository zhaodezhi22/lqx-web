<template>
  <div class="seat-mgmt-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>{{ event.title || '演出' }} - 座位管理</h2>
          <div class="actions">
            <el-button @click="$router.back()">返回</el-button>
            <el-button type="warning" @click="handleOffline" v-if="event.status == 1">下架活动</el-button>
            <el-button type="primary" @click="saveSeats">保存设置</el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading">
        <!-- Event Info -->
        <div class="event-info">
          <p><strong>场馆：</strong>{{ event.venue }}</p>
          <p><strong>时间：</strong>{{ event.showTime }}</p>
          <p><strong>状态：</strong>
             <el-tag :type="event.status === 1 ? 'success' : (event.status === 3 ? 'info' : 'warning')">
               {{ event.status === 1 ? '销售中' : (event.status === 3 ? '已下架' : '其他') }}
             </el-tag>
          </p>
        </div>

        <el-divider />

        <!-- Seat Editor Component -->
        <SeatEditor v-model="seats" />
        
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import SeatEditor from '../components/SeatEditor.vue'

const route = useRoute()
const router = useRouter()
const eventId = route.params.id

const loading = ref(false)
const event = ref({})
const seats = ref([])

const fetchEvent = async () => {
  loading.value = true
  try {
    const res = await request.get(`/events/${eventId}`)
    if (res.code === 200) {
      event.value = res.data
      if (event.value.seatLayoutJson) {
        try {
          const parsed = JSON.parse(event.value.seatLayoutJson)
          if (Array.isArray(parsed)) {
            seats.value = parsed
          }
        } catch (e) {
          console.error('Invalid JSON', e)
        }
      }
    }
  } catch (e) {
    ElMessage.error('获取演出信息失败')
  } finally {
    loading.value = false
  }
}

const saveSeats = async () => {
  try {
    // Send as { seatLayoutJson: [...] }
    await request.put(`/admin/event/seats/${eventId}`, {
      seatLayoutJson: seats.value
    })
    ElMessage.success('座位布局保存成功')
    fetchEvent()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '保存失败')
  }
}

const handleOffline = async () => {
  try {
    await ElMessageBox.confirm('确定要强制下架该活动吗？下架后用户将无法购买。', '警告', {
      type: 'warning',
      confirmButtonText: '确定下架',
      cancelButtonText: '取消'
    })
    
    await request.post(`/admin/event/offline/${eventId}`)
    ElMessage.success('活动已下架')
    fetchEvent() // Reload to update status
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || '操作失败')
    }
  }
}

onMounted(() => {
  fetchEvent()
})
</script>

<style scoped>
.seat-mgmt-page {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.event-info {
  display: flex;
  gap: 20px;
  color: #666;
}
</style>