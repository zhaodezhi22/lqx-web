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
          <el-carousel height="240px" indicator-position="outside">
            <el-carousel-item v-for="(item, idx) in carousel" :key="idx">
              <div class="carousel-item">
                <img :src="item.img" alt="" />
                <div class="overlay">
                  <h4>{{ item.title }}</h4>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
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
            <ul>
              <li v-for="(n, i) in notices" :key="i">
                <a href="javascript:void(0)" @click="viewNotice(n)">{{ n }}</a>
              </li>
            </ul>
          </el-scrollbar>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
const carousel = [
  { title: '临沂柳琴戏精品剧目展演', img: 'https://images.unsplash.com/photo-1558981403-c5f9899a28bd?q=80&w=1200&auto=format&fit=crop' },
  { title: '非遗进校园活动周', img: 'https://images.unsplash.com/photo-1523961131990-5ea7c61b2107?q=80&w=1200&auto=format&fit=crop' },
  { title: '传承人教学公开课', img: 'https://images.unsplash.com/photo-1515378791036-0648a3ef77b2?q=80&w=1200&auto=format&fit=crop' },
]

const notices = [
  '关于新一轮传承人遴选的通知',
  '近期演出排期及购票须知',
  '资源库上线“经典重温”专题',
  '文化节志愿者招募公告',
]

const viewNotice = (n) => {
  // Placeholder
  console.log('View notice:', n)
}
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
</style>
