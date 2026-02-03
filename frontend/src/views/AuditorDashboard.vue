<template>
  <div class="auditor-dashboard">
    <div class="dashboard-header">
      <div class="header-left">
        <h1>非遗文化平台</h1>
        <span class="subtitle">全局运营监控中心</span>
      </div>
      <div class="header-right">
        <el-radio-group v-model="revenuePeriod" size="small" @change="handlePeriodChange" class="period-selector">
            <el-radio-button label="day">近15天</el-radio-button>
            <el-radio-button label="week">近8周</el-radio-button>
            <el-radio-button label="month">近6月</el-radio-button>
        </el-radio-group>
        <div class="date-display">{{ currentDate }}</div>
      </div>
    </div>

    <!-- 顶部数据翻牌器 -->
    <div class="data-cards">
      <div class="card" v-for="(item, index) in topCards" :key="index">
        <div class="card-icon-bg">
          <component :is="item.icon" />
        </div>
        <div class="card-info">
          <div class="card-label">{{ item.label }}</div>
          <div class="card-value">{{ item.value.toLocaleString() }}</div>
        </div>
      </div>
    </div>

    <!-- 中间图表区域 -->
    <div class="charts-container">
      <!-- 左侧：商城收益统计 -->
      <div class="chart-box main-trend">
        <div class="box-header">
          <span class="header-icon">📊</span>
          <h3>商城收益统计</h3>
        </div>
        <div ref="mallChartRef" class="chart-content"></div>
      </div>

      <!-- 右侧上：演出售票统计 -->
      <div class="chart-box revenue-stats">
        <div class="box-header">
          <span class="header-icon">🎟️</span>
          <h3>演出活动售票统计</h3>
        </div>
        <div ref="ticketChartRef" class="chart-content"></div>
      </div>

      <!-- 左侧下：用户动态 -->
      <div class="chart-box resource-dist">
        <div class="box-header">
          <span class="header-icon">👥</span>
          <h3>用户动态</h3>
        </div>
        <div class="log-list-container custom-scroll">
          <ul class="log-list">
            <li v-for="(act, index) in userActivities" :key="index" class="log-item activity-item">
              <span class="log-time">{{ act.time }}</span>
              <div class="log-content-wrapper">
                <span class="user-name">{{ act.userName }}</span>
                <span class="action-tag">{{ act.action }}</span>
                <span class="target-name">{{ act.targetName }}</span>
              </div>
            </li>
          </ul>
        </div>
      </div>

      <!-- 右侧下：最新审核动态 -->
      <div class="chart-box latest-logs">
        <div class="box-header">
          <span class="header-icon">🛡️</span>
          <h3>最新审核动态 (工作成果)</h3>
        </div>
        <div class="log-list-container custom-scroll">
          <ul class="log-list">
            <li v-for="(log, index) in auditLogs" :key="index" class="log-item">
              <span class="log-time">{{ log.time }}</span>
              <div class="log-content-wrapper" :title="log.content">
                <span class="log-badge" :class="log.typeClass">{{ log.type }}</span>
                <span class="log-text">{{ log.content }}</span>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { User, Trophy, Collection } from '@element-plus/icons-vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

// --- 状态与数据 ---
const currentDate = ref('')
const mallChartRef = ref(null)
const ticketChartRef = ref(null)
let mallChart = null
let ticketChart = null
const revenuePeriod = ref('day')

// 顶部卡片
const topCards = ref([
  { label: '累计注册用户', value: 0, icon: User },
  { label: '非遗传承人总数', value: 0, icon: Trophy },
  { label: '全站资源总量', value: 0, icon: Collection }
])

const auditLogs = ref([])
const userActivities = ref([])

// 实时数据存储
const realMallRevenue = ref({ dates: [], amounts: [] })
const realTicketSales = ref({ dates: [], counts: [], amounts: [] })

// --- 获取真实数据 ---
const fetchDashboardData = async () => {
  try {
    const res = await request.get('/admin/dashboard/stats', {
      params: { revenuePeriod: revenuePeriod.value }
    })
    if (res.code === 200) {
      const data = res.data
      
      // 更新顶部卡片
      topCards.value[0].value = data.totalUsers
      topCards.value[1].value = data.totalInheritors
      topCards.value[2].value = data.totalResources

      // 更新图表数据引用
      realMallRevenue.value = data.mallRevenue
      realTicketSales.value = data.ticketSales
      
      // 更新列表
      userActivities.value = data.userActivities || []
      auditLogs.value = data.logs || []

      // 刷新图表
      updateCharts()
    } else {
      ElMessage.error(res.msg || '获取数据失败')
    }
  } catch (error) {
    console.error('Fetch dashboard error:', error)
    ElMessage.error('网络错误，无法获取数据')
  }
}

