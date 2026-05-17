<template>
  <div class="home-products">
    <SectionTitle text="非遗礼物铺" more-link="/products" />
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="8" v-for="p in productsToShow" :key="p.productId" class="product-col">
        <el-card class="product-card" shadow="hover" @click="viewProduct(p.productId)">
          <div class="pic">
            <img :src="p.mainImage || placeholder" />
            <span v-if="p.stock > 0" class="stock-badge">在售</span>
            <span v-else class="stock-badge sold-out">售罄</span>
          </div>
          <div class="card-body">
            <div class="name">{{ p.name }}</div>
            <div class="card-footer">
              <div class="price">¥{{ p.price }}</div>
              <div class="ops">
                <el-button circle size="small" type="primary" @click.stop="addToCart(p)">
                  <el-icon><ShoppingCart /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col v-if="!loading && productsToShow.length === 0">
        <el-empty description="暂无商品" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { ShoppingCart } from '@element-plus/icons-vue'
import SectionTitle from '../common/SectionTitle.vue'
import { useCartStore } from '../../stores/cart'

const props = defineProps({
  products: { type: Array, default: () => [] }
})

const products = ref([])
const displayProducts = ref([])
const loading = ref(false)
const placeholder = 'https://images.unsplash.com/photo-1546435770-a3e426bf472b?q=80&w=1200&auto=format&fit=crop'
const cartStore = useCartStore()
const router = useRouter()

const productsToShow = computed(() => {
  return displayProducts.value
})

const pickRandomProducts = (list) => {
  if (!Array.isArray(list) || list.length === 0) {
    displayProducts.value = []
    return
  }

  const shuffled = [...list].sort(() => Math.random() - 0.5)
  displayProducts.value = shuffled.slice(0, 3)
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await request.get('/products', { params: { status: 1 } })
    products.value = res.data || []
    if (!props.products || props.products.length === 0) {
      pickRandomProducts(products.value)
    }
  } finally {
    loading.value = false
  }
}

const addToCart = (p) => {
  if (p.stock <= 0) {
    ElMessage.warning('该商品暂时无货')
    return
  }

  if (cartStore.add(p)) {
    ElMessage.success('已加入购物车，请前往购物车完成支付')
  }
}

const viewProduct = (productId) => {
  router.push(`/products/${productId}`)
}

watch(
  () => props.products,
  (list) => {
    if (list && list.length) {
      pickRandomProducts(list)
    } else if (products.value.length) {
      pickRandomProducts(products.value)
    } else {
      displayProducts.value = []
    }
  },
  { immediate: true }
)

onMounted(() => {
  if (!props.products || props.products.length === 0) {
    fetchProducts()
  }
})
</script>

<style scoped>
.home-products {
  height: 100%;
}

.product-col {
  display: flex;
}

.product-card .pic {
  height: 220px;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}

.product-card {
  width: 100%;
  cursor: pointer;
  border: none;
  border-radius: 18px;
  height: 100%;
}

.product-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px;
}

.product-card img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform .3s;
}

.stock-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  color: #aa1d1d;
  font-size: 12px;
  font-weight: 600;
}

.stock-badge.sold-out {
  color: #909399;
}

.card-body {
  display: flex;
  flex-direction: column;
  flex: 1;
  padding-top: 14px;
}

.product-card .name {
  color: #333;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.6;
  min-height: 52px;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.card-footer {
  margin-top: auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.product-card .price {
  color: #AA1D1D;
  font-size: 24px;
  font-weight: 700;
}

.product-card .ops {
  flex-shrink: 0;
}

.product-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 24px rgba(170, 29, 29, .12);
}

.product-card:hover img {
  transform: scale(1.05);
}
</style>
