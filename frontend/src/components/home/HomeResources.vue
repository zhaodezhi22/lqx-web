<template>
  <div class="home-resources">
    <SectionTitle text="在线戏院" more-link="/resources" />
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="6" v-for="r in resourcesToShow" :key="r.resourceId || r.id" class="res-col">
        <el-card class="res-card" shadow="hover" @click="viewResource(r.resourceId || r.id)">
          <div class="thumb">
            <img :src="r.coverImg || placeholder" />
            <div class="play-icon">{{ getResourceBadge(r) }}</div>
          </div>
          <div class="card-body">
            <div class="title">{{ r.title }}</div>
            <div class="card-footer">
              <span class="meta-text">{{ getResourceMeta(r) }}</span>
              <span class="view-text">立即查看</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col v-if="!loading && resourcesToShow.length === 0" :span="24">
        <el-empty description="暂无资源" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import SectionTitle from '../common/SectionTitle.vue'

const router = useRouter()
const resources = ref([])
const loading = ref(false)
const placeholder = 'https://images.unsplash.com/photo-1514533450685-4493e01d1fdc?ixlib=rb-4.0.3&auto=format&fit=crop&w=1200&q=80'

const resourcesToShow = computed(() => {
  return resources.value
})

const typeBadgeMap = {
  1: '视频',
  2: '音频',
  3: '图文',
  4: '附件'
}

const getResourceBadge = (resource) => {
  return typeBadgeMap[resource?.type] || '精选'
}

const getResourceMeta = (resource) => {
  return resource?.category || '在线影院推荐'
}

const fetchResources = async () => {
  loading.value = true
  try {
    const res = await request.get('/resources/featured', { params: { limit: 4 } })
    resources.value = res.data?.records || []
    if (Array.isArray(res.data)) {
      resources.value = res.data
    }
  } finally {
    loading.value = false
  }
}

const viewResource = (id) => {
  router.push(`/resources/${id}`)
}

onMounted(() => {
  fetchResources()
})
</script>

<style scoped>
.res-col {
  display: flex;
}

.res-card {
  width: 100%;
  border: none;
  border-radius: 18px;
  height: 100%;
  cursor: pointer;
}

.res-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px;
}

.res-card .thumb {
  position: relative;
  height: 180px;
  border-radius: 12px;
  overflow: hidden;
}
.res-card img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform .3s;
}
.res-card .play-icon {
  position: absolute;
  right: 12px;
  bottom: 12px;
  background: rgba(170, 29, 29, 0.85);
  color: #fff;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.card-body {
  display: flex;
  flex-direction: column;
  flex: 1;
  padding-top: 14px;
}

.res-card .title {
  color: #333;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.6;
  min-height: 52px;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.card-footer {
  margin-top: auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #8b6b56;
  font-size: 13px;
}

.view-text {
  color: #aa1d1d;
  font-weight: 600;
}

.res-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 24px rgba(170, 29, 29, .12);
}
.res-card:hover img {
  transform: scale(1.05);
}
</style>
