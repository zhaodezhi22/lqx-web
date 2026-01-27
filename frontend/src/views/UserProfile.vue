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
              <el-table :data="orders" v-loading="loading" style="width: 100%" class="custom-table">
                <el-table-column label="订单信息" width="220">
                  <template #default="{ row }">
                    <div class="order-info-cell">
                      <div class="order-no">{{ row.orderNo }}</div>
                      <div class="create-time">{{ row.createTime }}</div>
                    </div>
                  </template>
                </el-table-column>
                
                <el-table-column label="商品" min-width="280">
                  <template #default="{ row }">
                    <div v-for="item in row.items" :key="item.id" class="product-item-flex" @click="openDetail(item.productId)">
                      <el-image :src="item.productImage" class="product-thumb" fit="cover">
                         <template #error>
                           <div class="image-slot"><el-icon><Picture /></el-icon></div>
                         </template>
                      </el-image>
                      <div class="product-meta">
                        <div class="product-name">{{ item.productName }}</div>
                        <div class="product-qty">x {{ item.quantity }}</div>
                      </div>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column prop="totalAmount" label="实付" width="100">
                   <template #default="{ row }">
                     <span class="price-text">¥{{ row.totalAmount }}</span>
                   </template>
                </el-table-column>

                <el-table-column label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="statusTagType(row.status)" size="small" effect="light" round>{{ statusText(row.status) }}</el-tag>
                  </template>
                </el-table-column>

                <el-table-column label="操作" width="100" fixed="right">
                  <template #default="{ row }">
                    <el-dropdown trigger="click">
                      <span class="el-dropdown-link">
                        更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
                      </span>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item v-if="row.status === 1" @click="handleMallRefund(row)">
                            <span class="text-gray">申请退款</span>
                          </el-dropdown-item>
                           <el-dropdown-item v-if="row.status >= 2">
                            <span class="text-gray">查看物流</span>
                          </el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
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
                    <el-tag type="warning" v-else-if="row.status === 3">退款审核中</el-tag>
                    <el-tag type="info" v-else-if="row.status === 4">已退票</el-tag>
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
            <el-tab-pane label="我的师门" name="apprenticeship">
              <div class="apprenticeship-container">
                <el-table :data="assignments" v-loading="assignmentsLoading" style="width: 100%">
                  <el-table-column prop="taskTitle" label="作业题目" min-width="150" />
                  <el-table-column prop="masterName" label="授课师父" width="120" />
                  <el-table-column label="状态" width="100">
                    <template #default="{ row }">
                      <el-tag v-if="row.status === 0" type="info">待提交</el-tag>
                      <el-tag v-else-if="row.status === 1" type="primary">已提交</el-tag>
                      <el-tag v-else-if="row.status === 2" type="success">已点评</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="150">
                    <template #default="{ row }">
                      <el-button link type="primary" @click="openSubmit(row)">
                        {{ row.status === 0 ? '去提交' : '查看/修改' }}
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <div v-if="!assignmentsLoading && (!assignments || assignments.length === 0)" style="margin-top: 16px">
                   <el-empty description="暂无作业任务" />
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <!-- Homework Submit Dialog -->
    <el-dialog v-model="submitDialogVisible" :title="'提交作业: ' + submitForm.taskTitle" width="600px">
      <el-form :model="submitForm" label-width="100px">
        <el-form-item label="演示视频" v-if="submitForm.demoVideoUrl">
           <video :src="submitForm.demoVideoUrl" controls style="width: 100%; max-height: 200px; background: #000"></video>
           <div class="tip" style="font-size: 12px; color: #999">师父的演示视频</div>
        </el-form-item>
        <el-form-item label="作业说明">
           <div style="background: #f5f7fa; padding: 10px; border-radius: 4px; width: 100%">{{ submitForm.taskDescription }}</div>
        </el-form-item>
        
        <el-divider />

        <el-form-item label="作业心得">
          <el-input 
            v-model="submitForm.content" 
            type="textarea" 
            :rows="4" 
            placeholder="请填写作业心得或说明..." 
          />
        </el-form-item>
        <el-form-item label="作业视频URL">
          <el-input v-model="submitForm.videoUrl" placeholder="请输入视频链接 (如 OSS 地址)" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="submitDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="doSubmit">提交作业</el-button>
        </span>
      </template>
    </el-dialog>

    <ProductDetailModal v-model:visible="detailVisible" :product-id="currentProductId" />
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { User, Phone, ArrowDown, Picture } from '@element-plus/icons-vue'
import ProductDetailModal from '../components/ProductDetailModal.vue'

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

const detailVisible = ref(false)
const currentProductId = ref(null)

const orders = ref([])
const loading = ref(false)

const openDetail = (productId) => {
  currentProductId.value = productId
  detailVisible.value = true
}

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

const assignments = ref([])
const assignmentsLoading = ref(false)
const submitDialogVisible = ref(false)
const submitForm = reactive({
  assignmentId: null,
  taskTitle: '',
  taskDescription: '',
  demoVideoUrl: '',
  content: '',
  videoUrl: ''
})

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

const fetchAssignments = async () => {
  assignmentsLoading.value = true
  try {
    const res = await request.get('/teaching/my-assignments')
    if (res.code === 200) {
      assignments.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('加载作业失败')
  } finally {
    assignmentsLoading.value = false
  }
}

const openSubmit = (row) => {
  submitForm.assignmentId = row.assignmentId
  submitForm.taskTitle = row.taskTitle
  submitForm.taskDescription = row.taskDescription
  submitForm.demoVideoUrl = row.demoVideoUrl
  submitForm.content = row.submissionContent || ''
  submitForm.videoUrl = row.submissionVideoUrl || ''
  submitDialogVisible.value = true
}

const doSubmit = async () => {
  if (!submitForm.content) {
      ElMessage.warning('请填写作业说明')
      return
  }
  try {
      const res = await request.post('/teaching/submit', {
          assignmentId: submitForm.assignmentId,
          content: submitForm.content,
          videoUrl: submitForm.videoUrl
      })
      if (res.code === 200) {
          ElMessage.success('提交成功')
          submitDialogVisible.value = false
          fetchAssignments()
      } else {
          ElMessage.error(res.message || '提交失败')
      }
  } catch (e) {
      ElMessage.error('提交失败')
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
  } else if (activeTab.value === 'apprenticeship') {
    fetchAssignments()
  }
})

watch(activeTab, (val) => {
  if (val === 'orders') {
    fetchMyOrders()
  } else if (val === 'tickets') {
    fetchTickets()
  } else if (val === 'apprenticeship') {
    fetchAssignments()
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
.order-info-cell {
  display: flex;
  flex-direction: column;
  line-height: 1.5;
}
.order-no {
  font-weight: 500;
  color: #303133;
  font-size: 13px;
}
.create-time {
  font-size: 12px;
  color: #909399;
}
.product-item-flex {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 0;
  cursor: pointer;
  transition: opacity 0.2s;
}
.product-item-flex:hover {
  opacity: 0.8;
}
.product-thumb {
  width: 48px;
  height: 48px;
  border-radius: 4px;
  background: #f5f7fa;
  flex-shrink: 0;
  border: 1px solid #ebeef5;
}
.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  color: #dcdfe6;
}
.product-meta {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.product-name {
  font-size: 13px;
  color: #303133;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.product-qty {
  font-size: 12px;
  color: #909399;
}
.price-text {
  font-weight: 600;
  color: #303133;
}
.el-dropdown-link {
  cursor: pointer;
  color: #409EFF;
  display: flex;
  align-items: center;
  font-size: 13px;
}
.text-gray {
  color: #606266;
}
</style>