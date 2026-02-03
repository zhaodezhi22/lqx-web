<template>
  <div class="event-list page-container">
    <div class="section-header">
      <span class="section-title-decoration"></span>
      <h2 class="section-title">近期演出</h2>
      <span class="section-title-decoration"></span>
    </div>
    <div style="text-align: right; margin-bottom: 20px;">
        <el-button type="info" plain @click="openHistory">查看往期演出</el-button>
    </div>
    <el-row :gutter="20">
      <el-col :span="8" v-for="ev in events" :key="ev.eventId">
        <el-card class="event-card" shadow="hover">
          <div class="event-head">
            <div style="display: flex; align-items: center; gap: 8px;">
              <h3>{{ ev.title }}</h3>
              <el-tag v-if="ev.publisherRole === 2 || ev.publisherRole === 3" type="warning" effect="dark" size="small">官方</el-tag>
            </div>
            <div>
              <el-tag type="success" v-if="ev.status === 1 && !isExpired(ev.showTime)">开放售票</el-tag>
              <el-tag type="info" v-else>已结束/未开放</el-tag>
            </div>
          </div>
          <div class="event-body">
            <p>地点：{{ ev.venue }}</p>
            <p>时间：{{ formatTime(ev.showTime) }}</p>
            <p>票价：¥{{ ev.ticketPrice }}</p>
          </div>
          <div class="event-actions">
            <el-button type="primary" @click="viewDetail(ev.eventId)">查看详情</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col v-if="!loading && events.length === 0">
        <el-empty description="暂无演出" />
      </el-col>
    </el-row>

    <!-- History Events Drawer -->
    <el-drawer v-model="historyVisible" title="往期演出" size="60%">
        <div v-loading="historyLoading">
            <el-empty v-if="historyEvents.length === 0" description="暂无往期演出" />
            <el-row :gutter="20" v-else>
                <el-col :span="12" v-for="ev in historyEvents" :key="ev.eventId" style="margin-bottom: 20px;">
                    <el-card shadow="hover">
                        <div class="event-head">
                            <div style="display: flex; align-items: center; gap: 8px;">
                                <h4 style="margin:0">{{ ev.title }}</h4>
                                <el-tag v-if="ev.publisherRole === 2 || ev.publisherRole === 3" type="warning" effect="dark" size="small">官方</el-tag>
                            </div>
                            <el-tag type="info">已结束</el-tag>
                        </div>
                        <div class="event-body" style="font-size: 13px; color: #666; margin-top: 10px;">
                            <p>地点：{{ ev.venue }}</p>
                            <p>时间：{{ formatTime(ev.showTime) }}</p>
                        </div>
                        <div class="event-actions" style="margin-top: 10px; text-align: right;">
                             <el-button type="info" plain size="small" @click="viewDetail(ev.eventId)">查看回顾</el-button>
                        </div>
                    </el-card>
                </el-col>
            </el-row>
        </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'

const router = useRouter()
const events = ref([])
const loading = ref(false)
const historyVisible = ref(false)
const historyLoading = ref(false)
const historyEvents = ref([])

const isExpired = (timeStr) => {
  if (!timeStr) return false
  return new Date(timeStr).getTime() < new Date().getTime()
}

const formatTime = (iso) => {
  if (!iso) return ''
  const d = new Date(iso)
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd} ${hh}:${mi}`
}

const fetchEvents = async () => {
  loading.value = true
  try {
    const res = await request.get('/events', { params: { upcoming: true } })
    events.value = res.data || []
  } finally {
    loading.value = false
  }
}

const viewDetail = (id) => {
  router.push(`/events/${id}`)
}

const openHistory = async () => {
    historyVisible.value = true
    historyLoading.value = true
    try {
        const res = await request.get('/events', { params: { history: true } })
        historyEvents.value = res.data || []
    } catch (e) {
        // ignore
    } finally {
        historyLoading.value = false
    }
}

onMounted(fetchEvents)
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;700&display=swap');
.event-list {
  padding: 20px;
}
.section-header {
  text-align: center;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.section-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 24px;
  color: #333;
  margin: 0 20px;
}
.section-title-decoration {
  display: inline-block;
  width: 40px;
  height: 2px;
  background-color: #AA1D1D;
  position: relative;
}
.section-title-decoration::before {
  content: '';
  position: absolute;
  width: 6px;
  height: 6px;
  background-color: #AA1D1D;
  transform: rotate(45deg);
  top: -2px;
}
.event-card h3 {
  margin: 0;
  font-size: 18px;
}
.event-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.event-body p {
  margin: 6px 0;
  color: #555;
}
.publisher-info {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}
.publisher-name {
  font-size: 14px;
  color: #333;
}
.event-actions {
  margin-top: 10px;
  text-align: right;
}
</style>
