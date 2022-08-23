<script setup lang="ts">
import AppSection from '../app/AppSection.vue';
import StatCard from './StatCard.vue';
import { NIcon } from 'naive-ui';
import { ChatboxOutline as Chat } from '@vicons/ionicons5';
import { SettingsPhoneOutlined as Phone } from '@vicons/material';
import { Clock32Regular as Clock } from '@vicons/fluent';
</script>

<template>
    <AppSection title="GENERAL STATISTICS">
        <StatCard title="Messages" data="32">
            <NIcon :size="34" color="var(--text-color-3)">
                <Chat/>
            </NIcon>
        </StatCard>
        <StatCard title="Voice Call Time" data="6h">
            <NIcon :size="34" color="var(--text-color-3)">
                <Phone/>
            </NIcon>
        </StatCard>
         <StatCard title="Active Time" data="23d">
            <NIcon :size="34" color="var(--text-color-3)">
                <Clock/>
            </NIcon>
        </StatCard>
    </AppSection>
</template>