// 监听周期变化，重新请求数据
const handlePeriodChange = () => {
  fetchDashboardData()
}

// --- ECharts 配置 ---

// 1. 商城收益统计 (Bar Chart) - 美化版
const getMallOption = () => {
  return {
    backgroundColor: 'transparent',
    tooltip: { 
      trigger: 'axis', 
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(11, 17, 32, 0.9)',
      borderColor: '#1f2d3d',
      textStyle: { color: '#fff' }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      data: realMallRevenue.value.dates,
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } },
      axisLabel: { color: 'rgba(255,255,255,0.6)' },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      name: '金额 (元)',
      splitLine: { lineStyle: { type: 'dashed', color: 'rgba(255,255,255,0.05)' } },
      axisLabel: { color: 'rgba(255,255,255,0.6)' },
      nameTextStyle: { color: 'rgba(255,255,255,0.6)', padding: [0, 0, 0, 10] }
    },
    series: [
      {
        name: '商城总收益',
        type: 'bar',
        barWidth: '30%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#00ddff' },
            { offset: 1, color: 'rgba(0, 221, 255, 0.1)' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        data: realMallRevenue.value.amounts
      }
    ]
  }
}

// 2. 演出售票统计 (Dual Axis) - 美化版
const getTicketOption = () => {
  return {
    backgroundColor: 'transparent',
    tooltip: { 
      trigger: 'axis', 
      axisPointer: { type: 'cross', label: { backgroundColor: '#152338' } },
      backgroundColor: 'rgba(11, 17, 32, 0.9)',
      borderColor: '#1f2d3d',
      textStyle: { color: '#fff' }
    },
    legend: { 
      textStyle: { color: 'rgba(255,255,255,0.8)' }, 
      top: 0,
      itemWidth: 10,
      itemHeight: 10
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      data: realTicketSales.value.dates,
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } },
      axisLabel: { color: 'rgba(255,255,255,0.6)' },
      axisTick: { show: false }
    },
    yAxis: [
      {
        type: 'value',
        name: '售票数 (张)',
        position: 'left',
        splitLine: { lineStyle: { type: 'dashed', color: 'rgba(255,255,255,0.05)' } },
        axisLabel: { color: '#ffbf00' },
        nameTextStyle: { color: '#ffbf00', padding: [0, 10, 0, 0] }
      },
      {
        type: 'value',
        name: '总金额 (元)',
        position: 'right',
        splitLine: { show: false },
        axisLabel: { color: '#37a2da' },
        nameTextStyle: { color: '#37a2da', padding: [0, 0, 0, 10] }
      }
    ],
    series: [
      {
        name: '售票数量',
        type: 'bar',
        yAxisIndex: 0,
        barWidth: '30%',
        itemStyle: { 
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#ffbf00' },
            { offset: 1, color: 'rgba(255, 191, 0, 0.1)' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        data: realTicketSales.value.counts
      },
      {
        name: '销售总额',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        showSymbol: false,
        lineStyle: { width: 3, shadowColor: 'rgba(55, 162, 218, 0.5)', shadowBlur: 10 },
        itemStyle: { color: '#37a2da' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(55, 162, 218, 0.3)' },
            { offset: 1, color: 'rgba(55, 162, 218, 0)' }
          ])
        },
        data: realTicketSales.value.amounts
      }
    ]
  }
}

// --- 逻辑方法 ---

const updateTime = () => {
  const now = new Date()
  currentDate.value = now.toLocaleString('zh-CN', { hour12: false })
}

const updateCharts = () => {
  if (mallChart) mallChart.setOption(getMallOption())
  if (ticketChart) ticketChart.setOption(getTicketOption())
}

const initCharts = () => {
  if (mallChartRef.value) {
    mallChart = echarts.init(mallChartRef.value)
  }
  if (ticketChartRef.value) {
    ticketChart = echarts.init(ticketChartRef.value)
  }
}

const handleResize = () => {
  mallChart?.resize()
  ticketChart?.resize()
}

// --- 生命周期 ---

let timeInterval = null

onMounted(async () => {
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
  
  nextTick(() => {
    initCharts()
    window.addEventListener('resize', handleResize)
  })

  await fetchDashboardData()
})

onUnmounted(() => {
  if (timeInterval) clearInterval(timeInterval)
  window.removeEventListener('resize', handleResize)
  mallChart?.dispose()
  ticketChart?.dispose()
})
</script>

