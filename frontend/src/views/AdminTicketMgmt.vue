<template>
  <div class="page">
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
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '售票中' : '已结束' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button link type="primary" @click="showEditDialog(row)">编辑</el-button>
            <el-button link type="primary">座位图</el-button>
            <el-button link type="warning">核销检票</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

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
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
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
  eventId: null,
  title: '',
  venue: '',
  showTime: '',
  ticketPrice: '',
  totalSeats: 200, // Default seats
  status: 1
})

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
  form.totalSeats = 200
  form.status = 1
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  Object.assign(form, row)
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
