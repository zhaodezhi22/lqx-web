<template>
  <div class="page">
    <div class="filters">
      <el-select v-model="category" placeholder="按分类筛选" clearable @change="filterList">
        <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
      </el-select>
    </div>
    <div class="grid">
      <div class="card" v-for="item in filtered" :key="item.resourceId" @click="goDetail(item.resourceId)">
        <img class="cover" :src="item.coverImg || placeholder" alt="cover" />
        <div class="title">{{ item.title }}</div>
        <div class="meta">{{ item.category || '未分类' }} · 浏览 {{ item.viewCount || 0 }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const list = ref([])
const category = ref()
const placeholder = 'https://via.placeholder.com/400x220?text=Heritage'

const categories = computed(() => {
  const set = new Set()
  list.value.forEach((i) => i.category && set.add(i.category))
  return Array.from(set)
})

const filtered = ref([])

const fetchList = async () => {
  try {
    const res = await request.get('/resources')
    list.value = res.data || []
    filterList()
  } catch (e) {
    ElMessage.error('加载资源失败')
  }
}

const filterList = () => {
  if (!category.value) {
    filtered.value = list.value
  } else {
    filtered.value = list.value.filter((i) => i.category === category.value)
  }
}

const goDetail = (id) => {
  router.push({ name: 'ResourceDetail', params: { id } })
}

onMounted(fetchList)
</script>

<style scoped>
.page {
  padding: 16px;
}
.filters {
  margin-bottom: 12px;
}
.grid {
  column-count: 4;
  column-gap: 16px;
}
@media (max-width: 1200px) {
  .grid { column-count: 3; }
}
@media (max-width: 768px) {
  .grid { column-count: 2; }
}
@media (max-width: 480px) {
  .grid { column-count: 1; }
}

.card {
  break-inside: avoid;
  margin-bottom: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s;
}
.card:hover {
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}
.cover {
  width: 100%;
  height: 140px;
  object-fit: cover;
  background: #f5f7fa;
}
.title {
  font-weight: 600;
  padding: 8px 10px 4px;
}
.meta {
  color: #909399;
  font-size: 12px;
  padding: 0 10px 10px;
}
</style>

