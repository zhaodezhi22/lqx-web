<template>
  <div class="page">
    <el-card>
      <div class="header">
        <h2>评论审核</h2>
        <div class="filter">
           <el-radio-group v-model="status" @change="fetchList">
             <el-radio-button label="pending">待审核</el-radio-button>
             <el-radio-button label="reported">被举报</el-radio-button>
           </el-radio-group>
        </div>
      </div>
      
      <el-table :data="list" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="content" label="评论内容" />
        <el-table-column prop="user" label="发布用户" width="150" />
        <el-table-column prop="time" label="发布时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="handle(row, 'pass')">通过</el-button>
            <el-button type="danger" size="small" @click="handle(row, 'block')">违规屏蔽</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const status = ref('pending')
const list = ref([
  { id: 101, content: '这戏唱的真不错！', user: 'test_user', time: '2024-05-20 10:00' },
  { id: 102, content: '有些地方听不懂，建议加字幕', user: 'user_123', time: '2024-05-20 10:05' },
])

const fetchList = () => {
  // Mock fetch
  ElMessage.info('刷新列表 (Mock)')
}

const handle = (row, action) => {
  ElMessage.success(action === 'pass' ? '已通过' : '已屏蔽')
  list.value = list.value.filter(i => i.id !== row.id)
}
</script>

<style scoped>
.page { padding: 16px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
</style>
