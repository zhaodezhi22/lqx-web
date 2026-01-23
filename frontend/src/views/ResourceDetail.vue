<template>
  <div class="page" v-if="detail">
    <h2>{{ detail.title }}</h2>
    <div class="meta">{{ detail.category || '未分类' }} · 浏览 {{ detail.viewCount || 0 }}</div>
    <div class="player">
      <video v-if="isVideo" controls :src="detail.fileUrl" style="width: 100%; max-width: 900px" />
      <audio v-else-if="isAudio" controls :src="detail.fileUrl" style="width: 100%; max-width: 600px" />
      <img v-else class="image" :src="detail.coverImg || placeholder" alt="cover" />
    </div>
    <div class="desc" v-html="detail.description || '暂无简介'"></div>
    <el-button type="primary" style="margin-top: 12px" @click="goEvent">去选座购票</el-button>
  </div>
  <div v-else class="page">加载中...</div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()
const detail = ref(null)
const placeholder = 'https://via.placeholder.com/900x500?text=Heritage'

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

const goEvent = () => {
  if (detail.value?.eventId) {
    router.push({ name: 'EventDetail', params: { id: detail.value.eventId } })
  } else {
    ElMessage.warning('暂无关联演出')
  }
}

onMounted(fetchDetail)
</script>

<style scoped>
.page {
  padding: 16px;
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

