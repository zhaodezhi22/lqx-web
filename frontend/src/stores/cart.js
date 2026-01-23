import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

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
          items.value = parsed
        }
      } catch {}
    }
  }
  // 初始化时加载
  loadFromStorage()

  const count = computed(() => items.value.reduce((sum, i) => sum + i.qty, 0))
  const totalPrice = computed(() => items.value.reduce((sum, i) => sum + i.qty * i.product.price, 0))

  const add = (product) => {
    const exist = items.value.find((i) => i.product.productId === product.productId)
    if (exist) {
      exist.qty += 1
    } else {
      items.value.push({ product, qty: 1 })
    }
    // 每次变更后持久化
    const key = getUserKey()
    localStorage.setItem(key, JSON.stringify(items.value))
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

  return { items, count, add, clear }
})

