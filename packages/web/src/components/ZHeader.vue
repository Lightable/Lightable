
<template>
   <header :style="(isDark) ? 'background-color: var(--header-dark);' : 'background-color: var(--header-light);'">
      <nav>
         <NElement type="div" class="logo-wrapper selectable" @click="$router.push('/')">
            <NIcon :size="32" color="var(--text-color-1)">
               <Chatbox height="32" width="32" />
            </NIcon>
            <span class="ztext">ZenSpace</span>
         </NElement>
         <NElement type="div" class="theme-select">
            <NButtonGroup>
               <NButton round @click="setTheme(false)" :disabled="!isDark">Light</NButton>
               <NButton round @click="setTheme(true)" :disabled="isDark">Dark</NButton>
            </NButtonGroup>
         </NElement>
      </nav>
   </header>
</template>

<script lang="ts">
import { computed, defineComponent } from "vue";
import { NElement, NIcon, NButton, NButtonGroup } from 'naive-ui';
import { Chatbox } from '@vicons/ionicons5';
import { useAppStore } from '../stores/AppStore';
export default defineComponent({
   name: 'ZHeader',
   components: {
      NIcon,
      Chatbox,
      NElement,
      NButton,
      NButtonGroup
   },
   setup() {
      let appStore = useAppStore();
      return {
         isDark: computed(() => appStore.isDark),
         setTheme: appStore.setTheme
      }
   }
})
</script>

<style lang="scss" scoped>
header {
     animation: bring-down-header 250ms ease-in-out;
   nav {
      display: flex;
      justify-content: space-between;
      padding: 4px;
    
      .logo-wrapper {
         display: inline-flex;
         width: 160px;
         flex-direction: row;
         justify-content: center;
         align-items: center;

         .ztext {
            font-size: 24px;
            font-weight: var(--font-weight-strong);
            line-height: var(--line-height);
            margin-left: 5px;
            font-family: 'strawfordregular';
            color: #5af2a2;
            user-select: none;
         }
      }

      .theme-select {
         margin-right: 20px;
         display: flex;
         justify-content: center;
         align-items: center;
      }
   }
}

@keyframes bring-down-header {
   0% {
      opacity: 0;
      transform: translateY(-46.4px);
   }
   100% {
      opacity: 1;
      transform: translateY(0);
   }
}
</style>
