<template>
  <div class="home-page">
    <!-- Hero Section -->
    <div class="hero-section">
      <el-carousel trigger="click" height="500px" :interval="5000" arrow="always">
        <el-carousel-item v-for="(item, index) in carouselItems" :key="index">
          <div class="carousel-content" :style="{ backgroundImage: `url(${item.image})` }">
            <div class="carousel-overlay">
              <h1 class="carousel-title">{{ item.title }}</h1>
              <p class="carousel-subtitle">{{ item.subtitle }}</p>
              <el-button type="primary" class="carousel-btn" @click="handleCarouselClick(item.link)">
                了解更多
              </el-button>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- Quick Navigation / Portal Entry -->
    <div class="portal-section">
      <div class="section-header">
        <span class="section-title-decoration"></span>
        <h2 class="section-title">探索非遗</h2>
        <span class="section-title-decoration"></span>
      </div>
      
      <el-row :gutter="30" justify="center">
        <el-col :span="6" :xs="24" :sm="12" :md="6">
          <div class="portal-card" @click="router.push('/resources')">
            <div class="card-icon">
              <el-icon><VideoPlay /></el-icon>
            </div>
            <h3>戏曲资源</h3>
            <p>经典剧目、唱段欣赏</p>
          </div>
        </el-col>
        
        <el-col :span="6" :xs="24" :sm="12" :md="6">
          <div class="portal-card" @click="router.push('/products')">
            <div class="card-icon">
              <el-icon><Goods /></el-icon>
            </div>
            <h3>文创商城</h3>
            <p>特色周边、传统工艺</p>
          </div>
        </el-col>

        <el-col :span="6" :xs="24" :sm="12" :md="6">
          <div class="portal-card" @click="router.push('/inheritor/apply')">
            <div class="card-icon">
              <el-icon><User /></el-icon>
            </div>
            <h3>传承人申请</h3>
            <p>加入我们，传承文化</p>
          </div>
        </el-col>

        <el-col :span="6" :xs="24" :sm="12" :md="6" v-if="role >= 2">
          <div class="portal-card admin-card" @click="router.push('/admin/inheritor-review')">
            <div class="card-icon">
              <el-icon><Setting /></el-icon>
            </div>
            <h3>后台管理</h3>
            <p>系统维护与审核</p>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- Section A: News & Announcements -->
    <home-news />

    <!-- Section B: Inheritor Spotlight -->
    <home-inheritors :items="featuredInheritors" />

    <!-- Section C: Heritage Resources -->
    <home-resources :resources="featuredResources" />

    <!-- Section D: Performance & Tickets -->
    <home-events :events="upcomingEvents" />

    <!-- Section E: Cultural Creative Mall -->
    <home-products :products="hotProducts" />

    <!-- Latest Updates / Showcase (Mock) -->
    <div class="showcase-section">
      <div class="section-header">
        <span class="section-title-decoration"></span>
        <h2 class="section-title">非遗风采</h2>
        <span class="section-title-decoration"></span>
      </div>
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="showcase-big">
             <div class="showcase-content">
               <h3>柳琴戏的历史渊源</h3>
               <p>柳琴戏，又称“拉魂腔”，国家级非物质文化遗产...</p>
             </div>
          </div>
        </el-col>
        <el-col :span="12">
          <el-row :gutter="20">
            <el-col :span="24" style="margin-bottom: 20px;">
              <div class="showcase-small">
                 <h3>名家名段欣赏</h3>
              </div>
            </el-col>
            <el-col :span="24">
              <div class="showcase-small">
                 <h3>近期演出预告</h3>
              </div>
            </el-col>
          </el-row>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { VideoPlay, Goods, User, Setting } from '@element-plus/icons-vue'
import HomeNews from '../components/home/HomeNews.vue'
import HomeInheritors from '../components/home/HomeInheritors.vue'
import HomeResources from '../components/home/HomeResources.vue'
import HomeEvents from '../components/home/HomeEvents.vue'
import HomeProducts from '../components/home/HomeProducts.vue'
import request from '../utils/request'

const router = useRouter()
const role = ref(0)
const featuredResources = ref([])
const featuredInheritors = ref([])
const hotProducts = ref([])
const upcomingEvents = ref([])

