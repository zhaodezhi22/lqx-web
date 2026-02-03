<template>
  <div class="home-page">
    <!-- Hero Section -->
    <div class="hero-section">
      <el-carousel trigger="click" height="500px" :interval="5000" arrow="always" v-if="carouselItems.length > 0">
        <el-carousel-item v-for="(item, index) in carouselItems" :key="index">
          <div class="carousel-item-content" @click="handleCarouselClick(item)">
            <img :src="item.imageUrl" alt="Carousel Image" class="carousel-image" />
            <div class="carousel-caption" v-if="item.title">
              <h3>{{ item.title }}</h3>
              <p>{{ item.subtitle }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
      <!-- Fallback if no carousel items -->
      <div v-else class="hero-section-placeholder" style="height: 500px; background: #eee; display: flex; align-items: center; justify-content: center;">
        <p>暂无轮播图配置</p>
      </div>
    </div>

    <!-- Quick Navigation / Portal Entry Removed -->
    
    <!-- Section A: Inheritor Spotlight -->
    <home-inheritors :items="featuredInheritors" />

    <!-- Section B: Heritage Resources (Online Theater) -->
    <home-resources :resources="featuredResources" />

    <!-- Section C: Performance & Tickets -->
    <home-events :events="upcomingEvents" />

    <!-- Section D: Cultural Creative Mall -->
    <home-products :products="hotProducts" style="margin-bottom: 80px;" />

    <!-- Section E: News & Announcements -->
    <home-news />

    <!-- Latest Updates / Showcase (Highlights) -->
    <div class="showcase-section" v-if="homeHighlights.length > 0">
      <div class="section-header">
        <span class="section-title-decoration"></span>
        <h2 class="section-title">非遗风采</h2>
        <span class="section-title-decoration"></span>
      </div>
      <el-row :gutter="20">
        <el-col :span="12" v-if="homeHighlights[0]">
          <div class="showcase-big" :style="{ backgroundImage: `url(${homeHighlights[0].imageUrl})` }" @click="handleHighlightClick(homeHighlights[0])">
             <div class="showcase-content">
               <h3>{{ homeHighlights[0].title }}</h3>
               <p>{{ homeHighlights[0].content }}</p>
             </div>
          </div>
        </el-col>
        <el-col :span="12">
          <el-row :gutter="20">
            <el-col :span="24" style="margin-bottom: 20px;" v-if="homeHighlights[1]">
              <div class="showcase-small" :style="{ backgroundImage: `url(${homeHighlights[1].imageUrl})` }" @click="handleHighlightClick(homeHighlights[1])">
                 <h3>{{ homeHighlights[1].title }}</h3>
              </div>
            </el-col>
            <el-col :span="24" v-if="homeHighlights[2]">
              <div class="showcase-small" :style="{ backgroundImage: `url(${homeHighlights[2].imageUrl})` }" @click="handleHighlightClick(homeHighlights[2])">
                 <h3>{{ homeHighlights[2].title }}</h3>
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
const carouselItems = ref([])
const homeHighlights = ref([])

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

const handleHighlightClick = (item) => {
  const link = item.linkUrl
  if (link && link.startsWith('http')) {
    window.open(link, '_blank')
  } else if (link) {
    router.push(link)
  } else if (item.id) {
    // Navigate to detail page if no explicit link
    router.push(`/content/${item.id}`)
  }
}

const handleCarouselClick = (item) => {
  const link = item.linkUrl
  if (link && link.startsWith('http')) {
    window.open(link, '_blank')
  } else if (link) {
    router.push(link)
  } else if (item.id) {
    // Navigate to detail page if no explicit link
    router.push(`/content/${item.id}`)
  }
}

const fillMocksIfEmpty = () => {
  // Only fill business entities mocks if API fails, but for Home Content (Carousel/Highlight), we prefer empty or fallback if needed.
  // Here we keep business entities mocks logic as is.
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
    const [resRes, resInh, resProd, resEvt, resCarousel, resHigh] = await Promise.all([
      request.get('/resources/featured', { params: { limit: 6 } }),
      request.get('/inheritor/featured', { params: { limit: 4 } }),
      request.get('/products/hot', { params: { limit: 5 } }),
      request.get('/ticket/upcoming', { params: { limit: 3 } }),
      request.get('/home/content', { params: { type: 'HOME_CAROUSEL' } }),
      request.get('/home/content', { params: { type: 'HIGHLIGHT' } }),
    ])
    featuredResources.value = resRes.data || []
    featuredInheritors.value = resInh.data || []
    hotProducts.value = resProd.data || []
    upcomingEvents.value = resEvt.data || []
    carouselItems.value = resCarousel.data || []
    homeHighlights.value = resHigh.data || []
  } catch (e) {
    // swallow errors and use mocks for business data
  } finally {
    fillMocksIfEmpty()
    // For carousel, if empty, we might want to provide default mocks if user hasn't configured anything yet?
    // Let's provide defaults for carousel if empty so the page doesn't look broken initially
    if (carouselItems.value.length === 0) {
       carouselItems.value = [
        {
          title: '临沂柳琴戏',
          subtitle: '国家级非物质文化遗产 | 听拉魂腔，品沂蒙情',
          imageUrl: 'https://images.unsplash.com/photo-1514533450685-4493e01d1fdc?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80',
          linkUrl: '/resources'
        },
        {
          title: '非遗文创周边',
          subtitle: '传统与现代的碰撞 | 把文化带回家',
          imageUrl: 'https://images.unsplash.com/photo-1593118845063-8896024107f7?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80',
          linkUrl: '/products'
        }
      ]
    }
    
    // Default Highlights
    if (homeHighlights.value.length === 0) {
      homeHighlights.value = [
        {
          title: '柳琴戏的历史渊源',
          content: '柳琴戏，又称“拉魂腔”，国家级非物质文化遗产...',
          imageUrl: 'https://images.unsplash.com/photo-1514533450685-4493e01d1fdc?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80'
        },
        {
          title: '名家名段欣赏',
          imageUrl: 'https://images.unsplash.com/photo-1516280440614-6697288d5d38?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'
        },
        {
          title: '近期演出预告',
          imageUrl: 'https://images.unsplash.com/photo-1519751138087-5bf79df62d5b?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'
        }
      ]
    }
  }
}

