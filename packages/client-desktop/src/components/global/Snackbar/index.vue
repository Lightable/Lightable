<template>
    <div class="snackbar">
        <Snack v-for="(snack, index) in snacks" v-bind:key="index" :snack="snack[1]" />
    </div>
</template>

<script lang="ts">
import { computed, defineComponent } from 'vue'
import { SnackStore } from '../../../stores/SnackStore'
import _snack from './_snack.vue';

export default defineComponent({
    setup() {
        let snackStore = SnackStore();
        return {snacks: computed(() => snackStore.snacks)}
    },
    components: { Snack: _snack },
})
</script>

<style lang="scss" scoped>
.snackbar {
  position: absolute;
  z-index: 1;
  left: 0;
  right: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin:auto;
  overflow: scroll;
  pointer-events: none;
  &::-webkit-scrollbar {
      display: none;
  }
}
</style>
