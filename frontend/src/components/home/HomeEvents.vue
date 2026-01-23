<template>
  <div class="home-events">
    <SectionTitle text="—— 现场演出 ——" />
    <el-row :gutter="20">
      <el-col :span="12" v-for="ev in eventsToShow" :key="ev.eventId">
        <el-card class="event-line" shadow="hover">
          <div class="date">{{ formatDate(ev.showTime) }}</div>
          <div class="info">
            <h3>{{ ev.title }}</h3>
            <p>地点：{{ ev.venue }}</p>
          </div>
          <div class="action">
            <el-button type="primary" @click="book(ev.eventId)" :disabled="ev.status !== 1">立即订票</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col v-if="!loading && eventsToShow.length === 0">
        <el-empty description="暂无演出" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
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

const formatDate = (iso) => {
  const d = new Date(iso)
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${mm}/${dd}`
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
.event-line {
  display: grid;
  grid-template-columns: 80px 1fr 140px;
  align-items: center;
  transition: box-shadow .3s;
}
.event-line .date {
  font-size: 28px;
  color: #AA1D1D;
  text-align: center;
  font-weight: bold;
}
.event-line .info h3 {
  margin: 0;
}
.event-line .info p {
  margin: 6px 0 0 0;
  color: #666;
}
.event-line .action {
  text-align: right;
}
.event-line:hover {
  box-shadow: 0 12px 24px rgba(170, 29, 29, .12);
}
</style>
