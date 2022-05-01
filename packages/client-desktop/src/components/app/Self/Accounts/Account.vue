<template>
  <BringUp>
    <div class="account" @click="() => { setDefault(account._id); $router.push('/app/channels/@me')}" type="button">
      <div class="main">
        <Avatar size="64" :user="account" :circle="true" />
        <div class="details">
          <span class="name">{{ account.name }}</span>
          <span class="email">{{ account.email }}</span>
        </div>
        <div class="remove" type="button">
          <tippy content="Remove Account" placement="top">
            <Close color="var(--error)" class="remove__icon" @click="removeAccount(account._id)"/>
          </tippy>
        </div>
      </div>
    </div>
  </BringUp>
</template>

<script lang="ts">
import {defineComponent, PropType} from 'vue';
import {ISelf, User} from '@/lib/structures/Users';
import Avatar from '@/components/app/User/Avatar/Avatar.vue';
import BringUp from './_extensions/BringUp.vue';
import Close from '../../../Icons/Close.vue';
import { AuthenticationStore } from '@/stores/AuthenticationStore';
export default defineComponent({
  name: 'Account',
  components: {Avatar, BringUp, Close},
  props: {
    account: {
      type: Object as PropType<User>,
      required: true,
    },
  },
  setup() {
      let authStore = AuthenticationStore();
      return {
          removeAccount: authStore.removeAccount,
          setDefault: authStore.setDefault
      }
  }
});
</script>

<style lang="scss" scoped>
.account {
  background: #2a2a2ac0;
  padding: 8px;
  border-radius: 8px;
  width: 98%;
  transition: 150ms ease;
  &:hover {
      background-color: #2a2a2a;
  }
}
.main {
  display: flex;
  flex-direction: row;
  padding: 8px;
  justify-content: space-between;
  .details {
    display: flex;
    flex-direction: column;
    margin-left: 8px;
    .name {
      font-weight: bold;
      font-size: calc(var(--font-size) + 12px);
    }
    .email {
      color: var(--orange);
      margin-top: 5px;
      font-size: calc(var(--font-size) + 8px);
    }
  }
}
.remove__icon {
  transition: all 250ms ease;
  &:hover {
    fill: var(--blue);
  }
}
</style>

