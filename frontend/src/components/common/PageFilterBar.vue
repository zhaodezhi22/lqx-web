<template>
  <div class="page-filter-bar">
    <div class="page-filter-main">
      <slot />
    </div>
    <div v-if="$slots.extra" class="page-filter-extra">
      <slot name="extra" />
    </div>
  </div>
</template>

<style scoped>
.page-filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}

.page-filter-main {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  flex: 1;
}

.page-filter-extra {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
</style>
