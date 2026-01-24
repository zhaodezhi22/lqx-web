<template>
  <div class="home-events">
    <SectionTitle text="—— 现场演出 ——" />
    <div class="events-container">
      <el-row :gutter="24">
        <el-col :span="8" v-for="(ev, index) in eventsToShow" :key="ev.eventId" class="event-col">
          <div class="event-card" @click="book(ev.eventId)">
            <div class="event-cover">
              <img :src="getCoverImage(index)" alt="cover" class="cover-img" />
              <div class="date-badge">
                <span class="day">{{ getDay(ev.showTime) }}</span>
                <span class="month">{{ getMonth(ev.showTime) }}月</span>
              </div>
              <div class="status-tag" :class="{ active: ev.status === 1 }">
                {{ ev.status === 1 ? '售票中' : '已结束' }}
              </div>
            </div>
            
            <div class="event-info">
              <h3 class="title" :title="ev.title">{{ ev.title }}</h3>
              
              <div class="meta-row">
                <el-icon><Location /></el-icon>
                <span class="text">{{ ev.venue }}</span>
              </div>
              
              <div class="meta-row">
                <el-icon><Clock /></el-icon>
                <span class="text">{{ formatFullTime(ev.showTime) }}</span>
              </div>

              <div class="divider"></div>

              <div class="footer">
                <div class="price-box">
                  <span class="currency">¥</span>
                  <span class="amount">{{ ev.ticketPrice || '0' }}</span>
                  <span class="suffix">起</span>
                </div>
                <el-button 
                  type="primary" 
                  round 
                  class="buy-btn"
                  :disabled="ev.status !== 1"
                >
                  立即预订
                </el-button>
              </div>
            </div>
          </div>
        </el-col>
        
        <el-col v-if="!loading && eventsToShow.length === 0" :span="24">
          <el-empty description="近期暂无演出安排" />
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Location, Clock } from '@element-plus/icons-vue'
import request from '../../utils/request'
import SectionTitle from '../common/SectionTitle.vue'

const props = defineProps({
  events: { type: Array, default: () => [] }
})

const router = useRouter()
const events = ref([])
const loading = ref(false)

const eventsToShow = computed(() => {
  return props.events && props.events.length ? props.events : events.value
})

const fetchEvents = async () => {
  loading.value = true
  try {
    const res = await request.get('/events', { params: { upcoming: true } })
    events.value = res.data || []
  } finally {
    loading.value = false
  }
}

const getMonth = (iso) => {
  if (!iso) return ''
  return new Date(iso).getMonth() + 1
}

const getDay = (iso) => {
  if (!iso) return ''
  return new Date(iso).getDate()
}

const formatFullTime = (iso) => {
  if (!iso) return ''
  const d = new Date(iso)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const da = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${da} ${h}:${min}`
}

// Placeholder images for visual variety
const covers = [
  'https://images.unsplash.com/photo-1514525253440-b393452e3383?q=80&w=800&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1469334031218-e382a71b716b?q=80&w=800&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1507676184212-d0370baf27db?q=80&w=800&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1492684223066-81342ee5ff30?q=80&w=800&auto=format&fit=crop'
]

const getCoverImage = (index) => {
  return covers[index % covers.length]
}

const book = (id) => {
  router.push(`/events/${id}`)
}

onMounted(() => {
  if (!props.events || props.events.length === 0) {
    fetchEvents()
  }
})
</script>

<style scoped>
.home-events {
  margin-bottom: 40px;
}

.events-container {
  padding: 0 10px;
}

.event-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  transition: all 0.3s ease;
  cursor: pointer;
  height: 100%;
  display: flex;
  flex-direction: column;
  border: 1px solid #f0f0f0;
}

.event-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(170, 29, 29, 0.15);
}

.event-cover {
  height: 180px;
  position: relative;
  overflow: hidden;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.event-card:hover .cover-img {
  transform: scale(1.05);
}

.date-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  padding: 4px 10px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
}

.date-badge .day {
  font-size: 20px;
  font-weight: bold;
  color: #AA1D1D;
  line-height: 1;
}

.date-badge .month {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
}

.status-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(0,0,0,0.6);
  color: #fff;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
}

.status-tag.active {
  background: #67C23A;
}

.event-info {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.title {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: bold;
  color: #333;
  line-height: 1.4;
  height: 44px; /* Limit to 2 lines */
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.meta-row {
  display: flex;
  align-items: center;
  color: #666;
  font-size: 13px;
  margin-bottom: 6px;
}

.meta-row .el-icon {
  margin-right: 6px;
  font-size: 14px;
}

.text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.divider {
  height: 1px;
  background: #f0f0f0;
  margin: 12px 0;
}

.footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.price-box {
  color: #AA1D1D;
  font-weight: bold;
}

.currency {
  font-size: 12px;
}

.amount {
  font-size: 20px;
  margin: 0 2px;
}

.suffix {
  font-size: 12px;
  color: #999;
  font-weight: normal;
}

.buy-btn {
  padding: 8px 20px;
}
</style>