<style scoped>
.auditor-dashboard {
  min-height: 100vh;
  background-color: #0b1120;
  background-image: radial-gradient(circle at 50% 0%, #1a2a40 0%, #0b1120 100%);
  color: #fff;
  padding: 24px;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  box-sizing: border-box;
}

/* 头部 */
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  margin-bottom: 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
.header-left h1 {
  margin: 0;
  font-size: 26px;
  background: linear-gradient(90deg, #fff, #00ddff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-weight: 700;
}
.header-left .subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 4px;
  display: block;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}
.period-selector :deep(.el-radio-button__inner) {
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: none;
}
.period-selector :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: rgba(0, 221, 255, 0.2);
  color: #00ddff;
  border-color: #00ddff;
  box-shadow: none;
}
.date-display {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.6);
  font-variant-numeric: tabular-nums;
  letter-spacing: 0.5px;
}

/* 顶部翻牌器 */
.data-cards {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
}
.card {
  flex: 1;
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
}
.card:hover {
  transform: translateY(-4px);
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(0, 221, 255, 0.3);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}
.card-icon-bg {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, rgba(0, 221, 255, 0.2) 0%, rgba(0, 221, 255, 0.05) 100%);
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 20px;
  color: #00ddff;
}
.card-icon-bg svg {
  width: 28px;
  height: 28px;
}
.card-info .card-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 8px;
}
.card-info .card-value {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  line-height: 1;
}

/* 图表容器 */
.charts-container {
  display: grid;
  grid-template-columns: 2fr 1fr;
  grid-template-rows: 420px 320px;
  gap: 24px;
}

.chart-box {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  transition: border-color 0.3s;
}
.chart-box:hover {
  border-color: rgba(255, 255, 255, 0.1);
}

.box-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding-left: 4px;
}
.header-icon {
  margin-right: 8px;
  font-size: 18px;
}
.box-header h3 {
  margin: 0;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
}

.chart-content {
  flex: 1;
  width: 100%;
  height: 100%;
  min-height: 0; /* Fix flex overflow */
}

/* 布局调整 */
.main-trend { grid-column: 1 / 2; grid-row: 1 / 2; }
.revenue-stats { grid-column: 2 / 3; grid-row: 1 / 2; }
.resource-dist { grid-column: 1 / 2; grid-row: 2 / 3; }
.latest-logs { grid-column: 2 / 3; grid-row: 2 / 3; }

/* 列表样式 */
.log-list-container {
  flex: 1;
  overflow-y: auto;
  position: relative;
  padding-right: 4px;
}
.log-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.log-item {
  display: flex;
  align-items: flex-start;
  padding: 12px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  font-size: 13px;
  transition: background 0.2s;
}
.log-item:hover {
  background: rgba(255, 255, 255, 0.02);
}
.log-item:last-child {
  border-bottom: none;
}
.log-time {
  color: rgba(255, 255, 255, 0.4);
  margin-right: 12px;
  width: 85px;
  font-family: 'JetBrains Mono', Consolas, monospace;
  font-size: 12px;
  padding-top: 2px;
}
.log-content-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}

/* 用户动态样式 */
.activity-item .user-name {
  color: #00ddff;
  font-weight: 500;
}
.activity-item .action-tag {
  color: rgba(255, 255, 255, 0.6);
  background: rgba(255, 255, 255, 0.1);
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 12px;
}
.activity-item .target-name {
  color: #fff;
}

/* 审核日志样式 */
.log-badge {
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}
.log-badge.success { background: rgba(103, 194, 58, 0.2); color: #67c23a; }
.log-badge.danger { background: rgba(245, 108, 108, 0.2); color: #f56c6c; }
.log-badge.warning { background: rgba(230, 162, 60, 0.2); color: #e6a23c; }
.log-text {
  color: rgba(255, 255, 255, 0.8);
}

/* 自定义滚动条 */
.custom-scroll::-webkit-scrollbar {
  width: 4px;
}
.custom-scroll::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scroll::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
}
.custom-scroll::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* 响应式 */
@media (max-width: 1200px) {
  .charts-container {
    grid-template-columns: 1fr;
    grid-template-rows: auto;
  }
  .main-trend, .revenue-stats, .resource-dist, .latest-logs {
    grid-column: 1 / -1;
    grid-row: auto;
    height: 380px;
  }
}
@media (max-width: 768px) {
  .data-cards {
    flex-direction: column;
    gap: 16px;
  }
  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  .header-right {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
