<template>
  <div class="friend-channel">
    <div class="check-if" v-if="friend">
      <DMTitle :friend="friend" v-on:open="settingsOpen = !settingsOpen"/>
      <ChannelSettings v-if="settingsOpen" :style="`height: ${!settingsOpen ? '0' : '100%'};`"/>
      <ChannelMessages :messages="friend.messages!!" :externaluser="friend"/>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent, computed, WritableComputedRef} from 'vue';
import DMTitle from '@/components/app/Channel/_dm/DMTitle.vue';
import Avatar from '@/components/app/User/Avatar/Avatar.vue';
import {ClientStore} from '@/stores/ClientStore';
import Users, {User} from '@/lib/structures/Users';
import {Nullable} from '@/lib/utils/null';
import {RouteLocationRaw} from 'vue-router';
import ChannelMessages from '@/components/app/Channel/ChannelMessages.vue';
import { AppStore } from '@/stores/AppStore';
import Messages from '@/lib/structures/Messages';
import ChannelSettings from '@/components/app/Channel/ChannelSettings.vue';
export default defineComponent({
  name: 'FriendChannel',
  components: { DMTitle, Avatar, ChannelMessages, ChannelSettings},
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
    let friend = this.friends.get(to.params.id!! as string) as User
    AppStore().setTitleDetails(`${friend.name}`);
    this.friend = friend;
  },
  data() {
    return {
      friend: null as Nullable<User>,
      messages: null as Nullable<WritableComputedRef<Messages>>,
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
