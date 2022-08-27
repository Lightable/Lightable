<script setup lang="ts">
import { computed } from 'vue';
import { useAppStore } from '../../../../../stores/AppStore';
import { NButton } from 'naive-ui';
import AppSection from '../../../../../components/app/AppSection.vue';
import FriendCard from '../../../../../components/friends/FriendCard.vue';

const appStore = useAppStore();
const friends = computed(() => appStore.relationships.friends);

appStore.setTitle('Friends • All')
</script>

<template>
    <AppSection title="FRIENDS">
        <FriendCard v-for="(friend, _) in friends" v-bind:key="friend.id" :user="friend" />
        <NButton style="width: 20%;" type="primary" secondary disabled>
            Add Friend
        </NButton>
    </AppSection>
</template>

