<script setup lang="ts">
import AppSection from '../../../../../components/app/AppSection.vue';
import { useAppStore } from '../../../../../stores/AppStore';
const appStore = useAppStore();
appStore.setTitle('Friends • Pending');
</script>

<template>
    <AppSection title="PENDING">
        <div class="empty">
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