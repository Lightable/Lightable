<template>
  <BaseModal @clicked="modalActive(false)">
    <template v-slot:icon>
      <AddFriend height="30px" width="30px" color="var(--white)" />
    </template>
    <template v-slot:name>
      <span class="title">Add a Friend</span>
    </template>
    <template v-slot:body>
      <div class="input-group">
        <span class="username-label">ID</span>
        <div class="input-wrapper">
          <input
            type="text"
            class="input"
            name="inputid"
            placeholder=""
            v-model="id"
            ref="inputmd"
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
      <button class="db" dbt="success" @click="addFriend(id)" v-if="!request.loading" style="margin-left: auto;">
        Add Friend
      </button>
      <LoadingIcon height="32px" width="32px" v-if="request.loading" />
    </template>
  </BaseModal>
</template>

<script lang="ts">
import {ClientStore} from '@/stores/ClientStore';
import {defineComponent} from 'vue';
import BaseModal from './BaseModal.vue';
import AddFriend from './Icons/AddFriend.vue';
import LoadingIcon from './Icons/Status/LoadingIcon.vue';
import ErrorIcon from './Icons/Status/ErrorIcon.vue';
import {SnackStore} from '@/stores/SnackStore';
import { AppStore } from '@/stores/AppStore';
export default defineComponent({
  name: 'AddFriendModal',
  components: {BaseModal, AddFriend, LoadingIcon, ErrorIcon},
  setup() {
    let appStore = AppStore();
    return {
      modalActive: appStore.setFriendModalVis
    }
  },
  mounted() {
    // @ts-ignore
    this.$refs.inputmd.focus();
  },
  methods: {
    async addFriend(id: string) {
      let client = ClientStore().client;
      let snackstore = SnackStore();
      if (id.length < 10) {
        this.id = '';
        this.request.error = {
          show: true,
          message: 'Input a valid id',
        };
        return;
      } else {
        this.request.loading = true;
        let pending = await client?.self?.addFriend(this.id);
        if (pending) {
          this.id = '';
          this.request.loading = false;
          this.$emit('clicked');
          snackstore.create('success', 'Friendship is pending', true, 5000);
        }
        if (!pending) {
          this.id = '';
          this.request.loading = false;
          this.request.error = {
            show: true,
            message: "User couldn't be found",
          };
        }
        return;
      }
    },
    modalClicked() {
      this.$emit('clicked');
    },
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
      id: '' as string,
    };
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
