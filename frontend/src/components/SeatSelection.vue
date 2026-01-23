<template>
  <div class="seat-map-container">
    <svg :viewBox="`0 0 ${width} ${height}`" class="seat-svg">
      <!-- Screen -->
      <path :d="`M ${width * 0.1} 20 Q ${width / 2} 0 ${width * 0.9} 20`" 
            fill="none" stroke="#ddd" stroke-width="4" />
      <text :x="width / 2" y="35" text-anchor="middle" font-size="12" fill="#999">舞台 / Stage</text>

      <!-- Seats -->
      <g v-for="(row, rIndex) in parsedLayout.rows" :key="rIndex">
        <g v-for="(seat, cIndex) in row.seats" :key="cIndex">
          <circle 
            v-if="seat !== null"
            :cx="seat.x" 
            :cy="seat.y" 
            :r="seatRadius" 
            :fill="getSeatColor(seat)"
            :stroke="selectedIds.includes(seat.id) ? '#e6a23c' : '#fff'"
            stroke-width="1"
            class="seat-circle"
            @click="handleClick(seat)"
          />
          <title>{{ seat.id }}</title>
        </g>
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
import { computed, ref, watch } from 'vue'

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

// 10x16 Grid Base
const rows = 10
const cols = 16
const w = cols * (seatRadius * 2 + gap) + 40
const h = rows * (seatRadius * 2 + gap) + 60
const width = ref(w)
const height = ref(h)

const parsedLayout = computed(() => {
  // Parse backend layout to get status map: { "1-1": 1, ... }
  const statusMap = {}
  let layoutList = []
  
  if (typeof props.layout === 'string' && props.layout) {
    try {
      const parsed = JSON.parse(props.layout)
      if (Array.isArray(parsed)) {
        layoutList = parsed
      }
    } catch (e) { console.error('Invalid JSON layout', e) }
  } else if (Array.isArray(props.layout)) {
    layoutList = props.layout
  }

  // Ensure layoutList is valid
  if (!Array.isArray(layoutList)) {
    layoutList = []
  }

  layoutList.forEach(s => {
    if (s && s.id) statusMap[s.id] = s.status // 0: Available, 1: Sold
  })

  // Generate Grid and apply status
  const rowData = []
  for (let r = 0; r < rows; r++) {
    const seats = []
    for (let c = 0; c < cols; c++) {
      const id = `${r + 1}-${c + 1}`
      seats.push({
        id: id,
        x: 20 + c * (seatRadius * 2 + gap) + seatRadius,
        y: 60 + r * (seatRadius * 2 + gap) + seatRadius,
        status: statusMap[id] !== undefined ? statusMap[id] : 0 // Default 0
      })
    }
    rowData.push({ seats })
  }
  return { rows: rowData }
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
