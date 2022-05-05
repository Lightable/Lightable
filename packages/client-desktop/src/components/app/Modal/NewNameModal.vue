<template>
  <BaseModal @clicked="modalActive(false)">
    <template v-slot:icon>
      <Name height="30px" width="30px" color="var(--white)" />
    </template>
    <template v-slot:name>
      <span class="title">Enter A New Name</span>
    </template>
    <template v-slot:body>
      <div class="input-group">
        <span class="username-label">Name</span>
        <div class="input-wrapper">
          <input
            type="text"
            class="input"
            name="inputid"
            placeholder=""
            ref="inputmd"
            v-model="name"
            @focus="request.error.show = false"
          />
           <div class="error-wrapper" v-if="request.error.show">
            <ErrorIcon height="24px" class="error-icon" />
            <span class="error-msg"> {{ request.error.message }} </span>
          </div>
          <span class="error-wrapper"> </span>
        </div>
      </div>
    </template>
    <template v-slot:actions>
      <button class="db" dbt="success" @click="updateUserName(name)" v-if="!request.loading">
        Submit
      </button>
      <LoadingIcon height="32px" width="32px" v-if="request.loading" />
      </template>
  </BaseModal>
</template>

<script lang="ts">
import {ClientStore} from '@/stores/ClientStore';
import {defineComponent} from 'vue';
import BaseModal from './BaseModal.vue';
import AddFriend from '@/components/Icons/AddFriend.vue';
import LoadingIcon from '@/components/Icons/Status/LoadingIcon.vue';
import ErrorIcon from '@/components/Icons/Status/ErrorIcon.vue';
import {SnackStore} from '@/stores/SnackStore';
import { AppStore } from '@/stores/AppStore';
import Name from '@/components/Icons/Name.vue';
import { computed } from '@vue/reactivity';
export default defineComponent({
  name: 'NewNameModal',
  components: { BaseModal, AddFriend, LoadingIcon, ErrorIcon, Name },
  setup() {
    let appStore = AppStore();
    return {
      modalActive: appStore.setNewNameModalVis,
      username: computed(() => ClientStore().client.self.name)
    }
  },
  mounted() {
    // @ts-ignore
    this.$refs.inputmd.focus();
    this.name = this.username;
  },
  methods: {
    async updateUserName(name: string) {
      if (name.length <= 1) {
        this.name = '';
        this.request.loading = false;
        this.request.error = {
          show: true,
          message: 'Username must be longer than 1 character'
        };
        return;
      }
      let clientStore = ClientStore();
      this.request.loading = true;
      await clientStore.client.self.changeName(name);
      this.request.loading = false;
      this.modalActive(false);
    }
  },
  data() {
    return {
      request: {
        loading: false as boolean,
        data: {} as any,
        error: {
          show: false,
          message: '',
        },
      },
      name: '' as string,
    }
  },
});
</script>

<style lang="scss" scoped>
@import '@/assets/sass/ui/input.scss';
@import '@/assets/sass/ui/button.scss';
.title {
  font-size: calc(var(--font-size) + 12px);
  margin-left: 8px;
  color: var(--white);
  font-weight: bold;
}
.input-group {
  display: flex;
  flex-direction: column;
  margin-top: 10px;
  .username-label {
    text-align: left;
    color: var(--ac);
    margin-bottom: 5px;
  }
  .input-wrapper {
    position: relative;
    .input {
      @include input($border-radius: 8px, $boxshad: none);
      width: 91%;
      &::-webkit-outer-spin-button,
      &::-webkit-inner-spin-button {
        -webkit-appearance: none;
        margin: 0;
        display: none;
      }
    }
    .error-wrapper {
      position: absolute;
      top: 7px;
      pointer-events: none;
      display: flex;
      justify-content: center;
      align-items: center;

      .error-icon {
        opacity: 0.9;
        margin-left: 10px;
      }
      .error-msg {
        margin-left: 4px;
        color: var(--red);
      }
    }
  }
}
</style>
