
<script setup lang="ts">
import AppHeader from '../header/AppHeader.vue';
import { NIcon, NButton } from 'naive-ui';
import { Person24Regular as Person } from '@vicons/fluent';
import { useRouter } from 'vue-router';

const router = useRouter();
const navigate = (path: string) => {
    router.push({name: path});
}
</script>

<template>
    <AppHeader title="Friends">
        <template #icon>
            <NIcon :size="32">
                <Person />
            </NIcon>
        </template>
        <template #actions>
            <NButton quaternary @click="navigate('friends-home')" :disabled="$router.currentRoute.value.name === 'friends-home'">
                <span class="friend-btn">All</span>
            </NButton>
            <NButton quaternary @click="navigate('friends-pending')" :disabled="$router.currentRoute.value.name === 'friends-pending'">
                <span class="friend-btn">Pending</span>
            </NButton>
            <NButton quaternary type="primary" @click="navigate('friends-add')" :disabled="$router.currentRoute.value.name === 'friends-add'">
                <span class="friend-btn">Add Friend</span>
            </NButton>
        </template>
    </AppHeader>
</template>

<style lang="scss" scoped>
.friend-btn {
    font-size: 20px;
}
</style>