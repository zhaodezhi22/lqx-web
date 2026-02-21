<template>
  <div class="page">
    <el-card>
      <div class="header">
        <h2>商城管理</h2>
        <el-button type="primary" @click="showAddDialog">新增商品</el-button>
      </div>
      
      <el-table :data="list" style="width: 100%" v-loading="loading">
        <el-table-column prop="productId" label="ID" width="80" />
        <el-table-column prop="name" label="商品名称" />
        <el-table-column label="图片" width="100">
          <template #default="{ row }">
            <img :src="row.mainImage" style="width: 50px; height: 50px; object-fit: cover" />
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100" />
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column prop="sales" label="销量" width="100" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary">编辑</el-button>
            <el-button link type="danger">下架</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <div style="margin-top: 20px">
      <el-card>
        <h2>订单发货管理</h2>
        <el-tabs v-model="activeTab" @tab-change="onTabChange">
           <el-tab-pane label="待发货" name="pending">
             <el-table :data="pendingOrders" v-loading="ordersLoading" style="width: 100%">
               <el-table-column prop="orderId" label="订单ID" width="90" />
               <el-table-column prop="orderNo" label="订单编号" />
               <el-table-column prop="userId" label="用户ID" width="90" />
               <el-table-column prop="totalAmount" label="总金额" width="120" />
               <el-table-column prop="payTime" label="支付时间" width="180" />
               <el-table-column label="操作" width="220">
                 <template #default="{ row }">
                   <el-button link type="primary" @click="viewItems(row)">查看商品</el-button>
                   <el-button link type="success" @click="openShip(row)">发货</el-button>
                 </template>
               </el-table-column>
             </el-table>
           </el-tab-pane>
           <el-tab-pane label="已发货" name="shipped">
             <el-table :data="shippedOrders" v-loading="ordersLoading" style="width: 100%">
               <el-table-column prop="orderId" label="订单ID" width="90" />
               <el-table-column prop="orderNo" label="订单编号" />
               <el-table-column prop="userId" label="用户ID" width="90" />
               <el-table-column prop="totalAmount" label="总金额" width="120" />
               <el-table-column prop="shipTime" label="发货时间" width="180" />
               <el-table-column prop="logisticsNo" label="物流单号" />
               <el-table-column label="操作" width="140">
                 <template #default="{ row }">
                   <el-button link type="primary" @click="viewItems(row)">查看商品</el-button>
                 </template>
               </el-table-column>
             </el-table>
           </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>

    <el-dialog v-model="dialogVisible" title="商品信息">
      <el-form :model="form" label-width="100px">
        <el-form-item label="商品名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input v-model="form.price" type="number" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input v-model="form.stock" type="number" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>

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

    <el-drawer v-model="itemsDrawer" title="订单商品" size="400px">
      <el-table :data="orderItems" style="width: 100%">
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="price" label="单价" width="100" />
        <el-table-column prop="quantity" label="数量" width="80" />
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = reactive({
  name: '',
  price: '',
  stock: 100
})

const activeTab = ref('pending')
const ordersLoading = ref(false)
const pendingOrders = ref([])
const shippedOrders = ref([])
const shipDialogVisible = ref(false)
const shipForm = reactive({ orderId: null, logisticsNo: '' })
const itemsDrawer = ref(false)
const orderItems = ref([])

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/products')
    list.value = res.data || []
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const fetchOrders = async () => {
  ordersLoading.value = true
  try {
    const resPending = await request.get('/mall/orders', { params: { status: 1 } })
    const resShipped = await request.get('/mall/orders', { params: { status: 2 } })
    pendingOrders.value = resPending.data || []
    shippedOrders.value = resShipped.data || []
  } catch (e) {
    ElMessage.error('加载订单失败')
  } finally {
    ordersLoading.value = false
  }
}

const showAddDialog = () => {
  dialogVisible.value = true
}

const submit = () => {
  ElMessage.success('保存成功 (Mock)')
  dialogVisible.value = false
  fetchList() // Reload
}

const onTabChange = () => {}

const openShip = (row) => {
  shipForm.orderId = row.orderId
  shipForm.logisticsNo = ''
  shipDialogVisible.value = true
}

const confirmShip = async () => {
  try {
    await request.put(`/mall/orders/${shipForm.orderId}/ship`, { logisticsNo: shipForm.logisticsNo })
    ElMessage.success('已发货')
    shipDialogVisible.value = false
    fetchOrders()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '发货失败')
  }
}

const viewItems = async (row) => {
  try {
    const res = await request.get(`/mall/orders/${row.orderId}/items`)
    orderItems.value = res.data || []
    itemsDrawer.value = true
  } catch {
    ElMessage.error('加载订单商品失败')
  }
}

onMounted(() => {
  fetchList()
  fetchOrders()
})
</script>

<style scoped>
.page { padding: 16px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
</style>
