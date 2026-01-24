<template>
  <div class="page-container">
    <div class="header-section">
      <h2>非遗传承人</h2>
      <div class="filters">
         <!-- Placeholder for future filters -->
      </div>
    </div>

    <div class="inheritor-grid" v-loading="loading">
      <el-card 
        v-for="item in inheritors" 
        :key="item.userId" 
        class="inheritor-card" 
        shadow="hover"
        @click="goToDetail(item)"
      >
        <div class="card-content">
          <el-avatar :size="80" :src="item.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
          <h3 class="name">{{ item.name }}</h3>
          <el-tag effect="plain" type="danger" size="small">{{ item.level }}传承人</el-tag>
          <div class="desc">
            <!-- 暂无简介字段，预留 -->
            致力于柳琴戏的传承与发扬...
          </div>
        </div>
      </el-card>
    </div>
    
    <el-empty v-if="!loading && inheritors.length === 0" description="暂无传承人数据" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const inheritors = ref([])
const loading = ref(false)

const fetchInheritors = async () => {
  loading.value = true
  try {
    // Using spotlight as list source for now
    const res = await request.get('/inheritor/spotlight', {
      params: { limit: 20 }
    })
    if (res.code === 200) {
      inheritors.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('加载传承人列表失败')
  } finally {
    loading.value = false
  }
}

const goToDetail = (item) => {
  // If we had a detail page:
  // router.push(`/inheritors/${item.userId}`)
  // For now, just show graph
  router.push('/inheritor/graph')
}

onMounted(() => {
  fetchInheritors()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.header-section {
  margin-bottom: 24px;
}
.inheritor-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}
.inheritor-card {
  cursor: pointer;
  transition: transform 0.3s;
  text-align: center;
}
.inheritor-card:hover {
  transform: translateY(-5px);
}
.card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 0;
}
.name {
  margin: 15px 0 10px;
  font-size: 18px;
  color: #333;
}
.desc {
  margin-top: 15px;
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
