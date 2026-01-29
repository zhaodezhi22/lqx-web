<template>
  <div class="register-container">
    <div class="register-left">
      <div class="register-info">
        <h1 class="title">临沂柳琴戏</h1>
        <p class="subtitle">非物质文化遗产传承与保护平台</p>
        <div class="decoration-line"></div>
        <p class="desc">
          注册成为会员，开启非遗之旅。<br>
          探索、学习、交流，让传统更有活力。
        </p>
      </div>
    </div>
    
    <div class="register-right">
      <div class="form-wrapper">
        <h2 class="form-title">创建账号</h2>
        <p class="form-subtitle">请填写以下信息完成注册</p>
        
        <el-form 
          :model="form" 
          :rules="rules" 
          ref="formRef" 
          @submit.prevent="onSubmit"
          size="large"
          class="register-form"
        >
          <el-form-item prop="username">
            <el-input 
              v-model="form.username" 
              placeholder="用户名" 
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="密码" 
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          
          <!-- Role selection removed as requested. Default is 0 (User) -->
          
          <el-form-item>
            <el-button 
              type="primary" 
              @click="onSubmit" 
              :loading="loading" 
              class="submit-btn"
            >
              注 册
            </el-button>
          </el-form-item>
          
          <div class="form-footer">
            <span>已有账号？</span>
            <el-link type="primary" @click="$router.push({ name: 'Login' })">立即登录</el-link>
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
  role: 0, // Default to normal user
})
const loading = ref(false)
const formRef = ref()

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

const onSubmit = async () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await request.post('/auth/register', form)
      ElMessage.success('注册成功，请登录')
      router.push({ name: 'Login' })
    } catch (e) {
      ElMessage.error(e?.response?.data?.message || '注册失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.register-container {
  display: flex;
  min-height: 100vh;
  width: 100%;
}

.register-left {
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
.register-left::before {
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

.register-info {
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

.register-right {
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

.register-form .el-input {
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
  .register-container {
    flex-direction: column;
  }
  .register-left {
    min-height: 200px;
    flex: none;
  }
  .register-right {
    width: 100%;
    flex: 1;
  }
  .title {
    font-size: 32px;
  }
}
</style>