<template>
<div class="unroll" :active="active">
    <div class="item" type="button" @click="active = false">
        <OnlineStatusSymb height="20px" class="icon"/> Online
    </div>
    <div class="item" type="button" @click="active = false">
         <IdleStatusSymb height="20px" class="icon"/> Idle
    </div>
    <div class="item" type="button" @click="active = false">
         <DoNotDisturbStatusSymb height="20px" class="icon"/> DND
    </div>
    <div class="item" type="button" @click="active = false">
        <OfflineStatusSymb height="20px" class="icon"/> Offline
    </div>
</div>
  <div class="self" @click="active = !active">
    <div class="inner-self">
      <Avatar size="35" :user="me" :displaywarn="false" @click="active = !active" type="button" :circle="true"/>
    </div>
  </div>
</template>

<script lang="ts">
import {ClientStore} from '@/stores/ClientStore';
import {computed, defineComponent} from 'vue';
import Avatar from './User/Avatar/Avatar.vue';
import OnlineStatusSymb from '@/components/app/User/Avatar/_extensions/status/OnlineStatusSymb.vue';
import IdleStatusSymb from '@/components/app/User/Avatar/_extensions/status/IdleStatusSymb.vue';
import DoNotDisturbStatusSymb from '@/components/app/User/Avatar/_extensions/status/DoNotDisturbStatusSymb.vue';
import OfflineStatusSymb from '@/components/app/User/Avatar/_extensions/status/OfflineStatusSymb.vue';

export default defineComponent({
  name: 'SelfItem',
  setup() {
    let self = ClientStore().client?.self;
    return {me: computed(() => self) };
  },
  components: { Avatar, OnlineStatusSymb, IdleStatusSymb, DoNotDisturbStatusSymb, OfflineStatusSymb },
  data() {
      return {
          active: false
      }
  }
});
</script>

<style lang="scss" scoped>
.unroll {
    position: absolute;
    z-index: 9;
    background: var(--titlebar);
    width: 100px;
    height: 45px;
    top: 35px;
    left: -275px;
    transition: all 150ms ease-in-out;
    display: flex;
    justify-content: center;
    flex-direction: column;
    justify-content: space-evenly;
    opacity: 0;
    border-radius: 8px;
    border: var(--ac) 1.8px solid;

    .item {
        color: var(--white);
        margin: 5px;
        display: flex;
        flex-direction: row;
        align-items: center;
        font-size: calc(var(--font-size) + 5px);
        user-select: none;
        background: var(--titlebar);
        border-radius: 8px;
        padding: 3px;
        .icon {
            margin-left: 2px;
            margin-right: 8px;
        }
        &:hover {
            filter: brightness(120%);
        }
    }
    &[active='true'] {
        left: 56px;
        height: 140px;
        opacity: 1;
    }
}
.self {
   position: relative;
   z-index: 1;
  display: flex;
  width: 100%;
  padding-bottom: 5px;
  padding-top: 2px;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  .details {
    .name {
      color: var(--white);
      font-size: calc(var(--font-size) + 4px);
      padding: 3px;
    }
    
  }

  .inner-self {
    border-radius: 50%;
    border: var(--green) 2px solid;
    height: 35px;
    width: 35px;
    transition: all 150ms ease-in-out;
    &:hover {
        filter: brightness(115%);

    }
    
  }
}
</style>
