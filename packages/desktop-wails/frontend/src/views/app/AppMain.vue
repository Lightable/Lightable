<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useAppStore } from '../../stores/AppStore';
import { PersonAdd, Home, Chatbubbles } from '@vicons/ionicons5';
import Loading from '../../components/loading/Loading.vue';
import { LoadingStates } from '../../composable/LoadingData';
import Overlay from '../../components/Overlay.vue';
import { DialSocket, LoginToSocket, GetUser } from '../../../wailsjs/go/client/Client';
import { NModal, NInput, NButton, NIcon, useMessage } from 'naive-ui';
import { GetRelations, RequestFriend } from '../../../wailsjs/go/client/RelationshipManager';
import { Workspace } from '@vicons/carbon';
import { GroupFilled } from '@vicons/material';
const appStore = useAppStore();
const toast = useMessage();
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
const complete = ref(false);

GetUser().then((user) => {
    appStore.user = user;
    loadingSteps.value[0] = {
        finished: true,
        name: `Get user`,
        state: LoadingStates.SUCCESS
    }
    DialSocket().then((msg) => {
        loadingSteps.value[1] = {
            finished: true,
            name: `Connect to socket (${msg})`,
            state: LoadingStates.SUCCESS
        }
        // @ts-ignore
        LoginToSocket().then(() => {
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
            complete.value = true;
        });
    }).catch((msg) => {
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
        text: 'Direct Messages',
        icon: Chatbubbles,
        path: "/app/channels/@me/dm",
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
        t: "Function",
        text: 'Add Friend',
        icon: PersonAdd,
        cb: () => {
            addFriendModal.value.show = true;
        }
    }
]

</script>


<template>
    <div class="main-app">
        <NModal v-model:show="addFriendModal.show" preset="dialog" :style="{ width: '50%' }">
            <template #icon>
                <NIcon :size="24">
                    <PersonAdd />
                </NIcon>
            </template>
            <template #header>
                Add Friend
            </template>
            <div class="content" :style="{ display: 'flex', flexDirection: 'column', gap: '4px', marginTop: '20px' }">
                <NInput placeholder="Enter a username" status="success" :loading="addFriendModal.loading" v-model:value="addFriendModal.friend" />
            </div>
            <template #action>
                <NButton secondary type="success" @click="addFriendModal.cb" :loading="addFriendModal.loading">
                    Send Request
                </NButton>
            </template>
        </NModal>
        <Transition>
            <Overlay v-if="!complete">
                <Loading :steps="loadingSteps" />
            </Overlay>
        </Transition>
        <div class="view">
            <router-view />
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
</style>