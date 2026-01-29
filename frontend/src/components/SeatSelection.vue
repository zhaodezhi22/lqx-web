<template>
  <div class="seat-map-container">
    <svg :viewBox="`0 0 ${width} ${height}`" class="seat-svg">
      <!-- Screen -->
      <path :d="`M ${width * 0.1} 20 Q ${width / 2} 0 ${width * 0.9} 20`" 
            fill="none" stroke="#ddd" stroke-width="4" />
      <text :x="width / 2" y="35" text-anchor="middle" font-size="12" fill="#999">舞台 / Stage</text>

      <!-- Seats -->
      <g v-for="seat in parsedLayout.seats" :key="seat.id">
        <circle 
          :cx="seat.x" 
          :cy="seat.y" 
          :r="seatRadius" 
          :fill="getSeatColor(seat)"
          :stroke="selectedIds.includes(seat.id) ? '#e6a23c' : '#fff'"
          stroke-width="1"
          class="seat-circle"
          @click="handleClick(seat)"
        />
        <title>{{ seat.row }}排{{ seat.col }}座</title>
      </g>
    </svg>
    <div class="legend">
      <div class="legend-item"><span class="dot available"></span> 可选</div>
      <div class="legend-item"><span class="dot selected"></span> 已选 (锁定)</div>
      <div class="legend-item"><span class="dot sold"></span> 已售</div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  layout: [String, Array], // JSON string or List Object
  selectedIds: { // IDs currently locked/selected by user
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['click-seat'])

const seatRadius = 12
const gap = 10

const layoutList = computed(() => {
  if (typeof props.layout === 'string' && props.layout) {
    try {
      const parsed = JSON.parse(props.layout)
      return Array.isArray(parsed) ? parsed : []
    } catch (e) { return [] }
  } else if (Array.isArray(props.layout)) {
    return props.layout
  }
  return []
})

const gridInfo = computed(() => {
  let maxR = 0
  let maxC = 0
  const list = layoutList.value
  
  if (list.length > 0) {
    list.forEach(s => {
      if (s.row > maxR) maxR = s.row
      if (s.col > maxC) maxC = s.col
    })
  } else {
    // Default fallback if empty
    maxR = 10
    maxC = 16
  }
  
  return { rows: maxR, cols: maxC }
})

const width = computed(() => gridInfo.value.cols * (seatRadius * 2 + gap) + 40)
const height = computed(() => gridInfo.value.rows * (seatRadius * 2 + gap) + 60)

const parsedLayout = computed(() => {
  const list = layoutList.value
  const seats = []
  
  list.forEach(s => {
    if (s.row && s.col) {
      seats.push({
        ...s,
        id: s.id || `${s.row}-${s.col}`,
        x: 20 + (s.col - 1) * (seatRadius * 2 + gap) + seatRadius,
        y: 60 + (s.row - 1) * (seatRadius * 2 + gap) + seatRadius,
        status: s.status !== undefined ? s.status : 0
      })
    }
  })
  
  return { seats }
})

const getSeatColor = (seat) => {
  if (seat.status === 1) return '#f56c6c' // Sold (Red)
  if (props.selectedIds.includes(seat.id)) return '#e6a23c' // Selected/Locked (Yellow)
  return '#67c23a' // Available (Green)
}

const handleClick = (seat) => {
  if (seat.status === 1) return // Sold
  emit('click-seat', seat.id)
}
</script>

<style scoped>
.seat-map-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
}
.seat-svg {
  max-width: 100%;
  height: auto;
  background: white;
  border: 1px solid #eee;
  border-radius: 4px;
}
.seat-circle {
  cursor: pointer;
  transition: all 0.3s;
}
.seat-circle:hover {
  opacity: 0.8;
  stroke: #333;
}
.legend {
  display: flex;
  gap: 20px;
  margin-top: 15px;
}
.legend-item {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #606266;
}
.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
}
.available { background: #67c23a; }
.selected { background: #e6a23c; }
.sold { background: #f56c6c; }
</style>
