<template>
  <BaseModal @clicked="modalActive(false)">
    <template v-slot:icon>
      <AddFriend height="30px"
        width="30px"
        color="var(--white)" />
    </template>
    <template v-slot:name>
      <span class="title">Add a Friend</span>
    </template>
    <template v-slot:body>
      <div class="input-group">
        <span class="username-label">Name</span>
        <div class="input-wrapper">
          <NInput type="text"
            name="inputid"
            placeholder=""
            @change="(n) => name = n"
            ref="inputmd"
            @focus="request.error.show = false"
            :status="(request.error.show) ? 'error' : ''" />
          <!-- <input
            type="text"
            class="input"
            name="inputid"
            placeholder=""
            v-model="name"
            ref="inputmd"
            @focus="request.error.show = false"
          /> -->
          <!-- <div class="error-wrapper" v-if="request.error.show">
            <ErrorIcon height="24px" class="error-icon" />
            <span class="error-msg"> {{ request.error.message }} </span>
          </div>
          <span class="error-wrapper"> </span>
          -->
        </div>
      </div>
    </template>
    <template v-slot:actions>
      <NButton type="primary"
        secondary
        :loading="request.loading"
        style="margin-left: auto"
        @click="addFriend(name)">
        <template #icon>
          <NIcon>
            <PersonAddOutline />
          </NIcon>
        </template>
        Add Friend
      </NButton>
    </template>
  </BaseModal>
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue';
import { ClientStore} from '@/stores/ClientStore';
import BaseModal from './BaseModal.vue';
import AddFriend from '@/components/Icons/AddFriend.vue';
import LoadingIcon from '@/components/Icons/Status/LoadingIcon.vue';
import ErrorIcon from '@/components/Icons/Status/ErrorIcon.vue';
import { SnackStore } from '@/stores/SnackStore';
import { AppStore } from '@/stores/AppStore';
import { NButton, NIcon, NInput } from 'naive-ui';
import PersonAddOutline from '@vicons/ionicons5/PersonAddOutline';
export default defineComponent({
  name: 'AddFriendModal',
  components: { BaseModal, AddFriend, LoadingIcon, ErrorIcon, NButton, NIcon, PersonAddOutline, NInput },
  setup() {
    let appStore = AppStore();
    return {
      modalActive: appStore.setFriendModalVis,
      currentUser: computed(() => ClientStore().client?.self!!),
      createSnack: SnackStore().create
    };
  },
  mounted() {
    // @ts-ignore
    this.$refs.inputmd.focus();
  },
  methods: {
    async addFriend(name: string) {
      let client = ClientStore().client;
      if (name.length <= 0) {
        this.request.error = {
          show: true,
          message: 'Input a valid name',
        }
        this.createSnack('error', `Input a valid name`, true, 1000);
        return;
        // @ts-ignore
      } else if ([...client?.requests.values()].filter(v => v.name == name).length > 0) {
        this.request.error = {
          show: true,
          message: `You already have a request for ${name}`,
        };
        return
      } else if (name == this.currentUser.name) {
        this.request.error = {
          show: true,
          message: `You can't friend yourself`,
        };
        this.createSnack('error', `You can't friend yourself`, true, 1000);
        return
      } else {
        this.request.loading = true;
        let pending = await client?.self?.addFriend(this.name);
        if (pending) {
          this.request.loading = false;
          this.$emit('clicked');
          this.createSnack('success', 'Friendship is pending', true, 5000);
        }
        if (!pending) {
          this.request.loading = false;
          this.request.error = {
            show: true,
            message: "User couldn't be found",
          };
          this.createSnack('error', `User couldn't be found`, true, 1000);
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
      name: '' as string,
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
      top: 5px;
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
