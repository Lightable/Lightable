<template>
<AddFriendModal @clicked="addFriendModalClicked" v-if="showFriendModal"/>
  <div class="sec-sidebar">
    <div class="title">
    </div>
    <div class="data">
      <!-- <SidebarItem path="/app/channels/@me/friends">
        <template v-slot:icon>
          <Friend color="var(--ac)"/>
        </template>
        <template v-slot:text>
               Friends
        </template>
      </SidebarItem> -->
     
        <SidebarItem class="friend" :path="`/app/channels/@me/fakeid`">
        <template v-slot:icon>
          <TempAvatar :status="1">
            <template v-slot:icon>
                <Missing color="random" height="32px" width="32px"/>
            </template>
          </TempAvatar>
          
        </template>
        <template v-slot:text>
               TEst account
        </template>
      </SidebarItem>
        <SidebarItem class="add-friend" @click="showFriendModal = true">
        <template v-slot:icon>
          <AddFriend color="var(--blue)"/>
        </template>
        <template v-slot:text>
               Add Friend
        </template>
      </SidebarItem>
    </div>
   
  </div>
   <div class="user-content">
        <SelfPanel/>
    </div>
</template>

<script>
import {defineComponent} from 'vue';
import SidebarItem from './SidebarItem.vue';
import Friend from '@/components/global/Icons/Friend.vue';
import SelfPanel from '../SelfPanel.vue';
import AddFriend from '@/components/global/Icons/AddFriend.vue';
import Overlay from '@/components/global/Overlay.vue';
import AddFriendModal from '@/components/global/AddFriendModal.vue';
import Missing from '@/components/global/Icons/Missing.vue';
import TempAvatar from '../../User/Avatar/TempAvatar.vue';

export default defineComponent({
  components: { SidebarItem, Friend, SelfPanel, AddFriend, Overlay, AddFriendModal, Missing, TempAvatar },
  data() {
    return {
      showFriendModal: false
    }
  },
  methods: {
    addFriendModalClicked() {
      this.showFriendModal = false;
    }
  }
});
</script>

<style lang="scss" scoped>

.sec-sidebar {
  width: 185px;
  background-color: var(--sb);
  height: calc(100vh - 70px);
  .data {
      display: flex;
      justify-content: flex-start;
      align-items: center;
      flex-direction: column;
      height: 100%;
  }
}
.title {
  display: flex;
  justify-content: center;
  background: var(--sc-bg);
  border-bottom: var(--ac) 2px solid;
  .title-text {
    margin-left: 5px;
    font-size: 1.5rem;
    color: var(--white);
    
  }
  
}
.add-friend {
  justify-self: flex-end;
}
</style>
