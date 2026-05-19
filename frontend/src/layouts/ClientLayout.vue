<template>
  <div class="client-layout">
    <el-header class="header sticky-header">
      <div class="header-content">
        <!-- Logo -->
        <div class="logo" @click="$router.push('/')">
          <span class="logo-text">临沂柳琴戏非遗传承</span>
        </div>

        <!-- Menu -->
        <div class="menu-container">
          <el-menu 
            mode="horizontal" 
            :router="true" 
            :default-active="$route.path"
            class="custom-menu"
            :ellipsis="false"
          >
            <el-menu-item index="/home">首页</el-menu-item>
            <el-menu-item index="/resources">戏曲资源</el-menu-item>
            <el-menu-item index="/inheritors">传承人</el-menu-item>
            <el-menu-item index="/events">演出订票</el-menu-item>
            <el-menu-item index="/products">文创商城</el-menu-item>
            <el-menu-item index="/community">社区交流</el-menu-item>
          </el-menu>
        </div>

        <!-- Search Bar -->
        <div class="search-bar">
          <el-input
            v-model="searchQuery"
            placeholder="搜索资源、商品..."
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #suffix>
              <el-icon class="search-icon" @click="handleSearch"><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <!-- User Actions -->
        <div class="user-actions">
          <div class="signin-icon" v-if="username" @click="handleQuickSignIn" title="每日签到">
             <el-icon><Calendar /></el-icon>
          </div>
          <el-dropdown v-if="username">
            <span class="el-dropdown-link">
              <el-avatar :size="32" :src="userAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
              <span class="username">{{ username }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item v-if="role == 1" @click="$router.push('/inheritor/center')">非遗工作台</el-dropdown-item>
                <el-dropdown-item v-if="role == 2 || role == 3" @click="$router.push('/admin/dashboard')">管理后台</el-dropdown-item>
                <el-dropdown-item v-if="role == 0" @click="$router.push('/inheritor/apply')">申请传承人</el-dropdown-item>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <div v-else class="auth-buttons">
            <el-button type="primary" class="login-btn" @click="$router.push('/login')">登录</el-button>
            <el-button class="register-btn" @click="$router.push('/register')">注册</el-button>
          </div>
        </div>
      </div>
    </el-header>

    <el-main class="main-content">
      <router-view />
    </el-main>

    <el-footer class="footer">
      <div class="footer-content">
        <div class="footer-links">
          <a @click.prevent="$router.push('/about')">关于我们</a>
          <span class="sep">|</span>
          <a @click.prevent="$router.push('/contact')">联系</a>
          <span class="sep">|</span>
          <a @click.prevent="$router.push('/privacy')">隐私政策</a>
        </div>
        <div class="footer-copy">
          © 2024 临沂柳琴戏非遗传承系统
        </div>
      </div>
    </el-footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, Search, Calendar, Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { useChatStore } from '../stores/chat'
import { storeToRefs } from 'pinia'

const router = useRouter()
const chatStore = useChatStore()
const { totalUnreadCount } = storeToRefs(chatStore)
const username = ref('')
const userId = ref(null)
const role = ref(0)
const userAvatar = ref('')
const searchQuery = ref('')

onMounted(() => {
  const userStr = localStorage.getItem('user')
  const token = localStorage.getItem('token')
  
  if (token) {
    chatStore.connect(token)
  }

  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      username.value = user.username
      userId.value = user.userId
      role.value = user.role
      userAvatar.value = user.avatar
    } catch (e) {
      // ignore
    }
  }
})

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}

const handleSearch = () => {
  const q = searchQuery.value && searchQuery.value.trim()
  if (!q) return
  router.push({ path: '/search', query: { keyword: q } })
}

