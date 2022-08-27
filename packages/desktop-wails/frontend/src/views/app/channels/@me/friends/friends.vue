<script setup lang="ts">
import FriendHeader from '../../../../../components/friends/FriendHeader.vue';
</script>

<template>
    <FriendHeader />
    <div class="body">
        <router-view />
    </div>
</template>

<style lang="scss" scoped>
.body {
    height: 100%;
    width: 100%;
    max-height: 100%;
    overflow-y: auto;

    &::-webkit-scrollbar {
        display: none;
    }
}
</style>