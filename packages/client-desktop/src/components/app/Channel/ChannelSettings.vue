<template>
  <div class="overlay" ref="overlay">
    <div class="channel-settings">
      <div class="group-containers">
        <ChannelSettingsGroup>
          <template v-slot:title> General </template>
          <template v-slot:body>
            <div class="toggle-wrapper">
              <span class="__text"> Allow <b>NSFW</b> </span>
              <div class="__toggle">
                <Toggle />
              </div>
            </div>
            <div class="toggle-wrapper">
              <span class="__text"> Receive <b>Notifications</b> </span>
              <div class="__toggle">
                <Toggle />
              </div>
            </div>
            <div class="toggle-wrapper">
              <span class="__text"> Show <b>Typing</b> </span>
              <div class="__toggle">
                <Toggle />
              </div>
            </div>
            <div class="toggle-wrapper">
              <span class="__text"> Show <b>Active</b> </span>
              <div class="__toggle">
                <Toggle />
              </div>
            </div>
          </template>
        </ChannelSettingsGroup>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import ChannelSettingsGroup from './ChannelSettingsGroup.vue';
import Toggle from '@vueform/toggle';
export default defineComponent({
  name: 'ChannelSettings',
  components: {ChannelSettingsGroup, Toggle},
});
</script>

<style lang="scss" scoped>
.channel-settings {
  background: var(--titlebar);
  width: 100%;
}
.overlay {
  width: 100%;
  height: 0;
  background-color: var(--titlebar);
  transition: 0.3s;
  position: absolute;
}
.toggle-wrapper {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-top: 10px;
  .__text {
    color: var(--white);
    width: 250px;
  }
  .__toggle {
    margin-left: 40px;
  }
}
</style>
