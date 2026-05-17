<template>
  <div class="product-detail-page">
    <div class="detail-container" v-loading="loading">
      <div class="back-row">
        <el-button link @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
      </div>

      <template v-if="detail">
        <el-card shadow="never" class="detail-card">
          <el-row :gutter="28">
            <el-col :xs="24" :md="10">
              <div class="image-container">
                <el-image
                  :src="detail.product.mainImage || placeholder"
                  fit="cover"
                  class="main-image"
                  :preview-src-list="[detail.product.mainImage || placeholder]"
                />
              </div>
            </el-col>

            <el-col :xs="24" :md="14">
              <div class="product-info">
                <div class="title-row">
                  <h1 class="title">{{ detail.product.name }}</h1>
                  <el-tag v-if="detail.product.stock > 0" type="success">有货</el-tag>
                  <el-tag v-else type="info">已售罄</el-tag>
                </div>
                <p v-if="detail.product.subtitle" class="subtitle">{{ detail.product.subtitle }}</p>

                <div class="price-section">
                  <span class="price-label">价格</span>
                  <span class="price">¥ {{ detail.product.price }}</span>
                </div>

                <div class="meta-item">
                  <span class="meta-label">库存</span>
                  <span>{{ detail.product.stock }}</span>
                </div>

                <div class="seller-section" v-if="detail.seller" @click="goProfile(detail.seller.userId)">
                  <el-avatar :size="36" :src="detail.seller.avatar" />
                  <span class="seller-name">{{ detail.seller.nickname }}</span>
                  <el-tag size="small" type="info">商家</el-tag>
                </div>

                <div class="action-section">
                  <template v-if="detail.product.stock > 0">
                    <div v-if="currentUserId && detail.seller && currentUserId === detail.seller.userId" class="self-product-tip">
                      <el-button type="info" size="large" disabled>您不能购买自己的商品</el-button>
                    </div>
                    <div v-else class="action-row">
                      <el-input-number v-model="quantity" :min="1" :max="detail.product.stock" />
                      <el-button type="primary" size="large" @click="handleBuyNow">加入购物车</el-button>
                    </div>
                  </template>
                  <el-button v-else type="info" size="large" disabled>已售罄</el-button>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <el-card shadow="never" class="content-card">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="商品详情" name="detail">
              <div class="detail-content" v-html="detail.product.detail || '暂无详情'"></div>
            </el-tab-pane>
            <el-tab-pane label="评价" name="comments">
              <div v-if="detail.comments && detail.comments.length > 0">
                <div v-for="comment in detail.comments" :key="comment.commentId" class="comment-item">
                  <div class="comment-header">
                    <div class="comment-user">
                      <el-avatar :size="24" :src="comment.user ? comment.user.avatar : ''" />
                      <span>{{ comment.user ? comment.user.nickname : '匿名用户' }}</span>
                    </div>
                    <span class="comment-time">{{ formatTime(comment.createdTime) }}</span>
                  </div>
                  <div class="comment-content">{{ comment.content }}</div>
                </div>
              </div>
              <el-empty v-else description="暂无评价" />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </template>

      <el-empty v-else-if="!loading" description="商品不存在或已下架" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import request from '../utils/request'
import { useCartStore } from '../stores/cart'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const loading = ref(false)
const detail = ref(null)
const quantity = ref(1)
const activeTab = ref('detail')
const currentUserId = ref(null)
const placeholder = 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png'

const loadCurrentUser = () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return
  try {
    const user = JSON.parse(userStr)
    currentUserId.value = user.userId
  } catch (e) {
    currentUserId.value = null
  }
}

const fetchDetail = async () => {
  loading.value = true
  detail.value = null
  try {
    const res = await request.get(`/mall/product/detail/${route.params.id}`)
    if (res.code === 200) {
      detail.value = res.data
      quantity.value = 1
      activeTab.value = 'detail'
    } else {
      ElMessage.error(res.message || '加载商品详情失败')
    }
  } catch (e) {
    ElMessage.error('加载商品详情失败')
  } finally {
    loading.value = false
  }
}

const handleBuyNow = () => {
  if (!detail.value) return

  const product = {
    ...detail.value.product
  }

  for (let i = 0; i < quantity.value; i += 1) {
    const success = cartStore.add(product)
    if (!success) {
      return
    }
  }

  ElMessage.success('已加入购物车，请前往购物车完成支付')
}

const goProfile = (userId) => {
  router.push({ name: 'UserPublicProfile', params: { id: userId } })
}

const goBack = () => {
  router.back()
}

const formatTime = (arr) => {
  if (!arr) return ''
  if (Array.isArray(arr)) {
    return `${arr[0]}-${String(arr[1]).padStart(2, '0')}-${String(arr[2]).padStart(2, '0')} ${String(arr[3]).padStart(2, '0')}:${String(arr[4]).padStart(2, '0')}`
  }
  return arr
}

watch(() => route.params.id, fetchDetail)

onMounted(() => {
  loadCurrentUser()
  fetchDetail()
})
</script>

<style scoped>
.product-detail-page {
  background: #f7f8fa;
  min-height: 100vh;
  padding: 24px 0 60px;
}

.detail-container {
  width: min(1200px, calc(100% - 40px));
  margin: 0 auto;
}

.back-row {
  margin-bottom: 16px;
}

.detail-card,
.content-card {
  border-radius: 16px;
  margin-bottom: 20px;
}

.image-container {
  height: 420px;
  border-radius: 14px;
  overflow: hidden;
  background: #f5f7fa;
}

.main-image {
  width: 100%;
  height: 100%;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.title {
  margin: 0;
  font-size: 30px;
  color: #222;
}

.subtitle {
  margin: 0;
  color: #666;
  line-height: 1.7;
}

.price-section {
  display: flex;
  align-items: baseline;
  gap: 14px;
  padding: 18px 20px;
  border-radius: 12px;
  background: linear-gradient(135deg, #fff5f5, #fffaf2);
}

.price-label,
.meta-label {
  color: #909399;
  font-size: 14px;
}

.price {
  color: #aa1d1d;
  font-size: 34px;
  font-weight: 700;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #333;
}

.seller-section {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  width: fit-content;
  padding: 10px 14px;
  border-radius: 12px;
  background: #faf6ef;
  cursor: pointer;
}

.seller-name {
  color: #333;
  font-weight: 500;
}

.action-section {
  padding-top: 8px;
}

.action-row {
  display: flex;
  align-items: center;
  gap: 14px;
}

.detail-content {
  line-height: 1.9;
  color: #333;
  min-height: 220px;
}

.comment-item {
  padding: 14px 0;
  border-bottom: 1px solid #f0f2f5;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-user {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #333;
}

.comment-time {
  color: #999;
  font-size: 13px;
}

.comment-content {
  color: #444;
  line-height: 1.7;
}
</style>
