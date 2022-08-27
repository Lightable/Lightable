<script setup lang="ts">
import AppSection from '../app/AppSection.vue';
import StatCard from './StatCard.vue';
import { NIcon } from 'naive-ui';
import { PersonOutlineRound as Person, GroupOutlined as Group, GroupsFilled as Space, ChildFriendlySharp } from '@vicons/material';

const props = defineProps({
    friends: Number,
    groups: Number,
    spaces: Number
})
</script>

<template>
    <AppSection title="FRIEND STATISTICS">
        <StatCard title="Friends" :data="String(friends)">
            <NIcon :size="34" color="var(--text-color-3)">
                <Person />
            </NIcon>
        </StatCard>
        <StatCard title="Groups" :data="String(groups)">
            <NIcon :size="34" color="var(--text-color-3)">
                <Group />
            </NIcon>
        </StatCard>
        <StatCard title="Spaces" :data="String(spaces)">
            <NIcon :size="34" color="var(--text-color-3)">
                <Space/>
            </NIcon>
        </StatCard>
    </AppSection>
</template>