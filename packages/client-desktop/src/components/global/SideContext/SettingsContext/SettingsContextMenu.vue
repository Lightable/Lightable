<template>
  <div class="settings">
    <SideContextMenu style="width: 350px">
      <template v-slot:icon>
        <Settings height="35px" width="35px" color="var(--purple)" />
      </template>
      <template v-slot:header>
        <span>Settings</span>
      </template>
      <template v-slot:body>
        <div class="profile">
          <span class="title"> Profile </span>
          <KinesisContainer
            easing="cubic-bezier(0.23, 1, 0.32, 1)"
            event="move"
            class="profile-card-container"
          >
            <KinesisElement
              tag="div"
              :strength="10"
              type="depth"
              transformOrigin="center"
              class="profile-card-wrap"
            >
              <ProfileCard/>
            </KinesisElement>
          </KinesisContainer>
        </div>
        <div class="theme-options card">
         <ThemeCard/>
        </div>
        <div class="developer-options card">
          <DeveloperCard/>
        </div>
      </template>
    </SideContextMenu>
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import SideContextMenu from '../SideContextMenu.vue';
import Settings from '../../Icons/Settings.vue';
import {KinesisContainer, KinesisElement} from 'vue-kinesis';
import Friend from '../../Icons/Friend.vue';
import ProfileCard from './ProfileCard.vue';
import Toggle from '@vueform/toggle';
import DeveloperCard from './DeveloperCard.vue';
import ThemeCard from './ThemeCard.vue';
export default defineComponent({
  name: 'SettingsSideMenu',
  components: { SideContextMenu, Settings, KinesisContainer, KinesisElement, Friend, ProfileCard, Toggle, DeveloperCard, ThemeCard },
});
</script>

<style lang="scss" scoped>
.profile {
  .title {
    color: var(--purple);
  }
  .profile-card-container {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    .profile-card-wrap {
      width: 100%;
      display: flex;
      justify-content: center;
      align-items: center;
      margin: 5px;
    }
  }
}
.card {
  padding: 8px;
  margin-top: 10px;
}
</style>
