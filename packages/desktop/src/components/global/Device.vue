<template>
  <div class="device-card">
    <div class="body">
      <div class="main">
        <Firefox
          height="128px"
          width="128px"
          :color="getEffectiveBrowserColor(device.browser)"
          v-if="device.browser == 'Firefox'"
        />
        <Desktop
          height="128px"
          width="128px"
          :color="getEffectiveBrowserColor(device.browser)"
          v-if="device.browser == 'Desktop'"
        />
        <div class="data">
          <span class="title">{{ device.browser }} on {{ device.os }}</span>
          <div class="details">
            <div class="ip data-pack">
              <span class="key">IP: </span><span class="value">{{ device.ip }}</span>
            </div>
            <div class="browser data-pack">
              <span class="key">Browser: </span><span class="value">{{ device.browser }}</span>
            </div>
            <div class="os data-pack">
              <span class="key">OS: </span><span class="value">{{ device.os }}</span>
            </div>
            <div class="build data-pack">
              <span class="key">Build: </span><span class="value">{{ device.build }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent, PropType} from 'vue';
import {BrowserType, Device} from '@/lib/structures/Device';
import Firefox from '@/components/Icons/Browser/firefox.vue';
import Desktop from '@/components/Icons/Browser/desktop.vue';
import { computed } from '@vue/reactivity';
import { ClientStore } from '@/stores/ClientStore';
export default defineComponent({
  name: 'Device',
  setup() {
    return {
      device: computed(() => ClientStore().connectedDevice),
      getEffectiveBrowserColor: (browser: BrowserType) => {
        switch (browser) {
          case 'Desktop': {
            return 'var(--ac)';
          }
          case 'Firefox': {
            return 'var(--orange)';
          }
          case 'Firefox Developer': {
            return 'var(--purple)';
          }
          case 'Chrome': {
            return 'var(--green)';
          }
        }
      },
    };
    
  },
  components: {Firefox, Desktop},
});
</script>

<style lang="scss" scoped>
.device-card {
  display: flex;
  flex-direction: column;
  background: var(--titlebar);
  padding: 8px;
  border-radius: 8px;
  .body {
    .main {
      display: inline-flex;
      flex-direction: row;
      .data {
        display: flex;
        flex-direction: column;
        .details {
            display: flex;
        flex-direction: row;
        flex-wrap: wrap;
        justify-content: space-between;
          .data-pack {
            color: var(--purple);
            padding: 8px;
            .value {
              color: var(--ac);
            }
          }
        }

        .title {
          font-size: calc(var(--font-size) + 18px);
          font-weight: bold;
          color: var(--blue);
          padding: 8px;
        }
      }
    }
  }
}
</style>
