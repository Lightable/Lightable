<template>
    <div class="avatar" v-if="user.avatar == null">
      <Missing :height="size" :width="size" color="var(--error)" class="avatar-src" :circle="circle"/>
    </div>
     <div class="avatar" v-else>
      <img :src="user.getAvatar()+`?size=64`" :height="size" :width="size" class="avatar-src" :circle="circle"/>
  </div>
  <slot/>
</template>

<script lang="ts">
import {defineComponent, computed, PropType} from 'vue';
import {ClientStore} from '../../../../stores/ClientStore';
import {Filter} from './_extensions/AvatarFilter';
import Missing from '@/components/Icons/Missing.vue';
import { User } from '@/lib/structures/Users';

export default defineComponent({
  name: 'Avatar',
  props: {
    size: {
      type: String,
      required: false,
      default: () => '32',
    },
    filter: {
      type: String,
      required: false,
      default: () => 'None' as Filter,
    },
    user: {
      type: Object as PropType<User>,
      required: true,
    },
    circle: Boolean
  },
  setup() {
    let client = ClientStore();
    return {
      client: computed(() => client.client),
    };
  },
  components: {Missing},
});
</script>

<style lang="scss" scoped>
  .avatar-src {
    &[circle='true'] {
      border-radius: 50%;
    }
  }
</style>
