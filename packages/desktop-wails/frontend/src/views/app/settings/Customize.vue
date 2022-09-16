<script setup lang="ts">
import AppSection from '../../../components/app/AppSection.vue';
import ThemePickerGroup from '../../../components/settings/ThemePickerGroup.vue';
import { useAppStore } from '../../../stores/AppStore';

const appStore = useAppStore();
</script>

<template>
  <AppSection title="DEFAULT THEME">
    <div class="theme-group">
      <button class="theme-default-btn" type="light" @click="appStore.changeTheme('Light')">
        <span class="theme-detail">Light Mode</span>
      </button>
      <button class="theme-default-btn" type="dark" @click="appStore.changeTheme('Dark')">
        <span class="theme-detail">Dark Mode</span>
      </button>
    </div>
  </AppSection>
  <AppSection title="COLORS">
    <div class="theme-group">
      <ThemePickerGroup/>
    </div>
  </AppSection>
  <AppSection title="OPACITY">

  </AppSection>
  <AppSection title="BACKGROUND">

  </AppSection>
</template>

<style lang="scss" scoped>
.theme-group {
  padding: 12px;
  display: flex;
  flex-direction: row;
  gap: 32px;

  .theme-default-btn {
    all: unset;
    position: relative;
    width: 150px;
    height: 150px;
    border-radius: .5rem;
    cursor: pointer;
    transition: opacity 0.25s ease;

    &[type='light'] {
      background-color: #fff;

      .theme-detail {
        color: #18181C;
      }
    }

    &[type='dark'] {
      background-color: #3c3c41;

      .theme-detail {
        color: #E8E8E8;
      }
    }

    .theme-detail {
      position: absolute;
      bottom: 2px;
      margin-left: 8px;
      font-weight: bold;
      font-size: 18px;
      font-family: "Nunito", sans-serif;
    }

    &:hover {
      opacity: 0.8;
    }
  }

}


</style>