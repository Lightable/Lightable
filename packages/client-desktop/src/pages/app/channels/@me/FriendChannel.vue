<template>
  <div class="friend-channel">
    <div class="check-if" v-if="friend">
      <DMTitle :friend="friend" />
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import DMTitle from '@/components/app/Channel/_dm/DMTitle.vue';
import Avatar from '@/components/app/User/Avatar/Avatar.vue';
import {ClientStore} from '@/stores/ClientStore';
import Users, {User} from '@/lib/structures/Users';
import {Nullable} from '@/lib/utils/null';

export default defineComponent({
  name: 'FriendChannel',
  components: {DMTitle, Avatar},
  setup() {
    let client = ClientStore();
    return {
      friends: client.users as Users,
      self: ClientStore().client?.self!! as User,
    };
  },
  created() {
    this.friend = this.friends.get(this.$route.params.id!! as string) as User;
    this.$watch(
      () => this.$route.params,
      (toParams: unknown, previousParams: unknown) => {
        console.log('TO', toParams, 'Previous', previousParams);
      }
    );
  },
  data() {
    return {
      friend: null as Nullable<User>,
    };
  },
});
</script>

<style lang="scss" scoped>
.friend-channel {
  display: flex;
  flex-direction: column;
}
</style>
