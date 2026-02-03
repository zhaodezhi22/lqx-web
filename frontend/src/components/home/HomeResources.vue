<template>
  <div class="home-resources">
    <SectionTitle text="—— 在线戏院 ——" />
    <el-tabs v-model="active" class="res-tabs" @tab-click="onTab">
      <el-tab-pane label="经典重温" name="classic" />
      <el-tab-pane label="教学视频" name="teaching" />
      <el-tab-pane label="剧本" name="scripts" />
    </el-tabs>
    <el-row :gutter="20">
      <el-col :span="6" v-for="r in resourcesToShow" :key="r.resourceId || r.id">
        <el-card class="res-card" shadow="hover" @click="viewResource(r.resourceId)">
          <div class="thumb">
            <img :src="r.coverImg || placeholder" />
            <div class="play-icon">▶</div>
          </div>
          <div class="title">{{ r.title }}</div>
        </el-card>
      </el-col>
      <el-col v-if="!loading && resourcesToShow.length === 0">
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

const props = defineProps({
  resources: { type: Array, default: () => [] }
})

const router = useRouter()
const active = ref('classic')
const resources = ref([])
const loading = ref(false)
const placeholder = 'https://images.unsplash.com/photo-1514533450685-4493e01d1fdc?ixlib=rb-4.0.3&auto=format&fit=crop&w=1200&q=80'

const resourcesToShow = computed(() => {
  return props.resources && props.resources.length ? props.resources : resources.value
})

const mapCategory = {
  classic: 'Classic Replay',
  teaching: 'Teaching Videos',
  scripts: 'Scripts',
}

const fetchResources = async () => {
  loading.value = true
  try {
    const category = mapCategory[active.value]
    const res = await request.get('/resources', { params: { category } })
    resources.value = res.data || []
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
  if (!props.resources || props.resources.length === 0) {
    fetchResources()
  }
})
</script>

<style scoped>
.res-tabs {
  margin-bottom: 10px;
}
.res-card .thumb {
  position: relative;
  height: 140px;
  border-radius: 6px;
  overflow: hidden;
  transition: transform .3s;
}
.res-card img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform .3s;
}
.res-card .play-icon {
  position: absolute;
  right: 8px;
  bottom: 8px;
  background: rgba(170, 29, 29, 0.85);
  color: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}
.res-card .title {
  margin-top: 8px;
  color: #333;
  font-size: 14px;
  font-weight: 500;
}
.res-card:hover {
  box-shadow: 0 12px 24px rgba(170, 29, 29, .12);
}
.res-card:hover img {
  transform: scale(1.05);
}
</style>
