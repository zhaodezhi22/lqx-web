<template>
  <div class="admin-layout">
    <el-aside width="200px" class="sidebar">
      <div class="logo">系统管理后台</div>
      <el-menu
        :default-active="$route.path"
        class="el-menu-vertical"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        
        <el-sub-menu index="1" v-if="isAdmin">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>非遗业务</span>
          </template>
          <el-menu-item index="/admin/audit-center">统一审核中心</el-menu-item>
          <el-menu-item index="/admin/inheritor-review">传承人审核</el-menu-item>
          <el-menu-item index="/admin/inheritor-level-audit">传承人等级审核</el-menu-item>
          <el-menu-item index="/admin/resource-audit">资源审核</el-menu-item>
          <el-menu-item index="/admin/event-audit">活动审核</el-menu-item>
          <el-menu-item index="/admin/product-audit">商品审核</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="2" v-if="isAdmin">
          <template #title>
            <el-icon><Money /></el-icon>
            <span>商业运营</span>
          </template>
          <el-menu-item index="/admin/tickets">票务管理</el-menu-item>
          <el-menu-item index="/admin/mall">商城管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="3" v-if="isAdmin">
          <template #title>
            <el-icon><Warning /></el-icon>
            <span>内容治理</span>
          </template>
          <el-menu-item index="/admin/resources">资源库管理</el-menu-item>
          <el-menu-item index="/admin/lineage">师承关系管理</el-menu-item>
          <el-menu-item index="/admin/comments">评论审核</el-menu-item>
          <el-menu-item index="/admin/posts">帖子管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="5" v-if="isAdmin">
          <template #title>
            <el-icon><Monitor /></el-icon>
            <span>站点管理</span>
          </template>
          <el-menu-item index="/admin/home-content">首页内容</el-menu-item>
          <el-menu-item index="/admin/users" v-if="isRoot">用户管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="4" v-if="isAdmin">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/admin/logs">操作日志</el-menu-item>
          <el-menu-item index="/admin/settings" v-if="isRoot">系统设置</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="breadcrumb">
          <!-- Breadcrumb could go here -->
        </div>
        <div class="user-actions">
          <el-dropdown>
            <span class="el-dropdown-link">
              管理员: {{ username }}
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/')">返回前台</el-dropdown-item>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Odometer, Document, Setting, ArrowDown, Money, Warning, Monitor } from '@element-plus/icons-vue'
import { getCurrentUser, isAdminRole, isRootRole } from '../utils/permission'

const router = useRouter()
const username = ref('')
const role = ref(0)
const isAdmin = computed(() => isAdminRole())
const isRoot = computed(() => isRootRole())

onMounted(() => {
  const user = getCurrentUser()
  if (user) {
    username.value = user.username
    role.value = user.role
    if (!isAdmin.value) {
      router.push('/')
    }
  } else {
    router.push('/login')
  }
})

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  display: flex;
}
.sidebar {
  background-color: #304156;
  color: #fff;
  display: flex;
  flex-direction: column;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  background-color: #2b3649;
}
.el-menu-vertical {
  border-right: none;
}
.header {
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
}
.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
