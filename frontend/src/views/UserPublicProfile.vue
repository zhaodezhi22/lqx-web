<template>
  <div class="page">
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="!profile" class="error">用户不存在</div>
    <div v-else class="profile-container">
      <!-- User Header Info -->
      <div class="user-header">
        <el-avatar :size="100" :src="profile.avatar || defaultAvatar" />
        <div class="user-info">
          <h1 class="nickname">{{ profile.nickname }}</h1>
          <div class="tags">
            <el-tag v-if="roleName" type="success" effect="dark">{{ roleName }}</el-tag>
            <el-tag v-if="profile.level" type="warning" effect="plain">{{ profile.level }}</el-tag>
            <el-tag v-if="profile.genre" type="info" effect="plain">{{ profile.genre }}</el-tag>
          </div>
          <div v-if="profile.artisticCareer" class="bio">
            <strong>艺术生涯:</strong> {{ profile.artisticCareer }}
          </div>
          <div v-if="profile.awards" class="bio">
            <strong>获奖经历:</strong> {{ profile.awards }}
          </div>
        </div>
      </div>

      <!-- Content Tabs -->
      <el-tabs v-model="activeTab" class="content-tabs">
        <el-tab-pane label="戏曲资源" name="resources">
          <div v-if="resources.length === 0" class="empty">暂无资源</div>
          <div v-else class="resource-grid">
            <div class="resource-card" v-for="item in resources" :key="item.resourceId" @click="goResource(item.resourceId)">
              <img class="cover" :src="item.coverImg || resourcePlaceholder" alt="cover" />
              <div class="card-body">
                <div class="title">{{ item.title }}</div>
                <div class="meta">{{ item.category || '未分类' }} · 浏览 {{ item.viewCount || 0 }}</div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="文创商品" name="products">
          <div v-if="products.length === 0" class="empty">暂无商品</div>
          <div v-else class="product-grid">
            <div class="product-card" v-for="item in products" :key="item.productId" @click="openProduct(item)">
              <img class="cover" :src="item.mainImage || productPlaceholder" alt="cover" />
              <div class="card-body">
                <div class="title">{{ item.name }}</div>
                <div class="price">¥ {{ item.price }}</div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="社区帖子" name="posts">
          <div v-if="posts.length === 0" class="empty">暂无帖子</div>
          <div v-else class="post-list">
            <div class="post-item" v-for="item in posts" :key="item.postId" @click="goPost(item.postId)">
              <div class="post-title">{{ item.title }}</div>
              <div class="post-meta">
                发布于 {{ formatDate(item.createdTime) }} · {{ item.viewCount || 0 }} 浏览
              </div>
              <div class="post-preview">{{ item.content.substring(0, 100) }}...</div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="演出活动" name="events">
          <div v-if="events.length === 0" class="empty">暂无演出</div>
          <div v-else class="event-list">
            <div class="event-item" v-for="item in events" :key="item.eventId" @click="goEvent(item.eventId)">
              <div class="event-time">
                <div class="date">{{ formatDay(item.showTime) }}</div>
                <div class="time">{{ formatTime(item.showTime) }}</div>
              </div>
              <div class="event-info">
                <div class="event-title">{{ item.title }}</div>
                <div class="event-venue">{{ item.venue }}</div>
                <div class="event-price">¥ {{ item.ticketPrice }} 起</div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- Product Detail Modal -->
    <ProductDetailModal v-model:visible="productVisible" :product-id="currentProductId" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import ProductDetailModal from '../components/ProductDetailModal.vue'

const route = useRoute()
const router = useRouter()
const userId = ref(route.params.id)
const activeTab = ref('resources')
const loading = ref(true)

const profile = ref(null)
const resources = ref([])
const products = ref([])
const posts = ref([])
const events = ref([])

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const resourcePlaceholder = 'https://via.placeholder.com/400x220?text=Heritage'
const productPlaceholder = 'https://via.placeholder.com/300x180?text=Product'

