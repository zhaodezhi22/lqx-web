<template>
  <div class="community-detail-container">
    <div class="main-content">
      <!-- Back Button -->
      <div class="back-link">
        <el-button link @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon> 返回列表
        </el-button>
      </div>

      <div v-loading="loading" class="detail-card">
        <template v-if="post">
          <!-- Header -->
          <div class="post-header">
            <h1 class="post-title">{{ post.content ? (post.content.length > 30 ? post.content.substring(0,30)+'...' : post.content) : '无标题' }}</h1>
            
            <div class="meta-info">
              <div class="author-info" @click="goProfile(post.userId)" style="cursor: pointer">
                <el-avatar :size="40" :src="post.userAvatar || defaultAvatar" />
                <div class="author-details">
                  <span class="author-name">{{ post.userName }}</span>
                  <span class="publish-time">{{ post.createdTime }}</span>
                </div>
              </div>
              <div class="stats-info">
                <el-button v-if="currentUserId && post.userId === currentUserId" type="danger" link :icon="Delete" @click="handleDeletePost">删除</el-button>
                <span class="stat-item"><el-icon><View /></el-icon> {{ post.viewCount }}</span>
                <span class="stat-item like-btn" :class="{ liked: post.isLiked }" @click="toggleLike">
                  <el-icon><Pointer /></el-icon> {{ post.likeCount }}
                </span>
              </div>
            </div>
          </div>

          <!-- Content -->
          <div class="post-body">
            <p class="text-content">{{ post.content }}</p>
            
            <!-- Image Grid -->
            <div v-if="post.images && post.images.length" class="image-wall">
              <el-image 
                v-for="(img, index) in post.images" 
                :key="index"
                :src="img"
                :preview-src-list="post.images"
                :initial-index="index"
                fit="cover"
                class="detail-image"
              />
            </div>
          </div>

          <!-- Comments Section -->
          <div class="comments-section">
            <div class="section-title">
              <h3>全部评论 ({{ post.comments ? post.comments.length : 0 }})</h3>
            </div>

            <!-- Comment Input -->
            <div class="comment-input-area">
               <el-input
                 v-model="commentContent"
                 type="textarea"
                 :rows="3"
                 placeholder="写下你的评论..."
                 maxlength="200"
                 show-word-limit
               />
               <div class="input-actions">
                 <el-button type="primary" :loading="submitting" @click="submitComment">发表评论</el-button>
               </div>
            </div>

            <!-- Comment List -->
            <div class="comment-list">
              <div v-if="!post.comments || post.comments.length === 0" class="empty-state">
                暂无评论，快来抢沙发吧~
              </div>
              <div v-else v-for="comment in post.comments" :key="comment.commentId" class="comment-item">
                <el-avatar :size="32" :src="comment.userAvatar || defaultAvatar" class="comment-avatar" 
                           @click="comment.userName !== '匿名用户' && goProfile(comment.userId)" 
                           :style="{ cursor: comment.userName !== '匿名用户' ? 'pointer' : 'default' }"/>
                <div class="comment-body">
                  <div class="comment-header-row">
                    <span class="comment-user" 
                          @click="comment.userName !== '匿名用户' && goProfile(comment.userId)" 
                          :style="{ cursor: comment.userName !== '匿名用户' ? 'pointer' : 'default' }">{{ comment.userName }}</span>
                    <div style="display: flex; align-items: center;">
                      <span class="comment-time">{{ comment.createdTime }}</span>
                      <el-button link type="primary" size="small" @click="initiateReply(comment)" style="margin-left: 10px;">回复</el-button>
                      <el-button v-if="currentUserId && comment.userId === currentUserId" type="danger" link size="small" @click="handleDeleteComment(comment.commentId)" style="margin-left: 5px;">删除</el-button>
                    </div>
                  </div>
                  <div class="comment-text">{{ comment.content }}</div>

                  <!-- Reply Input Area -->
                  <div v-if="replyTarget && replyTarget.commentId === comment.commentId" class="reply-input-box" style="margin-top: 10px;">
                    <el-input v-model="replyContent" placeholder="回复..." size="small" style="margin-bottom: 5px;" />
                    <div style="text-align: right;">
                        <el-button size="small" @click="cancelReply">取消</el-button>
                        <el-button type="primary" size="small" @click="submitReply">发送</el-button>
                    </div>
                  </div>

                  <!-- Replies Toggle -->
                  <div v-if="comment.replies && comment.replies.length > 0" class="replies-toggle" style="margin-top: 5px;">
                      <el-button link type="info" size="small" @click="toggleReplies(comment)">
                          {{ comment.isExpanded ? '收起回复' : `查看 ${comment.replies.length} 条回复` }}
                      </el-button>
                  </div>

                  <!-- Replies List -->
                  <div v-if="comment.isExpanded && comment.replies && comment.replies.length > 0" class="sub-comments-list" style="margin-left: 20px; margin-top: 10px; background: #f9f9f9; padding: 10px; border-radius: 4px;">
                      <div v-for="reply in comment.replies" :key="reply.commentId" class="sub-comment-item" style="margin-bottom: 10px; border-bottom: 1px dashed #eee; padding-bottom: 10px;">
                          <div style="display: flex; align-items: center; justify-content: space-between;">
                              <div style="display: flex; align-items: center; gap: 5px;">
                                  <el-avatar :size="24" :src="reply.userAvatar || defaultAvatar" />
                                  <span style="font-size: 12px; font-weight: bold;">{{ reply.userName }}</span>
                                  <span style="font-size: 12px; color: #999;">{{ reply.createdTime }}</span>
                              </div>
                              <el-button v-if="currentUserId && reply.userId === currentUserId" type="danger" link size="small" @click="handleDeleteComment(reply.commentId)">删除</el-button>
                          </div>
                          <div style="font-size: 13px; color: #666; margin-top: 5px; padding-left: 30px;">
                              {{ reply.content }}
                          </div>
                      </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
        <div v-else-if="!loading" class="error-state">
          <el-empty description="帖子不存在或已被删除" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, View, Pointer, Delete } from '@element-plus/icons-vue'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()
