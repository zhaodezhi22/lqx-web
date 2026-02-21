<template>
  <div class="page">
    <div class="header">
      <h2>文创商城</h2>
      <el-button type="primary" :icon="ShoppingCart" @click="drawer = true">
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
             <el-button 
               type="primary" 
               size="small" 
               :disabled="item.stock <= 0" 
               @click.stop="addToCart(item)"
             >
               <span v-if="item.stock > 0">加入购物车</span>
               <span v-else>已售罄</span>
             </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <ProductDetailModal v-model:visible="detailVisible" :product-id="currentProductId" />

    <el-drawer v-model="drawer" title="我的购物车" size="400px">
      <div v-if="cart.items.length === 0">购物车是空的</div>
      <div v-else>
        <div v-for="(item, index) in cart.items" :key="index" class="cart-item">
          <div class="cart-info" style="flex: 1; margin-right: 10px;">
            <h4>{{ item.product.name }}</h4>
            <div style="display: flex; align-items: center; margin-top: 5px;">
              <span style="margin-right: 8px;">¥ {{ item.product.price }}</span>
              <el-input-number 
                v-model="item.qty" 
                :min="1" 
                :max="item.product.stock" 
                size="small"
                @change="(val) => cart.updateQty(item.product.productId, val)"
                style="width: 100px;"
              />
            </div>
          </div>
          <div class="cart-total" style="display: flex; flex-direction: column; align-items: flex-end;">
            <span style="margin-bottom: 5px;">¥ {{ (item.product.price * item.qty).toFixed(2) }}</span>
            <el-button type="danger" link :icon="Delete" @click="cart.remove(item.product.productId)"></el-button>
          </div>
        </div>
        <div class="cart-summary">
          <div style="margin-bottom: 15px; padding: 10px; background: #f5f7fa; border-radius: 4px;">
            <div v-if="userPoints >= 1000">
                <el-checkbox v-model="usePoints">
                    使用积分抵扣 (可用 {{userPoints}}, 抵扣 ¥{{ (maxDeductiblePoints/1000).toFixed(2) }})
                </el-checkbox>
            </div>
            <div v-else class="text-gray" style="font-size: 12px; color: #909399;">
                当前积分 {{ userPoints }} (满1000可用)
            </div>
          </div>

          <div v-if="usePoints">
              <p style="display: flex; justify-content: space-between; margin: 5px 0;">
                <span>原价:</span> <span>¥ {{ totalAmount }}</span>
              </p>
              <p style="display: flex; justify-content: space-between; margin: 5px 0; color: #f56c6c;">
                <span>积分抵扣:</span> <span>-¥ {{ deductionAmount }}</span>
              </p>
              <h3 style="display: flex; justify-content: space-between; margin-top: 10px; border-top: 1px dashed #ddd; padding-top: 10px;">
                <span>实付:</span> <span>¥ {{ finalPrice }}</span>
              </h3>
          </div>
          <h3 v-else>总计: ¥ {{ totalAmount }}</h3>
          <div style="display: flex; gap: 10px; margin-top: 15px;">
            <el-button type="info" plain style="flex: 1;" @click="clearCart">清空</el-button>
            <el-button type="danger" style="flex: 2;" @click="openCheckoutDialog">去结算</el-button>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- Checkout Dialog -->
    <el-dialog v-model="checkoutVisible" title="确认订单" width="500px">
      <div v-if="checkoutStep === 1">
        <h4 style="margin-bottom: 10px;">选择收货地址</h4>
        <div v-if="myAddresses.length === 0" style="text-align: center; padding: 20px;">
          <p>暂无收货地址</p>
          <el-button type="primary" link @click="goToAddressMgmt">去添加地址</el-button>
        </div>
        <div v-else>
           <el-radio-group v-model="selectedAddressId" style="display: flex; flex-direction: column; width: 100%;">
             <el-radio 
               v-for="addr in myAddresses" 
               :key="addr.id" 
               :label="addr.id" 
               style="margin-bottom: 10px; border: 1px solid #eee; padding: 10px; width: 100%; box-sizing: border-box; height: auto;"
             >
               <div style="display: flex; flex-direction: column; align-items: flex-start; line-height: 1.5;">
                 <span>{{ addr.receiverName }} {{ addr.phone }}</span>
                 <span style="color: #666; font-size: 12px;">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detailAddress }}</span>
               </div>
             </el-radio>
           </el-radio-group>
        </div>
        
        <div style="margin-top: 20px; padding-top: 15px; border-top: 1px solid #eee;">
            <p><strong>订单摘要:</strong></p>
            <p>商品总额: ¥ {{ totalAmount }}</p>
            <p v-if="usePoints" style="color: #f56c6c;">积分抵扣: -¥ {{ deductionAmount }}</p>
            <h3>实付金额: ¥ {{ finalPrice }}</h3>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="checkoutVisible = false">取消</el-button>
          <el-button type="primary" :disabled="!selectedAddressId" @click="confirmOrder" :loading="submitting">
            提交订单
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingCart, Delete } from '@element-plus/icons-vue'
import request from '../utils/request'
import { useCartStore } from '../stores/cart'
import ProductDetailModal from '../components/ProductDetailModal.vue'

