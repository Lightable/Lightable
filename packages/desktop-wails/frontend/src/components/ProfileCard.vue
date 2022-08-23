<script setup lang="ts">
import {ref, onMounted, Ref} from 'vue';
import { NAvatar } from 'naive-ui';
import { GetAvatar } from '../../wailsjs/go/client/Client';
import { mocks } from '../../wailsjs/go/models';
import StatusIndicator from './StatusIndicator.vue';

const props = defineProps({
    friend: mocks.PublicUser
});
const avatar = ref() as Ref<string | undefined>
const getAvatar = async (user: string) => {
    return await GetAvatar(user, 64) as string | undefined
}
(props.friend?.id)
onMounted(async () => {
    avatar.value = await getAvatar(props.friend!!.id)
})
</script>

<template>
    <div class="outer" @click="$router.push({name: 'channel-dm', params: { id: friend?.id }})">
        <div class="friend-c ns">
            <StatusIndicator :user="friend">
                <NAvatar round :size="38" :src="avatar"/>
            </StatusIndicator>

            <div class="friend-meta">
                <div class="name">
                    <span>{{ friend?.name }}</span>
                </div>
            </div>
            <div class="actions">
                <slot />
            </div>
        </div>
    </div>
</template>

<style lang="scss" scoped>
.outer {
    display: flex;
    width: 100%;
    justify-content: center;
    align-items: center;
    border-radius: 8px;
    transition: background-color 150ms ease-in-out;
    cursor: pointer;

    .friend-c {
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 16px;
        width: 95%;
        padding: 4px;


        .friend-meta {
            display: flex;
            flex-direction: column;

            .name {
                font-family: "Rubik";
                font-size: 18px;
            }
        }

        .actions {
            display: flex;
            flex-direction: row;
            gap: 16px;
            margin-left: auto;
            padding: 8px;
        }


    }

    &:hover {
        background-color: var(--card-color);
    }
}
</style>