// Product modal
const productVisible = ref(false)
const currentProductId = ref(null)

const roleName = computed(() => {
  if (!profile.value) return ''
  switch (profile.value.role) {
    case 1: return '非遗传承人'
    case 2: return '普通用户' // Usually shouldn't be here unless they posted stuff
    case 3: return '管理员'
    default: return '用户'
  }
})

const fetchData = async () => {
  loading.value = true
  try {
    const id = userId.value
    if (!id) {
      ElMessage.error('用户ID无效')
      loading.value = false
      return
    }
    const pRes = await request.get(`/public/user/${id}/profile`)
    if (pRes.code === 200 && pRes.data) {
      profile.value = pRes.data
      
      // Load other data only if profile exists
      const rRes = await request.get(`/public/user/${id}/resources`)
      resources.value = rRes.data || []
      const prRes = await request.get(`/public/user/${id}/products`)
      products.value = prRes.data || []
      const poRes = await request.get(`/public/user/${id}/posts`)
      posts.value = poRes.data || []
      const evRes = await request.get(`/public/user/${id}/events`)
      events.value = evRes.data || []
    } else {
      profile.value = null
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('获取用户信息失败')
    profile.value = null
  } finally {
    loading.value = false
  }
}

watch(() => route.params.id, (newId) => {
  if (newId) {
    userId.value = newId
    fetchData()
  }
})

onMounted(fetchData)

// Navigation
const goResource = (id) => router.push({ name: 'ResourceDetail', params: { id } })
const goPost = (id) => router.push({ name: 'CommunityDetail', params: { id } })
const goEvent = (id) => router.push({ name: 'EventDetail', params: { id } })
const openProduct = (item) => {
  currentProductId.value = item.productId
  productVisible.value = true
}

// Formatters
const formatDate = (str) => {
  if (!str) return ''
  return new Date(str).toLocaleDateString()
}
const formatDay = (str) => {
  if (!str) return ''
  const d = new Date(str)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}
const formatTime = (str) => {
  if (!str) return ''
  const d = new Date(str)
  return `${d.getHours()}:${d.getMinutes().toString().padStart(2, '0')}`
}
</script>

<style scoped>
.page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.loading, .error {
  text-align: center;
  padding: 40px;
  font-size: 16px;
  color: #666;
}
.user-header {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  margin-bottom: 24px;
}
.user-info {
  flex: 1;
}
.nickname {
  margin: 0 0 12px 0;
  font-size: 24px;
  font-weight: bold;
}
.tags {
  margin-bottom: 16px;
  display: flex;
  gap: 8px;
}
.bio {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 8px;
}

/* Grids */
.resource-grid, .product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}
.resource-card, .product-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: all 0.2s;
}
.resource-card:hover, .product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.cover {
  width: 100%;
  height: 140px;
  object-fit: cover;
}
.card-body {
  padding: 12px;
}
.title {
  font-weight: bold;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.meta {
  font-size: 12px;
  color: #909399;
}
.price {
  color: #f56c6c;
  font-weight: bold;
}

/* Post List */
.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.post-item {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  cursor: pointer;
}
.post-item:hover {
  background: #fcfcfc;
}
.post-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 8px;
}
.post-meta {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}
.post-preview {
  color: #606266;
  font-size: 14px;
}

/* Event List */
.event-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.event-item {
  display: flex;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  overflow: hidden;
  cursor: pointer;
}
.event-time {
  background: #ecf5ff;
  padding: 16px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100px;
  color: #409eff;
}
.event-time .date { font-size: 18px; font-weight: bold; }
.event-time .time { font-size: 14px; }
.event-info {
  padding: 16px;
  flex: 1;
}
.event-title { font-size: 18px; font-weight: bold; margin-bottom: 8px; }
.event-venue { color: #666; margin-bottom: 8px; }
.event-price { color: #f56c6c; font-weight: bold; }

.empty {
  text-align: center;
  color: #999;
  padding: 40px;
}
</style>
