<template>
<div class="friends">
     <SideContextMenu>
          <template v-slot:icon>
            <Friend height="35px" width="35px" color="var(--white)"/>
          </template>
          <template v-slot:header>
            <span>Friends</span>
          </template>
          <template v-slot:actions>
              <div class="add-f-bg" type="button" @click="setActiveModal(true)">
                  <AddFriend width="20px" color="var(--white)"/>
              </div>
          </template>
          <template v-slot:body>
              <FriendItem v-for="(user, index) in users" :user="user[1]" v-bind:key="index" :last="last.get(user.id)"/>
              <span v-if="users.size < 1" class="empty">
                It's a bit empty here...<br><button type="button" class="db" @click="setActiveModal(true)">Add Friend</button>
              </span>
              <div class="pending">
                  <span class="title" v-if="pending.size >= 1">Pending</span>
                   <FriendItem v-for="(pend, index) in pending" :user="pend[1]" v-bind:key="index" :last="false" :pending="true"/>
              </div>
          </template>
        </SideContextMenu>
</div>
</template>


<script lang="ts">
import { defineComponent, computed } from "vue";
import SideContextMenu from "./SideContextMenu.vue";
import Friend from "../Icons/Friend.vue";
import AddFriend from "../Icons/AddFriend.vue";
import { AppStore } from "@/stores/AppStore";
import FriendItem from "./FriendItem.vue";
import { ClientStore } from "@/stores/ClientStore";
import { IMessage, Message } from "@/lib/structures/Messages";
import { Client } from '@/lib/Client';

export default defineComponent({
    name: "FriendContextMenu",
    components: { SideContextMenu, Friend, AddFriend, FriendItem },
    setup() {
        let appStore = AppStore();
        let clientStore = ClientStore();
        console.log(clientStore.client?.users)
        return {
            setActiveModal: appStore.setFriendModalVis,
            users: computed(() => clientStore.client?.users),
            pending: computed(() => clientStore.client?.pending)
        }
    },
    mounted() {
       let clientStore = ClientStore();
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
            last: new Map<string, Message>()
        }
    },
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
.pending {
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
</style>