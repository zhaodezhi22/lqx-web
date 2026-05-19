<template>
  <div class="wallet-panel">
    <el-row :gutter="16">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <template #header>文创收益</template>
          <div class="money-num">¥ {{ formatMoney(summary.mallIncome) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <template #header>票务收益</template>
          <div class="money-num">¥ {{ formatMoney(summary.ticketIncome) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <template #header>累计已提现</template>
          <div class="money-num">¥ {{ formatMoney(summary.withdrawnAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <template #header>当前可提现</template>
          <div class="money-num primary">¥ {{ formatMoney(summary.availableAmount) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="wallet-action" shadow="never">
      <div class="wallet-toolbar">
        <div class="wallet-desc">
          <div class="title">模拟钱包提现</div>
          <div class="sub-title">当前按已完成商城订单与已结算票务收入实时汇总，可直接做模拟提现。</div>
        </div>
        <div class="wallet-buttons">
          <el-button @click="loadSummary">刷新</el-button>
          <el-button type="primary" @click="openWithdrawDialog">申请提现</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="hover">
      <template #header>提现记录</template>
      <el-table :data="summary.recentRecords || []" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="记录ID" width="90" />
        <el-table-column label="提现金额" width="140">
          <template #default="{ row }">¥ {{ formatMoney(row.amount) }}</template>
        </el-table-column>
        <el-table-column prop="accountName" label="收款人" width="120" />
        <el-table-column prop="accountNo" label="收款账号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '模拟到账' : '处理中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="180">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="withdrawDialogVisible" title="申请提现" width="520px">
      <el-form :model="withdrawForm" label-width="100px">
        <el-form-item label="可提现余额">
          <el-input :model-value="`¥ ${formatMoney(summary.availableAmount)}`" disabled />
        </el-form-item>
        <el-form-item label="提现金额">
          <el-input-number
            v-model="withdrawForm.amount"
            :min="0"
            :precision="2"
            :step="10"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="收款人">
          <el-input v-model="withdrawForm.accountName" placeholder="请输入收款人姓名" />
        </el-form-item>
        <el-form-item label="收款账号">
          <el-input v-model="withdrawForm.accountNo" placeholder="请输入银行卡号/支付宝账号" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="withdrawForm.remark" type="textarea" :rows="3" placeholder="可选，填写提现说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="withdrawDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitWithdraw">确认提现</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const loading = ref(false)
const submitting = ref(false)
const withdrawDialogVisible = ref(false)
const summary = reactive({
  mallIncome: 0,
  ticketIncome: 0,
  totalIncome: 0,
  withdrawnAmount: 0,
  availableAmount: 0,
  recentRecords: []
})

const withdrawForm = reactive({
  amount: 0,
  accountName: '',
  accountNo: '',
  remark: ''
})

const formatMoney = (value) => {
  const num = Number(value || 0)
  return num.toFixed(2)
}

const formatDate = (value) => {
  if (!value) return '-'
  if (typeof value === 'string') return value.replace('T', ' ').slice(0, 19)
  if (Array.isArray(value)) {
    const [y, m, d, h, mm, s] = value
    return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(h).padStart(2, '0')}:${String(mm).padStart(2, '0')}:${String(s || 0).padStart(2, '0')}`
  }
  return String(value)
}

const resetWithdrawForm = () => {
  withdrawForm.amount = Number(summary.availableAmount || 0)
  withdrawForm.accountName = ''
  withdrawForm.accountNo = ''
  withdrawForm.remark = ''
}

const loadSummary = async () => {
  loading.value = true
  try {
    const res = await request.get('/wallet/summary')
    Object.assign(summary, res.data || {})
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '加载钱包信息失败')
  } finally {
    loading.value = false
  }
}

const openWithdrawDialog = () => {
  resetWithdrawForm()
  withdrawDialogVisible.value = true
}

const submitWithdraw = async () => {
  if (!withdrawForm.amount || Number(withdrawForm.amount) <= 0) {
    ElMessage.warning('请输入正确的提现金额')
    return
  }
  if (!withdrawForm.accountName.trim()) {
    ElMessage.warning('请输入收款人')
    return
  }
  if (!withdrawForm.accountNo.trim()) {
    ElMessage.warning('请输入收款账号')
    return
  }
  submitting.value = true
  try {
    await request.post('/wallet/withdraw', {
      amount: withdrawForm.amount,
      accountName: withdrawForm.accountName,
      accountNo: withdrawForm.accountNo,
      remark: withdrawForm.remark
    })
    ElMessage.success('模拟提现成功，已记录到账')
    withdrawDialogVisible.value = false
    loadSummary()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '提现失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadSummary)
</script>

<style scoped>
.wallet-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.money-num {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  text-align: center;
  padding: 10px 0;
}

.money-num.primary {
  color: #409eff;
}

.wallet-action {
  border: 1px solid #ebeef5;
}

.wallet-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.sub-title {
  margin-top: 6px;
  color: #909399;
  font-size: 13px;
}

.wallet-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
</style>
