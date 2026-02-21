<template>
  <el-dialog
    v-model="dialogVisible"
    :title="detail ? detail.product.name : '商品详情'"
    width="800px"
    @close="handleClose"
    destroy-on-close
  >
    <div v-loading="loading" v-if="detail">
      <el-row :gutter="20">
        <!-- Left: Image -->
        <el-col :span="10">
          <div class="image-container">
            <el-image 
              :src="detail.product.mainImage || placeholder" 
              fit="cover" 
              class="main-image"
              :preview-src-list="[detail.product.mainImage || placeholder]"
            />
          </div>
        </el-col>

        <!-- Right: Info -->
        <el-col :span="14">
          <div class="product-info">
            <h2 class="title">{{ detail.product.name }}</h2>
            <p class="subtitle" v-if="detail.product.subtitle">{{ detail.product.subtitle }}</p>
            
            <div class="price-section">
              <span class="price">¥ {{ detail.product.price }}</span>
              <span class="stock">库存: {{ detail.product.stock }}</span>
            </div>

            <div class="seller-section" v-if="detail.seller" @click="goProfile(detail.seller.userId)">
              <el-avatar :size="32" :src="detail.seller.avatar" />
              <span class="seller-name">{{ detail.seller.nickname }}</span>
              <el-tag size="small" type="info" style="margin-left: 8px">商家</el-tag>
            </div>

            <div class="actions" v-if="!isAdmin">
              <template v-if="detail.product.stock > 0">
                 <!-- Self-purchase check -->
                 <div v-if="currentUserId && detail.seller && currentUserId === detail.seller.userId" class="self-product-tip">
                    <el-button type="info" size="large" disabled>您不能购买自己的商品</el-button>
                 </div>
                 <div v-else>
                    <el-input-number v-model="quantity" :min="1" :max="detail.product.stock" style="margin-right: 10px;" />
                    <el-button type="primary" size="large" @click="handleBuyNow">立即购买</el-button>
                 </div>
              </template>
              <el-button v-else type="info" size="large" disabled>已售罄</el-button>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- Tabs -->
      <div class="tabs-section">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="商品详情" name="detail">
            <div class="detail-content" v-html="detail.product.detail || '暂无详情'"></div>
          </el-tab-pane>
          <el-tab-pane label="评价" name="comments">
            <div v-if="detail.comments && detail.comments.length > 0">
              <div v-for="comment in detail.comments" :key="comment.commentId" class="comment-item">
                <div class="comment-header">
                  <div class="user-info">
                    <el-avatar :size="24" :src="comment.user ? comment.user.avatar : ''" />
                    <span class="username">{{ comment.user ? comment.user.nickname : '匿名用户' }}</span>
                  </div>
                  <span class="time">{{ formatTime(comment.createdTime) }}</span>
                </div>
                <div class="comment-content">{{ comment.content }}</div>
              </div>
            </div>
            <div v-else class="empty-comments">暂无评价</div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch, defineProps, defineEmits } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { useCartStore } from '../stores/cart'
import { useRouter } from 'vue-router'

const props = defineProps({
  visible: Boolean,
  productId: Number,
  isAdmin: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible'])

const router = useRouter()
const cartStore = useCartStore()

const dialogVisible = ref(false)
const loading = ref(false)
const detail = ref(null)
const quantity = ref(1)
const activeTab = ref('detail')
const currentUserId = ref(null)

const placeholder = 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png'

watch(() => props.visible, (val) => {
  dialogVisible.value = val
  if (val && props.productId) {
    fetchDetail()
    // Get current user ID
    const token = localStorage.getItem('token')
    if (token) {
        const u = localStorage.getItem('user')
        if (u) {
            try {
                const user = JSON.parse(u)
                currentUserId.value = user.userId
            } catch(e){}
        }
    }
  }
})

watch(() => dialogVisible.value, (val) => {
  emit('update:visible', val)
})

const handleClose = () => {
  detail.value = null
  quantity.value = 1
  activeTab.value = 'detail'
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await request.get(`/mall/product/detail/${props.productId}`)
    if (res.code === 200) {
      detail.value = res.data
    } else {
      ElMessage.error(res.message || '加载详情失败')
    }
  } catch (e) {
    ElMessage.error('加载详情失败')
  } finally {
    loading.value = false
  }
}

const goProfile = (userId) => {
  emit('update:visible', false)
  router.push({ name: 'UserPublicProfile', params: { id: userId } })
}

const handleBuyNow = () => {
  if (!detail.value) return
  // Add to cart and redirect to checkout logic (or just add to cart)
  // Per requirement "立即购买", usually implies checkout flow.
  // For simplicity, we can reuse the "add to cart" logic from parent or just add to cart store here.
  
  if (cartStore.add({
    ...detail.value.product,
    qty: quantity.value
  })) {
    ElMessage.success('已加入购物车')
    dialogVisible.value = false
  }
  // Ideally, open cart drawer or redirect to checkout
}

const formatTime = (arr) => {
  if (!arr) return ''
  // Handle array format [yyyy, MM, dd, HH, mm, ss] or string
  if (Array.isArray(arr)) {
      return `${arr[0]}-${String(arr[1]).padStart(2, '0')}-${String(arr[2]).padStart(2, '0')} ${String(arr[3]).padStart(2, '0')}:${String(arr[4]).padStart(2, '0')}`
  }
  return arr
}
</script>

<style scoped>
.image-container {
  width: 100%;
  height: 300px;
  background: #f5f7fa;
  border-radius: 4px;
  overflow: hidden;
}
.main-image {
  width: 100%;
  height: 100%;
}
.product-info {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.title {
  margin: 0 0 10px 0;
  font-size: 20px;
  color: #303133;
}
.subtitle {
  color: #909399;
  font-size: 14px;
  margin-bottom: 20px;
}
.price-section {
  margin-bottom: 20px;
  display: flex;
  align-items: baseline;
}
.price {
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
  margin-right: 15px;
}
.stock {
  font-size: 13px;
  color: #909399;
}
.seller-section {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 24px;
  cursor: pointer;
  width: fit-content;
  padding: 4px 8px 4px 0;
  border-radius: 4px;
}
.seller-section:hover {
  background: #f5f7fa;
}
.seller-name {
  font-weight: bold;
  color: #333;
}
.actions {
  margin-top: auto;
}
.tabs-section {
  margin-top: 30px;
}
.detail-content {
  line-height: 1.6;
  color: #606266;
}
.comment-item {
  border-bottom: 1px solid #EBEEF5;
  padding: 15px 0;
}
.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.user-info {
  display: flex;
  align-items: center;
}
.user-info .username {
  margin-left: 8px;
  font-size: 13px;
  color: #606266;
}
.time {
  font-size: 12px;
  color: #909399;
}
.comment-content {
  color: #303133;
  font-size: 14px;
}
.empty-comments {
  text-align: center;
  color: #909399;
  padding: 20px;
}
</style>
