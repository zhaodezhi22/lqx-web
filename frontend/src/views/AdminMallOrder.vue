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
             <el-radio-button :label="5">已退款</el-radio-button>
           </el-radio-group>
        </div>
      </div>
      
      <el-table :data="list" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="totalAmount" label="金额" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
             <el-tag v-if="row.status === 0" type="warning">待支付</el-tag>
             <el-tag v-else-if="row.status === 1" type="primary">待发货</el-tag>
             <el-tag v-else-if="row.status === 2" type="success">已发货</el-tag>
             <el-tag v-else-if="row.status === 3" type="info">已完成</el-tag>
             <el-tag v-else-if="row.status === 4" type="warning" effect="dark">退款审核中</el-tag>
             <el-tag v-else-if="row.status === 5" type="info" effect="dark">已退款</el-tag>
             <el-tag v-else type="danger">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="logisticsNo" label="物流单号" />
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" type="primary" size="small" @click="openShip(row)">发货</el-button>
            <el-button v-if="row.status === 4" type="success" size="small" @click="auditRefund(row, true)">通过</el-button>
            <el-button v-if="row.status === 4" type="danger" size="small" @click="auditRefund(row, false)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="shipDialogVisible" title="发货操作" width="400px">
      <el-form :model="shipForm" label-width="100px">
        <el-form-item label="物流单号">
          <el-input v-model="shipForm.logisticsNo" placeholder="输入物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmShip">确认发货</el-button>
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
const shipForm = reactive({
  orderId: null,
  logisticsNo: ''
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
  shipForm.logisticsNo = ''
  shipDialogVisible.value = true
}

const auditRefund = (row, pass) => {
  ElMessageBox.confirm(
    `确定要${pass ? '通过' : '驳回'}退款申请吗？`,
    '审核确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: pass ? 'warning' : 'info',
    }
  ).then(async () => {
    try {
      await request.post(`/mall/refund/audit/${row.id}?pass=${pass}`)
      ElMessage.success('操作成功')
      fetchList()
    } catch (e) {
      ElMessage.error('操作失败')
    }
  })
}

const confirmShip = async () => {
  if (!shipForm.logisticsNo) {
    ElMessage.warning('请输入物流单号')
    return
  }
  try {
    await request.put(`/mall/orders/${shipForm.orderId}/ship`, {
      logisticsNo: shipForm.logisticsNo
    })
    ElMessage.success('发货成功')
    shipDialogVisible.value = false
    fetchList()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '发货失败')
  }
}

onMounted(fetchList)
</script>

<style scoped>
.page { padding: 16px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
</style>
