<template>
  <div class="page">
    <el-card v-if="applicationStatus === 0">
      <el-result
        icon="info"
        title="申请审核中"
        sub-title="您的传承人申请正在审核中，请耐心等待。"
      >
      </el-result>
    </el-card>

    <el-card v-else-if="applicationStatus === 1">
      <el-result
        icon="success"
        title="申请已通过"
        sub-title="恭喜您已成为认证传承人！请重新登录以激活您的身份权限。"
      >
        <template #extra>
          <el-button type="primary" @click="reLogin">重新登录</el-button>
        </template>
      </el-result>
    </el-card>

    <el-card v-else>
      <div v-if="applicationStatus === 2" style="margin-bottom: 20px;">
         <el-alert
           title="申请被驳回"
           type="error"
           :description="'驳回原因：' + (auditRemark || '无')"
           show-icon
           :closable="false"
         />
         <div style="margin-top: 10px; text-align: center;">
            <el-button type="primary" plain @click="applicationStatus = null">重新申请</el-button>
         </div>
      </div>

      <div v-else>
        <h2>传承人申请</h2>
        <el-form :model="form" label-width="100px">
          <el-form-item label="传承等级">
            <el-select v-model="form.level" placeholder="请选择等级">
               <el-option label="国家级" value="国家级" />
               <el-option label="省级" value="省级" />
               <el-option label="市级" value="市级" />
               <el-option label="县/区级" value="县/区级" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="流派">
            <el-select v-model="genreSelect" placeholder="请选择流派" @change="handleGenreChange">
              <el-option label="徐派" value="徐派" />
              <el-option label="王派" value="王派" />
              <el-option label="李派" value="李派" />
              <el-option label="其他" value="其他" />
            </el-select>
            <el-input 
              v-if="genreSelect === '其他'" 
              v-model="form.genre" 
              placeholder="请输入具体流派" 
              style="margin-top: 10px"
            />
          </el-form-item>

          <el-form-item label="师承">
             <div v-if="hasVerifiedMaster" style="color: #67C23A; font-weight: bold;">
               <el-icon><Check /></el-icon> 已关联授业恩师：{{ form.masterName }} ({{ masterLevel }})
             </div>
             <div v-else>
               <div style="display: flex; align-items: center; gap: 10px;">
                  <el-select
                      v-model="masterSelectValue"
                      filterable
                      remote
                      placeholder="请搜索并选择现有传承人"
                      :remote-method="searchMasters"
                      :loading="masterLoading"
                      @change="handleMasterChange"
                      :disabled="noMaster"
                      style="flex: 1"
                  >
                      <el-option
                        v-for="item in masterOptions"
                        :key="item.id"
                        :label="item.name + ' (' + item.level + ')'"
                        :value="item.id"
                      />
                  </el-select>
                  <el-checkbox v-model="noMaster" label="无师承 (自主传承)" @change="handleNoMasterChange" />
               </div>
               <div class="tip" style="color: #909399; font-size: 12px; margin-top: 5px;" v-if="!noMaster">
                 * 为保证谱系完整，请务必从系统中选择已认证的传承人作为师父
               </div>
             </div>
          </el-form-item>

          <el-form-item label="演艺经历">
            <el-input v-model="form.artisticCareer" type="textarea" :rows="4" />
          </el-form-item>
          <el-form-item label="获奖情况">
            <el-input v-model="form.awards" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="submit">提交申请</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import request from '../utils/request'
import { useRouter } from 'vue-router'

const router = useRouter()
const form = reactive({
  level: '',
  genre: '',
  masterName: '',
  masterId: null,
  artisticCareer: '',
  awards: '',
})

const genreSelect = ref('')
const masterSelectValue = ref(null)
const masterOptions = ref([])
const masterLoading = ref(false)
const loading = ref(false)

const hasVerifiedMaster = ref(false)
const masterLevel = ref('')
const noMaster = ref(false)

const applicationStatus = ref(null) // null=none, 0=pending, 1=approved, 2=rejected
const auditRemark = ref('')

onMounted(async () => {
  // Check application status first
  try {
    const res = await request.get('/inheritor/my-status')
    if (res.code === 200 && res.data) {
        applicationStatus.value = res.data.verifyStatus
        auditRemark.value = res.data.auditRemark
        
        // Pre-fill form if rejected
        if (applicationStatus.value === 2) {
             form.level = res.data.level
             form.genre = res.data.genre
             form.masterName = res.data.masterName
             form.masterId = res.data.masterId
             form.artisticCareer = res.data.artisticCareer
             form.awards = res.data.awards
             
             // Handle Genre Select
             if (['徐派', '王派', '李派'].includes(res.data.genre)) {
                 genreSelect.value = res.data.genre
             } else {
                 genreSelect.value = '其他'
             }
             
             // Handle Master Select
             if (res.data.masterId) {
                masterSelectValue.value = res.data.masterId
                // Need to fetch master name if not in data (data has masterName though)
                masterOptions.value = [{id: res.data.masterId, name: res.data.masterName, level: '未知'}]
             } else {
                 noMaster.value = true
             }
        }
    }
  } catch (e) {
    console.error('Check status failed', e)
  }

  if (applicationStatus.value !== 0 && applicationStatus.value !== 1) {
      checkMyMaster()
  }
})

const checkMyMaster = async () => {
  try {
    const res = await request.get('/master/apprentice/my-master')
    if (res.code === 200 && res.data) {
       hasVerifiedMaster.value = true
       form.masterId = res.data.id
       form.masterName = res.data.name
       masterLevel.value = res.data.level
       masterSelectValue.value = res.data.id
       masterOptions.value = [res.data]
    }
  } catch (e) {
    console.error('Check master failed', e)
  }
}

const reLogin = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}

const handleNoMasterChange = (val) => {
  if (val) {
    masterSelectValue.value = null
    form.masterId = null
    form.masterName = ''
  }
}

const handleGenreChange = (val) => {
  if (val !== '其他') {
    form.genre = val
  } else {
    form.genre = '' // Clear for manual input
  }
}

const searchMasters = async (query) => {
  if (!query) {
    masterOptions.value = []
    return
  }
  masterLoading.value = true
  try {
    const res = await request.get('/inheritor/masters', { params: { query } })
    if (res.code === 200) {
      masterOptions.value = res.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    masterLoading.value = false
  }
}

const handleMasterChange = (val) => {
    const selected = masterOptions.value.find(item => item.id === val)
    if (selected) {
      form.masterId = selected.id
      form.masterName = selected.name
    }
}

const submit = async () => {
  if (genreSelect.value === '其他' && !form.genre) {
     ElMessage.warning('请输入具体流派')
     return
  }
  
  // If not "No Master" and no master selected
  if (!noMaster.value && !form.masterId) {
     ElMessage.warning('请搜索并选择师父，或勾选“无师承”')
     return
  }
  
  loading.value = true
  try {
    await request.post('/inheritor/apply', form)
    ElMessage.success('申请已提交，等待管理员审核')
    applicationStatus.value = 0
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '提交失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page {
  padding: 16px;
}
</style>

