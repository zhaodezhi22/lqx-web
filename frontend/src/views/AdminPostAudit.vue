<template>
  <div class="page">
    <el-card>
      <div class="header">
        <h2>社区帖子管理</h2>
        <el-radio-group v-model="status" @change="fetchData" size="small">
          <el-radio-button :label="1">正常</el-radio-button>
          <el-radio-button :label="2">已屏蔽</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="list" v-loading="loading" style="width: 100%">
        <el-table-column prop="postId" label="ID" width="80" />
        <el-table-column prop="userName" label="发帖人" width="150" />
        <el-table-column prop="title" label="标题" width="200" show-overflow-tooltip>
           <template #default="{ row }">
             <span>{{ row.title || '无标题' }}</span>
           </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="300">
           <template #default="{ row }">
             <div class="content-text">{{ row.content }}</div>
           </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewPost(row)">查看帖子</el-button>
            <el-button v-if="row.status === 1" type="danger" size="small" @click="handleAudit(row, 2)">屏蔽帖子</el-button>
            <el-button v-if="row.status === 2" type="success" size="small" @click="handleAudit(row, 1)">恢复显示</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="showDetail" title="帖子详情" width="600px">
      <div v-if="currentPost">
        <div style="margin-bottom: 15px; font-weight: bold;">{{ currentPost.title || '无标题' }}</div>
        <div style="margin-bottom: 10px; color: #666;">
          <span>作者：{{ currentPost.userName }}</span>
          <span style="margin-left: 20px;">时间：{{ currentPost.createdTime }}</span>
        </div>
        <div style="white-space: pre-wrap; line-height: 1.6;">{{ currentPost.content }}</div>
        
        <!-- Image Gallery -->
        <div v-if="currentPost.images && currentPost.images.length" style="margin-top: 20px;">
          <el-image 
            v-for="(img, index) in currentPost.images" 
            :key="index"
            :src="img"
            :preview-src-list="currentPost.images"
            :initial-index="index"
            style="width: 100px; height: 100px; margin-right: 10px; margin-bottom: 10px; border-radius: 4px;"
            fit="cover"
          />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const status = ref(1)
const list = ref([])
const loading = ref(false)
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const showDetail = ref(false)
const currentPost = ref({})

const viewPost = (row) => {
  currentPost.value = row
  showDetail.value = true
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/post/audit/list', {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        status: status.value
      }
    })
    if (res.code === 200) {
      list.value = res.data.records
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchData()
}

const handleAudit = async (row, newStatus) => {
  try {
    const res = await request.put(`/admin/post/audit/${row.postId}`, { status: newStatus })
    if (res.code === 200) {
      ElMessage.success('操作成功')
      fetchData()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (e) {
    ElMessage.error('网络错误')
  }
}

onMounted(() => {
  fetchData()
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
.content-text {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
}
.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
