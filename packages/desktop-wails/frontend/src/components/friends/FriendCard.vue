<script setup lang="ts">
import { ref, PropType } from 'vue';
import { NIcon } from 'naive-ui';
import { MenuFilled as Menu, BlockFilled as Block } from '@vicons/material';
import { Chatbox, PersonRemove as Remove, Close } from '@vicons/ionicons5';
import { mocks } from '../../../wailsjs/go/models';

import RelationshipCard from './RelationshipCard.vue';
import ContextMenu from '../ContextMenu.vue';
import ContextMenuButton from '../ContextMenuButton.vue';


const props = defineProps({
    user: Object as PropType<mocks.PublicUser>
})

const contextMenuPos = ref({
    show: false,
    x: 0,
    y: 0
})
const onContextMenu = ({ x, y }: MouseEvent) => {
    contextMenuPos.value = {
        show: true,
        x, y
    }
}

const loadingStates = ref({
    remove: false,
    block: false
})



const remove = () => {
    loadingStates.value.remove = true;

}

const block = () => {
    loadingStates.value.block = true;
    
}
</script>

<template>
    <div class="outer">
        <RelationshipCard :friend="user">
            <button class="context-menu-btn" @click="onContextMenu">
                <NIcon :size="32" class="menu" v-if="!contextMenuPos.show">
                    <Menu />
                </NIcon>
                <NIcon :size="32" class="menu close" color="var(--error-color-hover)" v-if="contextMenuPos.show">
                    <Close />
                </NIcon>
            </button>
        </RelationshipCard>
        <ContextMenu :x="contextMenuPos.x" :y="contextMenuPos.y" :show="contextMenuPos.show" @close="(e) => contextMenuPos.show = e">
            <ContextMenuButton>
                <template #icon>
                    <Chatbox />
                </template>
                Send Message
            </ContextMenuButton>
            <ContextMenuButton type="danger" :loading="loadingStates.remove" @click="remove">
                <template #icon>
                    <Remove />
                </template>
                Remove Friend
            </ContextMenuButton>
            <ContextMenuButton type="danger" :loading="loadingStates.block" @click="block">
                <template #icon>
                    <Block />
                </template>
                Block
            </ContextMenuButton>
        </ContextMenu>
    </div>
</template>


<style lang="scss" scoped>
.outer {
    max-width: 65%;
    min-width: 10%;

}

.context-menu-btn {
    all: unset;
    display: flex;
    padding: 2px;
    height: 48px;
    width: 48px;
    border-radius: 50%;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    background-color: #121217;
    transition: background .3s ease-in-out;

    .menu {
        animation: fade .75s ease;
    }

    &:hover {
        background-color: #101014;
    }
}

@keyframes fade {
    from {
        opacity: .6;
    }

    to {
        opacity: 1;
    }
}
</style>