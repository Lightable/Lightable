<script setup lang="ts">
import { computed, defineProps, PropType } from 'vue';
import { Account, Friend } from '../../User';
import { NButtonGroup, NButton, NElement, NAvatar, NTooltip, NSkeleton } from 'naive-ui';
import { useClientStore } from '../../stores/ClientStore';
const props = defineProps({
    user: Object as PropType<Account>,
    loading: Boolean
});
let clientStore = useClientStore();
const lite = computed(() => clientStore.lite)
const friends = computed(() => Array.from(clientStore.friends.values() as Friend[]))
</script>

<template>
    <NElement class="pane-wrap">
        <!-- <div class="profile-right fill" style="width: 700px">
            <div class="card-header" style="color: var(--warning-color);">
                <h3>{{ props.user?.name }}'s Bio</h3>
            </div>
            <div class="card-body">
                <div class="bio-inner">
                    <NInput type="textarea" style="background-color: rgba(0,0,0,0.1); outline: none !important; height: 100%;" :resizable="false" placeholder="Enter Bio Here" />
                </div>
            </div>
        </div> -->
        <div class="profile-right">
            <div class="card-header flex" style="color: var(--info-color);">
                <h2>Stats</h2>
                <NSkeleton :width="110" :height="30" round v-if="loading"/>
                <NButtonGroup v-else>
                    <NTooltip trigger="hover">
                        <template #trigger>
                            <NButton type="error" round quaternary size="small">Deny</NButton>
                        </template>
                        <b style="color: red;">Deny</b> access to collect user statistics
                    </NTooltip>
                    <NTooltip trigger="hover">
                        <template #trigger>
                            <NButton type="success" round quaternary size="small" disabled>Allow</NButton>
                        </template>
                         <b style="color: lime;">Allow</b> access to collect user statistics
                    </NTooltip>
                </NButtonGroup>
            </div>
            <div class="card-body card-body-stats">
                <div class="stats">
                    <NSkeleton class="stat" :width="115" :height="45" v-if="loading"/>
                    <div class="stat" v-else>
                        <div class="top">
                            <span class="stat-key">Total Messages</span>
                        </div>
                        <div class="bottom">
                            <span class="stat-value">?</span>
                        </div>
                    </div>
                    <NSkeleton class="stat" :width="115" :height="45" v-if="loading"/>
                    <div class="stat" v-else>
                        <div class="top">
                            <span class="stat-key">Total Call Time</span>
                        </div>
                        <div class="bottom">
                            <span class="stat-value">?</span>
                        </div>
                    </div>
                    <NSkeleton class="stat" :width="115" :height="45" v-if="loading"/>
                    <div class="stat" v-else>
                        <div class="top">
                            <span class="stat-key">Total Logins</span>
                        </div>
                        <div class="bottom">
                            <span class="stat-value">?</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="profile-right" style="margin-top: 50px; height: fit-content;">
            <div class="card-header" style="color: var(--info-color);">
                <h2>Friends</h2>
            </div>
            <div class="card-body">
                <div class="friends-inner">
                    <!-- @ts-ignore -->
                    <NSkeleton class="friend" :height="48" :width="48" round v-if="loading" v-for="(_, index) in [0,0,0]" v-bind:key="index"/>
                    <div class="friend" v-for="(friend, index) in friends" :id="friend.id" v-bind:key="friend.id" v-else>
                        <NTooltip trigger="hover" placement="bottom">
                            <template #trigger>
                                <NAvatar round :size="48" :src="(friend.avatar) ? lite.$getExternalAvatar(friend.id, friend.avatar.id) : undefined" :alt="`${friend.name} Profile Picture`"/>
                            </template>
                            {{ friend.name }}
                        </NTooltip>

                    </div>
                </div>
            </div>
        </div>

    </NElement>
</template>

<style lang="scss" scoped>
.pane-wrap {
    display: flex;
    flex-direction: column;
    .profile-right {
        box-shadow: rgba(0, 0, 0, 0.19) 0px 10px 20px, rgba(0, 0, 0, 0.23) 0px 6px 6px;
        transition: 0.3s;
        border-radius: 8px;
        width: 100%;
        margin-top: 50px;
        padding: 8px;

        &:hover {
            box-shadow: rgba(0, 0, 0, 0.25) 0px 14px 28px, rgba(0, 0, 0, 0.22) 0px 10px 10px;
        }
    }
}

.card-header {
    margin-left: 4px;
    user-select: none;
    font-family: 'Titillium Web';
    padding: 8px;

    &.flex {
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        align-items: center;
    }

    h2 {
        padding: 0;
        margin: 0;
    }
}

.card-body {
    padding: 2px 16px;
    min-width: 300px;
    max-width: 55vw;

    .friends-inner {
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
        gap: 16px;

    }

    .stats {
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
        gap: 8px;

        .stat {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            font-size: 18px;

            .top {
                color: var(--text-color-1);
                user-select: none;
            }

            .bottom {
                color: var(--info-color);

            }
        }
    }

    .bio-inner {
        height: 400px;
    }
}
@media only screen and(max-width: 852px) {
    .pane-wrap {
        width: 100%;
        justify-content: center;
        align-items: center;
        .profile-right {
            justify-content: center;
            align-items: center;
            width: 80%;
        }
    }
    .card-body .stats {
        gap: 8px;
        justify-content: center;
        flex-wrap: wrap;
    }
    .card-body {
        min-width: 60px;
        max-width: 85vw;
        display: flex;
        &.card-body-stats {
            justify-content: center;
            align-items: center;
        }
        // align-items: center;
        // justify-content: center;
    }
    .card-body .stats .stat {
        font-size: 14px;
    }
}
</style>