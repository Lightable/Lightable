<script setup lang="ts">
import { computed, Ref, ref } from 'vue';
import { GetRelations, RequestFriend } from '../../../../../wailsjs/go/client/RelationshipManager';
import { useAppStore } from '../../../../stores/AppStore';
import { NButton, NIcon, NTooltip } from 'naive-ui';
import ProfileCard from '../../../../components/ProfileCard.vue';
import { Chatbox, CheckmarkCircle as Checkmark, CloseCircle as Close } from '@vicons/ionicons5';
import { mocks } from '../../../../../wailsjs/go/models';

const appStore = useAppStore();
const loadingStates = ref(new Map()) as Ref<Map<string, boolean>>;
GetRelations().then(async (r) => {
    appStore.relationships = r;
    r.pending.forEach(p => loadingStates.value.set(p.id, false));
    r.requests.forEach(rq => loadingStates.value.set(rq.id, false));
});

const relations = computed(() => appStore.relationships);

const cancelRequest = (id: string) =>  {

}
const acceptFriend = async (pend: mocks.PublicUser) => {
    loadingStates.value.set(pend.id, true);
    await RequestFriend(pend.name)
    loadingStates.value.set(pend.id, false);
}
</script>


<template>
    <div class="user-home">
        <div class="grid-relations">
            <div class="sub friends-grid" v-if="relations.friends.length > 0">
                <div class="header ns">
                    <span>Friends</span>
                </div>
                <div class="body">
                    <ProfileCard v-for="(friend, _) in relations.friends" v-bind:key="friend.id" :friend="friend">
                        <template #default>
                            <NButton text>
                                <template #icon>
                                    <NIcon :size="32">
                                        <Chatbox />
                                    </NIcon>
                                </template>
                            </NButton>
                        </template>
                    </ProfileCard>
                </div>
            </div>
            <div class="sub pending-grid" v-if="relations.pending.length > 0">
                <div class="header ns">
                    <span>Pending</span>
                </div>
                <div class="body">
                    <ProfileCard v-for="(pending, _) in relations.pending" v-bind:key="pending.id" :friend="pending">
                        <template #default>
                            <NTooltip trigger="hover">
                                <template #trigger>
                                    <NButton text type="error">
                                        <template #icon>
                                            <NIcon :size="32">
                                                <Close />
                                            </NIcon>
                                        </template>
                                    </NButton>
                                </template>
                                Deny request
                            </NTooltip>
                            <NTooltip trigger="hover">
                                <template #trigger>
                                    <NButton text type="success" @click="acceptFriend(pending)">
                                        <template #icon>
                                            <NIcon :size="32">
                                                <Checkmark />
                                            </NIcon>
                                        </template>
                                    </NButton>
                                </template>
                                Accept request
                            </NTooltip>
                        </template>
                    </ProfileCard>
                </div>
            </div>
            <div class="sub requests-grid" v-if="relations.requests.length > 0">
                <div class="header ns">
                    <span>Requests</span>
                </div>
                <div class="body">
                    <ProfileCard v-for="(requests, _) in relations.requests" v-bind:key="requests.id" :friend="requests">
                        <!-- <template #default>
                            <NTooltip trigger="hover">
                                <template #trigger>
                                    <NButton text type="error" :loading="loadingStates.get(requests.id)" @click="loadingStates.set(requests.id, true)">
                                        <template #icon>
                                            <NIcon :size="32">
                                                <Close />
                                            </NIcon>
                                        </template>
                                    </NButton>
                                </template>
                                Cancel request
                            </NTooltip>
                        </template> -->
                    </ProfileCard>
                </div>
            </div>
        </div>
    </div>
</template>

<style lang="scss" scoped>
.user-home {
    height: 100%;
    width: 100%;

    .grid-relations {
        height: 100%;
        width: 100%;
        display: grid;
        grid-template-columns: auto auto auto;

        .sub {

            .header {
                font-size: 24px;
                span {
                    margin-left: 5px;
                    font-family: "Open Sans";
                }
            }

            .body {
                height: 100%;
                // border-right: 2px solid rgb(63, 63, 63);
                display: flex;
                flex-direction: column;
                justify-content: flex-start;
                align-items: center;
                gap: 16px;
            }
        }
    }
}
</style>