<template>
  <div class="friend-channel">
    <div class="check-if" v-if="friend">
      <DMTitle :friend="friend" v-on:open="settingsOpen = true" />
      <ChannelSettings :style="`height: ${settingsOpen ? '100%' : '0'}`" />
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent, Transition} from 'vue';
import DMTitle from '@/components/app/Channel/_dm/DMTitle.vue';
import Avatar from '@/components/app/User/Avatar/Avatar.vue';
import {ClientStore} from '@/stores/ClientStore';
import Users, {User} from '@/lib/structures/Users';
import {Nullable} from '@/lib/utils/null';
import {RouteLocationRaw} from 'vue-router';
import ChannelSettings from '@/components/app/Channel/ChannelSettings.vue';
export default defineComponent({
  name: 'FriendChannel',
  components: {ChannelSettings, DMTitle, Avatar},
  setup() {
    let client = ClientStore();
    return {
      friends: client.users as Users,
      self: ClientStore().client?.self!! as User,
    };
  },
  created() {
    this.friend = this.friends.get(this.$route.params.id!! as string) as User;
  },
  beforeRouteUpdate(to: RouteLocationRaw) {
    this.friend = this.friends.get(to.params.id!! as string) as User;
  },
  data() {
    return {
      friend: null as Nullable<User>,
      settingsOpen: false as boolean,
    };
  },
});
</script>

<style lang="scss" scoped>
.friend-channel {
  display: flex;
  flex-direction: column;
  height: 100%;
  .check-if {
    height: 100%;
  }
}
</style>
