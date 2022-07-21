<script setup lang="ts">
import { computed } from 'vue';
import { NButton, NIcon } from 'naive-ui';
import { Settings32Regular, SquareMultiple24Filled, Subtract24Filled } from '@vicons/fluent';
import { CloseOutline, MoonOutline, SunnyOutline } from '@vicons/ionicons5';
import { WindowToggleMaximise, WindowMinimise, Quit } from '../../wailsjs/runtime';
import { useAppStore } from '../stores/AppStore';
const appStore = useAppStore();

const theme = computed(() => appStore.theme);

const lightTheme = () => {
    appStore.changeTheme('Light');
}
const darkTheme = () => {
    appStore.changeTheme('Dark');
}
</script>

<template>
    <div class="titlebar" data-wails-drag>
        <div class="titlebar-right">
            <div class="item">
                <NButton title="Settings" text style="padding: 0" type="info">
                    <template #icon>
                        <NIcon>
                            <Settings32Regular />
                        </NIcon>
                    </template>
                </NButton>
            </div>
            <div class="item" v-if="theme == 'Light'">
                <NButton title="Theme Picker Dark" text style="padding: 0; --n-text-color: #8d97fc; --n-text-color-hover: #9ea6ff; --n-text-color-pressed: #7883ff;" @click="darkTheme()">
                    <template #icon>
                        <NIcon>
                            <MoonOutline />
                        </NIcon>
                    </template>
                </NButton>
            </div>
            <div class="item" v-else>
                <NButton title="Theme Picker Light" text style="padding: 0; --n-text-color: #f8fc8d; --n-text-color-hover: #f6fcb1; --n-text-color-pressed: #f2fc83;" @click="lightTheme()">
                    <template #icon>
                        <NIcon>
                            <SunnyOutline />
                        </NIcon>
                    </template>
                </NButton>
            </div>
            <div class="item">
                <NButton title="Minimize" text style="padding: 0" type="warning" @click="WindowMinimise()">
                    <template #icon>
                        <NIcon>
                            <Subtract24Filled />
                        </NIcon>
                    </template>
                </NButton>
            </div>
            <div class="item">
                <NButton title="Maximize" text style="padding: 0" type="primary" @click="WindowToggleMaximise()">
                    <template #icon>
                        <NIcon>
                            <SquareMultiple24Filled />
                        </NIcon>
                    </template>
                </NButton>
            </div>
            <div class="item">
                <NButton title="Close" text style="padding: 0" type="error" @click="Quit()">
                    <template #icon>
                        <NIcon :size="24">
                            <CloseOutline />
                        </NIcon>
                    </template>
                </NButton>
            </div>
        </div>
    </div>
</template>


<style lang="scss" scoped>
.titlebar {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    flex-direction: row;
    height: 32px;
    position: absolute;
    top: 0;
    width: 100vw;

    .titlebar-right {
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: center;

        .item {
            display: flex;
            justify-content: center;
            padding: 0 15px;
        }
    }
}
</style>