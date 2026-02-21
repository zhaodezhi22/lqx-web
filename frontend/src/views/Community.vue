<template>
  <div class="page-container">
    <div class="header-section">
      <h2>社区交流</h2>
      <el-button type="primary" @click="showCreateDialog = true">发布帖子</el-button>
    </div>

    <div class="post-list" v-loading="loading">
      <el-card v-for="post in postList" :key="post.postId" class="post-card">
        <div class="post-header">
          <el-avatar :src="post.userAvatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
          <div class="post-info">
            <span class="nickname">{{ post.userName || '匿名用户' }}</span>
            <span class="time">{{ post.createdTime }}</span>
          </div>
        </div>
        
        <div class="post-content">
          <p class="content-text">{{ post.content }}</p>
          <div class="post-images" v-if="post.images && post.images.length">
            <el-image 
              v-for="(url, index) in post.images" 
              :key="index"
              :src="url" 
              :preview-src-list="post.images"
              class="post-img"
              fit="cover"
            />
          </div>
        </div>

        <div class="post-footer">
          <div class="action-item" @click="handleLike(post)" :class="{ active: post.isLiked }">
            <el-icon><Pointer /></el-icon>
            <span>{{ post.isLiked ? '已赞' : '点赞' }} {{ post.likeCount }}</span>
          </div>
        </div>
      </el-card>

      <el-empty v-if="!loading && postList.length === 0" description="暂无帖子，快来发布第一条吧" />
    </div>

    <!-- Create Post Dialog -->
    <el-dialog v-model="showCreateDialog" title="发布新帖" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="内容">
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="4" 
            placeholder="分享你的心得..." 
          />
        </el-form-item>
        <el-form-item label="图片URL">
          <el-input 
            v-model="form.imageUrlInput" 
            type="textarea" 
            :rows="2" 
            placeholder="输入图片链接，多个用逗号分隔" 
          />
          <div class="tip">注：实际项目中应使用文件上传组件</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="submitPost" :loading="submitting">发布</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Pointer } from '@element-plus/icons-vue'
import request from '../utils/request'

const loading = ref(false)
const postList = ref([])
const showCreateDialog = ref(false)
const submitting = ref(false)

const form = reactive({
  content: '',
  imageUrlInput: ''
})

const fetchPosts = async () => {
  loading.value = true
  try {
    const res = await request.get('/community/post/list', {
      params: { page: 1, size: 20 }
    })
    if (res.code === 200) {
      postList.value = res.data.records || []
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载帖子失败')
  } finally {
    loading.value = false
  }
}

const handleLike = async (post) => {
  try {
    const res = await request.post(`/community/post/like/${post.postId}`)
    if (res.code === 200) {
      // Toggle local state
      post.isLiked = !post.isLiked
      post.likeCount += post.isLiked ? 1 : -1
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

const submitPost = async () => {
  if (!form.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }
  
  submitting.value = true
  try {
    // Process images
    const imageUrls = form.imageUrlInput
      ? form.imageUrlInput.split(/,|，|\n/).map(s => s.trim()).filter(s => s)
      : []

    await request.post('/community/post/add', {
      content: form.content,
      imageUrls: imageUrls
    })
    
    ElMessage.success('发布成功')
    showCreateDialog.value = false
    form.content = ''
    form.imageUrlInput = ''
    fetchPosts() // Refresh list
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '发布失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchPosts()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.post-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.post-card {
  border-radius: 8px;
}
.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}
.post-info {
  margin-left: 12px;
  display: flex;
  flex-direction: column;
}
.nickname {
  font-weight: bold;
  font-size: 16px;
  color: #333;
}
.time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
.post-content {
  margin-bottom: 15px;
}
.content-text {
  font-size: 15px;
  line-height: 1.6;
  margin-bottom: 12px;
  color: #333;
  white-space: pre-wrap;
}
/* 9-Grid Layout for Images */
.post-images {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  max-width: 400px; /* Limit width for aesthetics */
}
/* If only 1 image, make it larger but restricted */
.post-images:has(> .post-img:only-child) {
  grid-template-columns: 1fr;
  max-width: 240px;
}
/* If 2 or 4 images, maybe 2 columns looks better? 
   Standard Wechat style: 1->auto, 2/4->2cols, 3/5-9->3cols.
   For simplicity, we keep 3 cols but max-width handles it or we can use dynamic class.
   CSS Grid auto-fill is also an option but 3 cols is standard.
*/
.post-img {
  width: 100%;
  aspect-ratio: 1 / 1; /* Square */
  border-radius: 4px;
  background-color: #f0f0f0;
  cursor: zoom-in;
}

.post-footer {
  border-top: 1px solid #eee;
  padding-top: 10px;
  display: flex;
  justify-content: flex-end;
}
.action-item {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #666;
  font-size: 14px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s;
}
.action-item:hover {
  background-color: #f5f5f5;
}
.action-item.active {
  color: #f56c6c; /* Red for liked */
}
.action-item .el-icon {
  margin-right: 4px;
  font-size: 18px;
}

.tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
