<script setup lang="ts">
import { computed, Ref, ref } from 'vue';
import { GetRelations, RequestFriend } from '../../../../../wailsjs/go/client/RelationshipManager';
import { useAppStore } from '../../../../stores/AppStore';
import { NButton, NIcon, NTooltip } from 'naive-ui';
import ProfileCard from '../../../../components/ProfileCard.vue';
import { Chatbox, CheckmarkCircle as Checkmark, CloseCircle as Close } from '@vicons/ionicons5';
import { mocks } from '../../../../../wailsjs/go/models';
import { PersonFilled } from '@vicons/material';
import { PendingRound, ArrowCircleRightFilled as ArrowRight } from '@vicons/material';

const appStore = useAppStore();
const loadingStates = ref(new Map()) as Ref<Map<string, boolean>>;


GetRelations().then(async (r) => {
    appStore.relationships = r;
    r.pending.forEach(p => loadingStates.value.set(p.id, false));
    r.requests.forEach(rq => loadingStates.value.set(rq.id, false));
});

const relations = computed(() => appStore.relationships);
const self = computed(() => appStore.user);
const cancelRequest = (id: string) => {

}
const acceptFriend = async (pend: mocks.PublicUser) => {
    loadingStates.value.set(pend.id, true);
    await RequestFriend(pend.name)
    loadingStates.value.set(pend.id, false);
}
</script>


<template>
    <div class="user-home">
        <div class="stats ns">
            <div class="stat">
                <div class="header">
                    <span class="header">Logins</span>
                </div>
                <span class="value">
                    {{self?.analytics?.logins}}
                </span>
            </div>

            <div class="stat">
                <div class="header">
                    <span class="header">Friends</span>
                </div>
                <span class="value" v-if="relations.friends">
                    {{relations.friends.length == 0 ? '0 :c' : relations.friends.length}}
                </span>
            </div>
            <!-- <div class="stat">
                <div class="header">
                    <span class="header">Voice</span>
                </div>
                <span class="value">
                    200<span class="define">hours</span>
                </span>
            </div> -->
            <!-- <div class="stat">
                <div class="header">
                    <span class="header">Messages</span>
                </div>
                <span class="value">
                    200
                </span>
            </div> -->
        </div>
        <div class="scroll-container">
            <div class="grid-relations">

                <div class="sub friends-grid" v-if="relations.friends">
                    <div class="header ns">
                        <NIcon :size="24">
                            <PersonFilled />
                        </NIcon>
                        <span>Friends</span>
                    </div>
                    <div class="scroll-container">
                        <div class="body">
                            <div class="empty ns" v-if="relations.friends.length == 0">
                                <h3>No friends. Sadge</h3>
                            </div>
                            <ProfileCard v-for="(friend, _) in relations.friends" v-bind:key="friend.id" :friend="friend" v-else>
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
                </div>
                <div class="sub pending-grid" v-if="relations.pending">
                    <div class="header ns">
                        <NIcon :size="24">
                            <PendingRound />
                        </NIcon>
                        <span>Pending</span>
                    </div>
                    <div class="scroll-container">
                        <div class="body">
                            <div class="empty ns" v-if="relations.pending.length == 0">
                                <h3>You don't have any pending friends</h3>
                            </div>
                            <ProfileCard v-for="(pending, _) in relations.pending" v-bind:key="pending.id" :friend="pending" v-else>
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
                </div>
                <div class="sub requests-grid" v-if="relations.requests">
                    <div class="header ns">
                        <NIcon :size="24">
                            <ArrowRight />
                        </NIcon>
                        <span>Requests</span>
                    </div>
                    <div class="scroll-container">
                        <div class="body">
                            <div class="empty ns" v-if="relations.requests.length == 0">
                                <h3>You don't have any requests</h3>
                            </div>
                            <ProfileCard v-for="(requests, _) in relations.requests" v-bind:key="requests.id" :friend="requests" v-else>
                            </ProfileCard>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style lang="scss" scoped>
.user-home {
    height: 100%;
    width: 100%;
    max-height: 100%;

    .stats {
        display: flex;
        flex-direction: row;
        margin-left: 20px;
        gap: 5em;

        .stat {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;

            .header {
                color: var(--text-color-3);
                font-size: 1rem;
            }

            .value {
                font-size: 1.5rem;
                font-weight: bold;
                font-family: "Jetbrains Mono";

                .define {
                    color: var(--text-color-3);
                    font-size: 1.2rem;
                    margin-left: 10px;
                    font-weight: normal;
                    font-family: "Open sans Medium";
                }
            }
        }

    }
}

.scroll-container {
    margin: auto;
    max-height: 85%;
    overflow: auto;

    .grid-relations {
        width: 100%;
        display: flex;
        flex-direction: column;
        margin-top: 20px;
        align-items: center;
        gap: 8px;
        justify-content: center;
        justify-items: center;

        .sub {
            background-color: rgba(59, 59, 59, 0.247);
            width: 98%;
            border-radius: .375rem;

            .header {
                display: flex;
                font-size: 1rem;
                flex-direction: row;
                justify-content: flex-start;
                align-items: center;
                padding-top: .5rem;
                padding-bottom: .5rem;
                padding-left: 1rem;
                padding-right: 1rem;

                span {
                    margin-left: 15px;
                    font-family: "InterVariable";
                    color: var(--text-color-3);
                    font-weight: 600;
                }

            }

            .scroll-container {
                margin: auto;
                max-height: 100%;
                overflow: auto;

                .body {
                    // border-right: 2px solid rgb(63, 63, 63);
                    display: flex;
                    flex-direction: column;
                    justify-content: flex-start;
                    align-items: center;
                    gap: 16px;
                    padding-bottom: .75rem;
                    padding-left: 1rem;
                    padding-right: 1rem;
                    flex-shrink: 1;

                    .empty {
                        text-align: center;
                    }

                }
            }

        }
    }

    &::-webkit-scrollbar {
        display: none;
    }
}
</style>