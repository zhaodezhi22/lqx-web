<template>
  <div class="page">
    <div v-if="loading" class="detail-skeleton">
      <el-button disabled style="margin-bottom: 20px">返回</el-button>
      <el-skeleton animated>
        <template #template>
          <el-skeleton-item variant="h1" style="width: 36%; height: 32px;" />
          <div style="margin: 16px 0 12px; display: flex; align-items: center; gap: 10px;">
            <el-skeleton-item variant="circle" style="width: 32px; height: 32px;" />
            <el-skeleton-item variant="text" style="width: 140px;" />
          </div>
          <el-skeleton-item variant="text" style="width: 50%; margin-bottom: 10px;" />
          <el-skeleton-item variant="text" style="width: 20%; margin-bottom: 20px;" />
          <div class="seat-skeleton"></div>
        </template>
      </el-skeleton>
    </div>
    <div v-else-if="event">
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
          <div>
            <div>请点击座位锁定（黄色为已锁定，红色为已售）</div>
            <div class="seat-tip">锁票 30 秒内有效，超时会自动释放</div>
          </div>
          <el-button 
            type="primary" 
            size="large" 
            :disabled="selectedIds.length === 0 || isEnded" 
            :loading="paymentLoading" 
            @click="buyNow"
          >
            {{ isEnded ? '已结束' : `立即支付 (${selectedIds.length})` }}
          </el-button>
        </div>
        <div v-if="selectedOrders.length" class="locked-summary">
          已锁定座位：{{ selectedOrders.map(item => item.seatInfo).join('、') }}
        </div>
        <div v-if="selectedOrders.length" class="countdown-box">
          待支付剩余：{{ eventRemainingLabel }}
        </div>
        <SeatSelection 
          :layout="layout" 
          :selected-ids="selectedIds" 
          @click-seat="onSeatClick" 
        />
      </el-card>
    </div>
    <div v-else class="empty-state">
      <el-empty description="未找到演出信息">
        <el-button type="primary" @click="$router.push('/events')">返回列表</el-button>
      </el-empty>
    </div>

    <el-dialog v-model="paymentVisible" title="支付订单" width="500px">
      <div v-if="selectedOrders.length">
        <p>演出名称：{{ event?.title }}</p>
        <p>座位：{{ selectedOrders.map(item => item.seatInfo).join('、') }}</p>
        <p>订单数量：{{ selectedOrders.length }}</p>
        <p>剩余支付时间：{{ eventRemainingLabel }}</p>
        <p>原价合计：¥ {{ totalAmount }}</p>
        <div class="points-box">
          <div v-if="userPoints >= 1000">
            <el-checkbox v-model="usePoints">
              使用积分抵扣（可用 {{ userPoints }}，最多抵扣 ¥{{ deductionPreview }}）
            </el-checkbox>
          </div>
          <div v-else class="points-muted">
            当前积分 {{ userPoints }}，满 1000 积分可抵扣
          </div>
        </div>
        <div class="pay-summary">
          <div>积分抵扣：-¥ {{ usePoints ? deductionAmount : '0.00' }}</div>
          <div class="pay-amount">应付金额：¥ {{ finalPayAmount }}</div>
        </div>
        <el-alert
          title="此处为模拟支付流程，点击确认支付后将直接完成订单支付。"
          type="info"
          :closable="false"
          show-icon
        />
      </div>
      <template #footer>
        <el-button @click="paymentVisible = false">取消</el-button>
        <el-button type="primary" :loading="paymentLoading" @click="payOrders">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import SeatSelection from '../components/SeatSelection.vue'
import request from '../utils/request'
import { getCurrentUser } from '../utils/permission'

const route = useRoute()
const router = useRouter()
const event = ref(null)
const layout = ref(null)
const selectedOrders = ref([])
const loading = ref(false)
const paymentVisible = ref(false)
const paymentLoading = ref(false)
const userPoints = ref(0)
const usePoints = ref(false)
const autoOpenedPayDialog = ref(false)
const nowTs = ref(Date.now())
let refreshTimer = null
let countdownTimer = null
let refreshing = false
const eventId = computed(() => {
  const parsed = Number(route.params.id)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null
})

const selectedIds = computed(() => selectedOrders.value.map(item => item.seatInfo))

const totalAmountValue = computed(() => selectedOrders.value.reduce((sum, item) => {
  return sum + Number(item.price || 0)
}, 0))

const totalAmount = computed(() => totalAmountValue.value.toFixed(2))

const maxDeductiblePoints = computed(() => {
  if (userPoints.value < 1000) return 0
  const maxByPrice = Math.floor(totalAmountValue.value * 1000)
  return Math.min(userPoints.value, maxByPrice)
})

const deductionAmount = computed(() => (maxDeductiblePoints.value / 1000).toFixed(2))
const deductionPreview = computed(() => (maxDeductiblePoints.value / 1000).toFixed(2))
const finalPayAmount = computed(() => {
  const amount = totalAmountValue.value - (usePoints.value ? Number(deductionAmount.value) : 0)
  return amount.toFixed(2)
})

const remainingSeconds = computed(() => {
  if (!selectedOrders.value.length) return 0
  const minExpireTs = Math.min(...selectedOrders.value.map(item => getExpireTimestamp(item)))
  return Math.max(0, Math.ceil((minExpireTs - nowTs.value) / 1000))
})

const eventRemainingLabel = computed(() => formatCountdown(remainingSeconds.value))

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

const getCreatedTimestamp = (order) => {
  if (!order?.createdTime) return 0
  const normalized = String(order.createdTime).replace(' ', 'T')
  const timestamp = new Date(normalized).getTime()
  return Number.isNaN(timestamp) ? 0 : timestamp
}