const router = useRouter()
const products = ref([])
const placeholder = 'https://via.placeholder.com/300x180?text=Product'
const cart = useCartStore()
const drawer = ref(false)
const checkoutVisible = ref(false)
const checkoutStep = ref(1)
const myAddresses = ref([])
const selectedAddressId = ref(null)
const submitting = ref(false)

const detailVisible = ref(false)
const currentProductId = ref(null)


// Points Logic
const userPoints = ref(0)
const usePoints = ref(false)

// Fix missing totalAmount if it was missing, or override it
const totalAmount = computed(() => {
  if (typeof cart.totalPrice === 'number') {
    return cart.totalPrice.toFixed(2)
  }
  return '0.00'
})

const maxDeductiblePoints = computed(() => {
    if (userPoints.value < 1000) return 0
    const price = cart.totalPrice || 0
    const maxPointsByPrice = Math.floor(price * 1000)
    return Math.min(userPoints.value, maxPointsByPrice)
})

const deductionAmount = computed(() => {
    if (!usePoints.value) return 0
    return (maxDeductiblePoints.value / 1000).toFixed(2)
})

const finalPrice = computed(() => {
    const price = cart.totalPrice || 0
    return (price - deductionAmount.value).toFixed(2)
})

const fetchUserPoints = async () => {
    const userStr = localStorage.getItem('user')
    if (userStr) {
        try {
            const u = JSON.parse(userStr)
            if (u.userId) {
                const res = await request.get('/points/info', { params: { userId: u.userId } })
                if (res.code === 200) {
                    userPoints.value = res.data.currentPoints || 0
                }
            }
        } catch(e) {}
    }
}

watch(drawer, (val) => {
    if (val) {
        fetchUserPoints()
        usePoints.value = false
    }
})

const fetchList = async () => {
  try {
    const res = await request.get('/products', { params: { status: 1 } })
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
  if (item.stock <= 0) {
    ElMessage.warning('该商品暂时无货')
    return
  }
  if (cart.add(item)) {
    ElMessage.success('已加入购物车')
  }
}

const fetchMyAddresses = async () => {
    try {
        const res = await request.get('/user/address/list')
        if (res.code === 200) {
            myAddresses.value = res.data || []
            // Default select the default address
            const defaultAddr = myAddresses.value.find(a => a.isDefault)
            if (defaultAddr) {
                selectedAddressId.value = defaultAddr.id
            } else if (myAddresses.value.length > 0) {
                selectedAddressId.value = myAddresses.value[0].id
            }
        }
    } catch (e) {
        console.error('Fetch addresses failed', e)
    }
}

const openCheckoutDialog = () => {
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
  checkoutVisible.value = true
  checkoutStep.value = 1
  fetchMyAddresses()
}

const goToAddressMgmt = () => {
    // Save current cart state logic if needed, but cart is persisted in localStorage
    checkoutVisible.value = false
    router.push({ name: 'UserProfile', query: { tab: 'address' } })
}

const confirmOrder = async () => {
  if (!selectedAddressId.value) {
      ElMessage.warning('请选择收货地址')
      return
  }
  
  submitting.value = true
  const payload = {
    items: cart.items.map(i => ({
      productId: i.product.productId,
      quantity: i.qty
    })),
    usedPoints: usePoints.value ? maxDeductiblePoints.value : 0,
    addressId: selectedAddressId.value
  }
  
  try {
      await request.post('/mall/orders', payload)
      ElMessage.success('下单成功')
      cart.clear()
      checkoutVisible.value = false
      drawer.value = false
      router.push({ name: 'UserProfile', query: { tab: 'orders' } })
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '下单失败')
  } finally {
      submitting.value = false
  }
}

const clearCart = () => {
  ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    cart.clear()
    ElMessage.success('购物车已清空')
  }).catch(() => {})
}

const checkout = () => {
    openCheckoutDialog()
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

