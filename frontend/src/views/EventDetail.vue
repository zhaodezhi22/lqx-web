<template>
  <div class="page" v-loading="loading">
    <div v-if="event">
      <el-button @click="$router.back()" style="margin-bottom: 20px">返回</el-button>
      <h2>{{ event.title }}</h2>
      <div class="publisher-info" v-if="event.publisherId && event.publisherName" @click="goProfile(event.publisherId, event.publisherRole)">
        <el-avatar :size="32" :src="event.publisherAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
        <span class="name">{{ event.publisherName }}</span>
        <el-tag size="small" v-if="event.publisherRole === 1">传承人</el-tag>
        <el-tag size="small" type="warning" effect="dark" v-if="event.publisherRole === 2 || event.publisherRole === 3">官方</el-tag>
      </div>
      <div class="publisher-info-disabled" v-else-if="event.publisherId">
        <el-avatar :size="32" src="https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png" />
        <span class="name">用户已注销</span>
      </div>
      <div class="meta">地点：{{ event.venue }} ｜ 时间：{{ event.showTime }}</div>
      <div v-if="isEnded" style="margin-bottom: 8px">
        <el-tag type="info" effect="dark">演出已结束</el-tag>
      </div>
      <div class="price">票价：¥ {{ event.ticketPrice }}</div>

      <el-card class="desc-card">
        <h3>活动介绍</h3>
        <p>{{ event.description || '暂无介绍' }}</p>
      </el-card>

      <el-card class="seat-card">
        <div class="seat-header">
          <div>请点击座位锁定（黄色为已锁定，红色为已售）</div>
          <el-button 
            type="primary" 
            size="large" 
            :disabled="selected.length === 0 || isEnded" 
            :loading="buying" 
            @click="buyNow"
          >
            {{ isEnded ? '已结束' : `立即购买 (${selected.length})` }}
          </el-button>
        </div>
        <SeatSelection 
          :layout="layout" 
          :selected-ids="selected" 
          @click-seat="onSeatClick" 
        />
      </el-card>
    </div>
    <div v-else-if="!loading" class="empty-state">
      <el-empty description="未找到演出信息">
        <el-button type="primary" @click="$router.push('/events')">返回列表</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import SeatSelection from '../components/SeatSelection.vue'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()
const event = ref(null)
const layout = ref(null)
const selected = ref([]) 
const buying = ref(false)
const loading = ref(false)

const isEnded = computed(() => {
  if (!event.value || !event.value.showTime) return false
  const showTime = new Date(event.value.showTime).getTime()
  const now = new Date().getTime()
  return now > showTime
})

const goProfile = (userId, role) => {
  if (role === 2 || role === 3) return // Don't navigate for official accounts
  router.push({ name: 'UserPublicProfile', params: { id: userId } })
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await request.get(`/events/${route.params.id}`)
    if (res.code === 200 && res.data) {
      event.value = res.data
      layout.value = res.data.seatLayoutJson
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载演出失败')
  } finally {
    loading.value = false
  }
}

const onSeatClick = async (seatId) => {
  if (isEnded.value) {
      ElMessage.warning('演出已结束，无法选座')
      return
  }
  if (selected.value.includes(seatId)) {
    // Optional: Unlock? Backend doesn't support unlock yet.
    // Just show message
    ElMessage.info('座位已锁定，请尽快支付')
    return
  }
  
  try {
    const res = await request.post('/ticket/lock', {
      eventId: event.value.eventId,
      seatId: seatId
    })
    
    if (res.code === 200) {
      selected.value.push(seatId)
      ElMessage.success('座位锁定成功 (15分钟有效)')
    } else {
      ElMessage.error(res.message || '锁定失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '锁定请求失败')
  }
}

const buyNow = async () => {
  if (selected.value.length === 0) return
  
  buying.value = true
  try {
    // Process all locked seats
    const successIds = []
    const failIds = []
    
    for (const seatId of selected.value) {
      try {
        const res = await request.post('/ticket/order', {
          eventId: event.value.eventId,
          seatInfo: seatId,
          price: event.value.ticketPrice
        })
        if (res.code === 200) {
          successIds.push(seatId)
        } else {
          failIds.push(seatId)
        }
      } catch (e) {
        failIds.push(seatId)
      }
    }
    
    if (failIds.length > 0) {
      ElMessage.warning(`购买部分成功。成功: ${successIds.length}, 失败: ${failIds.length}`)
    } else {
      ElMessage.success('购票成功！')
      router.push('/profile') // Redirect to profile page (which has tickets tab)
    }
    
    // Refresh to update layout (sold status)
    fetchDetail()
    selected.value = [] // Clear selection
    
  } catch (e) {
    ElMessage.error('购票流程异常')
  } finally {
    buying.value = false
  }
}

onMounted(fetchDetail)
</script>

<style scoped>
.page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}
.publisher-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  cursor: pointer;
  width: fit-content;
  padding: 4px 8px 4px 0;
  border-radius: 4px;
}
.publisher-info:hover {
  background: #f5f7fa;
}
.publisher-info .name {
  font-weight: bold;
  font-size: 14px;
  color: #303133;
}
.publisher-info-disabled {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  width: fit-content;
  padding: 4px 8px 4px 0;
  border-radius: 4px;
  opacity: 0.6;
  cursor: not-allowed;
}
.publisher-info-disabled .name {
  font-weight: bold;
  font-size: 14px;
  color: #909399;
}
.meta {
  color: #666;
  margin-bottom: 8px;
}
.price {
  font-weight: 600;
  margin-bottom: 24px;
  font-size: 18px;
  color: #f56c6c;
}
.seat-card {
  margin-top: 24px;
  margin-bottom: 40px;
}
.seat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.desc-card {
  margin-top: 24px;
  margin-bottom: 24px;
  line-height: 1.6;
}
.desc-card h3 {
  margin-bottom: 16px;
  border-left: 4px solid #409EFF;
  padding-left: 12px;
}
</style>
