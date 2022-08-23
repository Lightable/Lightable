<script setup lang="ts">
import AppSection from '../app/AppSection.vue';
import StatCard from './StatCard.vue';
import { NIcon } from 'naive-ui';
import { PersonOutlineRound as Person, GroupOutlined as Group, GroupsFilled as Space } from '@vicons/material';
</script>

<template>
    <AppSection title="FRIEND STATISTICS">
        <StatCard title="Friends" data="13">
            <NIcon :size="34" color="var(--text-color-3)">
                <Person />
            </NIcon>
        </StatCard>
        <StatCard title="Groups" data="3">
            <NIcon :size="34" color="var(--text-color-3)">
                <Group />
            </NIcon>
        </StatCard>
        <StatCard title="Spaces" data="45">
            <NIcon :size="34" color="var(--text-color-3)">
                <Space/>
            </NIcon>
        </StatCard>
    </AppSection>
</template>