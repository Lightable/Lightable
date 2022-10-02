<script setup lang="ts">
import {ref} from 'vue';
import AppSection from '../../../components/app/AppSection.vue';
import LightThemeDefaultPreview from '../../../components/settings/LightThemeDefaultPreview.vue';
import DarkThemeDefaultPreview from '../../../components/settings/DarkThemeDefaultPreview.vue';
import {useAppStore} from '../../../stores/AppStore';
import {NButton, NModal} from 'naive-ui';
import {PhFileImage} from '@dnlsndr/vue-phosphor-icons';
import {SelectSideDrawerPhoto} from '../../../../wailsjs/go/app/App';
const appStore = useAppStore();

const drawerPhotoSelector = ref({
  disabled: false,
  showPhotoPreview: false,
  selectedPhoto: null,
})
const selectDrawerPhoto = async () => {
  const drawerState = drawerPhotoSelector.value;
  drawerState.disabled = true;
  const photo = await SelectSideDrawerPhoto()
  if (photo) {
    drawerState.showPhotoPreview = true;
    drawerState.selectedPhoto = photo;
  }
  drawerState.disabled = false;
}
</script>

<template>
  <NModal preset="dialog" :show="drawerPhotoSelector.showPhotoPreview" title="Photo Preview">
    <div class="image" style="width: 100%; height: 100%;">
      <img :src="`TEMP_${drawerPhotoSelector.selectedPhoto}`" style="height: 363px; width: 124px; background-size: cover; object-fit: none; position: absolute; z-index: 2000;" v-if="drawerPhotoSelector.selectedPhoto"/>
      <div class="image-crop-wrapper" style="height: 500px; width: 500px; background:linear-gradient(90deg, rgba(0, 0, 0, 1) 0%, rgba(0, 0, 0, 0) 41%, rgba(30, 30, 30, 0) 55%, rgba(0, 0, 0, 0.83) 100%);
; z-index: 2001; position: absolute;">
        <div class="image-crop-border" style="margin: auto">
        </div>
      </div>
    </div>

  </NModal>
  <AppSection title="DEFAULT THEME">
    <div class="theme-group">
      <button class="theme-default-btn">
        <DarkThemeDefaultPreview style="border-radius: 4px;" @click="appStore.changeTheme('Dark')"/>
        <span class="theme-detail">Dark</span>
      </button>
      <button class="theme-default-btn">
        <LightThemeDefaultPreview style="border-radius: 4px;" @click="appStore.changeTheme('Light')"/>
        <span class="theme-detail">Light</span>
      </button>
    </div>
  </AppSection>
  <AppSection title="SIDE DRAWER">
    <div class="sect">
      <h2>Set Drawer Background</h2>
      <span class="hint">Recommended Aspect is <code>9:16</code></span>
      <NButton secondary type="primary" class="photo-selector" @click="selectDrawerPhoto" :disabled="drawerPhotoSelector.disabled">
        <template #icon>
          <PhFileImage weight="duotone" :size="24"/>
        </template>
        Select Photo
      </NButton>
    </div>
  </AppSection>
<!--  <AppSection title="COLORS">-->
<!--    <div class="theme-group">-->
<!--      <ThemePickerGroup/>-->
<!--    </div>-->
<!--  </AppSection>-->
<!--  <AppSection title="OPACITY">-->

<!--  </AppSection>-->
<!--  <AppSection title="BACKGROUND">-->

<!--  </AppSection>-->
</template>

<style lang="scss" scoped>
.theme-group {
  padding: 12px;
  display: flex;
  flex-direction: row;
  gap: 32px;

  .theme-default-btn {
    display: flex;
    flex-direction: column;
    all: unset;
    cursor: pointer;
    transition: all 0.25s ease;
    background-color: transparent;
    border-radius: 8px;
    color: var(--text-color-1);

    .theme-detail {
      margin-left: 8px;
      font-weight: bold;
      font-size: 18px;
      font-family: "Nunito", sans-serif;
    }

    &:hover {
      opacity: 0.8;
      scale: 1.02;
    }
  }

}
.sect {
  display: flex;
  flex-direction: column;
  width: fit-content;
  padding: 8px;
  color: var(--text-color-1);
  justify-content: center;
  align-items: center;
  gap: 16px;
  .photo-selector {
    width: 150px;
  }
}

</style>