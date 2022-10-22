<script setup lang="ts">
import {ref, onMounted, Ref, computed} from 'vue';
import {useAppStore} from '../../stores/AppStore';
import {useUpdateStore} from '../../stores/UpdateStore';
import Loading from '../../components/loading/Loading.vue';
import {LoadingStates} from '../../composable/LoadingData';
import Overlay from '../../components/Overlay.vue';
import {DialSocket, LoginToSocket, GetUser, GetSelfAvatar} from '../../../wailsjs/go/client/Client';
import {NModal, NInput, NButton, NIcon, useMessage, useLoadingBar} from 'naive-ui';
import {GetRelations, RequestFriend} from '../../../wailsjs/go/client/RelationshipManager';
import {
  PhHouse as Home,
  PhChats as Chatbubbles,
  PhUser as Friends,
  PhShareNetwork as Workspace,
  PhUsers as GroupFilled,
  PhGearSix as Cog,
  PhDownload as Download,
  PhShieldStar as Shield,
  PhShieldSlash
} from '@dnlsndr/vue-phosphor-icons';
import {useRouter} from 'vue-router';

const appStore = useAppStore();
const toast = useMessage();
const router = useRouter();
const loading = useLoadingBar();
const profileModal = computed(() => appStore.modalProfile);
appStore.users = [];
const loadingSteps = ref([
  {
    finished: false,
    name: 'Get user',
    state: LoadingStates.PENDING
  },
  {
    finished: false,
    name: 'Connect to socket',
    state: LoadingStates.PENDING
  },
  {
    finished: false,
    name: 'Login to socket',
    state: LoadingStates.PENDING
  },
]);

const activeUserPill = ref('profile') as Ref<'profile' | 'info' | 'relation'>
const complete = ref(false);

GetUser().then((user) => {
  appStore.user = user;
  loadingSteps.value[0] = {
    finished: true,
    name: `Get user`,
    state: LoadingStates.SUCCESS
  }
  appStore.setTitle('Loaded • User');
  DialSocket().then((msg) => {
    appStore.setTitle('Loaded • Socket • Connect');
    loadingSteps.value[1] = {
      finished: true,
      name: `Connect to socket (${msg})`,
      state: LoadingStates.SUCCESS
    }
    // @ts-ignore
    LoginToSocket().then(() => {
      appStore.setTitle('Loaded • Socket • Login');
      loadingSteps.value[2] = {
        finished: true,
        name: `Login to socket`,
        state: LoadingStates.SUCCESS
      }
      loadingSteps.value[3] = {
        finished: true,
        name: 'App finished',
        state: LoadingStates.FAILED
      }
      setTimeout(() => {
        GetRelations().then((r) => {
          appStore.relationships = r;
          console.log(r);
          appStore.users.push(...r.friends)
          appStore.users.push(...r.pending)
          appStore.users.push(...r.requests)
          complete.value = true;
          appStore.setTitle('Home');
          if (appStore.user!!.admin) {
            appStore.leftDrawer.components.push({
              t: "Route",
              text: 'Admin',
              name: 'admin',
              path: '/app/admin',
              icon: Shield,
            })
          }
        });
      }, 400)
    });
  }).catch((msg) => {
    appStore.setTitle('Loading • Socket • Failed');
    loadingSteps.value[0] = {
      finished: true,
      name: `Connect to socket ${msg}`,
      state: LoadingStates.FAILED
    }
  });
});
const addFriendModal = ref({
  show: false,
  friend: '' as string,
  loading: false as boolean,
  cb: async () => {
    let modal = addFriendModal.value;
    modal.loading = true;
    try {
      let req = await RequestFriend(modal.friend);
      modal.loading = false;
      toast.success(`Got it! We'll let ${req.name} know about your potential friendship ASAP`);
      GetRelations().then(r => {
        appStore.relationships = r;
      });
      return
    } catch (e) {
      modal.loading = false;
      toast.warning(e as string);
    }
  }
});
appStore.leftDrawer.show = true;

appStore.leftDrawer.components = [
  {
    t: "Route",
    text: 'Home',
    path: "/app/home",
    icon: Home,
  },
  {
    t: "Route",
    text: 'Messages',
    icon: Chatbubbles,
    path: '/app/channel/dm/home',
    name: "channel-dm",
  },
  // /app/channels/@me/groups
  {
    t: "Route",
    text: 'Groups',
    icon: GroupFilled,
    path: ""
  },
  // /app/channels/spaces
  {
    t: "Route",
    text: 'Spaces',
    icon: Workspace,
    path: ""
  },
  {
    t: "Route",
    text: 'Friends',
    name: 'friends',
    path: '/app/friends',
    icon: Friends
  },
  {
    t: "Route",
    text: 'Settings',
    name: 'settings',
    path: '/app/settings',
    icon: Cog,
  },
]

