<template>
  <div class="home-products">
    <SectionTitle text="—— 非遗礼物铺 ——" />
    <el-row :gutter="20">
      <el-col :span="4" v-for="p in productsToShow" :key="p.productId">
        <el-card class="product-card" shadow="hover">
          <div class="pic">
            <img :src="p.mainImage || placeholder" />
          </div>
          <div class="name">{{ p.name }}</div>
          <div class="price">¥{{ p.price }}</div>
          <div class="ops">
            <el-button circle size="small" type="primary" @click="addToCart(p)">
              <el-icon><ShoppingCart /></el-icon>
            </el-button>
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
import { ref, onMounted, computed } from 'vue'
import request from '../../utils/request'
import { ShoppingCart } from '@element-plus/icons-vue'
import SectionTitle from '../common/SectionTitle.vue'

const props = defineProps({
  products: { type: Array, default: () => [] }
})

const products = ref([])
const loading = ref(false)
const placeholder = 'https://images.unsplash.com/photo-1546435770-a3e426bf472b?q=80&w=1200&auto=format&fit=crop'

const productsToShow = computed(() => {
  return props.products && props.products.length ? props.products : products.value
})

const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await request.get('/products', { params: { status: 1 } })
    products.value = res.data || []
  } finally {
    loading.value = false
  }
}

const addToCart = (p) => {
  // Placeholder for cart operation
  console.log('Add to cart:', p.productId)
}

onMounted(() => {
  if (!props.products || props.products.length === 0) {
    fetchProducts()
  }
})
</script>

<style scoped>
.product-card .pic {
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
  transition: transform .3s;
}
.product-card img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform .3s;
}
.product-card .name {
  margin-top: 8px;
  color: #333;
}
.product-card .price {
  margin-top: 4px;
  color: #AA1D1D;
  font-weight: bold;
}
.product-card .ops {
  margin-top: 8px;
  text-align: right;
}
.product-card:hover {
  box-shadow: 0 12px 24px rgba(170, 29, 29, .12);
}
.product-card:hover img {
  transform: scale(1.05);
}
</style>
