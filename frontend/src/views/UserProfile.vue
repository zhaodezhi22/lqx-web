<template>
  <div class="page">
    <el-row :gutter="20">
      <el-col :span="6" class="profile-sidebar">
        <el-card class="user-card">
          <div class="avatar-section">
            <el-avatar :size="100" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
            <div class="role-badge" v-if="user.role === 1">
              <el-tag type="warning" effect="dark">非遗传承人</el-tag>
            </div>
            <div class="role-badge" v-else>
              <el-tag type="info">普通用户</el-tag>
            </div>
          </div>
          <h3 class="username">{{ user.username }}</h3>
          <div class="info-list">
            <p><el-icon><User /></el-icon> {{ user.realName || '未实名' }}</p>
            <p><el-icon><Phone /></el-icon> {{ user.phone || '无电话' }}</p>
          </div>
          <div class="actions">
             <el-button v-if="user.role === 0" type="primary" plain block @click="$router.push('/inheritor/apply')">
              申请成为传承人
            </el-button>
            <el-button v-if="user.role === 1" type="success" plain block @click="$router.push('/inheritor/center')">
              进入传承人工作台
            </el-button>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="18">
        <el-card class="content-card">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="我的订单" name="orders">
              <el-table :data="orders" v-loading="loading" style="width: 100%">
                <!-- 隐藏订单ID列 -->
                <!-- <el-table-column prop="orderId" label="订单ID" width="90" /> -->
                <el-table-column prop="orderNo" label="订单编号" />
                <el-table-column prop="createTime" label="下单时间" width="180" />
                <el-table-column label="商品信息" min-width="200">
                  <template #default="{ row }">
                    <div v-for="item in row.items" :key="item.id" class="order-item-link">
                      <el-link type="primary" @click="$router.push(`/mall/${item.productId}`)">
                        {{ item.productName }} x {{ item.quantity }}
                      </el-link>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="totalAmount" label="金额(¥)" width="120" />
                <el-table-column label="状态" width="120">
                  <template #default="{ row }">
                    <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <!-- <el-table-column prop="payTime" label="支付时间" width="180" /> -->
                <el-table-column prop="shipTime" label="发货时间" width="180" />
                <el-table-column prop="logisticsNo" label="物流单号" />
                <el-table-column label="操作" width="150" fixed="right">
                  <template #default="{ row }">
                    <el-button 
                      v-if="row.status === 1" 
                      link 
                      type="danger" 
                      @click="handleMallRefund(row)"
                    >
                      申请退款
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <div v-if="!loading && (!orders || orders.length === 0)" style="margin-top: 16px">
                <el-empty description="暂无订单" />
              </div>
            </el-tab-pane>
            <el-tab-pane label="我的票务" name="tickets">
              <el-table :data="tickets" v-loading="ticketsLoading" style="width: 100%">
                <el-table-column prop="eventTitle" label="演出名称" min-width="150" />
                <el-table-column prop="showTime" label="演出时间" width="170">
                   <template #default="{ row }">
                     {{ formatTime(row.showTime) }}
                   </template>
                </el-table-column>
                <el-table-column prop="seatInfo" label="座位" width="100" />
                <el-table-column prop="price" label="票价" width="80" />
                <el-table-column label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag type="success" v-if="row.status === 1">已支付</el-tag>
                    <el-tag type="warning" v-else-if="row.status === 0">待支付</el-tag>
                    <el-tag type="info" v-else-if="row.status === 3">已退票</el-tag>
                    <el-tag type="danger" v-else>已取消</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="220">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="$router.push(`/events/${row.eventId}`)">查看演出</el-button>
                    <el-button link type="danger" v-if="row.status === 1" @click="handleRefund(row)">退票</el-button>
                    <el-button link type="primary" v-if="row.status === 0">去支付</el-button>
                  </template>
                </el-table-column>
              </el-table>
               <div v-if="!ticketsLoading && (!tickets || tickets.length === 0)" style="margin-top: 16px">
                <el-empty description="暂无票务" />
              </div>
            </el-tab-pane>
            <el-tab-pane label="基本资料" name="profile">
              <el-form label-width="100px" style="max-width: 500px">
                <el-form-item label="昵称">
                  <el-input v-model="user.username" disabled />
                </el-form-item>
                <el-form-item label="真实姓名">
                  <el-input v-model="user.realName" />
                </el-form-item>
                <el-form-item label="联系电话">
                  <el-input v-model="user.phone" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveProfile">保存修改</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { User, Phone } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const activeTab = ref('orders')
