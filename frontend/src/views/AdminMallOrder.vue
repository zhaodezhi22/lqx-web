<template>
  <div class="page">
    <el-card>
      <div class="header">
        <h2>订单管理</h2>
        <div class="filter">
           <el-radio-group v-model="status" @change="fetchList">
             <el-radio-button :label="null">全部</el-radio-button>
             <el-radio-button :label="1">待发货</el-radio-button>
             <el-radio-button :label="2">已发货</el-radio-button>
             <el-radio-button :label="4">退款待审</el-radio-button>
             <el-radio-button :label="3">已取消</el-radio-button>
           </el-radio-group>
        </div>
      </div>
      
      <el-table :data="list" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="addressSnapshot" label="收货地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="totalAmount" label="金额" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
             <el-tag v-if="row.status === 0" type="info">待支付</el-tag>
             <el-tag v-else-if="row.status === 1" type="primary">已支付</el-tag>
             <el-tag v-else-if="row.status === 2" type="success">已发货</el-tag>
             <el-tag v-else-if="row.status === 6" type="success">已完成</el-tag>
             <el-tag v-else-if="row.status === 3" type="danger">已取消</el-tag>
             <el-tag v-else-if="row.status === 4" type="warning">退款申请</el-tag>
             <el-tag v-else-if="row.status === 5" type="info">已退款</el-tag>
             <el-tag v-else type="info">未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="物流追踪" min-width="200">
            <template #default="{ row }">
                <div v-if="row.deliveryCompany || row.deliveryNo">
                    <div>{{ row.deliveryCompany }}</div>
                    <small>{{ row.deliveryNo }}</small>
                </div>
                <span v-else>-</span>
            </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" type="primary" size="small" @click="openShip(row)">去发货</el-button>
            <el-button v-if="row.status === 2" type="warning" size="small" @click="openShip(row)">修改物流</el-button>
            <el-button v-if="row.status === 4" type="success" size="small" @click="confirmRefund(row)">同意退款</el-button>
            <!-- 预留驳回功能 -->
            <!-- <el-button v-if="row.status === 4" type="danger" size="small" @click="auditRefund(row, false)">驳回</el-button> -->
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="shipDialogVisible" :title="dialogTitle" width="400px">
      <el-form :model="shipForm" label-width="100px">
        <el-form-item label="物流公司">
            <el-select v-model="shipForm.deliveryCompany" placeholder="请选择物流公司" style="width: 100%">
                <el-option label="顺丰速运" value="顺丰速运" />
                <el-option label="圆通速递" value="圆通速递" />
                <el-option label="中通快递" value="中通快递" />
                <el-option label="韵达快递" value="韵达快递" />
                <el-option label="EMS" value="EMS" />
                <el-option label="其他" value="其他" />
            </el-select>
        </el-form-item>
        <el-form-item label="物流单号">
          <el-input v-model="shipForm.deliveryNo" placeholder="输入物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleShip">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const list = ref([])
const loading = ref(false)
const status = ref(null)

const shipDialogVisible = ref(false)
const dialogTitle = ref('发货操作')
const shipForm = reactive({
  orderId: null,
  deliveryCompany: '',
  deliveryNo: ''
})

const fetchList = async () => {
  loading.value = true
  try {
    const params = {}
    if (status.value !== null) params.status = status.value
    const res = await request.get('/mall/orders', { params })
    list.value = res.data || []
  } catch (e) {
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

const openShip = (row) => {
  shipForm.orderId = row.id
  if (row.status === 2) {
      dialogTitle.value = '修改物流'
      shipForm.deliveryCompany = row.deliveryCompany || ''
      shipForm.deliveryNo = row.deliveryNo || ''
  } else {
      dialogTitle.value = '发货操作'
      shipForm.deliveryCompany = ''
      shipForm.deliveryNo = ''
  }
  shipDialogVisible.value = true
}

const handleShip = async () => {
  if (!shipForm.deliveryCompany) {
      ElMessage.warning('请选择物流公司')
      return
  }
  if (!shipForm.deliveryNo) {
    ElMessage.warning('请输入物流单号')
    return
  }
  try {
    // 使用 URLSearchParams 传递 query parameters，或者修改后端接收 JSON
    // 后端 Controller 定义为 @RequestParam，所以使用 params 或 form data 格式
    const formData = new FormData()
    formData.append('deliveryCompany', shipForm.deliveryCompany)
    formData.append('deliveryNo', shipForm.deliveryNo)
    
    await request.post(`/mall/ship/${shipForm.orderId}`, formData)
    
    ElMessage.success(dialogTitle.value === '修改物流' ? '修改成功' : '发货成功')
    shipDialogVisible.value = false
    fetchList()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '发货失败')
  }
}

const confirmRefund = (row) => {
  ElMessageBox.confirm(
    `确定同意退款吗？此操作将取消订单并回滚库存。`,
    '退款确认',
    {
      confirmButtonText: '同意退款',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await request.post(`/mall/refund/confirm/${row.id}`)
      ElMessage.success('退款处理成功，订单已取消')
      fetchList()
    } catch (e) {
      ElMessage.error(e?.response?.data?.message || '操作失败')
    }
  })
}

onMounted(fetchList)
</script>

<style scoped>
.page { padding: 16px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.filter { display: flex; gap: 10px; }
</style>