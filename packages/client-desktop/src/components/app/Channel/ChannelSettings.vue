<template>
  <div class="overlay" ref="overlay">
    <div class="channel-settings">

    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue';

export default defineComponent({
  name: 'ChannelSettings',
  render() {
      this.$refs.overlay.style.height = "100%";
  }
});
</script>

<style lang="scss" scoped>
.channel-settings {
  position: absolute;
  background: var(--titlebar);
  width: 100%;
}
.overlay {
  width: 100%;
  height: 0;
  background-color: var(--titlebar);
  transition: 0.3s;
}
</style>
