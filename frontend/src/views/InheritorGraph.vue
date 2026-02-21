<template>
  <div class="page-container">
    <div class="chart-wrapper">
      <div ref="chartRef" class="echarts-box"></div>
    </div>

    <!-- Inheritor Detail Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="currentInheritor.name"
      width="30%"
      align-center
    >
      <div class="inheritor-detail">
        <div class="avatar-container">
          <el-avatar :size="100" :src="currentInheritor.avatarUrl" />
        </div>
        <div class="info-item">
          <span class="label">等级：</span>
          <el-tag :type="getLevelType(currentInheritor.category)">{{ currentInheritor.category }}</el-tag>
        </div>
        <div class="info-item" v-if="currentInheritor.genre">
          <span class="label">流派：</span>
          <span>{{ currentInheritor.genre }}</span>
        </div>
        <div class="info-item intro" v-if="currentInheritor.intro">
          <span class="label">简介：</span>
          <p>{{ currentInheritor.intro }}</p>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="goToDetail(currentInheritor.userId)">查看详情</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const route = useRoute()
const chartRef = ref(null)
let myChart = null

// Dialog state
const dialogVisible = ref(false)
const currentInheritor = reactive({
  userId: '', // Add userId
  id: '',
  name: '',
  avatarUrl: '',
  category: '',
  genre: '',
  intro: ''
})

const goToDetail = (userId) => {
  if (userId) {
     router.push({ name: 'UserPublicProfile', params: { id: userId } })
  } else {
     ElMessage.warning('该用户暂无详情页')
  }
}

const getLevelType = (level) => {
  if (level === '国家级') return 'danger'
  if (level === '省级') return 'warning'
  if (level === '市级') return 'success'
  if (level === '县级' || level === '县/区级') return ''
  return 'info'
}

const initChart = (data) => {
  if (myChart) myChart.dispose()
  
  myChart = echarts.init(chartRef.value)
  
  // Dynamic categories from data to ensure all levels are displayed
  const uniqueCategories = Array.from(new Set(data.nodes.map(n => n.category))).filter(c => c)
  // Define order preference
  const levelOrder = ['国家级', '省级', '市级', '县级', '县/区级']
  uniqueCategories.sort((a, b) => {
    const idxA = levelOrder.indexOf(a)
    const idxB = levelOrder.indexOf(b)
    if (idxA !== -1 && idxB !== -1) return idxA - idxB
    if (idxA !== -1) return -1
    if (idxB !== -1) return 1
    return a.localeCompare(b)
  })

  const categories = uniqueCategories.map(name => ({ name }))

  const option = {
    title: {
      text: '非遗传承人谱系图',
      subtext: '点击节点查看详情',
      left: 'center',
      top: 20,
      textStyle: {
        color: '#333',
        fontFamily: 'Noto Serif SC',
        fontSize: 24
      }
    },
    tooltip: {
      formatter: '{b}'
    },
    legend: {
      data: categories.map(c => c.name),
      bottom: 20
    },
    animationDurationUpdate: 1500,
    animationEasingUpdate: 'quinticInOut',
    series: [
      {
        type: 'graph',
        layout: 'force',
        symbolSize: 60,
        roam: true,
        label: {
          show: true,
          position: 'bottom',
          formatter: '{b}'
        },
        edgeSymbol: ['none', 'arrow'], // Arrow indicating inheritance direction
        edgeSymbolSize: [4, 10],
        edgeLabel: {
          fontSize: 12
        },
        data: data.nodes.map(node => {
          // Handle symbol image prefix
          let symbol = node.symbol
          let avatarUrl = ''
          if (symbol) {
             if (symbol.startsWith('image://')) {
                avatarUrl = symbol.substring(8)
             } else {
                avatarUrl = symbol
                symbol = 'image://' + symbol
             }
          } else {
             // Default avatar if none provided
             avatarUrl = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
             symbol = 'image://' + avatarUrl
          }

          return {
            id: String(node.id),
            name: node.name,
            symbol: symbol,
            category: node.category,
            value: node.category,
            itemStyle: node.isTarget ? {
              borderColor: '#E6A23C',
              borderWidth: 4,
              shadowBlur: 10,
              shadowColor: '#E6A23C'
            } : undefined,
            label: node.isTarget ? {
              show: true,
              position: 'bottom',
              formatter: '{b}',
              fontWeight: 'bold',
              color: '#E6A23C',
              fontSize: 14
            } : undefined,
            // Custom data for click handler
            rawData: {
              ...node,
              avatarUrl
            }
          }
        }),
        links: data.links.map(link => ({
          source: String(link.source),
          target: String(link.target)
        })),
        categories: categories,
        force: {
          repulsion: 500, // Nodes repel each other
          edgeLength: 150,
          gravity: 0.1
        },
        lineStyle: {
          color: 'source',
          curveness: 0.3
        }
      }
    ]
  }

  myChart.setOption(option)

  // Click Event Listener
  myChart.on('click', (params) => {
    if (params.dataType === 'node') {
      const data = params.data.rawData
      currentInheritor.id = data.id
      currentInheritor.userId = data.userId
      currentInheritor.name = data.name
      currentInheritor.avatarUrl = data.avatarUrl
      currentInheritor.category = data.category
      currentInheritor.genre = data.genre
      currentInheritor.intro = data.intro
      
      dialogVisible.value = true
    }
  })
}

const fetchData = async () => {
  try {
    const id = route.query.id
    const url = id ? `/inheritor/graph/${id}` : '/inheritor/graph/all'
    const res = await request.get(url)
    if (res.code === 200 && res.data) {
      initChart(res.data)
    } else {
      ElMessage.warning('暂无谱系数据')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载图谱失败')
  }
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (myChart) {
    myChart.dispose()
    myChart = null
  }
  window.removeEventListener('resize', handleResize)
})

const handleResize = () => {
  if (myChart) myChart.resize()
}
</script>

<style scoped>
.page-container {
  padding: 20px;
  height: calc(100vh - 100px);
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.header-actions {
  margin-bottom: 20px;
}

.chart-wrapper {
  width: 100%;
  flex: 1;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  box-sizing: border-box;
}
.echarts-box {
  width: 100%;
  height: 100%;
}
.inheritor-detail {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.avatar-container {
  display: flex;
  justify-content: center;
  margin-bottom: 10px;
}
.info-item {
  display: flex;
  align-items: center;
  font-size: 16px;
}
.info-item.intro {
  align-items: flex-start;
}
.info-item .label {
  font-weight: bold;
  width: 60px;
  flex-shrink: 0;
}
.info-item p {
  margin: 0;
  line-height: 1.5;
  color: #666;
}
</style>
