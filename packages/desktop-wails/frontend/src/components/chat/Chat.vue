<script setup lang="ts">
import { computed, onMounted, PropType, Ref, ref } from 'vue';
import ChatHeader from './ChatHeader.vue';
import ChatInput from './ChatInput.vue';
import { NAvatar, NScrollbar, NAlert } from 'naive-ui';
import Message from '../message/Message.vue';
import { useAppStore } from '../../stores/AppStore';
import { mocks } from '../../../wailsjs/go/models';
import { GetAvatar } from '../../../wailsjs/go/client/Client';
import { RelationshipStatus } from '../../stores/AppStore'
const appStore = useAppStore();

const props = defineProps({
    channel: Object as PropType<mocks.Channel>
});

const avatar = ref() as Ref<string | undefined>
const getUser = (id: string | undefined): mocks.PublicUser | undefined => {
    return appStore.users.find(u => u.id === id)
}

const user = computed(() => getUser(props.channel?.id))
const status = computed(() => appStore.getUserTypeRelation(props.channel?.id))

onMounted(async () => {
    if (props.channel) {
        avatar.value = await GetAvatar(props.channel?.id, 64)
    }
})
</script>

<template>
    <div class="chat">
        <ChatHeader :title="(user) ? user?.name : 'Unknown'">
            <template #icon>
                <NAvatar round :src="avatar" />
            </template>
        </ChatHeader>
        <NScrollbar style="height: 100%; width: 100%; padding-bottom: 5px; padding-top: 5px;" trigger="hover">
            <div class="pad">
                <!-- <div class="infinite-load ns">
                    <span>Loading Messages...</span>
                </div> -->
                <div class="errors ns">
                    <NAlert id="request" title="Can't send messages to this user" type="error" v-if="!status || typeof status === 'number' && status != RelationshipStatus.FRIEND">
                        <span class="error" v-if="status === RelationshipStatus.REQUEST">This user has yet to accept your friend request</span>
                        <span class="error" v-if="status === RelationshipStatus.PENDING">You still need to accept this user's friend request!</span>
                        <span class="error" v-if="status === RelationshipStatus.UNKNOWN">We're not sure who, or what you're trying to message :/</span>
                        <span class="error" v-else-if="!status">How did you get here? How did we allow you to get here? We'll never know 🤫</span>
                    </NAlert>
                </div>
                <Message v-for="(item, _) in channel.messages" v-if="channel?.messages" :author="getUser(item?.author as any)" :content="item.content" />
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
    width: 100%;
    position: fixed;
    top: 0;
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    .pad {
        padding: 8px;
        margin-top: 50px;

        .infinite-load,
        .errors {
            color: var(--text-color-3);
            text-align: center;
        }
    }
}
</style>