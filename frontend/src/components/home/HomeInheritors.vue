<template>
  <div class="home-inheritors">
    <SectionTitle text="—— 大师风采 ——" />
    <el-row :gutter="20">
      <el-col :span="6" v-for="it in itemsToShow" :key="it.userId">
        <div class="inheritor-card">
          <el-avatar :size="80" :src="it.avatar || defaultAvatar" />
          <h3>{{ it.name }}</h3>
          <p class="title">{{ it.level || '传承人' }}</p>
          <el-button type="primary" link @click="viewProfile(it.userId)">查看资料</el-button>
        </div>
      </el-col>
      <el-col v-if="!loading && itemsToShow.length === 0">
        <el-empty description="暂无数据" />
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
  items: { type: Array, default: () => [] }
})

const router = useRouter()
const items = ref([])
const loading = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const itemsToShow = computed(() => {
  return props.items && props.items.length ? props.items : items.value
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/inheritor/spotlight', { params: { limit: 8 } })
    items.value = res.data || []
  } finally {
    loading.value = false
  }
}

const viewProfile = (userId) => {
  router.push('/inheritors') // Placeholder: could navigate to specific profile page later
}

onMounted(() => {
  if (!props.items || props.items.length === 0) {
    fetchData()
  }
})
</script>

<style scoped>
.home-inheritors {
  margin: 20px 0;
}
.inheritor-card {
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 20px;
  text-align: center;
  background: #fff url('data:image/svg+xml;utf8,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%22100%22 height=%22100%22><rect width=%22100%22 height=%22100%22 fill=%22none%22 stroke=%22%23e7d8c3%22 stroke-width=%221%22/></svg>') repeat;
  background-size: 16px 16px;
  transition: box-shadow .3s, transform .3s;
}
.inheritor-card h3 {
  margin: 10px 0 4px 0;
  color: #333;
}
.inheritor-card .title {
  margin: 0 0 10px 0;
  color: #AA1D1D;
}
.inheritor-card:hover {
  box-shadow: 0 12px 24px rgba(170, 29, 29, .12);
}
</style>
