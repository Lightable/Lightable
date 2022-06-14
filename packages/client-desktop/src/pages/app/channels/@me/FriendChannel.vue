<template>
  <div class="friend-channel">
    <div class="check-if"
      v-if="friend">
      <AddAttachmentModal v-if="drag"
        :files="draggedFiles" :showConfirm="showConfirm" @upload-close="drag = false" @remove-file="(d) => { draggedFiles = draggedFiles.filter(f => f.fullPath != d.fullPath); (draggedFiles.length < 1) ? drag = false : drag = true; }"/>
      <DMTitle :friend="friend"
        v-on:open="settingsOpen = !settingsOpen" />
      <ChannelSettings v-if="settingsOpen"
        :style="`height: ${!settingsOpen ? '0' : '100%'};`" />
      <ChannelMessages :messages="friend.messages!!"
        :externaluser="friend" />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, WritableComputedRef } from 'vue';
import DMTitle from '@/components/app/Channel/_dm/DMTitle.vue';
import Avatar from '@/components/app/User/Avatar/Avatar.vue';
import { ClientStore } from '@/stores/ClientStore';
import Users, { User } from '@/lib/structures/Users';
import { Nullable } from '@/lib/utils/null';
import { RouteLocationRaw } from 'vue-router';
import ChannelMessages from '@/components/app/Channel/ChannelMessages.vue';
import { AppStore } from '@/stores/AppStore';
import Messages, { IAttachment } from '@/lib/structures/Messages';
import ChannelSettings from '@/components/app/Channel/ChannelSettings.vue';
import AddAttachmentModal from '@/components/app/Modal/_attachments/AddAttachmentModal.vue';
import { listen } from '@tauri-apps/api/event';
import { basename } from '@tauri-apps/api/path';
export default defineComponent({
  name: 'FriendChannel',
  components: { DMTitle, Avatar, ChannelMessages, ChannelSettings, AddAttachmentModal },
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
    console.log('Changed nav chan')
    AppStore().setNavigatedToNewChannel(true);
  },
  mounted() {
    listen('tauri://file-drop-hover', (d: any) => {
      let files = [] as IAttachment[];
      d.payload.forEach((file: string) => {
        var extension = file.slice((file.lastIndexOf(".") - 1 >>> 0) + 2);
        var baseName = file.split(/[\/\\]/).slice(-1).pop();
        files.push({
          name: baseName!!.replace(`.${extension}`, ''),
          extension: extension,
          size: 0,
          fullPath: file
        });
      })
      this.drag = true;
      this.draggedFiles = files;
    });
    listen('tauri://file-drop-cancelled', (d) => {
      this.drag = false;
      this.draggedFiles = [];
    });
    listen('tauri://file-drop', (d: any) => {
      this.showConfirm = true;
    });
  },
  data() {
    return {
      friend: null as Nullable<User>,
      messages: null as Nullable<WritableComputedRef<Messages>>,
      settingsOpen: false as boolean,
      drag: false as boolean,
      draggedFiles: [] as IAttachment[],
      showConfirm: false
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
