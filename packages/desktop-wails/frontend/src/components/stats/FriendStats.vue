<script setup lang="ts">
import AppSection from '../app/AppSection.vue';
import StatCard from './StatCard.vue';
import { PhUsers, PhUser, PhShareNetwork} from '@dnlsndr/vue-phosphor-icons'; 
const props = defineProps({
    friends: Number,
    groups: Number,
    spaces: Number
})
</script>

<template>
    <AppSection title="FRIEND STATISTICS">
        <StatCard title="Friends" :data="String(friends)">
            <PhUser :size="34" weight="duotone" color="var(--text-color-3)" class="stats-icon"/>
        </StatCard>
        <StatCard title="Groups" :data="String(groups)">
                <PhUsers :size="34" weight="duotone" color="var(--text-color-3)" class="stats-icon"/>
        </StatCard>
        <StatCard title="Spaces" :data="String(spaces)">
           <PhShareNetwork :size="34" weight="duotone" color="var(--text-color-3)" class="stats-icon"/>
        </StatCard>
    </AppSection>
</template>
