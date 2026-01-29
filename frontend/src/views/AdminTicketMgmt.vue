<template>
  <div class="page">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="演出管理" name="events">
        <el-card>
          <div class="header">
            <h2>演出与票务管理</h2>
            <el-button type="primary" @click="showAddDialog">新增演出</el-button>
          </div>
          
          <el-table :data="list" style="width: 100%" v-loading="loading">
            <el-table-column prop="eventId" label="ID" width="80" />
            <el-table-column prop="title" label="演出名称" />
            <el-table-column prop="venue" label="场馆" width="150" />
            <el-table-column prop="showTime" label="演出时间" width="180" />
            <el-table-column prop="ticketPrice" label="票价" width="100" />
            <el-table-column prop="status" label="状态" width="100">
               <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : (row.status === 3 ? 'warning' : 'info')">
                  {{ row.status === 1 ? '售票中' : (row.status === 3 ? '已下架' : '已结束') }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="250">
              <template #default="{ row }">
                <el-button link type="primary" @click="showEditDialog(row)">编辑</el-button>
                <el-button link type="primary" @click="$router.push(`/admin/event/${row.eventId}/seats`)">座位图</el-button>
                <el-button link type="warning" v-if="row.status === 1" @click="handleOffline(row)">下架</el-button>
                <el-button link type="info" @click="openCheckIn">核销检票</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="退票审核" name="audit">
        <el-card>
          <div class="header">
            <h2>退票申请列表</h2>
            <el-radio-group v-model="auditStatus" @change="fetchAuditList" size="small">
              <el-radio-button :label="3">待审核</el-radio-button>
              <el-radio-button :label="4">已退票</el-radio-button>
              <el-radio-button :label="1">已拒绝</el-radio-button>
            </el-radio-group>
          </div>
          <el-table :data="auditList" v-loading="auditLoading" style="width: 100%">
            <el-table-column prop="orderId" label="订单ID" width="80" />
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="eventTitle" label="演出名称" />
            <el-table-column prop="userId" label="用户ID" width="100" />
            <el-table-column prop="price" label="金额" width="100" />
            <el-table-column prop="seatInfo" label="座位" />
            <el-table-column prop="createdTime" label="购票时间" width="180" />
            <el-table-column label="状态" width="100">
               <template #default="{ row }">
                 <el-tag v-if="row.status === 3" type="warning">待审核</el-tag>
                 <el-tag v-else-if="row.status === 4" type="info">已退票</el-tag>
                 <el-tag v-else type="danger">已拒绝/正常</el-tag>
               </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button v-if="row.status === 3" type="success" size="small" @click="auditRefund(row, true)">通过</el-button>
                <el-button v-if="row.status === 3" type="danger" size="small" @click="auditRefund(row, false)">驳回</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" title="演出信息">
      <el-form :model="form" label-width="100px">
        <el-form-item label="演出名称">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="场馆">
          <el-input v-model="form.venue" />
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker v-model="form.showTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="票价">
          <el-input v-model="form.ticketPrice" type="number" />
        </el-form-item>
        <el-form-item label="座位布局">
           <el-button type="primary" plain @click="openSeatEditor">
             {{ form.seatLayoutJson && form.seatLayoutJson.length > 5 ? '修改座位布局' : '去设置座位布局' }}
           </el-button>
           <span style="margin-left: 10px; color: #666">
             当前座位数: {{ form.totalSeats }}
           </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <!-- Seat Editor Dialog -->
    <el-dialog v-model="seatDialogVisible" title="座位布局设置" width="80%" append-to-body>
        <SeatEditor v-model="currentSeatLayout" />
        <template #footer>
            <el-button @click="seatDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmSeatLayout">确认保存</el-button>
        </template>
    </el-dialog>
    <!-- Check-in Dialog -->
    <el-dialog v-model="checkInDialogVisible" title="门票核销" width="400px">
        <div style="text-align: center; margin-bottom: 20px;">
            <p>请输入8位数字核销码或完整订单号</p>
        </div>
        <el-input v-model="checkInCode" placeholder="请输入核销码/订单号" size="large" clearable @keyup.enter="handleCheckIn">
             <template #prefix>
                <el-icon><Ticket /></el-icon>
             </template>
        </el-input>
        <template #footer>
            <el-button @click="checkInDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleCheckIn" :loading="checkInLoading">确认核销</el-button>
        </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Ticket } from '@element-plus/icons-vue'
import request from '../utils/request'
import SeatEditor from '../components/SeatEditor.vue'

