<template>
  <div class="community-index">
    <!-- Publish Area -->
    <div class="publish-bar">
      <el-button type="primary" size="large" @click="dialogVisible = true" :icon="Plus">
        分享你的新鲜事
      </el-button>
    </div>

    <!-- Publish Dialog -->
    <el-dialog v-model="dialogVisible" title="发布新帖" width="500px">
      <el-form :model="publishForm">
        <el-form-item>
          <el-input
            v-model="publishForm.content"
            type="textarea"
            :rows="4"
            placeholder="这一刻的想法..."
          />
        </el-form-item>
        <el-form-item>
          <el-upload
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :file-list="fileList"
            multiple
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handlePublish" :loading="submitting">
            发布
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Post List -->
    <div class="post-list" v-loading="loading">
      <el-card 
        v-for="post in postList" 
        :key="post.postId" 
        class="post-card clickable-card" 
        @click="goToDetail(post.postId)"
      >
        <template #header>
          <div class="post-header">
            <div class="user-info">
              <el-avatar :src="post.userAvatar" :size="40" />
              <span class="nickname">{{ post.userName }}</span>
              <el-tag v-if="post.status === 0" size="small" type="warning" style="margin-left: 10px">审核中</el-tag>
              <el-tag v-if="post.status === 2" size="small" type="danger" style="margin-left: 10px">未通过</el-tag>
            </div>
            <span class="time">{{ post.createdTime }}</span>
          </div>
        </template>
        
        <div class="post-content">
          <p class="text">{{ post.content }}</p>
          
          <!-- Image Wall -->
          <div v-if="post.images && post.images.length > 0" 
               class="image-wall" 
               :class="{ 'single-img': post.images.length === 1, 'grid-imgs': post.images.length > 1 }">
            <el-image 
              v-for="(img, index) in post.images" 
              :key="index"
              :src="img"
              :preview-src-list="post.images"
              :initial-index="index"
              fit="cover"
              class="post-image"
            />
          </div>
        </div>

        <div class="post-footer">
          <div class="action-item" @click.stop="handleLike(post)" :class="{ active: post.isLiked }">
            <el-icon><StarFilled v-if="post.isLiked" /><Star v-else /></el-icon>
            <span class="count">{{ post.likeCount }}</span>
          </div>
          <!-- Placeholder for other actions like comment/share -->
        </div>
      </el-card>
      
      <el-empty v-if="!loading && postList.length === 0" description="暂无内容" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Star, StarFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const postList = ref([])
const fileList = ref([])

const publishForm = reactive({
  content: '',
  images: []
})

// Mock upload implementation - in real world this would upload to server
// Since we don't have a confirmed upload API, we'll simulate it or use a placeholder
const handleFileChange = (file, files) => {
  fileList.value = files
}

const handleFileRemove = (file, files) => {
  fileList.value = files
}

const fetchPosts = async () => {
  loading.value = true
  try {
    const res = await request.get('/community/post/list', {
      params: { page: 1, size: 10 }
    })
    postList.value = res.data?.records || []
  } catch (e) {
    ElMessage.error('加载帖子失败')
  } finally {
    loading.value = false
  }
}

const handlePublish = async () => {
  if (!publishForm.content && fileList.value.length === 0) {
    ElMessage.warning('请输入内容或上传图片')
    return
  }

  submitting.value = true
  try {
    // 1. Upload images
    const imageUrls = []
    if (fileList.value.length > 0) {
      console.log('Starting upload for files:', fileList.value)
      for (const file of fileList.value) {
        const formData = new FormData()
        if (!file.raw) {
             console.error('File raw object is missing', file)
             continue
        }
        formData.append('file', file.raw)
        
        try {
          // Remove manual Content-Type header to let browser set boundary
          const res = await request.post('/file/upload', formData)
          
          console.log('Upload response:', res)
          if (res.code === 200) {
             imageUrls.push(res.data) 
          } else {
             throw new Error(res.message || 'Upload failed')
          }
        } catch (err) {
          console.error('Upload failed for file', file.name, err)
          ElMessage.error(`图片 ${file.name} 上传失败: ${err.message || '网络错误'}`)
          submitting.value = false
          return // Stop publishing if upload fails
        }
      }
    }
    
    const payload = {
      content: publishForm.content,
      images: imageUrls
    }

    await request.post('/community/post/add', payload)
    
    ElMessage.success('发布成功')
    dialogVisible.value = false
    publishForm.content = ''
    fileList.value = []
    fetchPosts() // Refresh list
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '发布失败')
  } finally {
    submitting.value = false
  }
}

const goToDetail = (id) => {
  router.push(`/community/detail/${id}`)
}

const handleLike = async (post) => {
  // Prevent card click event bubbling
  // Note: we can't easily pass event here without changing template call, but we used @click.stop in template if possible.
  // Actually, standard way is @click.stop on the button.
  // But wait, the like button is inside the card which has @click.
  // We need to modify the like button to use @click.stop

  // Optimistic update
  const originalLiked = post.isLiked
  const originalCount = post.likeCount
  
  post.isLiked = !originalLiked
  post.likeCount = originalLiked ? originalCount - 1 : originalCount + 1
  
  try {
    await request.post(`/community/post/like/${post.postId}`)
  } catch (e) {
    // Revert on failure
    post.isLiked = originalLiked
    post.likeCount = originalCount
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  fetchPosts()
})
</script>

<style scoped>
.community-index {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.publish-bar {
  margin-bottom: 20px;
  text-align: center;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}

.post-card {
  margin-bottom: 20px;
}
.clickable-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.clickable-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.nickname {
  font-weight: bold;
  color: #333;
}

.time {
  color: #999;
  font-size: 12px;
}

.post-content {
  margin-bottom: 15px;
}

.text {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 10px;
  color: #333;
  white-space: pre-wrap;
}

.image-wall {
  margin-top: 10px;
}

.single-img .post-image {
  max-width: 100%;
  max-height: 400px;
  border-radius: 4px;
}

.grid-imgs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 4px;
}

.grid-imgs .post-image {
  width: 100%;
  aspect-ratio: 1 / 1;
  border-radius: 4px;
}

.post-footer {
  display: flex;
  align-items: center;
  border-top: 1px solid #f0f0f0;
  padding-top: 10px;
}

.action-item {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #666;
  gap: 5px;
  transition: color 0.2s;
}

.action-item:hover {
  color: #409EFF;
}

.action-item.active {
  color: #F56C6C;
}

.count {
  font-size: 14px;
}
</style>
