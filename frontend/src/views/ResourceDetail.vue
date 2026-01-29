<template>
  <div class="page" v-if="detail">
    <h2>{{ detail.title }}</h2>
    <div class="publisher-info" v-if="detail.uploaderId && detail.uploaderName" @click="goProfile(detail.uploaderId)">
      <el-avatar :size="32" :src="detail.uploaderAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
      <span class="name">{{ detail.uploaderName }}</span>
      <el-tag size="small" v-if="detail.uploaderRole === 1">传承人</el-tag>
    </div>
    <div class="publisher-info-disabled" v-else-if="detail.uploaderId">
       <el-avatar :size="32" src="https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png" />
       <span class="name">用户已注销</span>
    </div>
    <div class="meta">{{ detail.category || '未分类' }} · 浏览 {{ detail.viewCount || 0 }}</div>
    <div class="player">
      <video v-if="isVideo" controls :src="detail.fileUrl" style="width: 100%; max-width: 900px" />
      <audio v-else-if="isAudio" controls :src="detail.fileUrl" style="width: 100%; max-width: 600px" />
      <img v-else class="image" :src="detail.coverImg || placeholder" alt="cover" />
    </div>
    <div class="desc" v-html="detail.description || '暂无简介'"></div>
  </div>
  <div v-else class="page">加载中...</div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()
const detail = ref(null)
const placeholder = 'https://via.placeholder.com/900x500?text=Heritage'
const viewTimer = ref(null)

const isVideo = computed(() => detail.value?.type === 1)
const isAudio = computed(() => detail.value?.type === 2)

const fetchDetail = async () => {
  try {
    const res = await request.get(`/resources/${route.params.id}`)
    detail.value = res.data
  } catch (e) {
    ElMessage.error('加载详情失败')
  }
}

const goProfile = (userId) => {
  router.push({ name: 'UserPublicProfile', params: { id: userId } })
}

const startViewTimer = () => {
  if (viewTimer.value) clearTimeout(viewTimer.value)
  // Check if user is logged in
  const userStr = localStorage.getItem('user')
  if (!userStr) return
  
  const user = JSON.parse(userStr)
  if (!user.userId) return

  viewTimer.value = setTimeout(async () => {
      try {
          const res = await request.post('/points/record-view', null, { params: { userId: user.userId } })
          if (res.code === 200 && res.data) {
              ElMessage.success('观看满15秒，获得10积分！')
          }
      } catch(e) {}
  }, 15000)
}

watch(detail, (val) => {
    if (val && (val.type === 1 || val.type === 2)) {
        startViewTimer()
    }
})

onUnmounted(() => {
    if (viewTimer.value) clearTimeout(viewTimer.value)
})

onMounted(fetchDetail)
</script>

<style scoped>
.page {
  padding: 16px;
}
.publisher-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  cursor: pointer;
  width: fit-content;
  padding: 4px 8px 4px 0;
  border-radius: 4px;
}
.publisher-info:hover {
  background: #f5f7fa;
}
.publisher-info .name {
  font-weight: bold;
  font-size: 14px;
  color: #303133;
}
.publisher-info-disabled {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  width: fit-content;
  padding: 4px 8px 4px 0;
  border-radius: 4px;
  opacity: 0.6;
  cursor: not-allowed;
}
.publisher-info-disabled .name {
  font-weight: bold;
  font-size: 14px;
  color: #909399;
}
.meta {
  color: #909399;
  margin-bottom: 12px;
}
.player {
  margin-bottom: 16px;
}
.image {
  max-width: 900px;
  width: 100%;
  display: block;
  background: #f5f7fa;
}
.desc {
  line-height: 1.6;
  color: #303133;
  white-space: pre-wrap;
}
</style>

