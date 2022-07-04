<script setup lang="ts">
import ZPaneWrapVue from './ZPaneWrap.vue';
import ZRightPane from './ZRightPane.vue';
import { NSkeleton, NAvatar, NIcon } from 'naive-ui';
import { CheckmarkCircle } from '@vicons/ionicons5';
import ZOverlay from '../ZOverlay.vue';
const props = defineProps({
     loading: Boolean
});
</script>

<template>
     <ZPaneWrapVue>
          <ZRightPane>
               <div class="card-header">
                    <h2 style="color: var(--warning-color)">Disabled Accounts</h2>
                    <!-- <NSkeleton :width="180" :height="30" round v-if="loading" /> -->
                    <div class="card-body">
                         <div class="disabled-inner">
                              <NSkeleton class="friend" :height="72" :width="72" round v-if="loading" v-for="(_, index) in [0, 0, 0]" v-bind:key="index" />
                              <!-- <ZOverlay class="treat-as-button disabled">
                                   <NAvatar round :size="72" src="https://api.zenspace.cf/cdn/user/63319240013946880/avatars/avatar_66703325155971072" style="opacity: 0.5;"/>
                                   <template #hover>
                                             <NIcon color="var(--success-color)" :size="18">
                                                  <CheckmarkCircle/>
                                             </NIcon>
                                             <span style="color: var(--success-color)">Enable</span>
                                   </template>
                              </ZOverlay>
                              <ZOverlay class="treat-as-button disabled">
                                   <NAvatar round :size="72" src="https://api.zenspace.cf/cdn/user/63319240013946880/avatars/avatar_66703325155971072" style="opacity: 0.5;" />
                                   <template #hover>
                                             <NIcon color="var(--success-color)" :size="18">
                                                  <CheckmarkCircle/>
                                             </NIcon>
                                             <span style="color: var(--success-color)">Enable</span>
                                   </template>
                              </ZOverlay> -->
                              
                         </div>
                    </div>
               </div>
          </ZRightPane>
     </ZPaneWrapVue>
</template>


<style lang="scss" scoped>
.treat-as-button {
     cursor: pointer;
}
.card-header {
     margin-left: 4px;
     user-select: none;
     font-family: 'Titillium Web';
     padding: 8px;

     &.flex {
          display: flex;
          flex-direction: row;
          justify-content: space-between;
          align-items: center;
     }

     h2 {
          padding: 0;
          margin: 0;
     }
}

.card-body {
     min-width: 300px;
     max-width: 55vw;

     .disabled-inner {
          display: flex;
          flex-direction: row;
          flex-wrap: wrap;
          gap: 16px;
     }

}
</style>