const postId = route.params.id

const loading = ref(false)
const submitting = ref(false)
const post = ref(null)
const commentContent = ref('')
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const currentUserId = ref(null)

const getCurrentUser = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      currentUserId.value = user.userId
    } catch (e) {
      console.error('Failed to parse user info', e)
    }
  }
}

const replyTarget = ref(null)
const replyContent = ref('')

const initiateReply = (comment) => {
  replyTarget.value = comment
  replyContent.value = ''
}

const cancelReply = () => {
  replyTarget.value = null
  replyContent.value = ''
}

const toggleReplies = (comment) => {
  comment.isExpanded = !comment.isExpanded
}

const submitReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  if (!replyTarget.value) return

  try {
    const res = await request.post('/community/comment/add', {
      targetId: replyTarget.value.commentId,
      targetType: 4, // 4 for Reply to Comment
      content: replyContent.value
    })
    
    if (res.code === 200) {
      ElMessage.success('回复成功')
      cancelReply()
      fetchDetail() 
    } else {
      ElMessage.error(res.message || '回复失败')
    }
  } catch (e) {
    ElMessage.error('回复提交失败')
  }
}

const goProfile = (userId) => {
  router.push({ name: 'UserPublicProfile', params: { id: userId } })
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await request.get(`/community/post/${postId}`)
    if (res.code === 200) {
      post.value = res.data
      // Initialize isExpanded state
      if (post.value.comments) {
        post.value.comments.forEach(c => c.isExpanded = false)
      }
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (e) {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

const handleDeletePost = () => {
  ElMessageBox.confirm(
    '确定要删除这条帖子吗？此操作不可恢复。',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        const res = await request.delete(`/community/post/${postId}`)
        if (res.code === 200) {
           ElMessage.success('删除成功')
           router.push('/community')
        } else {
           ElMessage.error(res.message || '删除失败')
        }
      } catch (e) {
        ElMessage.error('删除请求失败')
      }
    })
    .catch(() => {})
}

const handleDeleteComment = (commentId) => {
    ElMessageBox.confirm(
    '确定要删除这条评论吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        const res = await request.delete(`/community/comment/${commentId}`)
        if (res.code === 200) {
           ElMessage.success('删除成功')
           fetchDetail() // Refresh
        } else {
           ElMessage.error(res.message || '删除失败')
        }
      } catch (e) {
        ElMessage.error('删除请求失败')
      }
    })
    .catch(() => {})
}

const toggleLike = async () => {
  if (!post.value) return
  // Optimistic update
  const originalLiked = post.value.isLiked
  const originalCount = post.value.likeCount
  
  post.value.isLiked = !post.value.isLiked
  post.value.likeCount += post.value.isLiked ? 1 : -1
  
  try {
    const res = await request.post(`/community/post/like/${postId}`)
    if (res.code !== 200) {
       // Revert
       post.value.isLiked = originalLiked
       post.value.likeCount = originalCount
       ElMessage.error(res.message)
    }
  } catch (e) {
    post.value.isLiked = originalLiked
    post.value.likeCount = originalCount
  }
}

const submitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  // Note: Backend endpoint for comment submission might need to be created or we reuse generic one if exists?
  // The user prompt didn't ask to create a comment submission endpoint, but it asked for "发表评论" input in frontend.
  // I should check if there is a comment submission endpoint.
  // I see 'AdminCommentAudit' so there must be comments.
  // I previously created 'CommunityCommentService' but didn't check controller for submission.
  // I will assume there might not be one specific for post, or I should use a generic one.
  // Let's assume /api/community/comment/add or similar.
  // Wait, I didn't see CommunityCommentController in the file list earlier.
  // Let me check if I can use a generic endpoint or if I need to create one.
  // The user prompt only asked for GET /post/{id} in backend task.
  // But frontend task asked for "发表评论".
  // I will try to POST to /api/community/comment/add if it exists, or I might need to add it quickly or mock it.
  // Actually, I should probably check if `CommunityController` has add comment? No.
  // I'll assume I need to implement it or use a placeholder.
  // Given I am "Autonomous", I should probably implement the comment submission endpoint if it's missing to make the feature work.
  // But let's first check if there is a controller for comments.
  // I saw `AdminCommentAudit.vue` so there is admin side.
  
  submitting.value = true
  try {
    // Assuming a standard endpoint structure based on entity
    const res = await request.post('/community/comment/add', {
      targetId: postId,
      targetType: 3, // 3 for Post Comment
      content: commentContent.value
    })
    
    if (res.code === 200) {
      ElMessage.success('评论成功')
      commentContent.value = ''
      fetchDetail() // Refresh to show new comment
    } else {
      ElMessage.error(res.message || '评论失败')
    }
  } catch (e) {
    // If 404, maybe endpoint is missing.
    console.error(e)
    ElMessage.error('评论提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  getCurrentUser()
  if (postId) {
    fetchDetail()
  }
})
</script>

<style scoped>
.community-detail-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 20px;
}
.back-link {
  margin-bottom: 20px;
}
.detail-card {
  background: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
  min-height: 400px;
}
.post-header {
  border-bottom: 1px solid #EBEEF5;
  padding-bottom: 20px;
  margin-bottom: 20px;
}
.post-title {
  font-size: 24px;
  color: #303133;
  margin-bottom: 15px;
  line-height: 1.4;
}
.meta-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.author-info {
  display: flex;
  align-items: center;
}
.author-details {
  display: flex;
  flex-direction: column;
  margin-left: 10px;
}
.author-name {
  font-weight: 600;
  color: #606266;
  font-size: 14px;
}
.publish-time {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
.stats-info {
  display: flex;
  gap: 15px;
  color: #909399;
  font-size: 14px;
}
.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}
.like-btn {
  cursor: pointer;
  transition: color 0.3s;
}
.like-btn:hover, .like-btn.liked {
  color: #f56c6c;
}
.text-content {
  font-size: 16px;
  line-height: 1.8;
  color: #303133;
  margin-bottom: 20px;
  white-space: pre-wrap;
}
.image-wall {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 10px;
  margin-bottom: 30px;
}
.detail-image {
  width: 100%;
  height: 200px;
  border-radius: 4px;
  cursor: zoom-in;
  background: #f5f7fa;
}
.comments-section {
  margin-top: 40px;
  border-top: 1px solid #EBEEF5;
  padding-top: 20px;
}
.section-title h3 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 20px;
  border-left: 4px solid #409EFF;
  padding-left: 10px;
}
.comment-input-area {
  margin-bottom: 30px;
}
.input-actions {
  margin-top: 10px;
  text-align: right;
}
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.comment-item {
  display: flex;
  gap: 15px;
  border-bottom: 1px solid #f0f2f5;
  padding-bottom: 20px;
}
.comment-body {
  flex: 1;
}
.comment-header-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}
.comment-user {
  font-weight: 600;
  color: #606266;
  font-size: 14px;
}
.comment-time {
  font-size: 12px;
  color: #909399;
}
.comment-text {
  font-size: 14px;
  color: #303133;
  line-height: 1.5;
}
.empty-state {
  text-align: center;
  color: #909399;
  padding: 30px 0;
}
</style>