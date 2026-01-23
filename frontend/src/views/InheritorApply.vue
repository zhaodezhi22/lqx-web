<template>
  <div class="page">
    <el-card>
      <h2>传承人申请</h2>
      <el-form :model="form" label-width="100px">
        <el-form-item label="传承等级">
          <el-input v-model="form.level" placeholder="如：国家级、省级、市级" />
        </el-form-item>
        <el-form-item label="流派">
          <el-input v-model="form.genre" />
        </el-form-item>
        <el-form-item label="师承">
          <el-input v-model="form.masterName" />
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
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const form = reactive({
  level: '',
  genre: '',
  masterName: '',
  artisticCareer: '',
  awards: '',
})
const loading = ref(false)

const submit = async () => {
  loading.value = true
  try {
    await request.post('/inheritor/apply', form)
    ElMessage.success('申请已提交，等待管理员审核')
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

