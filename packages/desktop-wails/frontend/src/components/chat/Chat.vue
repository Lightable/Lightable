<script setup lang="ts">
import { computed, PropType } from 'vue';
import ChatHeader from './ChatHeader.vue';
import ChatInput from './ChatInput.vue';
import { NAvatar, NScrollbar } from 'naive-ui';
import Message from '../message/Message.vue';
import { useAppStore } from '../../stores/AppStore';
import { mocks } from '../../../wailsjs/go/models';
const appStore = useAppStore();

const props = defineProps({
    channel: Object as PropType<mocks.Channel>
});


const getUser = (id: number) => {
    return appStore.users.find(u => u.id === String(id) )
}
</script>

<template>
    <div class="chat">
        <ChatHeader :title="(channel?.owner) ? channel?.owner?.name : 'Unknown'">
            <template #icon>
                <NAvatar round :src="channel?.owner?.avatar"/>
            </template>
        </ChatHeader>
        <NScrollbar style="height: 100%; width: 100%; padding-bottom: 5px; padding-top: 5px;" trigger="hover">
            <div class="pad">
                <div class="infinite-load ns">
                    <span>Loading Messages...</span>
                </div>
                <Message v-for="(item, _) in channel.messages" v-if="channel?.messages" :author="getUser(item.author)" :content="item.content"/>
                <!-- <Message :is-preview="true" />
                <Message :is-preview="true" />
                <Message :is-preview="true" />
                <Message :is-preview="true" />
                <Message :is-preview="true" />
                <Message :is-preview="true" />
                <Message :is-preview="true" />
                <Message :is-preview="true" />
                <Message :is-preview="true" />
                <Message :author="tempAuthor" /> -->
            </div>
        </NScrollbar>
        <ChatInput />
    </div>
</template>

<style lang="scss" scoped>
.chat {
    height: 100vh;
    width: calc(100vw - 250px);
    position: fixed;
    top: 0;
    display: flex;
    flex-direction: column;
    align-items: flex-start;

    .pad {
        padding: 8px;
        margin-top: 50px;
        .infinite-load {
            color: var(--text-color-3);
            text-align: center;
        }
    }
}
</style>