<template>
  <div class="admin-settings">
    <h2>系统设置</h2>
    
    <el-tabs type="border-card">
      <el-tab-pane label="支付配置">
        <el-form :model="paymentForm" label-width="120px" style="max-width: 600px">
          <h3>支付宝沙箱环境</h3>
          <el-form-item label="App ID">
            <el-input v-model="paymentForm.appId" />
          </el-form-item>
          <el-form-item label="应用私钥">
            <el-input v-model="paymentForm.privateKey" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item label="支付宝公钥">
            <el-input v-model="paymentForm.alipayPublicKey" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="savePaymentConfig">保存配置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="Banner 管理">
        <div class="banner-list">
          <el-button type="primary" style="margin-bottom: 15px">添加 Banner</el-button>
          <el-table :data="bannerList" border>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column label="图片预览" width="150">
              <template #default="{ row }">
                <el-image :src="row.url" style="width: 100px; height: 50px" fit="cover" />
              </template>
            </el-table-column>
            <el-table-column prop="link" label="跳转链接" />
            <el-table-column prop="order" label="排序" width="80" />
            <el-table-column label="操作">
              <template #default>
                <el-button link type="primary">编辑</el-button>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

const paymentForm = reactive({
  appId: '2021000118625xxx',
  privateKey: 'MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQ...',
  alipayPublicKey: 'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...'
})

const bannerList = ref([
  { id: 1, url: 'https://placeholder.com/800x400', link: '/mall', order: 1 },
  { id: 2, url: 'https://placeholder.com/800x400', link: '/resources', order: 2 }
])

const savePaymentConfig = () => {
  ElMessage.success('支付配置已保存 (Mock)')
}
</script>

<style scoped>
.admin-settings {
  padding: 20px;
}
</style>
