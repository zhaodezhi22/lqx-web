<template>
  <div class="lineage-tree-container" v-loading="loading">
    <div class="controls">
      <el-radio-group v-model="layoutType" @change="initChart">
        <el-radio-button label="orthogonal">横向树</el-radio-button>
        <el-radio-button label="radial">径向树</el-radio-button>
      </el-radio-group>
    </div>
    <div ref="chartRef" class="chart-container"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const props = defineProps({
  inheritorId: {
    type: Number,
    required: true
  }
})

const chartRef = ref(null)
const chartInstance = ref(null)
const loading = ref(false)
const layoutType = ref('orthogonal')
const treeData = ref(null)

const fetchTreeData = async (id) => {
  loading.value = true
  try {
    const res = await request.get(`/inheritor/lineage/${id}`)
    if (res.code === 200) {
      treeData.value = res.data
      renderChart()
    } else {
      ElMessage.error(res.message || '获取谱系数据失败')
    }
  } catch (e) {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

const initChart = () => {
  if (chartInstance.value) {
    chartInstance.value.dispose()
  }
  if (!chartRef.value) return
  
  chartInstance.value = echarts.init(chartRef.value)
  // Don't render yet, wait for data
  if (treeData.value) {
      renderChart()
  }
  
  chartInstance.value.on('click', (params) => {
    if (params.data && params.data.id) {
       // Refresh tree centered on clicked node
       fetchTreeData(params.data.id)
    }
  })
}

const renderChart = () => {
  if (!chartInstance.value || !treeData.value) return
  
  const data = treeData.value
  
  // Recursively format data to add itemStyle for target
  const formatData = (node) => {
    const formatted = {
      name: node.name,
      value: node.level, // Display level in tooltip
      id: node.id,
      children: node.children ? node.children.map(formatData) : [],
      itemStyle: node.isTarget ? {
        borderColor: '#E6A23C',
        color: '#FDF6EC',
        borderWidth: 3
      } : undefined,
      label: node.isTarget ? {
        fontWeight: 'bold',
        color: '#E6A23C'
      } : undefined
    }
    return formatted
  }

  const formattedData = formatData(data)

  const option = {
    tooltip: {
      trigger: 'item',
      triggerOn: 'mousemove',
      formatter: '{b} ({c})'
    },
    series: [
      {
        type: 'tree',
        data: [formattedData],
        top: '1%',
        left: '7%',
        bottom: '1%',
        right: '20%',
        symbolSize: 10,
        layout: layoutType.value,
        label: {
          position: layoutType.value === 'orthogonal' ? 'left' : 'top',
          verticalAlign: 'middle',
          align: 'right',
          fontSize: 14
        },
        leaves: {
          label: {
            position: layoutType.value === 'orthogonal' ? 'right' : 'bottom',
            verticalAlign: 'middle',
            align: 'left'
          }
        },
        emphasis: {
          focus: 'descendant'
        },
        expandAndCollapse: true,
        animationDuration: 550,
        animationDurationUpdate: 750
      }
    ]
  }
  
  chartInstance.value.setOption(option)
}

watch(() => props.inheritorId, (newId) => {
  if (newId) fetchTreeData(newId)
})

onMounted(() => {
  initChart()
  if (props.inheritorId) {
    fetchTreeData(props.inheritorId)
  }
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance.value) {
    chartInstance.value.dispose()
  }
})

const handleResize = () => {
  chartInstance.value && chartInstance.value.resize()
}
</script>

<style scoped>
.lineage-tree-container {
  width: 100%;
  height: 600px;
  position: relative;
  background: #fff;
  border-radius: 8px;
}
.controls {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 10;
}
.chart-container {
  width: 100%;
  height: 100%;
}
</style>