const checkInDialogVisible = ref(false)
const checkInCode = ref('')
const checkInLoading = ref(false)

const openCheckIn = () => {
    checkInCode.value = ''
    checkInDialogVisible.value = true
}

const handleCheckIn = async () => {
    if (!checkInCode.value) {
        ElMessage.warning('请输入核销码')
        return
    }
    checkInLoading.value = true
    try {
        await request.post('/ticket/verify', { orderNo: checkInCode.value })
        ElMessage.success('核销成功！用户积分已到账')
        checkInCode.value = ''
        checkInDialogVisible.value = false
        // Refresh list if needed, but not strictly necessary as this is global check-in
    } catch (e) {
        ElMessage.error(e.response?.data?.message || '核销失败，请检查核销码')
    } finally {
        checkInLoading.value = false
    }
}

const activeTab = ref('events')
const list = ref([])
const loading = ref(false)

const auditList = ref([])
const auditLoading = ref(false)
const auditStatus = ref(3)

const dialogVisible = ref(false)
const form = reactive({
  eventId: null,
  title: '',
  venue: '',
  showTime: '',
  ticketPrice: '',
  totalSeats: 200, // Default seats
  status: 1,
  seatLayoutJson: '[]'
})

const seatDialogVisible = ref(false)
const currentSeatLayout = ref([])

const openSeatEditor = async () => {
  console.log('Opening seat editor, json:', form.seatLayoutJson)
  if (form.seatLayoutJson && typeof form.seatLayoutJson === 'string' && form.seatLayoutJson.length > 5) {
    try {
      currentSeatLayout.value = JSON.parse(form.seatLayoutJson)
    } catch (e) {
      console.error('JSON parse error', e)
      currentSeatLayout.value = []
    }
  } else {
    currentSeatLayout.value = []
  }
  await nextTick()
  seatDialogVisible.value = true
}

const confirmSeatLayout = () => {
  form.seatLayoutJson = JSON.stringify(currentSeatLayout.value)
  form.totalSeats = currentSeatLayout.value.length
  seatDialogVisible.value = false
  ElMessage.success('座位布局已保存')
}

const handleOffline = async (row) => {
  try {
    await ElMessageBox.confirm('确定要强制下架该活动吗？下架后用户将无法购买。', '警告', {
      type: 'warning',
      confirmButtonText: '确定下架',
      cancelButtonText: '取消'
    })
    
    await request.post(`/admin/event/offline/${row.eventId}`)
    ElMessage.success('活动已下架')
    fetchList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || '操作失败')
    }
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/events', { params: { all: true } })
    if (res.code === 200) {
      list.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const fetchAuditList = async () => {
  auditLoading.value = true
  try {
    const res = await request.get('/ticket/orders', { params: { status: auditStatus.value } })
    if (res.code === 200) {
      auditList.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('加载退票申请失败')
  } finally {
    auditLoading.value = false
  }
}

const auditRefund = (row, pass) => {
  ElMessageBox.confirm(
    `确定要${pass ? '通过' : '驳回'}退票申请吗？`,
    '审核确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: pass ? 'warning' : 'info',
    }
  ).then(async () => {
    try {
      await request.post(`/ticket/refund/audit/${row.orderId}?pass=${pass}`)
      ElMessage.success('操作成功')
      fetchAuditList()
    } catch (e) {
      ElMessage.error('操作失败')
    }
  })
}

watch(activeTab, (val) => {
  if (val === 'events') {
    fetchList()
  } else {
    fetchAuditList()
  }
})

const getStatusType = (status) => {
  if (status === 1) return 'success'
  if (status === 0) return 'warning'
  return 'info'
}

const getStatusLabel = (status) => {
  if (status === 1) return '售票中'
  if (status === 0) return '未开始'
  return '已结束'
}

const showAddDialog = () => {
  form.eventId = null
  form.title = ''
  form.venue = ''
  form.showTime = ''
  form.ticketPrice = ''
  form.totalSeats = 0
  form.status = 1
  form.seatLayoutJson = '[]'
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  Object.assign(form, row)
  if (!form.seatLayoutJson) form.seatLayoutJson = '[]'
  dialogVisible.value = true
}

const submit = async () => {
  if (!form.title || !form.showTime) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  try {
    if (form.eventId) {
      await request.put('/events', form)
    } else {
      await request.post('/events', form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

onMounted(fetchList)
</script>

<style scoped>
.page { padding: 16px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
</style>
