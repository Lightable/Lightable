<template>
  <BringOutAni>
    <div class="side-ctx-menu">
      <div class="header">
        <BringOutAni class="flex-header" v-if="true">
          <div class="header-icon">
            <slot name="icon" class="header-icon" />
          </div>

          <div class="header-title">
            <slot name="header" class="header-title" />
          </div>
          <div class="header-actions">
            <slot name="actions" class="header-title" />
          </div>
        </BringOutAni>
      </div>
      <div class="body">
        <slot name="body" />
      </div>
    </div>
  </BringOutAni>
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import BringOutAni from './BringOutAni.vue';
export default defineComponent({
  name: 'DefaultSideContextMenu',
  components: {BringOutAni},
});
</script>

<style lang="scss" scoped>
.side-ctx-menu {

  .header {
    border-bottom: var(--gray) 1px solid;
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
    }
    .header-icon {
      margin-top: 2px;
    }
    .header-actions {
      width: 100%;
      display: flex;
      justify-content: flex-end;
      padding: 20px;
      align-items: center;
    }
  }
  height: 100vh;
  background-color: var(--titlebar);
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
</style>
