<template>
  <div class="section-title-wrap">
    <span class="decor"></span>
    <h2 class="title">{{ text }}</h2>
    <span class="decor"></span>
  </div>
</template>

<script setup>
const props = defineProps({
  text: { type: String, default: '—— 非遗 · 传承人 ——' }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@600&display=swap');
.section-title-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 24px 0;
}
.title {
  font-family: 'Noto Serif SC', serif;
  font-size: 26px;
  color: #333;
  margin: 0 16px;
  letter-spacing: 1px;
}
.decor {
  display: inline-block;
  width: 80px;
  height: 2px;
  background: linear-gradient(to right, transparent, #AA1D1D, transparent);
  position: relative;
}
.decor::before {
  content: '';
  position: absolute;
  width: 8px;
  height: 8px;
  background-color: #AA1D1D;
  transform: rotate(45deg);
  top: -3px;
  left: calc(50% - 4px);
}
</style>
