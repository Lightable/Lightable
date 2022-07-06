<script setup lang="ts">
import { computed, defineAsyncComponent } from "vue";
import { useAppStore } from "../stores/AppStore";
import { NIcon as Icon } from "naive-ui";
const Element = defineAsyncComponent({
   loader: () => import('naive-ui/lib/element/src/Element')
});
const Button = defineAsyncComponent({
   loader: () => import('naive-ui/lib/button/src/Button')
});
const ButtonGroup = defineAsyncComponent({
   loader: () => import('naive-ui/lib/button-group/src/ButtonGroup')
});
const ChatBoxIcon = defineAsyncComponent({
   loader: () => import('@vicons/ionicons5/Chatbox')
})
const appStore = useAppStore();
const isDark = computed(() => appStore.isDark);
const setTheme = appStore.setTheme
</script>
<template>
   <header :style="(isDark) ? 'background-color: var(--header-dark);' : 'background-color: var(--header-light);'">
      <nav>
         <Element type="div" class="logo-wrapper selectable" @click="$router.push('/')">
            <Icon :size="32" color="var(--text-color-1)">
               <ChatBoxIcon height="32" width="32" />
            </Icon>
            <span class="ztext">ZenSpace</span>
         </Element>
         <Element type="div" class="theme-select">
            <ButtonGroup>
               <Button round @click="setTheme(false)" :disabled="!isDark">Light</Button>
               <Button round @click="setTheme(true)" :disabled="isDark">Dark</Button>
            </ButtonGroup>
         </Element>
      </nav>
   </header>
</template>

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
