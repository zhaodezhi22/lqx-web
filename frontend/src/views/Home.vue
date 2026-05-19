<template>
  <div class="home-page">
    <div class="page-glow glow-left"></div>
    <div class="page-glow glow-right"></div>
    <!-- Hero Section -->
    <div class="hero-section">
      <div class="hero-frame">
        <el-carousel trigger="click" height="500px" :interval="5000" arrow="always" v-if="carouselItems.length > 0">
          <el-carousel-item v-for="(item, index) in carouselItems" :key="index">
            <div class="carousel-item-content" @click="handleCarouselClick(item)">
              <img :src="item.imageUrl" alt="Carousel Image" class="carousel-image" />
              <div class="carousel-copy" v-if="item.subtitle">
                <div class="carousel-copy-inner">
                  <p class="carousel-copy-subtitle">{{ item.subtitle }}</p>
                </div>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
        <!-- Fallback if no carousel items -->
        <div v-else class="hero-section-placeholder">
          <p>暂无轮播图配置</p>
        </div>
      </div>
    </div>

    <div class="home-main">
      <div class="showcase-section section-shell section-shell-highlight" v-if="homeHighlights.length > 0">
        <div class="section-header">
          <span class="section-title-decoration"></span>
          <h2 class="section-title">非遗风采</h2>
          <span class="section-title-decoration"></span>
        </div>
        <el-row :gutter="24">
          <el-col :xs="24" :md="12" v-if="homeHighlights[0]">
            <div class="showcase-big" :style="{ backgroundImage: `url(${homeHighlights[0].imageUrl})` }" @click="handleHighlightClick(homeHighlights[0])">
              <div class="showcase-content">
                <h3>{{ homeHighlights[0].title }}</h3>
                <p>{{ homeHighlights[0].content }}</p>
              </div>
            </div>
          </el-col>
          <el-col :xs="24" :md="12" v-if="homeHighlights[1]">
            <div class="showcase-big" :style="{ backgroundImage: `url(${homeHighlights[1].imageUrl})` }" @click="handleHighlightClick(homeHighlights[1])">
              <div class="showcase-content">
                <h3>{{ homeHighlights[1].title }}</h3>
                <p>{{ homeHighlights[1].content }}</p>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <div class="two-column-section section-shell">
        <div class="column-main">
          <home-events :events="upcomingEvents" />
        </div>
        <div class="column-side">
          <home-news />
        </div>
      </div>

      <div class="section-shell">
        <home-resources />
      </div>

      <div class="feature-grid">
        <div class="feature-block">
          <div class="section-shell section-shell-compact">
            <home-inheritors :items="featuredInheritors" />
          </div>
        </div>
        <div class="feature-block products-block">
          <div class="section-shell section-shell-compact">
            <home-products :products="hotProducts" />
          </div>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="detailVisible"
      width="680px"
      destroy-on-close
      class="home-content-dialog"
      :title="selectedContent.title || '内容详情'"
    >
      <div class="content-detail" v-if="selectedContent.title || selectedContent.content || selectedContent.imageUrl">
        <el-image
          v-if="selectedContent.imageUrl"
          :src="selectedContent.imageUrl"
          fit="cover"
          class="content-detail-image"
        />
        <div class="content-detail-subtitle" v-if="selectedContent.subtitle">
          {{ selectedContent.subtitle }}
        </div>
        <div class="content-detail-text">
          {{ selectedContent.content || selectedContent.subtitle || '暂无详细内容' }}
        </div>
      </div>
    </el-dialog>

    <!-- Floating Chat Button -->
    <div class="floating-chat" @click="$router.push('/chat')">
      <el-badge :value="totalUnreadCount" :hidden="totalUnreadCount === 0" class="chat-badge">
        <div class="chat-btn" title="联系客服/好友">
          <el-icon :size="24"><ChatDotRound /></el-icon>
        </div>
      </el-badge>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound } from '@element-plus/icons-vue'
import HomeNews from '../components/home/HomeNews.vue'
import HomeInheritors from '../components/home/HomeInheritors.vue'
import HomeResources from '../components/home/HomeResources.vue'
import HomeEvents from '../components/home/HomeEvents.vue'
import HomeProducts from '../components/home/HomeProducts.vue'
import request from '../utils/request'
import { useChatStore } from '../stores/chat'
import { storeToRefs } from 'pinia'

const router = useRouter()
const chatStore = useChatStore()
const { totalUnreadCount } = storeToRefs(chatStore)
const featuredInheritors = ref([])
const hotProducts = ref([])
const upcomingEvents = ref([])
const carouselItems = ref([])
const homeHighlights = ref([])
const detailVisible = ref(false)
const selectedContent = ref({})

const openContentDetail = (item) => {
  selectedContent.value = {
    title: item.title || '',
    subtitle: item.subtitle || '',
    content: item.content || '',
    imageUrl: item.imageUrl || ''
  }
  detailVisible.value = true
}

const handleHighlightClick = (item) => {
  openContentDetail(item)
}

