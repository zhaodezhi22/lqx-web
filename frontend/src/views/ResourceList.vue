<template>
  <div class="page">
    <PageFilterBar>
      <el-input
        v-model="keyword"
        placeholder="搜索资源标题"
        clearable
        class="filter-item keyword-input"
        @keyup.enter="handleFilterChange"
      />
      <el-select v-model="category" placeholder="按分类筛选" clearable class="filter-item" @change="handleFilterChange">
        <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
      </el-select>
      <el-select v-model="type" placeholder="按类型筛选" clearable class="filter-item" @change="handleFilterChange">
        <el-option v-for="item in RESOURCE_TYPE_OPTIONS.filter(option => option.value !== '')" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button type="primary" @click="handleFilterChange">筛选</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </PageFilterBar>
    
    <div v-if="loading" class="grid">
        <div class="card skeleton-card" v-for="item in 8" :key="`resource-skeleton-${item}`">
            <el-skeleton animated>
              <template #template>
                <div class="skeleton-cover"></div>
                <div class="info-box">
                  <el-skeleton-item variant="h3" style="width: 70%" />
                  <el-skeleton-item variant="text" style="width: 55%; margin-top: 10px" />
                </div>
              </template>
            </el-skeleton>
        </div>
    </div>
    <div v-else>
        <div class="grid" v-if="list.length > 0">
            <div class="card" v-for="item in list" :key="item.resourceId" @click="goDetail(item.resourceId)">
                <img class="cover" :src="item.coverImg || placeholder" alt="cover" />
                <div class="info-box">
                    <div class="title">{{ item.title }}</div>
                    <div class="meta">{{ item.category || '未分类' }} · 浏览 {{ item.viewCount || 0 }}</div>
                </div>
            </div>
        </div>
        <el-empty v-else description="暂无资源" />
    </div>

    <!-- Pagination -->
    <div class="pagination-container" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="prev, pager, next, total"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { RESOURCE_TYPE_OPTIONS } from '../constants/dicts'
import PageFilterBar from '../components/common/PageFilterBar.vue'
import { clearRememberedState, loadRememberedState, saveRememberedState } from '../utils/viewState'

const router = useRouter()
const list = ref([])
const rememberedState = loadRememberedState('resource-list-filters', {
  keyword: '',
  category: '',
  type: ''
})
const category = ref(rememberedState.category)
const keyword = ref(rememberedState.keyword)
const type = ref(rememberedState.type)
const placeholder = 'https://via.placeholder.com/400x220?text=Heritage'
const loading = ref(false)

const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const categories = ref([])

const fetchCategories = async () => {
  try {
    const res = await request.get('/resource-categories')
    categories.value = (res.data || []).map(item => item.name)
  } catch (e) {
    categories.value = []
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = {
        page: currentPage.value,
        size: pageSize.value
    }
    if (category.value) {
        params.category = category.value
    }
    if (keyword.value.trim()) {
        params.keyword = keyword.value.trim()
    }
    if (type.value !== '' && type.value !== null && type.value !== undefined) {
        params.type = type.value
    }
    
    const res = await request.get('/resources', { params })
    if (res.code === 200) {
        list.value = res.data?.records || []
        total.value = res.data?.total || 0
    }
  } catch (e) {
    ElMessage.error('加载资源失败')
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
    currentPage.value = 1
    saveRememberedState('resource-list-filters', {
      keyword: keyword.value,
      category: category.value,
      type: type.value
    })
    fetchList()
}

const resetFilters = () => {
    keyword.value = ''
    category.value = ''
    type.value = ''
    clearRememberedState('resource-list-filters')
    handleFilterChange()
}

const handlePageChange = (val) => {
    currentPage.value = val
    fetchList()
}

const goDetail = (id) => {
  router.push(`/resources/${id}`)
}

onMounted(() => {
  fetchCategories()
  fetchList()
})
</script>

<style scoped>
.page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.filter-item {
  width: 180px;
}
.keyword-input {
  width: 240px;
}
.grid {
  column-count: 4;
  column-gap: 20px;
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
  margin-bottom: 20px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}
.card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-4px);
}
.skeleton-card {
  cursor: default;
}
.cover {
  width: 100%;
  display: block;
  min-height: 140px;
  object-fit: cover;
  background: #f5f7fa;
}
.skeleton-cover {
  width: 100%;
  min-height: 140px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ebeef5 100%);
}
.info-box {
    padding: 12px;
}
.title {
  font-weight: 600;
  font-size: 16px;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.4;
}
.meta {
  color: #909399;
  font-size: 13px;
}
.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>

