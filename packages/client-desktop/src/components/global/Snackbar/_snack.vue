<template>
  <div
    class="snack"
    :closing="Boolean(closing)"
    :id="String(snack.id)"
    :clickable="snack.close"
    @click="closeSnack(snack.id)"
  >
    <div class="icon">
      <SuccessIcon v-if="snack.type == 'success'" />
      <WarningIcon v-if="snack.type == 'warning'" />
      <ErrorIcon v-if="snack.type == 'error'" />
      <LoadingIcon v-if="snack.type == 'loading'" />
    </div>
    <span class="title" v-html="snack.text"></span>
  </div>
</template>

<script lang="ts">
import {defineComponent, PropType} from 'vue';
import {Snack, SnackStore} from '../../../stores/SnackStore';
import SuccessIcon from '../Icons/Status/SuccessIcon.vue';
import WarningIcon from '../Icons/Status/WarningIcon.vue';
import ErrorIcon from '../Icons/Status/ErrorIcon.vue';
import LoadingIcon from '../Icons/Status/LoadingIcon.vue';
export default defineComponent({
  props: {
    snack: {
      type: Object as PropType<Snack>,
      required: true,
    },
  },
  mounted() {
    let snackStore = SnackStore();
    if (this.snack.timeout) {
      setTimeout(() => {
        this.closing = true;
        setTimeout(() => {
          snackStore.remove(this.snack.id);
        }, 100);
      }, this.snack.timeout);
    }
  },
  methods: {
    closeSnack(id: string) {
      let snackStore = SnackStore();
      this.closing = true;
      setTimeout(() => {
        snackStore.remove(id);
      }, 100);
    },
  },
  data() {
    return {
      closing: false,
    };
  },
  components: {SuccessIcon, WarningIcon, ErrorIcon, LoadingIcon},
});
</script>
<style lang="scss" scoped>
.snack {
  background: var(--sb);
  padding: 8px;
  border-radius: 8px;
  max-width: 75%;
  color: var(--white);
  margin-top: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
  pointer-events: all;
  &[closing='true'] {
    animation: bring-up 500ms ease forwards;
  }
  &[closing='false'] {
    animation: bring-down 500ms ease;
  }
  &[clickable='true'] {
    cursor: pointer;
  }
  .icon {
    height: 24px;
    width: 24px;
  }
  .title {
    margin-left: 5px;
  }
}

@keyframes bring-down {
  from {
    opacity: 0;
    transform: translateY(-120%);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
@keyframes bring-up {
  from {
    opacity: 1;
    transform: translateY(0);
  }
  to {
    opacity: 0;
    transform: translateY(-120%);
  }
}
</style>