const carouselItems = [
  {
    title: '临沂柳琴戏',
    subtitle: '国家级非物质文化遗产 | 听拉魂腔，品沂蒙情',
    image: 'https://images.unsplash.com/photo-1514533450685-4493e01d1fdc?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80', // Placeholder: Traditional Performance
    link: '/resources'
  },
  {
    title: '非遗文创周边',
    subtitle: '传统与现代的碰撞 | 把文化带回家',
    image: 'https://images.unsplash.com/photo-1593118845063-8896024107f7?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80', // Placeholder: Crafts
    link: '/products'
  },
  {
    title: '寻找传承人',
    subtitle: '薪火相传，生生不息 | 期待您的加入',
    image: 'https://images.unsplash.com/photo-1531853123282-359f13c63907?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80', // Placeholder: Artist
    link: '/inheritor/apply'
  }
]

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      role.value = user.role
    } catch (e) {
      // ignore
    }
  }
})

const handleCarouselClick = (link) => {
  router.push(link)
}

const fillMocksIfEmpty = () => {
  if (!featuredResources.value.length) {
    featuredResources.value = [
      { resourceId: 1, title: 'Liuqin Opera: The Soul of Linyi', coverImg: 'https://placeholder.co/400x300' },
      { resourceId: 2, title: 'Classic Replay: 拉魂腔精选', coverImg: 'https://placeholder.co/400x300' },
      { resourceId: 3, title: 'Teaching Video: 唱腔入门', coverImg: 'https://placeholder.co/400x300' },
      { resourceId: 4, title: 'Script: 传统剧本合集', coverImg: 'https://placeholder.co/400x300' },
      { resourceId: 5, title: 'Stage Highlights', coverImg: 'https://placeholder.co/400x300' },
      { resourceId: 6, title: '幕后花絮', coverImg: 'https://placeholder.co/400x300' },
    ]
  }
  if (!featuredInheritors.value.length) {
    featuredInheritors.value = [
      { userId: 101, name: '张大师', avatar: 'https://placeholder.co/200x200', level: '国家级传承人' },
      { userId: 102, name: '李老师', avatar: 'https://placeholder.co/200x200', level: '省级传承人' },
      { userId: 103, name: '王前辈', avatar: 'https://placeholder.co/200x200', level: '市级传承人' },
      { userId: 104, name: '赵艺术家', avatar: 'https://placeholder.co/200x200', level: '国家级传承人' },
    ]
  }
  if (!hotProducts.value.length) {
    hotProducts.value = [
      { productId: 201, name: '柳琴戏主题手工本', price: 39, mainImage: 'https://placeholder.co/300x300' },
      { productId: 202, name: '非遗纪念徽章', price: 59, mainImage: 'https://placeholder.co/300x300' },
      { productId: 203, name: '戏曲口琴', price: 99, mainImage: 'https://placeholder.co/300x300' },
      { productId: 204, name: '戏服元素周边', price: 129, mainImage: 'https://placeholder.co/300x300' },
      { productId: 205, name: '曲谱精选集', price: 49, mainImage: 'https://placeholder.co/300x300' },
    ]
  }
  if (!upcomingEvents.value.length) {
    upcomingEvents.value = [
      { eventId: 301, title: '新春演出专场', venue: '临沂大剧院', showTime: new Date().toISOString(), status: 1 },
      { eventId: 302, title: '经典剧目再现', venue: '市文化中心', showTime: new Date(Date.now()+86400000).toISOString(), status: 1 },
      { eventId: 303, title: '传承教学示范', venue: '艺术馆小剧场', showTime: new Date(Date.now()+2*86400000).toISOString(), status: 1 },
    ]
  }
}

const fetchFeatured = async () => {
  try {
    const [resRes, resInh, resProd, resEvt] = await Promise.all([
      request.get('/resources/featured', { params: { limit: 6 } }),
      request.get('/inheritor/featured', { params: { limit: 4 } }),
      request.get('/products/hot', { params: { limit: 5 } }),
      request.get('/ticket/upcoming', { params: { limit: 3 } }),
    ])
    featuredResources.value = resRes.data || []
    featuredInheritors.value = resInh.data || []
    hotProducts.value = resProd.data || []
    upcomingEvents.value = resEvt.data || []
  } catch (e) {
    // swallow errors and use mocks
  } finally {
    fillMocksIfEmpty()
  }
}

