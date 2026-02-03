<template>
  <div class="page">
    <el-card>
      <div class="header">
        <h2>商品审核</h2>
        <div class="filter">
           <el-button type="primary" @click="fetchList">刷新</el-button>
        </div>
      </div>
      
      <el-table :data="list" style="width: 100%" v-loading="loading">
        <el-table-column prop="productId" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="price" label="价格" width="100">
             <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="sellerId" label="卖家ID" width="100" />
        <el-table-column prop="createdTime" label="创建时间" width="180">
             <template #default="{ row }">
                 {{ row.createdTime?.replace('T', ' ') }}
             </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
           <template #default="{ row }">
             <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button link type="info" @click="viewLogs(row)">修改记录</el-button>
            <template v-if="row.status === 0">
                <el-button link type="success" @click="audit(row, 1)">通过</el-button>
                <el-button link type="danger" @click="audit(row, 2)">驳回</el-button>
            </template>
            <template v-else-if="row.status === 1">
                <el-button link type="danger" @click="audit(row, 2)">下架</el-button>
            </template>
            <span v-else class="text-gray" style="margin-left: 10px;">已下架</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <ProductDetailModal 
      v-model:visible="detailVisible" 
      :product-id="detailId" 
      :is-admin="true"
    />
    
    <el-dialog v-model="logsVisible" title="商品修改记录" width="700px">
        <el-table :data="logsList" border stripe style="width: 100%" height="400">
            <el-table-column prop="createTime" label="时间" width="170">
                 <template #default="{row}">{{ row.createTime?.replace('T', ' ') }}</template>
            </el-table-column>
            <el-table-column prop="actionType" label="类型" width="80">
                <template #default="{row}">
                    <el-tag :type="getActionType(row.actionType)">{{ row.actionType }}</el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="content" label="变更内容" />
        </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import ProductDetailModal from '../components/ProductDetailModal.vue'

const list = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const detailId = ref(null)

const logsVisible = ref(false)
const logsList = ref([])

const viewLogs = async (row) => {
    logsVisible.value = true
    logsList.value = [] // clear
    try {
        const res = await request.get(`/products/logs/${row.productId}`)
        logsList.value = res.data || []
    } catch(e) {
        ElMessage.error('获取记录失败')
    }
}

const getActionType = (type) => {
    if (type === 'CREATE') return 'success'
    if (type === 'UPDATE') return 'warning'
    if (type === 'DELETE') return 'danger'
    return 'info'
}

const viewDetail = (row) => {
  detailId.value = row.productId
  detailVisible.value = true
}

const getStatusLabel = (status) => {
  if (status === 0) return '待审核'
  if (status === 1) return '已上架'
  if (status === 2) return '已下架/驳回'
  return '未知'
}

const getStatusType = (status) => {
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'info'
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/products')
    // Sort: Pending (0) first
    let data = res.data || []
    data.sort((a, b) => {
        if (a.status === 0 && b.status !== 0) return -1
        if (a.status !== 0 && b.status === 0) return 1
        return 0
    })
    list.value = data
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const audit = async (row, status) => {
  try {
    await request.put(`/products/audit`, { 
        id: row.productId, 
        status: status 
    })
    ElMessage.success('操作成功')
    fetchList()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.page {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.text-gray {
    color: #999;
    font-size: 12px;
}
</style>