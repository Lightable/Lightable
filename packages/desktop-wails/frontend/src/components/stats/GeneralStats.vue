<script setup lang="ts">
import { PropType, ref, onUnmounted } from 'vue';
import { NIcon } from 'naive-ui';
import { PhMicrophone as Microphone, PhTimer as Timer, PhChats as Chat } from '@dnlsndr/vue-phosphor-icons'
import { mocks } from '../../../wailsjs/go/models';
import AppSection from '../app/AppSection.vue';
import StatCard from './StatCard.vue';
import secondsToReadableString from '../../composable/convertSecondsToReadableString';
const props = defineProps({
    analytics: Object as PropType<mocks.UserAnalytics>
})

const activeTime = ref(props.analytics?.activeTime!!)


var interval = setInterval(() => {
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
        <StatCard title="Active Time" :data="secondsToReadableString(activeTime) + '~'" info="Total time you've been connected to the socket">
            <Timer :size="34" weight="duotone" color="var(--text-color-3)" />
        </StatCard>
    </AppSection>
</template>