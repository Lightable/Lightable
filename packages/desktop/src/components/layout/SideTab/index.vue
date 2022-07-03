<template>
    <div class="side-ctx-menu">
      <div class="header">
          <div class="header-icon">
            <slot name="icon" class="header-icon" />
          </div>

          <div class="header-title">
            <slot name="header" class="header-title" />
          </div>
          <div class="header-actions">
            <slot name="actions" class="header-title" />
          </div>
      </div>
      <div class="body">
        <slot name="body" />
      </div>
    </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import BringOutAni from './_extensions/BringOutAni.vue';
export default defineComponent({
  name: 'DefaultSideContextMenu',
  components: {BringOutAni},
});
</script>

<style lang="scss" scoped>
.side-ctx-menu {
  box-shadow: 10px 2px 11px -5px var(--titlebar);
  margin-left: 5px;
  .header {
    height: 50px;
    display: flex;
    flex-direction: row;
    align-items: center;
    width: 100%;
    .flex-header {
      height: 50px;
      display: flex;
      flex-direction: row;
      align-items: center;
      width: 100%;
    }
    .header-title {
      font-size: calc(var(--font-size) + 18px);
      color: var(--white);
      margin-left: 10px;
      animation: slide-out-right 250ms ease;
    }
    .header-icon {
      margin-top: 2px;
      animation: slide-out-right 250ms ease;
    }
    .header-actions {
      width: 100%;
      display: flex;
      justify-content: flex-end;
      padding: 20px;
      align-items: center;
      animation: slide-down 250ms ease;
    }
  }
  height: 100vh;
  min-width: 250px;
  overflow-y: scroll;
}
.body {
  height: 100%;
  display: flex;
  flex-direction: column;
}
::-webkit-scrollbar {
    display: none;
}

@keyframes slide-out-right {
  from {
    transform: translateX(-250px);
  }
  to {
    tranform: translateX(0);
  }
}
@keyframes slide-down {
  from {
    transform: translateY(-60px);
  }
  to {
    transform: translateY(0);
  }
}
</style>
