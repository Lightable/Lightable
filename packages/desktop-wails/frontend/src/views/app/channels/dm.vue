<script setup lang="ts">
import { computed, onBeforeMount, ref, Ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAppStore } from '../../../stores/AppStore';
import Chat from '../../../components/chat/Chat.vue';
import { mocks } from '../../../../wailsjs/go/models';
import { GetRelations } from '../../../../wailsjs/go/client/RelationshipManager';
const appStore = useAppStore();
const router = useRouter();
if (router.currentRoute.value.params.id === 'home') router.push('/concepts/soon');

const id = computed(() => router.currentRoute.value.params.id as string);

let dm = ref(appStore.users.find(u => u.id === id.value)) as Ref<null | mocks.PublicUser | undefined>
</script>


<template>
    <div class="dm">
        <Chat :channel="dm?.channel" />
    </div>
</template>

<style lang="scss" scoped>
.dm {
    display: flex;
    height: inherit;
    width: 100%;
    position: relative;
}
</style>