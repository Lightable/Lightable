<script setup lang="ts">
import { PropType, ref } from 'vue';
import { NIcon } from 'naive-ui';
import { PhMicrophone as Microphone, PhTimer as Timer, PhChats as Chat } from '@dnlsndr/vue-phosphor-icons'
import { mocks } from '../../../wailsjs/go/models';
import AppSection from '../app/AppSection.vue';
import StatCard from './StatCard.vue';
const props = defineProps({
    analytics: Object as PropType<mocks.UserAnalytics>
})

const activeTime = ref(props.analytics?.activeTime!!)
function convertSecondsToReadableString(seconds: number) {
    seconds = seconds || 0;
    seconds = Number(seconds);
    seconds = Math.abs(seconds);
    const d = Math.floor(seconds / (3600 * 24));
    const h = Math.floor(seconds % (3600 * 24) / 3600);
    const m = Math.floor(seconds % 3600 / 60);
    const s = Math.floor(seconds % 60);
    const parts = [];

    if (d > 0) {
        parts.push(d + 'd' + (d > 1 ? 's' : ''));
    }

    if (h > 0) {
        parts.push(h + 'h' + (h > 1 ? 'rs' : ''));
    }

    if (m > 0) {
        parts.push(m + 'm');
    }

    if (s > 0) {
        parts.push(s + 's');
    }

    return parts.join(', ');
}


setInterval(() => {
    activeTime.value += 1
}, 1000)
</script>

<template>
    <AppSection title="GENERAL STATISTICS">
        <StatCard title="Messages" :data="String(analytics?.messages)" info="Total messages you've sent">
            <NIcon :size="34" color="var(--text-color-3)">
                <Chat :size="34" weight="duotone" color="var(--text-color-3)" />
            </NIcon>
        </StatCard>
        <StatCard title="Voice Call Time" :data="String(analytics?.callTime)" info="Total time you spent in a call">
            <Microphone :size="34" weight="duotone" color="var(--text-color-3)" />
        </StatCard>
        <StatCard title="Active Time" :data="convertSecondsToReadableString(activeTime) + '~'" info="Total time you've been connected to the socket">
            <Timer :size="34" weight="duotone" color="var(--text-color-3)" />
        </StatCard>
    </AppSection>
</template>