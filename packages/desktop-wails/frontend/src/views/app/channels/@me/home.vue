<script setup lang="ts">
import { computed, Ref, ref } from 'vue';
import { GetRelations, RequestFriend } from '../../../../../wailsjs/go/client/RelationshipManager';
import { useAppStore } from '../../../../stores/AppStore';
import { NButton, NIcon, NTooltip } from 'naive-ui';
import ProfileCard from '../../../../components/ProfileCard.vue';
import { Chatbox, CheckmarkCircle as Checkmark, CloseCircle as Close } from '@vicons/ionicons5';
import { mocks } from '../../../../../wailsjs/go/models';
import { PersonFilled } from '@vicons/material';
import { PendingRound, ArrowCircleRightFilled as ArrowRight } from '@vicons/material';
import StatisticsHeader from '../../../../components/stats/StatisticsHeader.vue';
import GeneralStats from '../../../../components/stats/GeneralStats.vue';
import FriendStats from '../../../../components/stats/FriendStats.vue';

const appStore = useAppStore();
const loadingStates = ref(new Map()) as Ref<Map<string, boolean>>;


GetRelations().then(async (r) => {
    appStore.relationships = r;
    r.pending.forEach(p => loadingStates.value.set(p.id, false));
    r.requests.forEach(rq => loadingStates.value.set(rq.id, false));
});

const relations = computed(() => appStore.relationships);
const self = computed(() => appStore.user);
const cancelRequest = (id: string) => {

}
const acceptFriend = async (pend: mocks.PublicUser) => {
    loadingStates.value.set(pend.id, true);
    await RequestFriend(pend.name)
    loadingStates.value.set(pend.id, false);
}

appStore.setTitle('Stats')
</script>


<template>
    <div class="user-home">
        <StatisticsHeader />
        <GeneralStats :analytics="self?.analytics"/>
        <FriendStats :friends="relations.friends.length"/>
    </div>
</template>

<style lang="scss" scoped>
.user-home {
    height: 100%;
    width: 100%;
    max-height: 100%;
    overflow-y: auto;

    &::-webkit-scrollbar {
        display: none;
    }
}
</style>