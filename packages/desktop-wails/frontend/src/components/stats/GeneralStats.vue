<script setup lang="ts">
import { PropType } from 'vue';
import { NIcon } from 'naive-ui';
import { ChatboxOutline as Chat } from '@vicons/ionicons5';
import { SettingsPhoneOutlined as Phone } from '@vicons/material';
import { Clock32Regular as Clock } from '@vicons/fluent';
import { mocks } from '../../../wailsjs/go/models';
import AppSection from '../app/AppSection.vue';
import StatCard from './StatCard.vue';
import ms from 'ms';
const props = defineProps({
    analytics: Object as PropType<mocks.UserAnalytics>
})
</script>

<template>
    <AppSection title="GENERAL STATISTICS">
        <StatCard title="Messages" :data="String(analytics?.messages)">
            <NIcon :size="34" color="var(--text-color-3)">
                <Chat/>
            </NIcon>
        </StatCard>
        <StatCard title="Voice Call Time" :data="String(analytics?.callTime)">
            <NIcon :size="34" color="var(--text-color-3)">
                <Phone/>
            </NIcon>
        </StatCard>
         <StatCard title="Active Time" :data="ms(analytics!!.activeTime * 1000)">
            <NIcon :size="34" color="var(--text-color-3)">
                <Clock/>
            </NIcon>
        </StatCard>
    </AppSection>
</template>