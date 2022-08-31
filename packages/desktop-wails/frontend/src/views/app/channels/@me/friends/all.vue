<script setup lang="ts">
import { computed } from 'vue';
import { useAppStore } from '../../../../../stores/AppStore';
import { NButton, NIcon } from 'naive-ui';
import AppSection from '../../../../../components/app/AppSection.vue';
import FriendCard from '../../../../../components/friends/FriendCard.vue';
import { GrassRound as Grass} from '@vicons/material';
const appStore = useAppStore();
const friends = computed(() => appStore.relationships.friends);

appStore.setTitle('Friends • All')
</script>

<template>
    <AppSection title="FRIENDS">
        <FriendCard v-for="(friend, _) in friends" v-bind:key="friend.id" :user="friend" />
        <div class="empty">
            <h3 style="color: var(--text-color-2); text-align: center;" class="center">No friends were found. That sucks..<span>Maybe touch <span style="color: var(--success-color-pressed)">grass</span> instead?</span></h3>
            <NIcon :size="128" class="center" color="var(--success-color-pressed)" style="opacity: 0.6;">
                <Grass/>
            </NIcon>
        </div>
    </AppSection>
</template>


<style lang="scss" scoped>
    .empty {
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
    }
</style>

