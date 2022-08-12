<script setup lang="ts">
import { useAppStore } from '../../../stores/AppStore';
import { Chat } from '@vicons/carbon';
import { RectangleLandscape20Regular as ChatInputIcon } from '@vicons/fluent';
import { Reload } from '@vicons/ionicons5';
import { useRouter } from 'vue-router';

const appStore = useAppStore();
const router = useRouter();

appStore.leftDrawer.show = true;
appStore.leftDrawer.components = [
    {
        t: 'Route',
        icon: Chat, 
        text: 'Message', 
        color: '#fff', 
        path: '/concepts/ui/message'
    },
    {
        t: 'Route',
        icon: ChatInputIcon, 
        text: 'Chat Input', 
        color: '#fff',
        path: '/concepts/ui/chat-input'
    },
   {
        t: 'Route',
        icon: Reload, 
        text: 'Loading', 
        color: '#fff',
        path: '/concepts/ui/loading'
    },
];
</script>


<template>
    <div class="ui-example">
        <div class="explain" v-if="$router.currentRoute.value.name == 'ui'">
            <h1>Hi welcome to the UI concepts</h1>
            <span>Here, you'll be able to see the various amount of components used in Lightable</span>
            <h2>Enjoy!</h2>
        </div>
        <router-view />
    </div>
</template>


<style lang="scss" scoped>
.ui-example {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    height: 100%;
    width: 100%;
    gap: 16px;
}
</style>