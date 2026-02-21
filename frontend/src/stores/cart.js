import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'

export const useCartStore = defineStore('cart', () => {
  const items = ref([]) // {product, qty}

  const getUserKey = () => {
    const userStr = localStorage.getItem('user')
    try {
      const u = userStr ? JSON.parse(userStr) : null
      const keyId = u?.userId || u?.username || 'guest'
      return `cart_${keyId}`
    } catch {
      return 'cart_guest'
    }
  }

  const loadFromStorage = () => {
    const key = getUserKey()
    const raw = localStorage.getItem(key)
    if (raw) {
      try {
        const parsed = JSON.parse(raw)
        if (Array.isArray(parsed)) {
          // Filter out invalid items to prevent crashes
          items.value = parsed.filter(i => i && i.product && i.product.productId && typeof i.qty === 'number')
        }
      } catch {}
    }
  }
  // 初始化时加载
  loadFromStorage()

  const count = computed(() => items.value.reduce((sum, i) => sum + (i.qty || 0), 0))
  const totalPrice = computed(() => {
      return items.value.reduce((sum, i) => {
          if (!i.product || !i.product.price) return sum
          return sum + (i.qty || 0) * i.product.price
      }, 0)
  })

  const add = (product) => {
    // Check for self-purchase
    const userStr = localStorage.getItem('user')
    if (userStr) {
      try {
        const u = JSON.parse(userStr)
        if (u.userId && product.sellerId && u.userId === product.sellerId) {
          ElMessage.warning('您不能将自己的商品加入购物车')
          return false
        }
      } catch (e) {}
    }

    const exist = items.value.find((i) => i.product.productId === product.productId)
    if (exist) {
      exist.qty += 1
    } else {
      items.value.push({ product, qty: 1 })
    }
    // 每次变更后持久化
    const key = getUserKey()
    localStorage.setItem(key, JSON.stringify(items.value))
    return true
  }

  const remove = (productId) => {
    const idx = items.value.findIndex((i) => i.product.productId === productId)
    if (idx !== -1) {
      items.value.splice(idx, 1)
      const key = getUserKey()
      localStorage.setItem(key, JSON.stringify(items.value))
    }
  }

  const clear = () => {
    items.value = []
    const key = getUserKey()
    localStorage.setItem(key, JSON.stringify(items.value))
  }

  // 深度监听 items，任何变更均持久化（双保险）
  watch(items, (val) => {
    const key = getUserKey()
    localStorage.setItem(key, JSON.stringify(val))
  }, { deep: true })

  const updateQty = (productId, qty) => {
    const exist = items.value.find((i) => i.product.productId === productId)
    if (exist) {
      if (qty > exist.product.stock) {
        ElMessage.warning(`库存不足，最多可选 ${exist.product.stock} 件`)
        exist.qty = exist.product.stock
      } else if (qty < 1) {
        exist.qty = 1
      } else {
        exist.qty = qty
      }
      const key = getUserKey()
      localStorage.setItem(key, JSON.stringify(items.value))
    }
  }

  return { items, count, totalPrice, add, updateQty, remove, clear }
})

