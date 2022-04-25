<template>
  <div class="profile">
    <div class="left">
      <Avatar :user="user" size="64" class="avatar" :circle="true">
        <Edit
          class="avatar-edit"
          color="var(--white)"
          width="15px"
          height="15px"
          @click="() => {}"
          type="button"
        />
      </Avatar>
    </div>
    <div class="title-details">
      <span class="name"
        >{{user.name}}
        <Edit
          class="profile-edit"
          color="var(--white)"
          width="15px"
          height="15px"
          type="button"
          @click="setModalActive(true)"
      /></span>
    </div>
    <div class="upload-bar">
      <ProgressBar :percent-complete="upload" height="10px" />
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent, computed} from 'vue';
import Avatar from '@/components/app/User/Avatar/Avatar.vue';
import Edit from '../../Icons/Edit.vue';
import ProgressBar from '../../ProgressBar.vue';
import { AppStore } from '@/stores/AppStore';
import { ClientStore } from '@/stores/ClientStore';
export default defineComponent({
  name: 'ProfileCard',
  components: {Avatar, Edit, ProgressBar},
  setup() {
    let appStore = AppStore();
    let clientStore = ClientStore();
    return {
      setModalActive: appStore.setNewNameModalVis,
      user: computed(() => clientStore.client.self)
    }
  },
  mounted() {
    // setInterval(() => {
    //   if (this.upload >= 100) this.upload = 0;
    //   this.upload += 50;
    // }, 1000);
  },
  methods: {
    testModalCreation(bool: boolean) {
      console.log('CREATING MODAL', bool);
    }
  },
  data() {
    return {
      upload: 0,
    };
  },
});
</script>

<style lang="scss" scoped>
.profile {
  display: inline-flex;
  position: relative;
  flex-direction: row;
  background: var(--sb);
  border-radius: 8px;
  width: 90%;
  .left {
    .avatar {
      margin-top: auto;
      margin-bottom: auto;
    }
    .avatar-edit {
      position: absolute;
      top: 38px;
      left: 40px;
      padding: 5px;
      background-color: var(--titlebar);
      border-radius: 50%;
      transition: all 250ms ease;
      &:hover {
        filter: brightness(125%);
      }
    }
  }
  .title-details {
    display: inline-flex;
    justify-content: center;
    align-items: center;
    height: 100%;
    flex-direction: column;
    .name {
      font-size: calc(var(--font-size) + 12px);
      font-weight: bold;
      color: var(--white);
      font-family: 'Inter', Courier, monospace;
      .profile-edit {
        border-radius: 50%;
        transition: all 250ms ease;
        margin-bottom: 3px;
        &:hover {
          filter: brightness(125%);
        }
      }
    }
  }
  .upload-bar {
    width: 80px;
    margin-left: auto;
  }
}
</style>