const getExpireTimestamp = (order) => getCreatedTimestamp(order) + 30 * 1000

const formatCountdown = (seconds) => {
  if (seconds <= 0) return '已超时'
  return `${String(Math.floor(seconds / 60)).padStart(2, '0')}:${String(seconds % 60).padStart(2, '0')}`
}

const fetchDetail = async () => {
  if (!eventId.value) {
    event.value = null
    layout.value = null
    return
  }
  try {
    const res = await request.get(`/events/${eventId.value}`)
    if (res.code === 200 && res.data) {
      event.value = res.data
      layout.value = res.data.seatLayoutJson
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载演出失败')
  }
}

const fetchUserPoints = async () => {
  const currentUser = getCurrentUser()
  if (!currentUser) {
    userPoints.value = 0
    return
  }
  try {
    if (!currentUser?.userId) {
      userPoints.value = 0
      return
    }
    const res = await request.get('/points/info', { params: { userId: currentUser.userId } })
    if (res.code === 200 && res.data) {
      userPoints.value = res.data.currentPoints || 0
    }
  } catch (e) {
    userPoints.value = 0
  }
}

const fetchPendingOrders = async () => {
  const token = localStorage.getItem('token')
  if (!token || !eventId.value) {
    selectedOrders.value = []
    return
  }
  try {
    const res = await request.get('/ticket/my-tickets')
    if (res.code === 200) {
      selectedOrders.value = (res.data || []).filter(item => item.status === 0 && item.eventId === eventId.value)
      const payOrderId = Number(route.query.payOrderId)
      if (!autoOpenedPayDialog.value && payOrderId) {
        const targetOrder = selectedOrders.value.find(item => item.orderId === payOrderId)
        if (targetOrder) {
          await buyNow()
          autoOpenedPayDialog.value = true
        }
      }
    }
  } catch (e) {
    selectedOrders.value = []
  }
}

const refreshOrderState = async ({ showLoading = false } = {}) => {
  if (refreshing) return
  refreshing = true
  if (showLoading) loading.value = true
  try {
    await Promise.all([fetchDetail(), fetchPendingOrders()])
  } finally {
    if (showLoading) loading.value = false
    refreshing = false
  }
}

const onSeatClick = async (seat) => {
  if (isEnded.value) {
    ElMessage.warning('演出已结束，无法选座')
    return
  }
  const seatId = typeof seat === 'string' ? seat : seat?.id
  if (!seatId) return
  if (seat?.status === 1) {
    ElMessage.warning('该座位已售出，请选择其他座位')
    return
  }
  if (seat?.status === 2) {
    ElMessage.warning('该座位已被锁定，请选择其他座位')
    return
  }
  if (selectedIds.value.includes(seatId)) {
    ElMessage.warning('该座位已被锁定，请尽快支付')
    return
  }
  
  try {
    const res = await request.post('/ticket/lock', {
      eventId: event.value.eventId,
      seatId: seatId
    })
    
    if (res.code === 200 && res.data) {
      selectedOrders.value.push(res.data)
      ElMessage.success('座位锁定成功（30 秒内有效）')
    } else {
      ElMessage.error(res.message || '锁定失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '锁定请求失败')
  }
}

const buyNow = async () => {
  if (selectedOrders.value.length === 0) {
    ElMessage.warning('请先锁定座位')
    return
  }
  usePoints.value = false
  await fetchUserPoints()
  paymentVisible.value = true
}

const payOrders = async () => {
  if (!selectedOrders.value.length) {
    ElMessage.warning('没有可支付的订单')
    paymentVisible.value = false
    return
  }

  paymentLoading.value = true
  try {
    const res = await request.post('/ticket/pay', {
      orderIds: selectedOrders.value.map(item => item.orderId),
      usedPoints: usePoints.value ? maxDeductiblePoints.value : 0
    })
    if (res.code === 200) {
      ElMessage.success('支付成功，门票已加入我的票务')
      paymentVisible.value = false
      await fetchUserPoints()
      await refreshOrderState()
      router.replace({ path: route.path })
    } else {
      ElMessage.error(res.message || '支付失败')
      await refreshOrderState()
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '支付失败')
    await refreshOrderState()
  } finally {
    paymentLoading.value = false
  }
}

onMounted(async () => {
  await refreshOrderState({ showLoading: true })
  if (eventId.value) {
    refreshTimer = window.setInterval(() => {
      refreshOrderState()
    }, 5000)
    countdownTimer = window.setInterval(async () => {
      nowTs.value = Date.now()
      if (selectedOrders.value.length && remainingSeconds.value <= 0) {
        paymentVisible.value = false
        await refreshOrderState()
      }
    }, 1000)
  }
})

onUnmounted(() => {
  if (refreshTimer) {
    window.clearInterval(refreshTimer)
    refreshTimer = null
  }
  if (countdownTimer) {
    window.clearInterval(countdownTimer)
    countdownTimer = null
  }
})
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
  gap: 16px;
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
.seat-tip {
  margin-top: 6px;
  color: #909399;
  font-size: 13px;
}
.locked-summary {
  margin-bottom: 16px;
  color: #606266;
}
.countdown-box {
  margin-bottom: 16px;
  color: #e6a23c;
  font-weight: 600;
}
.points-box {
  margin: 16px 0;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}
.points-muted {
  color: #909399;
  font-size: 13px;
}
.pay-summary {
  margin-bottom: 16px;
  line-height: 1.9;
}
.pay-amount {
  color: #f56c6c;
  font-size: 18px;
  font-weight: 600;
}
.seat-skeleton {
  width: 100%;
  min-height: 360px;
  border-radius: 12px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ebeef5 100%);
}
</style>
