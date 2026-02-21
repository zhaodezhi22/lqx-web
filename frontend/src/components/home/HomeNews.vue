<template>
  <div class="home-news">
    <el-row :gutter="20">
      <el-col :span="14">
        <el-card shadow="never" class="news-carousel">
          <template #header>
            <div class="card-header">
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
      <el-col :span="10">
        <el-card shadow="never" class="news-list">
          <template #header>
            <div class="card-header">
              <h3>公告与通知</h3>
            </div>
          </template>
          <el-scrollbar height="240px">
            <ul v-if="notices.length > 0">
              <li v-for="(n, i) in notices" :key="i">
                <a href="javascript:void(0)" @click="viewNotice(n)">{{ n.title }}</a>
              </li>
            </ul>
            <div v-else class="no-data">暂无公告</div>
          </el-scrollbar>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../utils/request'
import { useRouter } from 'vue-router'

const router = useRouter()
const carousel = ref([])
const notices = ref([])

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
  if (item.linkUrl) {
    if (item.linkUrl.startsWith('http')) {
      window.open(item.linkUrl, '_blank')
    } else {
      router.push(item.linkUrl)
    }
  } else if (item.id) {
    // Navigate to detail page
    router.push(`/content/${item.id}`)
  }
}

const viewNotice = (n) => {
  if (n.linkUrl) {
    if (n.linkUrl.startsWith('http')) {
      window.open(n.linkUrl, '_blank')
    } else {
      router.push(n.linkUrl)
    }
  } else if (n.id) {
    // Navigate to detail page
    router.push(`/content/${n.id}`)
  }
}

onMounted(fetchNews)
</script>

<style scoped>
.home-news .card-header h3 {
  margin: 0;
  color: #333;
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
  background: rgba(170, 29, 29, 0.7);
  color: #fff;
  padding: 10px 15px;
}
.news-carousel .carousel-item:hover img {
  transform: scale(1.05);
}
.news-list ul {
  list-style: none;
  margin: 0;
  padding: 0 10px;
}
.news-list li {
  margin: 10px 0;
}
.news-list a {
  color: #333;
  text-decoration: none;
}
.news-list a:hover {
  color: #AA1D1D;
}
.no-data {
  text-align: center;
  color: #999;
  padding: 20px;
}
</style>
