<template>
  <div class="page">
    <div class="header">
      <el-input 
        v-model="keyword" 
        placeholder="搜索商品、演出、资源、传承人..." 
        class="kw" 
        :prefix-icon="Search"
        clearable
        @keyup.enter="doSearch" 
      />
      <el-button type="primary" @click="doSearch">搜索</el-button>
    </div>

    <div v-loading="loading" class="result-container">
      <el-tabs v-model="activeTab" class="search-tabs">
        <el-tab-pane label="全部" name="all">
            <template #label>
                <span>全部 <el-tag size="small" type="info" round>{{ results.length }}</el-tag></span>
            </template>
        </el-tab-pane>
        <el-tab-pane label="戏曲资源" name="resource">
             <template #label>
                <span>戏曲资源 <el-tag size="small" type="info" round>{{ countByType['resource'] || 0 }}</el-tag></span>
            </template>
        </el-tab-pane>
        <el-tab-pane label="文创商品" name="product">
             <template #label>
                <span>文创商品 <el-tag size="small" type="info" round>{{ countByType['product'] || 0 }}</el-tag></span>
            </template>
        </el-tab-pane>
        <el-tab-pane label="演出活动" name="event">
             <template #label>
                <span>演出活动 <el-tag size="small" type="info" round>{{ countByType['event'] || 0 }}</el-tag></span>
            </template>
        </el-tab-pane>
        <el-tab-pane label="传承人" name="inheritor">
             <template #label>
                <span>传承人 <el-tag size="small" type="info" round>{{ countByType['inheritor'] || 0 }}</el-tag></span>
            </template>
        </el-tab-pane>
      </el-tabs>

      <div v-if="filteredResults.length === 0" class="empty">
        <el-empty description="未找到相关内容" />
      </div>

      <el-row :gutter="20" v-else>
        <el-col v-for="item in filteredResults" :key="item.id + '-' + item.type" :xs="24" :sm="12" :md="8" :lg="6">
          <el-card class="result-card" shadow="hover" :body-style="{ padding: '0px' }" @click="openItem(item)">
            <div class="card-image-wrapper">
                <img class="cover" :src="item.cover || placeholder(item.type)" alt="cover" />
                <div class="type-badge" :class="item.type">
                    {{ typeLabel(item.type) }}
                </div>
            </div>
            <div class="info">
              <div class="title" :title="item.title">{{ item.title }}</div>
              <div class="subtitle" :title="item.subTitle">{{ item.subTitle || '暂无描述' }}</div>
              <div class="meta">
                <span v-if="item.price != null" class="price">¥ {{ item.price }}</span>
                <span v-else class="free-tag">免费</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()
const keyword = ref('')
const results = ref([])
const loading = ref(false)
const activeTab = ref('all')

const countByType = computed(() => {
    const counts = {}
    results.value.forEach(item => {
        counts[item.type] = (counts[item.type] || 0) + 1
    })
    return counts
})

const filteredResults = computed(() => {
    if (activeTab.value === 'all') {
        return results.value
    }
    return results.value.filter(item => item.type === activeTab.value)
})

const placeholder = (type) => {
  if (type === 'product') return 'https://via.placeholder.com/300x200?text=Product'
  if (type === 'event') return 'https://via.placeholder.com/300x200?text=Event'
  if (type === 'inheritor') return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  return 'https://via.placeholder.com/300x200?text=Resource'
}

const typeLabel = (t) => {
  if (t === 'product') return '商品'
  if (t === 'event') return '活动'
  if (t === 'inheritor') return '传承人'
  return '资源'
}

const doSearch = async () => {
  const q = keyword.value && keyword.value.trim()
  if (!q) {
    ElMessage.warning('请输入关键词')
    return
  }
  
  // Update URL query
  router.replace({ query: { keyword: q } })
  
  loading.value = true
  try {
    const res = await request.get('/common/search', { params: { keyword: q } })
    if (res.code === 200) {
      results.value = res.data || []
    } else {
      results.value = []
    }
  } catch (e) {
    results.value = []
    ElMessage.error('搜索服务暂不可用')
  } finally {
    loading.value = false
  }
}

const openItem = (item) => {
  if (item.type === 'resource') {
    router.push(`/resources/${item.originalId}`)
  } else if (item.type === 'event') {
    router.push(`/events/${item.originalId}`)
  } else if (item.type === 'product') {
    // Mall usually uses query param or hash for detail
    // Assuming product detail is a modal or separate page
    // Based on previous code: ProductMall uses modal. 
    // Let's redirect to mall and maybe trigger detail if possible, or just go to mall.
    // If we have a dedicated product detail page, better.
    // But currently ProductDetailModal is used.
    // We can pass productId in query and handle it in ProductMall.vue
    router.push({ name: 'ProductMall', query: { productId: item.originalId } })
  } else if (item.type === 'inheritor') {
    router.push(`/user/${item.originalId}`)
  }
}

onMounted(() => {
  const q = route.query.keyword
  if (q) {
    keyword.value = q
    doSearch()
  }
})

// Watch route query changes (e.g. searching from header in another page)
watch(() => route.query.keyword, (newVal) => {
    if (newVal && newVal !== keyword.value) {
        keyword.value = newVal
        doSearch()
    }
})
</script>

<style scoped>
.page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  min-height: 80vh;
}
.header {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
  gap: 10px;
}
.kw {
  max-width: 500px;
}
.search-tabs {
    margin-bottom: 20px;
}
.result-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}
.result-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.1);
}
.card-image-wrapper {
    position: relative;
    width: 100%;
    height: 180px;
    overflow: hidden;
}
.cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.result-card:hover .cover {
    transform: scale(1.05);
}
.type-badge {
    position: absolute;
    top: 10px;
    right: 10px;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 12px;
    color: #fff;
    font-weight: bold;
    box-shadow: 0 2px 4px rgba(0,0,0,0.2);
}
.type-badge.product { background-color: #F56C6C; }
.type-badge.event { background-color: #E6A23C; }
.type-badge.resource { background-color: #409EFF; }
.type-badge.inheritor { background-color: #67C23A; }

.info {
  padding: 12px;
}
.title {
  font-weight: bold;
  font-size: 16px;
  color: #333;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.subtitle {
  font-size: 13px;
  color: #999;
  margin-bottom: 10px;
  height: 36px; /* 2 lines */
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.price {
  color: #F56C6C;
  font-weight: bold;
  font-size: 16px;
}
.free-tag {
    color: #67C23A;
    font-size: 14px;
}
</style>
