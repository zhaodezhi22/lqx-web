<template>
  <div class="page">
    <div class="filters">
      <el-select v-model="category" placeholder="按分类筛选" clearable @change="handleFilterChange">
        <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
      </el-select>
    </div>
    
    <div v-loading="loading">
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

const router = useRouter()
const list = ref([])
const category = ref('')
const placeholder = 'https://via.placeholder.com/400x220?text=Heritage'
const loading = ref(false)

const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// Hardcoded or fetched categories. Since we paginate, we can't derive all categories from full list easily.
// Better to hardcode or fetch distinct categories API.
const categories = ref(['柳琴戏历史', '经典剧目', '名家唱段', '脸谱艺术', '服饰道具', '其他'])

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
    fetchList()
}

const handlePageChange = (val) => {
    currentPage.value = val
    fetchList()
}

const goDetail = (id) => {
  router.push(`/resources/${id}`)
}

onMounted(fetchList)
</script>

<style scoped>
.page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.filters {
  margin-bottom: 20px;
  text-align: right;
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
.cover {
  width: 100%;
  display: block;
  min-height: 140px;
  object-fit: cover;
  background: #f5f7fa;
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

