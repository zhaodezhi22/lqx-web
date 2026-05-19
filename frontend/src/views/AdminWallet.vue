<template>
  <div class="page">
    <el-card shadow="never" class="page-header">
      <h2>收益提现中心</h2>
      <p>管理员发布的商品和活动收入会在这里统一汇总，当前为模拟提现。</p>
    </el-card>
    <WalletPanel />
  </div>
</template>

<script setup>
import WalletPanel from '../components/WalletPanel.vue'
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header h2 {
  margin: 0 0 8px;
}

.page-header p {
  margin: 0;
  color: #909399;
}
</style>
