<template>
  <div class="section-title-wrap">
    <div class="title-main">
      <span class="decor"></span>
      <h2 class="title">{{ text }}</h2>
    </div>
    <a v-if="moreLink" class="more-link" href="javascript:void(0)" @click="goMore">
      {{ moreText }}
    </a>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  text: { type: String, default: '非遗传承' },
  moreText: { type: String, default: '查看更多' },
  moreLink: { type: String, default: '' }
})

const router = useRouter()

const goMore = () => {
  if (!props.moreLink) return
  if (props.moreLink.startsWith('http')) {
    window.open(props.moreLink, '_blank')
    return
  }
  router.push(props.moreLink)
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@600&display=swap');
.section-title-wrap {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 24px 0 26px;
}

.title-main {
  display: flex;
  align-items: center;
  gap: 14px;
}

.title {
  font-family: 'Noto Serif SC', serif;
  font-size: 28px;
  color: #2f241f;
  margin: 0;
  letter-spacing: 2px;
  font-weight: 600;
}
.decor {
  display: inline-block;
  width: 78px;
  height: 2px;
  background: linear-gradient(to right, rgba(170, 29, 29, 0), rgba(170, 29, 29, 0.9), rgba(214, 174, 96, 0.7), rgba(170, 29, 29, 0));
  position: relative;
}
.decor::before {
  content: '';
  position: absolute;
  width: 8px;
  height: 8px;
  background: linear-gradient(135deg, #AA1D1D, #d6ae60);
  transform: rotate(45deg);
  top: -3px;
  left: calc(50% - 4px);
  box-shadow: 0 0 0 4px rgba(255, 247, 239, 0.9);
}

.more-link {
  color: #8b5e3c;
  font-size: 14px;
  text-decoration: none;
  transition: color 0.2s ease, transform 0.2s ease;
  position: relative;
  padding-right: 16px;
}

.more-link::after {
  content: '';
  position: absolute;
  right: 0;
  top: 50%;
  width: 8px;
  height: 8px;
  border-top: 1.5px solid currentColor;
  border-right: 1.5px solid currentColor;
  transform: translateY(-50%) rotate(45deg);
}

.more-link:hover {
  color: #aa1d1d;
  transform: translateX(2px);
}

@media (max-width: 768px) {
  .section-title-wrap {
    margin: 20px 0;
  }

  .title {
    font-size: 24px;
  }

  .decor {
    width: 58px;
  }
}
</style>
