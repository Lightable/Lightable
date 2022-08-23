<script setup lang="ts">
import { NButton, NIcon, NTooltip } from 'naive-ui';
import { ColorPalette as ColorPalette } from '@vicons/ionicons5';
import { Person28Filled as Person, PlugDisconnected24Filled as Plug, Bug24Filled as Debug } from '@vicons/fluent';
import { useSettingsStore, SettingsPane } from '../../stores/SettingsStore';


const settingsStore = useSettingsStore();
let timer: any;

const emits = defineEmits(['close'])
const onLeaveMouse = () => {
    timer = setTimeout(() => {
        console.log('Should close')
        emits('close');
    }, 5000)
}

const onEnterMouse = () => {
    if (timer) {
        clearTimeout(timer);
    }
}

const setCurrentPane = (pane: SettingsPane) => {
    settingsStore.setLivePane(pane)
    emits('close');
}
</script>

<template>
    <div class="settings dropdown" @mouseleave="onLeaveMouse" @mouseover="onEnterMouse">
        <section class="pane-pick">
            <NTooltip trigger="hover" placement="left">
                <template #trigger>
                    <NButton text type="info" @click="setCurrentPane('Customization')">
                        <template #icon>
                            <NIcon :size="32">
                                <ColorPalette />
                            </NIcon>
                        </template>
                    </NButton>
                </template>
                Customize
            </NTooltip>
        </section>
        <section class="pane-pick">
            <NTooltip trigger="hover" placement="left">
                <template #trigger>
                    <NButton text type="warning">
                        <template #icon>
                            <NIcon :size="32">
                                <Person />
                            </NIcon>
                        </template>
                    </NButton>
                </template>
                User
            </NTooltip>
        </section>
        <section class="pane-pick">
            <NTooltip trigger="hover" placement="left">
                <template #trigger>
                    <NButton text type="primary">
                        <template #icon>
                            <NIcon :size="32">
                                <Debug />
                            </NIcon>
                        </template>
                    </NButton>
                </template>
                Debug
            </NTooltip>
        </section>
        <section class="pane-pick">
            <NTooltip trigger="hover" placement="left">
                <template #trigger>
                    <NButton text type="error">
                        <template #icon>
                            <NIcon :size="32">
                                <Plug />
                            </NIcon>
                        </template>
                    </NButton>
                </template>
                Debug Socket
            </NTooltip>
        </section>
    </div>
</template>

<style lang="scss" scoped>
.settings,
.dropdown {
    position: absolute;
    width: 50px;
    background-color: rgba(255, 255, 255, 0.050);
    right: -50px;
    top: 50px;
    animation: slideRight 150ms ease-in forwards;
    display: flex;
    align-items: center;
    flex-direction: column;
    border-top-left-radius: 8px;
    border-bottom-left-radius: 8px;
    padding-top: 8px;
    padding-bottom: 4px;
    backdrop-filter: blur(20px);
    z-index: 5;

    .pane-pick {
        margin: 10px;
    }
}

@keyframes slideRight {
    from {
        transform: translateX(50px);
    }

    to {
        transform: translateX(-50px);
    }
}
</style>