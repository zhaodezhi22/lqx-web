<template>
  <div class="seat-editor">
    <!-- Generator (if empty) -->
    <div v-if="!hasSeats" class="generator">
      <el-alert type="info" show-icon :closable="false" title="暂无座位数据，请生成初始布局" style="margin-bottom: 20px" />
      <el-form inline>
        <el-form-item label="行数">
          <el-input-number v-model="genRows" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="列数">
          <el-input-number v-model="genCols" :min="1" :max="50" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="generateGrid">生成网格</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Seat Grid -->
    <div v-else class="seat-container">
      <div class="legend">
        <div class="legend-item"><div class="seat-icon available"></div> 可选</div>
        <div class="legend-item"><div class="seat-icon sold"></div> 已售/禁用</div>
        <span class="tip">点击座位切换状态</span>
        <el-button size="small" type="danger" link @click="clearSeats" style="margin-left: 20px">重置/清空</el-button>
      </div>

      <div class="stage">舞台方向</div>
      
      <div class="grid-wrapper" :style="gridStyle">
        <div 
          v-for="seat in seats" 
          :key="seat.id" 
          class="seat-item"
          :class="{ 'is-sold': seat.status === 1 }"
          @click="toggleSeat(seat)"
        >
          <span class="seat-text">{{ seat.row }}排{{ seat.col }}座</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

const seats = ref([])
const hasSeats = ref(false)
const genRows = ref(10)
const genCols = ref(15)
const maxCol = ref(0)

// Watch for external changes
watch(() => props.modelValue, (newVal) => {
  const newJson = JSON.stringify(newVal || [])
  const currentJson = JSON.stringify(seats.value || [])
  
  if (newJson !== currentJson) {
    if (newVal && newVal.length > 0) {
      seats.value = JSON.parse(newJson) // Deep copy
      hasSeats.value = true
      maxCol.value = Math.max(...seats.value.map(s => s.col || 0))
    } else {
      seats.value = []
      hasSeats.value = false
    }
  }
}, { immediate: true, deep: true })

// Watch for internal changes to emit
watch(seats, (newVal) => {
  emit('update:modelValue', newVal)
}, { deep: true })

const generateGrid = () => {
  const newSeats = []
  for (let r = 1; r <= genRows.value; r++) {
    for (let c = 1; c <= genCols.value; c++) {
      newSeats.push({
        id: `s-${r}-${c}`,
        row: r,
        col: c,
        status: 0 // 0-Available, 1-Sold/Disabled
      })
    }
  }
  seats.value = newSeats
  maxCol.value = genCols.value
  hasSeats.value = true
}

const toggleSeat = (seat) => {
  seat.status = seat.status === 0 ? 1 : 0
}

const clearSeats = () => {
  ElMessageBox.confirm('确定要清空当前座位布局吗？', '提示', { type: 'warning' })
    .then(() => {
      seats.value = []
      hasSeats.value = false
    })
    .catch(() => {})
}

const gridStyle = computed(() => {
  return {
    display: 'grid',
    gridTemplateColumns: `repeat(${maxCol.value}, 40px)`,
    gap: '8px',
    justifyContent: 'center',
    marginTop: '20px'
  }
})
</script>

<style scoped>
.seat-editor {
  width: 100%;
}
.seat-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.legend {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
}
.seat-icon {
  width: 20px;
  height: 20px;
  border-radius: 4px;
}
.seat-icon.available {
  background-color: #f0f0f0;
  border: 1px solid #ccc;
}
.seat-icon.sold {
  background-color: #ff4d4f;
  border: 1px solid #d32f2f;
}
.tip {
  color: #999;
  font-size: 12px;
}
.stage {
  width: 80%;
  height: 40px;
  background-color: #f0f2f5;
  border-radius: 0 0 50px 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30px;
  color: #666;
  font-weight: bold;
  letter-spacing: 4px;
}
.grid-wrapper {
  max-width: 100%;
  overflow-x: auto;
  padding: 10px;
}
.seat-item {
  width: 40px;
  height: 40px;
  background-color: #fff;
  border: 1px solid #ccc;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 10px;
  color: #666;
  user-select: none;
}
.seat-item:hover {
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.seat-item.is-sold {
  background-color: #ff4d4f;
  color: white;
  border-color: #d32f2f;
}
</style>
