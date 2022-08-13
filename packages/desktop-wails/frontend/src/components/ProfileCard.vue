<script setup lang="ts">
import { NAvatar } from 'naive-ui';
import { mocks } from '../../wailsjs/go/models';
import StatusIndicator from './StatusIndicator.vue';

const props = defineProps({
    friend: mocks.PublicUser
});
</script>

<template>
    <div class="outer">
        <div class="friend-c ns">
            <StatusIndicator :user="friend">
                <NAvatar round :size="38" :src="friend?.avatar?.id" />
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

