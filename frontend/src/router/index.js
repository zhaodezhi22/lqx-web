import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  // Auth routes (Standalone)
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },

  // Client Portal (Roles 0 & 1)
  {
    path: '/',
    component: () => import('../layouts/ClientLayout.vue'),
    redirect: '/home',
    children: [
      { path: 'home', name: 'Home', component: () => import('../views/Home.vue') },
      { path: 'resources', name: 'ResourceList', component: () => import('../views/ResourceList.vue') },
      { path: 'resources/:id', name: 'ResourceDetail', component: () => import('../views/ResourceDetail.vue') },
      { path: 'products', name: 'ProductMall', component: () => import('../views/ProductMall.vue') },
      { path: 'events', name: 'EventList', component: () => import('../views/EventList.vue') },
      { path: 'events/:id', name: 'EventDetail', component: () => import('../views/EventDetail.vue') },
      { path: 'inheritors', name: 'InheritorList', component: () => import('../views/InheritorList.vue') },
      { path: 'inheritor/apply', name: 'InheritorApply', component: () => import('../views/InheritorApply.vue') },
      { path: 'inheritor/graph', name: 'InheritorGraph', component: () => import('../views/InheritorGraph.vue') },
      { path: 'inheritor/center', name: 'InheritorCenter', component: () => import('../views/InheritorCenter.vue') },
      { path: 'about', name: 'About', component: () => import('../views/About.vue') },
      { path: 'contact', name: 'Contact', component: () => import('../views/Contact.vue') },
      { path: 'privacy', name: 'Privacy', component: () => import('../views/Privacy.vue') },
      { path: 'community', name: 'Community', component: () => import('../views/CommunityIndex.vue') },
      { path: 'community/detail/:id', name: 'CommunityDetail', component: () => import('../views/CommunityDetail.vue') },
      { path: 'profile', name: 'UserProfile', component: () => import('../views/UserProfile.vue') },
    ]
  },

  // Admin Dashboard (Roles 2 & 3)
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    children: [
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('../views/AdminDashboard.vue') },
      { path: 'inheritor-review', name: 'AdminInheritorReview', component: () => import('../views/AdminInheritorReview.vue') },
      { path: 'resources', name: 'AdminResource', component: () => import('../views/AdminResource.vue') },
      { path: 'lineage', name: 'AdminLineageMgmt', component: () => import('../views/AdminLineageMgmt.vue') },
      { path: 'tickets', name: 'AdminTicketMgmt', component: () => import('../views/AdminTicketMgmt.vue') },
      { path: 'mall', name: 'AdminMallOrder', component: () => import('../views/AdminMallOrder.vue') },
      { path: 'comments', name: 'AdminCommentAudit', component: () => import('../views/AdminCommentAudit.vue') },
      { path: 'users', name: 'AdminUser', component: () => import('../views/AdminUser.vue') },
      { path: 'logs', name: 'AdminSystemLogs', component: () => import('../views/AdminSystemLogs.vue') },
      { path: 'settings', name: 'AdminSettings', component: () => import('../views/AdminSettings.vue') },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userStr = localStorage.getItem('user')
  let role = 0
  if (userStr) {
    try {
      role = JSON.parse(userStr).role
    } catch (e) {
      role = 0
    }
  }

  // Protect User Profile
  if (to.name === 'UserProfile' && !token) {
    return next({ name: 'Login' })
  }

  // Admin Routes Guard
  if (to.path.startsWith('/admin')) {
    if (!token || role < 2) {
      alert('无权访问后台管理系统')
      return next({ path: '/' })
    }
  }

  // Inheritor Routes Guard
  if (to.path.startsWith('/inheritor/center')) {
    if (!token || role !== 1) {
      alert('仅传承人可访问')
      return next({ path: '/' })
    }
  }

  next()
})

export default router