const handleCarouselClick = (item) => {
  openContentDetail(item)
}

const fillMocksIfEmpty = () => {
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
    const [resInh, resProd, resEvt, resCarousel, resHigh] = await Promise.all([
      request.get('/inheritor/featured', { params: { limit: 4 } }),
      request.get('/products/hot', { params: { limit: 6 } }),
      request.get('/ticket/upcoming', { params: { limit: 3 } }),
      request.get('/home/content', { params: { type: 'HOME_CAROUSEL' } }),
      request.get('/home/content', { params: { type: 'HIGHLIGHT' } }),
    ])
    featuredInheritors.value = resInh.data || []
    hotProducts.value = resProd.data || []
    upcomingEvents.value = resEvt.data || []
    carouselItems.value = resCarousel.data || []
    homeHighlights.value = (resHigh.data || []).slice(0, 2)
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
          content: '临沂柳琴戏是国家级非物质文化遗产，承载着浓厚的地方文化记忆与戏曲艺术魅力。',
          imageUrl: 'https://images.unsplash.com/photo-1514533450685-4493e01d1fdc?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80',
        },
        {
          title: '非遗文创周边',
          subtitle: '传统与现代的碰撞 | 把文化带回家',
          content: '精选柳琴戏主题文创与非遗周边，将传统美学与现代生活方式结合，让文化触手可及。',
          imageUrl: 'https://images.unsplash.com/photo-1593118845063-8896024107f7?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80',
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
          content: '精选经典唱段与舞台影像，快速了解柳琴戏的声腔魅力与艺术特色。',
          imageUrl: 'https://images.unsplash.com/photo-1516280440614-6697288d5d38?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'
        }
      ]
    }
  }
}

onMounted(() => {
  fetchFeatured()
})
</script>

