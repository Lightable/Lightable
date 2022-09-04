<script setup lang="ts">
import { computed } from 'vue';
import { useAppStore } from '../../../../../stores/AppStore';
import AppSection from '../../../../../components/app/AppSection.vue';
import PendingCard from '../../../../../components/friends/PendingCard.vue';

const appStore = useAppStore();
appStore.setTitle('Friends • Pending');

const pending = computed(() => appStore.relationships.pending);
</script>

<template>
    <AppSection title="PENDING">
        <PendingCard v-for="(p, index) in pending" v-bind:key="index" :user="p"/>
        <div class="empty" v-if="pending.length === 0">
            <h3 style="color: var(--text-color-2); text-align: center;" class="center">Could not find any pending requests...<br>Perhaps they lied about wanting to be your friend?</h3>
            <span class="angry">😡</span>
        </div>
    </AppSection>
</template>

<style lang="scss" scoped>
    .empty {
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        .angry {
            font-size: 48px;
            opacity: 0.6;
        }
    }
</style>