const user = reactive({
  userId: null,
  username: '',
  role: 0,
  realName: '',
  phone: ''
})

const orders = ref([])
const loading = ref(false)

const statusText = (s) => {
  switch (s) {
    case 0: return '待支付'
    case 1: return '待发货'
    case 2: return '已发货'
    case 3: return '已完成'
    case 4: return '退款审核中'
    case 5: return '已退款'
    default: return '未知'
  }
}
const statusTagType = (s) => {
  switch (s) {
    case 0: return 'warning'
    case 1: return 'info'
    case 2: return 'primary'
    case 3: return 'success'
    case 4: return 'warning'
    case 5: return 'info'
    default: return ''
  }
}

const tickets = ref([])
const ticketsLoading = ref(false)

const formatTime = (arr) => {
  if (!arr) return ''
  if (Array.isArray(arr)) {
    const [y, m, d, h, min, s] = arr
    return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(h).padStart(2, '0')}:${String(min).padStart(2, '0')}`
  }
  return arr
}

const fetchMyOrders = async () => {
  loading.value = true
  try {
    const res = await request.get('/mall/my-orders')
    if (res.code === 200) {
      orders.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

const fetchTickets = async () => {
  ticketsLoading.value = true
  try {
    const res = await request.get('/ticket/my-tickets')
    if (res.code === 200) {
      tickets.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('加载票务失败')
  } finally {
    ticketsLoading.value = false
  }
}

const handleRefund = (row) => {
  ElMessageBox.confirm(
    '确定要申请退票吗？需要等待审核员审核。',
    '退票申请',
    {
      confirmButtonText: '提交申请',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        const res = await request.post(`/ticket/refund/${row.orderId}`)
        if (res.code === 200) {
          ElMessage.success('申请提交成功，请等待审核')
          fetchTickets()
        } else {
          ElMessage.error(res.message || '申请失败')
        }
      } catch (e) {
        ElMessage.error(e.response?.data?.message || '申请失败')
      }
    })
    .catch(() => {})
}

const handleMallRefund = (row) => {
  ElMessageBox.confirm(
    '确定要申请取消订单并退款吗？需要等待审核员审核。',
    '退款申请',
    {
      confirmButtonText: '提交申请',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        const res = await request.post(`/mall/refund/apply/${row.id}`)
        if (res.code === 200) {
          ElMessage.success('申请提交成功')
          fetchMyOrders()
        } else {
          ElMessage.error(res.message || '申请失败')
        }
      } catch (e) {
        ElMessage.error(e.response?.data?.message || '申请失败')
      }
    })
    .catch(() => {})
}

const saveProfile = async () => {
  if (!user.userId) {
    ElMessage.error('用户信息丢失，请重新登录')
    return
  }
  try {
    await request.put(`/users/${user.userId}`, {
      realName: user.realName,
      phone: user.phone
    })
    ElMessage.success('保存成功')
    // 更新本地缓存
    localStorage.setItem('user', JSON.stringify(user))
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    Object.assign(user, JSON.parse(userStr))
  }
  if (route.query.tab) {
    activeTab.value = route.query.tab
  }
  // Initial fetch based on active tab
  if (activeTab.value === 'orders') {
    fetchMyOrders()
  } else if (activeTab.value === 'tickets') {
    fetchTickets()
  }
})

watch(activeTab, (val) => {
  if (val === 'orders') {
    fetchMyOrders()
  } else if (val === 'tickets') {
    fetchTickets()
  }
})
</script>

<style scoped>
.page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.avatar-section {
  text-align: center;
  margin-bottom: 20px;
}
.username {
  text-align: center;
  margin-bottom: 20px;
}
.role-badge {
  margin-top: 10px;
}
.info-list p {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #606266;
  margin-bottom: 10px;
}
.actions {
  margin-top: 30px;
}
.user-card {
  height: 100%;
}
.content-card {
  min-height: 500px;
}
</style>