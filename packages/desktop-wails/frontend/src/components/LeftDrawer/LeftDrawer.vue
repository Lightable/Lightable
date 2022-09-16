<script setup lang="ts">
import {computed, ref} from 'vue';
import {NButton, NIcon, NInput} from 'naive-ui';
import {Light} from '@vicons/carbon';
import {Search24Regular} from '@vicons/fluent';
import {useAppStore} from '../../stores/AppStore';
import {useClickOutside} from '../../composable/click-outside'
import SearchGlobal from '../search/SearchGlobal.vue';
import {ChevronForward, ChevronBack} from '@vicons/ionicons5';

const appStore = useAppStore();

const isCollapsed = computed(() => appStore.leftDrawer.collapsed);
const version = computed(() => appStore.version);
const search = ref({
  searching: false,
  term: ''
});
const collapsed = ref({
  pos: {y: 0},
  showChevron: false
});
const container = ref(null);

let intervalDelayChevron: any;

const onSearchClick = () => {
  appStore.search.show = true;
}

useClickOutside(container, () => {
  search.value.searching = false;
})

document.addEventListener('keydown', (ev) => {
  if (ev.key === 'f' && ev.ctrlKey && !appStore.search.show) {
    ev.preventDefault();
    appStore.search.show = true;
    return
  }
})

const trackCollapseMouseOver = () => {
  if (intervalDelayChevron) clearInterval(intervalDelayChevron);
  collapsed.value.showChevron = true;
}

const trackCollapseMouseLeave = () => {
  intervalDelayChevron = setInterval(() => {
    collapsed.value.showChevron = false;
  }, 1000)
}

const unCollapse = () => {
  appStore.leftDrawer.collapsed = false;
}
const collapse = () => {
  appStore.leftDrawer.collapsed = true;
}
</script>


<template>
  <div class="bring-out" v-if="isCollapsed" @mouseenter="trackCollapseMouseOver" @mouseleave="trackCollapseMouseLeave">
    <Transition name="bring-out">
      <NButton text type="success" class="chevron" v-if="collapsed.showChevron" @click="unCollapse">
        <template #icon>
          <NIcon>
            <ChevronForward/>
          </NIcon>
        </template>
      </NButton>
    </Transition>

    <!-- <Transition>
        <div class="inner" :style="{ left: `15px`, top: `${collapsed.pos.y}px` }" v-if="showChevron">

        </div>
    </Transition> -->
  </div>
    <div class="drawer-container" :style="isCollapsed ? 'transform: translateX(-250px); width: 0;' : 'transform: translateX(0); width: 250px;'">
      <div class="header">
        <NButton text type="error" class="chevron" @click="collapse">
          <template #icon>
            <NIcon>
              <ChevronBack/>
            </NIcon>
          </template>
        </NButton>
        <div class="search-container" @click="onSearchClick" ref="container">
          <input type="text" placeholder="Search..." class="search-input" :searching="search.searching"/>
          <NButton text type="info" size="large" class="search-btn" v-if="!search.searching">
            <template #icon>
              <NIcon>
                <Search24Regular/>
              </NIcon>
            </template>
          </NButton>
        </div>
      </div>
      <div class="actions">
        <div class="top-level">
          <slot name="top"/>
        </div>
        <div class="groups">
          <slot name="groups"/>
        </div>
      </div>
      <div class="lower ns">
        <span>{{ version }}</span>
      </div>
    </div>
</template>


<style lang="scss" scoped>
.bring-out {
  height: 100vh;
  width: 20px;
  transition: background-color 250ms ease;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: absolute;
  z-index: 5;
}

.drawer-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: rgba(51, 51, 51, 0.055);
  transition: all .25s ease;
  .header {
    display: flex;
    justify-content: flex-start;
    gap: 8px;
    align-items: center;
    height: 45px;
    padding-top: 1px;
    padding-left: 8px;

    .icon {
      height: 26px;
      width: 26px;
      font-size: 18px;
    }

    .title {
      color: #fff;
      font-size: 1rem;
    }

    .search-container {
      height: 25px;
      width: 100%;
      margin-left: auto;
      margin-right: 12px;
      background-color: rgba(41, 41, 41, 0.411);
      border-radius: .5rem;
      display: flex;
      justify-content: space-evenly;
      align-items: center;

      .search-input {
        all: unset;
        font-size: 12px;
        line-height: inherit;
        height: 20px;
        width: 75%;
        padding-left: 5px;
        color: var(--text-color-2);

        &[searching='true'] {
          width: 111px;
        }
      }

      .search-btn {
      }

      // .search-icon {
      //     height: 24px;
      //     width: 24px;
      //     font-size: 18px;
      // }
    }

  }

  .actions {
    display: flex;
    flex-direction: column;
    gap: 8px;
    justify-content: center;
    align-items: center;

    .top-level {
      flex-direction: column;
      display: flex;
      width: 100%;
      justify-content: center;
      align-items: center;
    }

    .groups {
      flex-direction: column;
      display: flex;
      width: 100%;
      justify-content: center;
      align-items: center;
    }

  }

  .lower {
    margin-top: auto;
    margin-left: 5px;
    color: var(--text-color-3);

    span {
      font-family: "Jetbrains Mono";
      font-weight: lighter;
    }
  }
}

</style>


