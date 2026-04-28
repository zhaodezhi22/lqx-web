<template>
  <div class="forbidden-page">
    <el-result icon="warning" title="无权访问" :sub-title="message">
      <template #extra>
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="goBack">返回上一页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const message = computed(() => {
  const queryMessage = typeof route.query.message === 'string' ? route.query.message : ''
  return queryMessage || '当前账号没有管理员权限，无法进入后台管理界面。'
})

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/')
}
</script>

<style scoped>
.forbidden-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  padding: 24px;
}
</style>
