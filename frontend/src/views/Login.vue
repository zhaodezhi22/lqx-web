<template>
  <div class="login-container">
    <div class="login-left">
      <div class="login-info">
        <h1 class="title">临沂柳琴戏</h1>
        <p class="subtitle">非物质文化遗产传承与保护平台</p>
        <div class="decoration-line"></div>
        <p class="desc">
          传承千年文化，演绎时代新声。<br>
          加入我们，共同守护这一抹乡音。
        </p>
      </div>
    </div>
    
    <div class="login-right">
      <div class="form-wrapper">
        <h2 class="form-title">欢迎登录</h2>
        <p class="form-subtitle">请输入您的账号和密码</p>
        
        <el-form 
          :model="form" 
          @submit.prevent="onSubmit" 
          size="large"
          class="login-form"
        >
          <el-form-item>
            <el-input 
              v-model="form.username" 
              placeholder="用户名" 
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item>
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="密码" 
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              @click="onSubmit" 
              :loading="loading" 
              class="submit-btn"
            >
              登 录
            </el-button>
          </el-form-item>
          
          <div class="form-footer">
            <span>还没有账号？</span>
            <el-link type="primary" @click="$router.push({ name: 'Register' })">立即注册</el-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import request from '../utils/request'
import { useRouter } from 'vue-router'

const router = useRouter()
const form = reactive({
  username: '',
  password: '',
})
const loading = ref(false)

const onSubmit = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  
  loading.value = true
  try {
    const res = await request.post('/auth/login', form)
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data.user))
    
    ElMessage.success('登录成功')
    
    const role = res.data.user.role
    if (role >= 2) {
      router.push('/admin')
    } else {
      router.push('/')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  width: 100%;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #2c3e50 0%, #000000 100%);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  overflow: hidden;
}

/* Add a pseudo-element for a texture or pattern overlay */
.login-left::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: radial-gradient(circle at 20% 50%, rgba(170, 29, 29, 0.3) 0%, transparent 50%),
                    radial-gradient(circle at 80% 80%, rgba(170, 29, 29, 0.2) 0%, transparent 50%);
  z-index: 1;
}

.login-info {
  position: relative;
  z-index: 2;
  text-align: center;
  padding: 0 40px;
}

.title {
  font-family: 'Noto Serif SC', serif;
  font-size: 48px;
  margin-bottom: 16px;
  font-weight: bold;
  letter-spacing: 4px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.5);
}

.subtitle {
  font-size: 20px;
  margin-bottom: 30px;
  opacity: 0.9;
  font-weight: 300;
}

.decoration-line {
  width: 60px;
  height: 4px;
  background-color: #AA1D1D;
  margin: 0 auto 30px;
  border-radius: 2px;
}

.desc {
  font-size: 16px;
  line-height: 1.8;
  opacity: 0.8;
  font-family: 'Noto Serif SC', serif;
}

.login-right {
  width: 500px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: -10px 0 20px rgba(0,0,0,0.05);
  position: relative;
  z-index: 10;
}

.form-wrapper {
  width: 100%;
  max-width: 360px;
  padding: 40px;
}

.form-title {
  font-size: 32px;
  color: #333;
  margin-bottom: 10px;
  font-weight: 600;
}

.form-subtitle {
  color: #999;
  margin-bottom: 40px;
  font-size: 14px;
}

.login-form .el-input {
  --el-input-height: 48px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  letter-spacing: 2px;
  margin-top: 10px;
  background-color: #AA1D1D;
  border-color: #AA1D1D;
  transition: all 0.3s;
}

.submit-btn:hover {
  background-color: #c92e2e;
  border-color: #c92e2e;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(170, 29, 29, 0.3);
}

.form-footer {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #666;
}

.form-footer .el-link {
  margin-left: 5px;
  vertical-align: baseline;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }
  .login-left {
    min-height: 200px;
    flex: none;
  }
  .login-right {
    width: 100%;
    flex: 1;
  }
  .title {
    font-size: 32px;
  }
}
</style>