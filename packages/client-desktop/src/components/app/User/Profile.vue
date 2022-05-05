<template>
  <BaseModal v-on:clicked="setCurrentProfile(null)" :headerbg="profile.getAvatar()+'?size=512'">
    <template v-slot:icon>
      <Friend height="30px" width="30px" color="var(--white)" />
    </template>
    <template v-slot:name>
      <span class="title">@{{ profile.name }}</span>
    </template>
    <template v-slot:body>
      <div
        class="bio"
        v-html="profile.about == null ? 'No bio was found' : parse(profile.about)"
      ></div>
    </template>
    <template v-slot:actions>
      <div class="actions">
        <button class="db" dbt="danger" @click="async () => { await profile.removeFriend(); setCurrentProfile(null); }">Remove</button>
        <button class="db" dbt="success" @click="() => {setCurrentProfile(null); $router.push(`/app/channels/@me/${profile._id}`)}">Send Message</button>
      </div>
    </template>
  </BaseModal>
  <!-- <DebugModalData :content="profile" v-if="true"/> -->
</template>

<script lang="ts">
import {defineComponent, PropType} from 'vue';
import BaseModal from '@/components/app/Modal/BaseModal.vue';
import Friend from '@/components/Icons/Friend.vue';
import parseMarkdown from '@/lib/utils/parseMarkdown';
import { AppStore } from '@/stores/AppStore';
import { User } from '@/lib/structures/Users';
import DebugModalData from '@/components/app/Modal/DebugModalData.vue';
export default defineComponent({
  name: 'ProfileModal',
  components: {BaseModal, Friend, DebugModalData},
  props: {
    profile: {
      type: Object as PropType<User>,
      required: true
    },
  },
  setup() {
      let appStore = AppStore();
    return {
      parse: parseMarkdown,
      setCurrentProfile: appStore.setCurrentlyActiveProfile
    };
  },
});
</script>

<style lang="scss" scoped>
.title {
  font-size: calc(var(--font-size) + 12px);
  margin-left: 8px;
  color: var(--white);
  font-weight: bold;
}
.bio {
  padding: 8px;
  color: var(--white);
}
.actions {
  width: 100%;
  justify-content: space-between;
  align-content: space-around;
  display: flex;
  flex-direction: row;
}
</style>
