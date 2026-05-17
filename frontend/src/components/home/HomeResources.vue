<template>
  <div class="home-resources">
    <SectionTitle text="在线戏院" more-link="/resources" />
    <el-tabs v-model="active" class="res-tabs" @tab-click="onTab">
      <el-tab-pane label="经典重温" name="classic" />
      <el-tab-pane label="教学视频" name="teaching" />
      <el-tab-pane label="剧本" name="scripts" />
    </el-tabs>
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="6" v-for="r in resourcesToShow" :key="r.resourceId || r.id" class="res-col">
        <el-card class="res-card" shadow="hover" @click="viewResource(r.resourceId || r.id)">
          <div class="thumb">
            <img :src="r.coverImg || placeholder" />
            <div class="play-icon">{{ currentBadge }}</div>
          </div>
          <div class="card-body">
            <div class="title">{{ r.title }}</div>
            <div class="card-footer">
              <span class="meta-text">{{ currentMeta }}</span>
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
const active = ref('classic')
const resources = ref([])
const loading = ref(false)
const placeholder = 'https://images.unsplash.com/photo-1514533450685-4493e01d1fdc?ixlib=rb-4.0.3&auto=format&fit=crop&w=1200&q=80'

const resourcesToShow = computed(() => {
  return resources.value
})

const badgeMap = {
  classic: '经典',
  teaching: '教学',
  scripts: '剧本',
}

const metaMap = {
  classic: '精选舞台影像',
  teaching: '入门与进阶内容',
  scripts: '经典剧本资料',
}

const currentBadge = computed(() => badgeMap[active.value] || '推荐')
const currentMeta = computed(() => metaMap[active.value] || '精选内容')

const fetchResources = async () => {
  loading.value = true
  try {
    if (active.value === 'classic') {
      const res = await request.get('/resources/featured', { params: { limit: 4 } })
      resources.value = res.data || []
      return
    }

    const type = active.value === 'teaching' ? 1 : 4
    const res = await request.get('/resources', { params: { type, page: 1, size: 4 } })
    resources.value = res.data?.records || []
  } finally {
    loading.value = false
  }
}

const onTab = () => {
  fetchResources()
}

const viewResource = (id) => {
  router.push(`/resources/${id}`)
}

onMounted(() => {
  fetchResources()
})
</script>

<style scoped>
.res-tabs {
  margin-bottom: 16px;
}

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
