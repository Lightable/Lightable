<script setup lang="ts">
import { ref } from 'vue';
import { NModal, NInput, NButton, NTooltip } from 'naive-ui';
import { PhWarning } from '@dnlsndr/vue-phosphor-icons';
let previousRightClick = false;

const modal = ref({
  show: false,
});

window.addEventListener('contextmenu', (e: MouseEvent) => {
  if (!e.shiftKey) {
    e.preventDefault();
    if (previousRightClick) {
      modal.value.show = true;
    }
    previousRightClick = true;
    setTimeout(() => {
      previousRightClick = false;
    }, 200)
  } else {
    return
  }
})
</script>

<template>
  <NModal preset="dialog" v-model:show="modal.show" title="Bug Report" type="error">
    <div class="body">
      It seems you've found a bug to report.. Double right click activates this.. What went wrong?
      <NInput placeholder="Let us know what went wrong" type="textarea" :resizable="false" class="info" status="warning"/>
    </div>
    <template #action>
      <NTooltip trigger="hover">
        <template #trigger>
          <div>
            <NButton secondary disabled>Submit</NButton>
          </div>
        </template>
        <span class="not-yet-implemented">
          <PhWarning weight="duotone" color="yellow" :size="18"/> This feature is not yet supported
        </span>
      </NTooltip>
    </template>
  </NModal>
</template>


<style lang="scss" scoped>
.body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  .info {
    height: 250px;
  }
}
.not-yet-implemented {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  gap: 4px;
}
</style>