<style scoped>
.home-page {
  position: relative;
  overflow: hidden;
  background:
    radial-gradient(circle at top left, rgba(170, 29, 29, 0.08), transparent 28%),
    radial-gradient(circle at top right, rgba(214, 174, 96, 0.14), transparent 24%),
    linear-gradient(180deg, #fbf7f1 0%, #fff 260px);
  padding-bottom: 72px;
}

.home-main {
  width: min(1320px, calc(100% - 48px));
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.page-glow {
  position: absolute;
  width: 420px;
  height: 420px;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.45;
  pointer-events: none;
}

.glow-left {
  left: -180px;
  top: 220px;
  background: rgba(170, 29, 29, 0.12);
}

.glow-right {
  right: -160px;
  top: 680px;
  background: rgba(214, 174, 96, 0.18);
}

/* Floating Chat */
.floating-chat {
  position: fixed;
  bottom: 100px;
  right: 40px;
  z-index: 2000;
  cursor: pointer;
}

.chat-btn {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #8f231e, #c94b3f 55%, #d6ae60);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12px 28px rgba(80, 42, 30, 0.24);
  border: 1px solid rgba(255, 255, 255, 0.45);
  transition: all 0.3s;
}

.chat-btn:hover {
  transform: translateY(-3px) scale(1.05);
}

/* Hero Carousel */
.hero-section {
  position: relative;
  padding: 24px 24px 0;
  margin-bottom: 30px;
}

.hero-frame {
  overflow: hidden;
  border-radius: 30px;
  box-shadow: 0 24px 60px rgba(48, 30, 23, 0.18);
  border: 1px solid rgba(255, 248, 240, 0.85);
  position: relative;
  background: #fff;
}

.hero-frame::before {
  content: '';
  position: absolute;
  inset: 16px;
  border: 1px solid rgba(255, 255, 255, 0.42);
  border-radius: 22px;
  z-index: 1;
  pointer-events: none;
}

.hero-frame::after {
  content: '';
  position: absolute;
  inset: auto 0 0;
  height: 160px;
  background: linear-gradient(180deg, rgba(10, 10, 10, 0), rgba(10, 10, 10, 0.24));
  pointer-events: none;
  z-index: 1;
}
.carousel-item-content {
  width: 100%;
  height: 100%;
  position: relative;
  cursor: pointer;
}

.carousel-copy {
  position: absolute;
  left: 28px;
  right: 28px;
  bottom: 30px;
  z-index: 2;
  display: flex;
  align-items: flex-end;
  pointer-events: none;
}

.carousel-copy-inner {
  max-width: min(560px, 100%);
  padding: 14px 18px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(27, 19, 16, 0.60), rgba(93, 34, 29, 0.42), rgba(214, 174, 96, 0.18));
  border: 1px solid rgba(255, 255, 255, 0.22);
  box-shadow: 0 20px 40px rgba(17, 10, 8, 0.24);
  backdrop-filter: blur(12px);
}

.carousel-copy-subtitle {
  margin: 0;
  color: rgba(255, 234, 205, 0.95);
  font-size: 17px;
  line-height: 1.8;
  font-weight: 500;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.hero-frame :deep(.el-carousel__button) {
  width: 28px;
  height: 4px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
}

.hero-frame :deep(.el-carousel__indicator.is-active .el-carousel__button) {
  background: #fff;
}

.hero-frame :deep(.el-carousel__arrow) {
  background: rgba(44, 28, 22, 0.32);
  border: 1px solid rgba(255, 255, 255, 0.26);
  backdrop-filter: blur(8px);
}

.hero-frame :deep(.el-carousel__arrow:hover) {
  background: rgba(170, 29, 29, 0.78);
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.hero-section-placeholder {
  height: 500px;
  background: linear-gradient(135deg, #efe6d8, #f8f4ed);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8b6b56;
  font-size: 16px;
}

.content-detail-image {
  width: 100%;
  height: 260px;
  border-radius: 14px;
  margin-bottom: 18px;
}

.content-detail-subtitle {
  margin-bottom: 12px;
  color: #8b6b56;
  font-size: 15px;
  line-height: 1.7;
}

.content-detail-text {
  color: #333;
  font-size: 15px;
  line-height: 1.9;
  white-space: pre-wrap;
}

/* Section Styles */
.section-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 28px;
}
.section-title {
  margin: 0;
  font-size: 32px;
  color: #2f241f;
  font-weight: bold;
  font-family: 'STKaiti', serif;
  letter-spacing: 3px;
}
.section-title-decoration {
  display: inline-block;
  width: 72px;
  height: 2px;
  background: linear-gradient(90deg, rgba(170, 29, 29, 0), #AA1D1D, rgba(214, 174, 96, 0.9), rgba(170, 29, 29, 0));
}

/* Showcase Section */
.showcase-section {
  margin-bottom: 36px;
}
.showcase-big, .showcase-small {
  background-size: cover;
  background-position: center;
  border-radius: 24px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
  box-shadow: 0 14px 30px rgba(40, 24, 18, 0.10);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.showcase-big {
  min-height: 420px;
}
.showcase-big::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(20, 20, 20, 0.04), rgba(20, 20, 20, 0.76)),
    linear-gradient(135deg, rgba(170, 29, 29, 0.12), rgba(214, 174, 96, 0.08));
}
.showcase-content {
  position: absolute;
  z-index: 1;
  bottom: 32px;
  left: 32px;
  right: 32px;
  color: #fff;
}
.showcase-content h3 {
  margin: 0 0 12px;
  font-size: 32px;
  letter-spacing: 1px;
}
.showcase-content p {
  margin: 0;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.88);
  min-height: 48px;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.showcase-big:hover {
  transform: translateY(-8px);
  box-shadow: 0 22px 38px rgba(40, 24, 18, 0.16);
}

.section-shell {
  position: relative;
  padding: 24px 26px 20px;
  border-radius: 28px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(255, 249, 243, 0.96));
  border: 1px solid #f0e1d1;
  box-shadow: 0 16px 34px rgba(54, 33, 24, 0.07);
  overflow: hidden;
}

.section-shell::before {
  content: '';
  position: absolute;
  inset: 0 auto auto 0;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, rgba(170, 29, 29, 0), rgba(170, 29, 29, 0.22), rgba(214, 174, 96, 0.22), rgba(170, 29, 29, 0));
}

.section-shell::after {
  content: '';
  position: absolute;
  right: -40px;
  top: -40px;
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(214, 174, 96, 0.10), transparent 72%);
  pointer-events: none;
}

.section-shell-highlight {
  background:
    linear-gradient(180deg, rgba(255, 252, 247, 0.98), rgba(255, 246, 238, 0.95));
}

.section-shell-compact {
  height: 100%;
}
.two-column-section {
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(320px, 0.95fr);
  gap: 24px;
  margin-bottom: 36px;
  align-items: start;
}

.column-main,
.column-side,
.feature-block {
  min-width: 0;
}

.feature-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.2fr);
  gap: 24px;
  align-items: start;
}

.products-block {
  margin-bottom: 80px;
}

/* Responsive */
@media (max-width: 992px) {
  .two-column-section,
  .feature-grid {
    display: block;
  }

  .column-side,
  .feature-block {
    margin-top: 24px;
  }

  .showcase-big {
    min-height: 320px;
    margin-bottom: 20px;
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: 12px 12px 0;
  }

  .home-main {
    width: calc(100% - 24px);
  }

  .showcase-big {
    min-height: 300px;
  }

  .carousel-copy {
    left: 16px;
    right: 16px;
    bottom: 20px;
  }

  .carousel-copy-inner {
    padding: 12px 14px;
    border-radius: 16px;
  }

  .carousel-copy-subtitle {
    font-size: 14px;
    line-height: 1.7;
  }

  .showcase-content {
    left: 20px;
    right: 20px;
    bottom: 20px;
  }

  .showcase-content h3 {
    font-size: 24px;
  }

  .section-title {
    font-size: 26px;
    letter-spacing: 2px;
  }

  .section-title-decoration {
    width: 48px;
  }

  .section-shell {
    padding: 18px 16px 14px;
    border-radius: 20px;
  }
}
</style>