const handleQuickSignIn = async () => {
    if (!userId.value) return
    try {
        // Check sign in status first or just try to sign in
        // If we want to jump to points page anyway, we can do:
        // Try sign in -> if success -> show msg -> jump
        // If already signed -> show msg -> jump
        
        // Let's try sign in directly.
        const res = await request.post('/points/signin', null, { params: { userId: userId.value } })
        if (res.code === 200) {
            const { points, continuousSignDays } = res.data
            ElMessage.success(`签到成功！获得 ${points} 积分`)
        } else {
            // Probably already signed in or error
            // We can assume if code!=200 it might be "already signed in" or just show message
            // If the message says "Today already signed in", it's fine.
            if (res.message && res.message.includes('已签到')) {
                 ElMessage.info('今日已签到')
            } else {
                 // ElMessage.error(res.message || '签到失败')
            }
        }
    } catch (e) {
        // If error, just ignore and jump
        if (e.response?.data?.message?.includes('已签到')) {
             ElMessage.info('今日已签到')
        }
    }
    
    // Jump to points tab
    router.push({ path: '/profile', query: { tab: 'points' } })
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;700&display=swap');

.client-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #F8F3E6; /* Beige / Rice Paper */
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}

.sticky-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background-color: #fff;
  border-bottom: 3px solid #AA1D1D; /* Chinese Red */
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  padding: 0;
  height: 70px;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-right: 20px;
}

.logo-text {
  font-family: 'Noto Serif SC', 'Songti SC', serif;
  font-size: 24px;
  font-weight: bold;
  color: #AA1D1D;
  letter-spacing: 1px;
}

.menu-container {
  flex: 1;
  display: flex;
  justify-content: center;
}

.custom-menu {
  border-bottom: none !important;
  background-color: transparent;
}

.custom-menu .el-menu-item {
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.custom-menu .el-menu-item:hover,
.custom-menu .el-menu-item.is-active {
  color: #AA1D1D !important;
  background-color: rgba(170, 29, 29, 0.05) !important;
  border-bottom: 2px solid #AA1D1D !important;
}

.search-bar {
  margin: 0 20px;
  width: 250px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 20px;
  background-color: #f5f5f5;
  box-shadow: none;
  border: 1px solid #e0e0e0;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: #AA1D1D;
  box-shadow: 0 0 0 1px #AA1D1D;
}

.search-icon {
  cursor: pointer;
  transition: color 0.2s;
}

.search-icon:hover {
  color: #AA1D1D;
}

.user-actions {
  display: flex;
  align-items: center;
  min-width: 100px;
  justify-content: flex-end;
}

.signin-icon {
  cursor: pointer;
  margin-right: 15px;
  color: #AA1D1D;
  font-size: 20px;
  display: flex;
  align-items: center;
  transition: transform 0.2s;
}

.signin-icon:hover {
  transform: scale(1.1);
}

.message-icon {
  margin-right: 20px;
  cursor: pointer;
  position: relative;
  display: flex;
  align-items: center;
  font-size: 20px;
  color: #606266;
}

.message-icon:hover {
  color: #AA1D1D;
}

.unread-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background-color: #f56c6c;
  color: #fff;
  border-radius: 10px;
  padding: 0 5px;
  font-size: 12px;
  line-height: 18px;
  height: 18px;
  min-width: 18px;
  text-align: center;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #333;
}

.username {
  margin: 0 8px;
  font-weight: 500;
}

.auth-buttons {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: nowrap;
}

.auth-buttons :deep(.el-button + .el-button) {
  margin-left: 0;
}

.auth-buttons .login-btn {
  background-color: #AA1D1D;
  border-color: #AA1D1D;
}

.auth-buttons .register-btn {
  color: #AA1D1D;
  border-color: #AA1D1D;
  background-color: transparent;
}

.auth-buttons .register-btn:hover {
  background-color: rgba(170, 29, 29, 0.1);
}

.main-content {
  flex: 1;
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  box-sizing: border-box;
}

.footer {
  background-color: #5A0F0F;
  color: #f0e6e6;
  padding: 30px 0;
  text-align: center;
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
}
.footer-links {
  margin-bottom: 8px;
}
.footer-links a {
  color: #f0e6e6;
  text-decoration: none;
  padding: 0 8px;
  cursor: pointer;
}
.footer-links a:hover {
  text-decoration: underline;
}
.footer-links .sep {
  opacity: .6;
}
.footer-copy {
  font-size: 13px;
  opacity: .85;
}
</style>
