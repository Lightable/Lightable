<script setup lang="ts">
import { computed, PropType } from 'vue';
import { Account, Friend } from '../../User';
import { NButtonGroup, NButton, NElement, NAvatar, NTooltip, NSkeleton } from 'naive-ui';
import { useClientStore } from '../../stores/ClientStore';
import ZRightPane from './ZRightPane.vue';
import ZPaneWrap from './ZPaneWrap.vue';
const props = defineProps({
    user: Object as PropType<Account>,
    loading: Boolean
});
let clientStore = useClientStore();
const lite = computed(() => clientStore.lite)
const friends = computed(() => Array.from(clientStore.friends.values()))
</script>

<template>
    <ZPaneWrap>
        <ZRightPane>
            <div class="card-header flex" style="color: var(--info-color);">
                <h2>Stats</h2>
                <NSkeleton :width="110" :height="30" round v-if="loading" />
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
            <div class="card-body">
                <div class="stats">
                    <NSkeleton class="stat" :width="115" :height="45" v-if="loading" />
                    <div class="stat" v-else>
                        <div class="top">
                            <span class="stat-key">Total Messages</span>
                        </div>
                        <div class="bottom">
                            <span class="stat-value">?</span>
                        </div>
                    </div>
                    <NSkeleton class="stat" :width="115" :height="45" v-if="loading" />
                    <div class="stat" v-else>
                        <div class="top">
                            <span class="stat-key">Total Call Time</span>
                        </div>
                        <div class="bottom">
                            <span class="stat-value">?</span>
                        </div>
                    </div>
                    <NSkeleton class="stat" :width="115" :height="45" v-if="loading" />
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
        </ZRightPane>
        <ZRightPane>
            <div class="card-header" style="color: var(--info-color);">
                <h2>Friends</h2>
            </div>
            <div class="card-body">
                <div class="friends-inner">
                    <!-- @ts-ignore -->
                    <NSkeleton class="friend" :height="48" :width="48" round v-if="loading" v-for="(_, index) in [0, 0, 0]" v-bind:key="index" />
                    <div class="friend" v-for="(friend, index) in friends" :id="friend.id" v-bind:key="friend.id" v-else>
                        <NTooltip trigger="hover" placement="bottom">
                            <template #trigger>
                                <NAvatar round :size="48" :src="(friend.avatar) ? lite.$getExternalAvatar(friend.id, friend.avatar.id) : undefined" :alt="`${friend.name} Profile Picture`" />
                            </template>
                            {{ friend.name }}
                        </NTooltip>

                    </div>
                </div>
            </div>
        </ZRightPane>
    </ZPaneWrap>
</template>

<style lang="scss" scoped>
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