onMounted(fetchFeatured)
</script>

<style scoped>
.home-page {
  padding-bottom: 60px;
}

/* Hero Carousel */
.hero-section {
  position: relative;
  margin-bottom: 40px;
}
.carousel-item-content {
  width: 100%;
  height: 100%;
  position: relative;
  cursor: pointer;
}
.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.carousel-caption {
  position: absolute;
  bottom: 50px;
  left: 50px;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0,0,0,0.5);
  background: rgba(0,0,0,0.3);
  padding: 20px;
  border-radius: 8px;
}
.carousel-caption h3 {
  font-size: 32px;
  margin-bottom: 10px;
}
.carousel-caption p {
  font-size: 18px;
}

/* Section Styles */
.section-header {
  text-align: center;
  margin-bottom: 40px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}
.section-title {
  font-size: 32px;
  color: #333;
  font-weight: bold;
  font-family: 'STKaiti', serif;
}
.section-title-decoration {
  display: inline-block;
  width: 60px;
  height: 2px;
  background-color: #AA1D1D;
}

/* Showcase Section */
.showcase-section {
  margin-bottom: 60px;
}
.showcase-big, .showcase-small {
  background-size: cover;
  background-position: center;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
}
.showcase-big {
  height: 420px;
}
.showcase-small {
  height: 200px;
}
.showcase-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.8));
  color: #fff;
  padding: 20px;
}
.showcase-small h3 {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.8));
  color: #fff;
  padding: 10px 15px;
  margin: 0;
  font-size: 16px;
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
