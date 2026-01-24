<template>
  <div class="page">
    <div class="header">
      <h2>文创商城</h2>
      <el-button type="primary" icon="ShoppingCart" @click="drawer = true">
        购物车 ({{ cart.count }})
      </el-button>
    </div>
    <el-row :gutter="16">
      <el-col v-for="item in products" :key="item.productId" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="product-card" shadow="hover" :body-style="{ padding: '0px' }">
          <div class="card-content" @click="openDetail(item)">
            <img class="cover" :src="item.mainImage || placeholder" alt="cover" />
            <div class="info">
              <div class="title">{{ item.name }}</div>
              <div class="price">¥ {{ item.price }}</div>
            </div>
          </div>
          <div class="actions">
             <el-button type="primary" size="small" @click.stop="addToCart(item)">加入购物车</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <ProductDetailModal v-model:visible="detailVisible" :product-id="currentProductId" />

    <el-drawer v-model="drawer" title="我的购物车" size="400px">
      <div v-if="cart.items.length === 0">购物车是空的</div>
      <div v-else>
        <div v-for="(item, index) in cart.items" :key="index" class="cart-item">
          <div class="cart-info">
            <h4>{{ item.product.name }}</h4>
            <p>¥ {{ item.product.price }} x {{ item.qty }}</p>
          </div>
          <div class="cart-total">¥ {{ (item.product.price * item.qty).toFixed(2) }}</div>
        </div>
        <div class="cart-summary">
          <h3>总计: ¥ {{ totalAmount }}</h3>
          <el-button type="danger" style="width: 100%" @click="checkout">去结算</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Delete } from '@element-plus/icons-vue'
import request from '../utils/request'
import { useCartStore } from '../stores/cart'
import ProductDetailModal from '../components/ProductDetailModal.vue'

const router = useRouter()
const products = ref([])
const placeholder = 'https://via.placeholder.com/300x180?text=Product'
const cart = useCartStore()
const drawer = ref(false)

const detailVisible = ref(false)
const currentProductId = ref(null)

const fetchList = async () => {
  try {
    const res = await request.get('/products')
    products.value = res.data || []
  } catch (e) {
    ElMessage.error('加载商品失败')
  }
}

const openDetail = (item) => {
  currentProductId.value = item.productId
  detailVisible.value = true
}

const addToCart = (item) => {
  cart.add(item)
  ElMessage.success('已加入购物车')
}

const checkout = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录后再结算')
    router.push('/login')
    return
  }
  if (cart.items.length === 0) {
    ElMessage.warning('购物车为空')
    return
  }
  const payload = {
    items: cart.items.map(i => ({
      productId: i.product.productId,
      quantity: i.qty
    }))
  }
  request.post('/mall/orders', payload).then(() => {
    ElMessage.success('支付成功，即将跳转到订单页')
    cart.clear()
    drawer.value = false
    router.push({ name: 'UserProfile', query: { tab: 'orders' } })
  }).catch((e) => {
    ElMessage.error(e?.response?.data?.message || '下单失败')
  })
}

onMounted(fetchList)
</script>

<style scoped>
.page {
  padding: 16px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.product-card {
  margin-bottom: 16px;
  cursor: pointer;
  transition: transform 0.2s;
}
.product-card:hover {
  transform: translateY(-5px);
}
.card-content {
  padding-bottom: 10px;
}
.cover {
  width: 100%;
  height: 180px;
  object-fit: cover;
  display: block;
}
.info {
  padding: 14px;
}
.title {
  font-weight: 600;
  margin-bottom: 8px;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}
.actions {
  padding: 0 14px 14px;
  text-align: right;
}
.cart-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
  padding: 10px 0;
}
.cart-summary {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 2px solid #eee;
}
</style>

