<template>
  <div class="friends">
    <SideContextMenu>
      <template v-slot:icon>
        <Admin height="35px" width="35px" color="var(--white)" />
      </template>
      <template v-slot:header>
        <span>Admin</span>
      </template>
      <template v-slot:actions> </template>
      <template v-slot:body>
        <div class="releases">
          <div class="__header">
            <h2>Releases</h2>
          </div>
          <button class="db" dbt="default" @click="setReleaseModal(true)">
            Create Release
          </button>
          <div class="releases">
            <Release v-for="(release, index) in releases" v-bind:key="index" :release="release"/>
          </div>
          
        </div>
      </template>
    </SideContextMenu>
  </div>
</template>


<script lang="ts">
import {defineComponent, computed} from 'vue';
import SideContextMenu from '../index.vue';
import {AppStore} from '@/stores/AppStore';
import {ClientStore} from '@/stores/ClientStore';
import Admin from '@/components/Icons/Admin.vue';
import Release from './_cards/Release/Release.vue';
import { Nullable } from '@/lib/utils/null';
import { Release  as R } from '@/lib/structures/Release';
export default defineComponent({
  name: 'AdminContextMenu',
  components: {SideContextMenu, Admin, Release},
  setup() {
    let appStore = AppStore();
    return {
      setReleaseModal: appStore.setNewReleaseModalVis,
    };
  },
  mounted() {
    let clientStore = ClientStore();
    clientStore.client?.getReleases().then((releases) => {
        this.releases = releases;
    })
    //    let cvt = Array.from(this.users!!.values())
    //    for (let i = 0; cvt.length > i; i++) {
    //        let user = cvt[i];
    //        console.log(user, user.messages?.last[1]);
    //        this.last.set(user._id,user.messages?.last[1] as Message);
    //    }
    //    clientStore.client?.on('message', (msg: any) => {
    //        let message = msg.message as IMessage;
    //        this.last.set(message.user.id, new Message(clientStore.client as Client, message));
    //    })
  },
  data() {
      return {
          releases: null as Nullable<R[]>
      }
  }
});
</script>

<style lang="scss" scoped>
.add-f-bg {
  background-color: var(--ac);
  border-radius: 50%;
  height: 28px;
  width: 28px;
  display: inline-flex;
  justify-content: center;
  align-items: center;
}
.pending,
.requests {
  .title {
    font-size: calc(var(--font-size) + 12px);
    color: var(--white);
    display: flex;
    justify-content: flex-start;
    margin-top: 20px;
    border-bottom: 1px var(--gray) solid;
    padding: 3px;
  }
}
.empty {
  color: gray;
  padding: 8px;
}
.releases {
  .__header {
    color: var(--blue);
  }
}
</style>