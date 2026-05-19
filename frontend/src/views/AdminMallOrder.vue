<template>
  <div class="page">
    <el-card class="card-block">
      <div class="header">
        <h2>商品管理</h2>
        <div class="actions">
          <el-button @click="fetchProducts">刷新</el-button>
          <el-button type="primary" @click="openProductDialog()">新增商品</el-button>
        </div>
      </div>

      <el-table :data="products" style="width: 100%" v-loading="productLoading">
        <el-table-column prop="productId" label="ID" width="80" />
        <el-table-column label="图片" width="90">
          <template #default="{ row }">
            <el-image
              :src="row.mainImage"
              fit="cover"
              style="width: 48px; height: 48px; border-radius: 6px; background: #f5f7fa"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="180" />
        <el-table-column prop="subTitle" label="副标题" min-width="180" show-overflow-tooltip />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getProductStatusType(row.status)">{{ getProductStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">{{ formatDate(row.createdTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openProductDialog(row)">编辑</el-button>
            <el-button
              link
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleProductStatus(row)"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button link type="danger" @click="deleteProduct(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="card-block">
      <div class="header">
        <h2>订单管理</h2>
        <div class="filter">
          <el-radio-group v-model="status" @change="fetchOrders">
            <el-radio-button :label="null">全部</el-radio-button>
            <el-radio-button :label="1">待发货</el-radio-button>
            <el-radio-button :label="2">已发货</el-radio-button>
            <el-radio-button :label="4">退款待审</el-radio-button>
            <el-radio-button :label="3">已取消</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <el-table :data="orders" style="width: 100%" v-loading="orderLoading">
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
        <el-table-column label="下单时间" width="180">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" type="primary" size="small" @click="openShip(row)">去发货</el-button>
            <el-button v-if="row.status === 2" type="warning" size="small" @click="openShip(row)">修改物流</el-button>
            <el-button v-if="row.status === 4" type="success" size="small" @click="confirmRefund(row)">同意退款</el-button>
            <el-button v-if="row.status === 4" type="danger" size="small" @click="auditRefund(row, false)">驳回退款</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="productDialogVisible" :title="productForm.productId ? '编辑商品' : '新增商品'" width="600px">
      <el-form :model="productForm" label-width="100px">
        <el-form-item label="商品名称">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="productForm.subTitle" placeholder="请输入商品副标题" />
        </el-form-item>
        <el-form-item label="主图">
          <el-upload
            class="avatar-uploader"
            action="/api/file/upload"
            :show-file-list="false"
            :on-success="handleProductImageSuccess"
          >
            <img v-if="productForm.mainImage" :src="productForm.mainImage" class="product-cover" />
            <el-button v-else type="primary" plain>上传主图</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="productForm.price" :min="0" :precision="2" :step="0.1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="productForm.stock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="productForm.status" style="width: 100%">
            <el-option label="已上架" :value="1" />
            <el-option label="已下架" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="详情">
          <el-input v-model="productForm.detail" type="textarea" :rows="4" placeholder="请输入商品详情" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="productDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="productSubmitting" @click="submitProduct">保存</el-button>
      </template>
    </el-dialog>

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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const products = ref([])
const productLoading = ref(false)
const productDialogVisible = ref(false)
const productSubmitting = ref(false)
const productForm = reactive({
  productId: null,
  name: '',
  subTitle: '',
  mainImage: '',
  price: 0,
  stock: 0,
  status: 1,
  detail: ''
})

const orders = ref([])
const orderLoading = ref(false)
const status = ref(null)

const shipDialogVisible = ref(false)
const dialogTitle = ref('发货操作')
const shipForm = reactive({
  orderId: null,
  deliveryCompany: '',
  deliveryNo: ''
})

const formatDate = (value) => {
  if (!value) return '-'
  if (typeof value === 'string') return value.replace('T', ' ').slice(0, 19)
  if (Array.isArray(value)) {
    const [y, m, d, h, mm, s] = value
    return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(h).padStart(2, '0')}:${String(mm).padStart(2, '0')}:${String(s || 0).padStart(2, '0')}`
  }
  return String(value)
}

const getProductStatusText = (statusValue) => {
  if (statusValue === 1) return '已上架'
  if (statusValue === 2) return '已下架'
  if (statusValue === 0) return '待审核'
  return '未知'
}

const getProductStatusType = (statusValue) => {
  if (statusValue === 1) return 'success'
  if (statusValue === 2) return 'info'
  if (statusValue === 0) return 'warning'
  return 'info'
}

const fetchProducts = async () => {
  productLoading.value = true
  try {
    const res = await request.get('/products')
    products.value = res.data || []
  } catch (e) {
    ElMessage.error('加载商品失败')
  } finally {
    productLoading.value = false
  }
}

const resetProductForm = () => {
  productForm.productId = null
  productForm.name = ''
  productForm.subTitle = ''
  productForm.mainImage = ''
  productForm.price = 0
  productForm.stock = 0
  productForm.status = 1
  productForm.detail = ''
}

const openProductDialog = (row = null) => {
  if (row) {
    productForm.productId = row.productId
    productForm.name = row.name || ''
    productForm.subTitle = row.subTitle || ''
    productForm.mainImage = row.mainImage || ''
    productForm.price = Number(row.price || 0)
    productForm.stock = row.stock || 0
    productForm.status = row.status ?? 1
    productForm.detail = row.detail || ''
  } else {
    resetProductForm()
  }
  productDialogVisible.value = true
}

const handleProductImageSuccess = (res) => {
  if (res.code === 200) {
    productForm.mainImage = res.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

const submitProduct = async () => {
  if (!productForm.name.trim()) {
    ElMessage.warning('请输入商品名称')
    return
  }
  if (!productForm.mainImage) {
    ElMessage.warning('请上传商品主图')
    return
  }
  productSubmitting.value = true
  try {
    const payload = {
      name: productForm.name.trim(),
      subTitle: productForm.subTitle.trim(),
      mainImage: productForm.mainImage,
      price: productForm.price,
      stock: productForm.stock,
      status: productForm.status,
      detail: productForm.detail
    }
    if (productForm.productId) {
      await request.put(`/products/${productForm.productId}`, payload)
      ElMessage.success('商品更新成功')
    } else {
      await request.post('/products', payload)
      ElMessage.success('商品发布成功')
    }
    productDialogVisible.value = false
    fetchProducts()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
  } finally {
    productSubmitting.value = false
  }
}

const deleteProduct = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除商品“${row.name}”吗？`, '提示', { type: 'warning' })
    await request.delete(`/products/${row.productId}`)
    ElMessage.success('删除成功')
    fetchProducts()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e?.response?.data?.message || '删除失败')
    }
  }
}

