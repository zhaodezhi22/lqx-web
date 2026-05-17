<template>
  <div class="home-news">
    <SectionTitle text="资讯公告" />
    <el-row :gutter="20" class="news-grid">
      <el-col :xs="24" :md="14" class="news-col">
        <el-card shadow="never" class="news-panel news-carousel">
          <template #header>
            <div class="card-header">
              <span class="header-tag">资讯</span>
              <h3>最新戏曲资讯</h3>
            </div>
          </template>
          <el-carousel height="240px" indicator-position="outside" v-if="carousel.length > 0">
            <el-carousel-item v-for="(item, idx) in carousel" :key="idx">
              <div class="carousel-item" @click="handleNewsClick(item)">
                <img :src="item.imageUrl" alt="" />
                <div class="overlay">
                  <h4>{{ item.title }}</h4>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
          <div v-else class="no-data">暂无资讯</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="10" class="news-col">
        <el-card shadow="never" class="news-panel news-list">
          <template #header>
            <div class="card-header">
              <span class="header-tag">公告</span>
              <h3>公告与通知</h3>
            </div>
          </template>
          <el-scrollbar height="240px">
            <ul v-if="notices.length > 0">
              <li v-for="(n, i) in notices" :key="i">
                <a href="javascript:void(0)" @click="viewNotice(n)">
                  <span class="notice-dot"></span>
                  <span class="notice-text">{{ n.title }}</span>
                </a>
              </li>
            </ul>
            <div v-else class="no-data">暂无公告</div>
          </el-scrollbar>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
      v-model="detailVisible"
      width="680px"
      destroy-on-close
      :title="selectedContent.title || '内容详情'"
    >
      <div class="news-detail" v-if="selectedContent.title || selectedContent.content || selectedContent.imageUrl">
        <el-image
          v-if="selectedContent.imageUrl"
          :src="selectedContent.imageUrl"
          fit="cover"
          class="news-detail-image"
        />
        <div class="news-detail-text">
          {{ selectedContent.content || '暂无详细内容' }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../utils/request'
import SectionTitle from '../common/SectionTitle.vue'

const carousel = ref([])
const notices = ref([])
const detailVisible = ref(false)
const selectedContent = ref({})

const fetchNews = async () => {
  try {
    const [resNews, resNotices] = await Promise.all([
      request.get('/home/content', { params: { type: 'NEWS_CAROUSEL' } }),
      request.get('/home/content', { params: { type: 'NOTICE' } })
    ])
    carousel.value = resNews.data || []
    notices.value = resNotices.data || []
  } catch (e) {
    // ignore
  } finally {
    // Fill defaults if empty
    if (carousel.value.length === 0) {
      carousel.value = [
        { title: '临沂柳琴戏精品剧目展演', imageUrl: 'https://images.unsplash.com/photo-1514533450685-4493e01d1fdc?ixlib=rb-4.0.3&auto=format&fit=crop&w=1200&q=80' },
        { title: '非遗进校园活动周', imageUrl: 'https://images.unsplash.com/photo-1593118845063-8896024107f7?ixlib=rb-4.0.3&auto=format&fit=crop&w=1200&q=80' },
        { title: '传承人教学公开课', imageUrl: 'https://images.unsplash.com/photo-1535025639604-9a804c092faa?ixlib=rb-4.0.3&auto=format&fit=crop&w=1200&q=80' },
      ]
    }
    if (notices.value.length === 0) {
      notices.value = [
        { title: '关于新一轮传承人遴选的通知' },
        { title: '近期演出排期及购票须知' },
        { title: '资源库上线“经典重温”专题' },
        { title: '文化节志愿者招募公告' },
      ]
    }
  }
}

const handleNewsClick = (item) => {
  selectedContent.value = {
    title: item.title || '',
    content: item.content || '',
    imageUrl: item.imageUrl || ''
  }
  detailVisible.value = true
}

const viewNotice = (n) => {
  selectedContent.value = {
    title: n.title || '',
    content: n.content || '',
    imageUrl: n.imageUrl || ''
  }
  detailVisible.value = true
}

onMounted(fetchNews)
</script>

<style scoped>
.home-news {
  height: 100%;
}

.news-grid {
  align-items: stretch;
}

.news-col {
  display: flex;
}

.news-panel {
  width: 100%;
  border: 1px solid #efe0cf;
  border-radius: 22px;
  box-shadow: 0 14px 28px rgba(40, 24, 18, 0.08);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(255, 248, 241, 0.96));
  overflow: hidden;
}

.news-panel :deep(.el-card__header) {
  padding: 20px 22px 16px;
  border-bottom: 1px solid #f2e7db;
  background: linear-gradient(180deg, rgba(255, 252, 247, 0.9), rgba(255, 250, 245, 0.75));
}

.news-panel :deep(.el-card__body) {
  padding: 0;
}

.home-news .card-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.home-news .card-header h3 {
  margin: 0;
  color: #2f241f;
  font-size: 18px;
  letter-spacing: 1px;
}

.header-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(170, 29, 29, 0.10), rgba(214, 174, 96, 0.16));
  color: #8f231e;
  font-size: 12px;
  font-weight: 700;
}

.news-carousel .carousel-item {
  position: relative;
  width: 100%;
  height: 240px;
  overflow: hidden;
  cursor: pointer;
}
.news-carousel img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform .3s;
}
.news-carousel .overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background:
    linear-gradient(180deg, rgba(15, 15, 15, 0), rgba(15, 15, 15, 0.72)),
    linear-gradient(135deg, rgba(170, 29, 29, 0.22), rgba(214, 174, 96, 0.10));
  color: #fff;
  padding: 22px 20px 18px;
}

.news-carousel .overlay h4 {
  margin: 0;
  font-size: 20px;
  line-height: 1.5;
  letter-spacing: 0.5px;
}

.news-carousel .carousel-item:hover img {
  transform: scale(1.05);
}

.news-carousel :deep(.el-carousel__button) {
  width: 22px;
  height: 4px;
  border-radius: 999px;
  background: rgba(170, 29, 29, 0.28);
}

.news-carousel :deep(.el-carousel__indicator.is-active .el-carousel__button) {
  background: #aa1d1d;
}

.news-list ul {
  list-style: none;
  margin: 0;
  padding: 10px 18px 14px;
}

.news-list li {
  margin: 0;
  border-bottom: 1px solid #f4ebe2;
}

.news-list a {
  color: #3b2d26;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 6px;
  transition: color 0.2s ease, transform 0.2s ease, background 0.2s ease;
  border-radius: 12px;
}

.news-list a:hover {
  color: #AA1D1D;
  transform: translateX(4px);
  background: rgba(170, 29, 29, 0.04);
}

.notice-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: linear-gradient(135deg, #aa1d1d, #d6ae60);
  flex-shrink: 0;
  box-shadow: 0 0 0 4px rgba(170, 29, 29, 0.08);
}

.notice-text {
  line-height: 1.6;
  font-size: 14px;
}

.no-data {
  text-align: center;
  color: #8b6b56;
  padding: 32px 20px;
}

.news-detail-image {
  width: 100%;
  height: 260px;
  border-radius: 14px;
  margin-bottom: 18px;
}

.news-detail-text {
  color: #333;
  font-size: 15px;
  line-height: 1.9;
  white-space: pre-wrap;
}

@media (max-width: 992px) {
  .news-col + .news-col {
    margin-top: 20px;
  }
}
</style>
