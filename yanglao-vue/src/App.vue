<template>
  <div id="app">
    <router-view />

    <!-- 全局「问医生」浮动小组件 -->
    <DoctorConsultWidget />

    <!-- 全局明暗主题切换 -->
    <button
      class="theme-toggle"
      type="button"
      :aria-label="isDark ? '切换到浅色模式' : '切换到深色模式'"
      :title="isDark ? '切换到浅色模式' : '切换到深色模式'"
      @click="toggleTheme"
    >
      <i :class="isDark ? 'bi bi-sun' : 'bi bi-moon-stars'"></i>
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import DoctorConsultWidget from './components/DoctorConsultWidget.vue'

const isDark = ref(localStorage.getItem('darkMode') === 'true')
document.body.classList.toggle('dark-mode', isDark.value)

const toggleTheme = () => {
  isDark.value = !isDark.value
  document.body.classList.toggle('dark-mode', isDark.value)
  localStorage.setItem('darkMode', String(isDark.value))
}
</script>

<style>
.theme-toggle {
  position: fixed;
  left: var(--sp-5);
  bottom: var(--sp-6);
  z-index: 9998;
  width: 40px;
  height: 40px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--c-border-strong);
  border-radius: 50%;
  background: var(--c-surface);
  color: var(--c-text-2);
  font-size: 16px;
  cursor: pointer;
  box-shadow: var(--sh-1);
  transition: color var(--dur) var(--ease), border-color var(--dur) var(--ease),
              background-color var(--dur) var(--ease), transform var(--dur) var(--ease);
}

.theme-toggle:hover {
  color: var(--c-text);
  border-color: var(--c-accent);
  background: var(--c-accent-soft);
  transform: translateY(-1px);
}

.theme-toggle:focus-visible {
  outline: 2px solid var(--c-accent);
  outline-offset: 2px;
}
</style>