onMounted(fetchFeatured)
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;700&display=swap');

.home-page {
  /* Padding is handled by layout */
}

/* Hero Section */
.hero-section {
  margin-bottom: 60px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
}

.carousel-content {
  height: 100%;
  background-size: cover;
  background-position: center;
  position: relative;
}

.carousel-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.4); /* Dark overlay */
}

.carousel-overlay {
  position: absolute;
  top: 50%;
  left: 10%;
  transform: translateY(-50%);
  color: #fff;
  max-width: 600px;
}

.carousel-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 48px;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.5);
}

.carousel-subtitle {
  font-size: 20px;
  margin-bottom: 30px;
  opacity: 0.9;
}

.carousel-btn {
  background-color: #AA1D1D;
  border-color: #AA1D1D;
  padding: 12px 30px;
  font-size: 16px;
}

.carousel-btn:hover {
  background-color: #c0392b;
  border-color: #c0392b;
}

/* Section Headers */
.section-header {
  text-align: center;
  margin-bottom: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.section-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 32px;
  color: #333;
  margin: 0 20px;
}

.section-title-decoration {
  display: inline-block;
  width: 50px;
  height: 2px;
  background-color: #AA1D1D;
  position: relative;
}

.section-title-decoration::before {
  content: '';
  position: absolute;
  width: 8px;
  height: 8px;
  background-color: #AA1D1D;
  transform: rotate(45deg);
  top: -3px;
}
/* Left decoration diamond */
.section-header .section-title-decoration:first-child::before {
  right: 0;
}
/* Right decoration diamond */
.section-header .section-title-decoration:last-child::before {
  left: 0;
}

/* Portal Cards */
.portal-section {
  margin-bottom: 60px;
}

.portal-card {
  background: #fff;
  padding: 30px;
  text-align: center;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e0e0e0;
  height: 100%;
  box-sizing: border-box;
}

.portal-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 15px 30px rgba(170, 29, 29, 0.1);
  border-color: #AA1D1D;
}

.card-icon {
  font-size: 40px;
  color: #AA1D1D;
  margin-bottom: 20px;
}

.portal-card h3 {
  font-family: 'Noto Serif SC', serif;
  font-size: 20px;
  margin-bottom: 10px;
  color: #333;
}

.portal-card p {
  color: #666;
  font-size: 14px;
}

.admin-card {
  background-color: #fdf6f6;
  border-color: #f5c6cb;
}

/* Showcase Section */
.showcase-section {
  margin-bottom: 40px;
}

.showcase-big, .showcase-small {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  height: 300px; /* Fixed height for demo */
  background-size: cover;
  background-position: center;
  position: relative;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0,0,0,0.05);
  display: flex;
  align-items: flex-end;
}

.showcase-big {
  background-image: url('https://images.unsplash.com/photo-1535025639604-9a804c092faa?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80');
}

.showcase-small {
  height: 140px;
  background-color: #AA1D1D; /* Fallback */
}

.showcase-small:first-child {
  background-image: url('https://images.unsplash.com/photo-1516280440614-6697288d5d38?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80');
}

.showcase-small:nth-child(2) {
  background-image: url('https://images.unsplash.com/photo-1519751138087-5bf79df62d5b?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80');
}

.showcase-content {
  background: rgba(255, 255, 255, 0.9);
  padding: 20px;
  width: 100%;
}

.showcase-content h3 {
  margin: 0 0 10px 0;
  color: #AA1D1D;
  font-family: 'Noto Serif SC', serif;
}

.showcase-small h3 {
  background: rgba(170, 29, 29, 0.8);
  color: #fff;
  padding: 10px 20px;
  margin: 0;
  width: 100%;
  font-size: 16px;
  text-align: center;
}

/* Responsive */
@media (max-width: 768px) {
  .carousel-title {
    font-size: 32px;
  }
  .header-content {
    flex-direction: column;
    height: auto;
    padding: 10px;
  }
}
</style>