router.beforeResolve((to, from, next) => {
  // If this isn't an initial page load.
  if (to.name) {
    // Start the route progress bar.
    loading.start()
  }
  next()
});
router.afterEach(() => {
  loading.finish();
});


const activatePill = (pill: string) => {
  activeUserPill.value = pill;
}
</script>


<template>
  <Teleport to="#profile-modal">

    <div class="profile-overlay" v-if="profileModal.visible">
      <Overlay @overlay-click="appStore.closeProfile()">
        <Transition name="bounce" mode="out-in">
          <div class="profile" v-if="profileModal.visible" @click.stop>
            <div class="header">
              <div class="prof">
                <img
                    :src="profileModal.avatarURL"
                    class="avatar"/>
                <span class="name">{{profileModal.data.name}}</span>
              </div>

              <div class="pills">
                <div :data-active="activeUserPill === 'profile'" @click="activatePill('profile')">PROFILE</div>
                <div :data-active="activeUserPill === 'info'" @click="activatePill('info')">
                  <PhShieldSlash weight="duotone" color="var(--info-color)"/>
                  INFO
                </div>
              </div>
            </div>

            <div class="pill-content" v-if="activeUserPill === 'info'">
              <span class="gray ns">This is a debug pane</span>
              <span>Avatar: {{ profileModal.avatarURL }}</span>
              <span class="data">ID: {{ profileModal.data.id }}</span>
            </div>
          </div>
        </Transition>
      </Overlay>
    </div>


  </Teleport>
  <div class="main-app">
    <Transition>
      <Overlay v-if="!complete">
        <Loading :steps="loadingSteps"/>
      </Overlay>
    </Transition>
    <div class="view">
      <router-view v-if="complete"/>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.main-app,
.view {
  height: 100%;
  width: 100%;
  max-height: 100%;
}

.header {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8px;
}

.v-enter-active,
.v-leave-active {
  transition: opacity 0.5s ease;
}

.v-enter-from,
.v-leave-to {
  opacity: 0;
}

.profile-overlay {
  position: absolute;
  width: 100vw;
  height: 100vh;

  .profile {
    background: var(--lightable-card-color);
    min-height: 200px;
    min-width: 500px;
    width: 100px;
    max-width: min(100vw - 20px, 560px);
    max-height: min(100vh - 20px, 650px);
    border-radius: .25rem;
    position: relative;
    animation: bounce-in .5s ease-out both;
    .header {
      height: 140px;
      background-image: linear-gradient(rgba(0, 0, 0, 0.7), rgba(0, 0, 0, 0.7));
      border-top-left-radius: .25rem;
      border-top-right-radius: .25rem;
      background-size: cover;
      display: flex;
      flex-direction: column;
      align-items: flex-start;
      .prof {
        display: flex;
        flex-direction: row;
        align-items: center;
        margin-top: 10px;
        margin-left: 10px;
        .avatar {
          height: 72px;
          width: 72px;
          border-radius: 50%;
          margin-left: 20px;
        }

        .name {
          padding: 22px;
          font-size: 32px;
          font-weight: bold;
        }
      }
    }

  }
}

.pills {
  display: flex;
  flex-direction: row;
  gap: 24px;
  margin-left: 34px;
  div {
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    border-bottom: 2px solid transparent;
    transition: 150ms ease;
    border-bottom: transparent 2px solid;
    flex-direction: row;
    gap: 3px;
    font-family: "Sarabun";

    &:hover {
      border-bottom: var(--text-color-3) 2px dashed;
    }

    &[data-active='true'] {
      border-bottom: var(--text-color-1) 2px solid;
    }
  }
}

.pill-content {
  padding: 1em;
  display: flex;
  flex-direction: column;
  font-family: "Titillium Web";

  .gray {
    font-size: 12px;

    color: var(--text-color-3);
  }
}

.bounce-enter-active {
  animation: bounce-in .5s ease-out both;
}

.bounce-leave-active {
  animation: bounce-in .5s reverse ease-in both;
}

@keyframes bounce-in {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.10);
  }
  100% {
    transform: scale(1);
  }
}
</style>