const toggleProductStatus = async (row) => {
  const nextStatus = row.status === 1 ? 2 : 1
  const actionText = nextStatus === 1 ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(`确定要${actionText}商品“${row.name}”吗？`, '提示', { type: 'warning' })
    await request.put(`/products/${row.productId}`, {
      name: row.name,
      subTitle: row.subTitle,
      mainImage: row.mainImage,
      price: row.price,
      stock: row.stock,
      detail: row.detail,
      status: nextStatus
    })
    ElMessage.success(`${actionText}成功`)
    fetchProducts()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e?.response?.data?.message || `${actionText}失败`)
    }
  }
}

const fetchOrders = async () => {
  orderLoading.value = true
  try {
    const params = {}
    if (status.value !== null) params.status = status.value
    const res = await request.get('/mall/orders', { params })
    orders.value = res.data || []
  } catch (e) {
    ElMessage.error('加载订单失败')
  } finally {
    orderLoading.value = false
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
    await request.post(`/mall/ship/${shipForm.orderId}`, null, {
      params: {
        deliveryCompany: shipForm.deliveryCompany,
        deliveryNo: shipForm.deliveryNo
      }
    })
    ElMessage.success(dialogTitle.value === '修改物流' ? '修改成功' : '发货成功')
    shipDialogVisible.value = false
    fetchOrders()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '发货失败')
  }
}

const confirmRefund = (row) => {
  ElMessageBox.confirm(
    '确定同意退款吗？此操作将取消订单并回滚库存。',
    '退款确认',
    {
      confirmButtonText: '同意退款',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request.post(`/mall/refund/confirm/${row.id}`)
      ElMessage.success('退款处理成功，订单已取消')
      fetchOrders()
    } catch (e) {
      ElMessage.error(e?.response?.data?.message || '操作失败')
    }
  })
}

const auditRefund = (row, pass) => {
  ElMessageBox.confirm(
    pass ? '确定同意退款吗？此操作将回滚库存。' : '确定驳回该退款申请吗？',
    pass ? '同意退款' : '驳回退款',
    {
      confirmButtonText: pass ? '同意' : '驳回',
      cancelButtonText: '取消',
      type: pass ? 'warning' : 'info'
    }
  ).then(async () => {
    try {
      await request.post(`/mall/refund/audit/${row.id}`, null, { params: { pass } })
      ElMessage.success(pass ? '退款已通过' : '退款已驳回')
      fetchOrders()
    } catch (e) {
      ElMessage.error(e?.response?.data?.message || '操作失败')
    }
  })
}

onMounted(() => {
  fetchProducts()
  fetchOrders()
})
</script>

<style scoped>
.page {
  padding: 16px;
}

.card-block + .card-block {
  margin-top: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.actions,
.filter {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.product-cover {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}
</style>
