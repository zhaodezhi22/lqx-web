<template>
  <div class="content-detail-page">
    <div class="container">
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="10" animated />
      </div>
      
      <div v-else-if="content" class="article-wrapper">
        <el-button @click="$router.back()" style="margin-bottom: 20px">返回</el-button>
        <h1 class="article-title">{{ content.title }}</h1>
        <div class="article-meta">
          <span class="meta-item">发布时间：{{ formatDate(content.createTime) }}</span>
          <span class="meta-item" v-if="content.subtitle">{{ content.subtitle }}</span>
        </div>
        
        <div class="article-banner" v-if="content.imageUrl">
          <img :src="content.imageUrl" alt="Cover Image" />
        </div>
        
        <div class="article-content">
          <!-- Render content with line breaks -->
          <p v-for="(paragraph, index) in formattedContent" :key="index">
            {{ paragraph }}
          </p>
        </div>
      </div>
      
      <div v-else class="empty-state">
        <el-empty description="内容不存在或已删除" />
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const contentId = route.params.id
const content = ref(null)
const loading = ref(true)

const fetchData = async () => {
  if (!contentId) return
  loading.value = true
  try {
    const res = await request.get(`/home/content/${contentId}`)
    if (res.code === 200) {
      content.value = res.data
    } else {
      ElMessage.error(res.message || '获取内容失败')
    }
  } catch (e) {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

const formattedContent = computed(() => {
  if (!content.value || !content.value.content) return []
  // Split by newlines to preserve formatting
  return content.value.content.split('\n')
})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.content-detail-page {
  padding: 40px 0;
  min-height: 80vh;
  background-color: #f9f9f9;
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.article-title {
  font-size: 32px;
  font-weight: 700;
  color: #333;
  margin-bottom: 20px;
  text-align: center;
  line-height: 1.4;
}

.article-meta {
  display: flex;
  justify-content: center;
  gap: 20px;
  color: #999;
  font-size: 14px;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.article-banner {
  margin-bottom: 30px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.article-banner img {
  width: 100%;
  height: auto;
  display: block;
}

.article-content {
  font-size: 18px;
  line-height: 1.8;
  color: #444;
  text-align: justify;
}

.article-content p {
  margin-bottom: 1.5em;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

@media (max-width: 768px) {
  .container {
    padding: 20px;
    margin: 10px;
  }
  
  .article-title {
    font-size: 24px;
  }
}
</style>