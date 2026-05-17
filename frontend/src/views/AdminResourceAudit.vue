<template>
  <div class="page">
    <el-card>
      <div class="redirect-box">
        <h2>资源审核已迁移</h2>
        <p>当前页面已收口到统一审核中心，正在为你跳转到资源审核视图。</p>
        <el-button type="primary" @click="goToAuditCenter">立即前往</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const goToAuditCenter = () => {
  router.replace({ path: '/admin/audit-center', query: { type: 'RESOURCE', status: '0' } })
}

onMounted(goToAuditCenter)
</script>

<style scoped>
.page { padding: 16px; }
.redirect-box {
  padding: 24px 12px;
  text-align: center;
}
.redirect-box p {
  margin: 12px 0 20px;
  color: #666;
}
</style>
