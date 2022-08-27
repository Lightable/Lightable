<script setup lang="ts">
import { computed, ref } from 'vue';
import { NButton, NIcon, NModal, NInput, NProgress } from 'naive-ui';
import { Settings32Regular, SquareMultiple24Filled, Subtract24Filled } from '@vicons/fluent';
import { CloseOutline, MoonOutline, SunnyOutline, SquareOutline as Square, DownloadOutline as Download, ChevronBack } from '@vicons/ionicons5';
import { WindowToggleMaximise, WindowMinimise, Quit } from '../../wailsjs/runtime';
import { useAppStore } from '../stores/AppStore';
import { debug } from '../composable/Logger';
import { DownloadUpdate, Restart } from '../../wailsjs/go/app/App';
import { EventsOn } from '../../wailsjs/runtime/runtime';
import SettingsPickerDropdown from './settings/SettingsPickerDropdown.vue';
const appStore = useAppStore();

const downloadModal = ref({
    show: false,
    downloading: false,
    percent: 0,
    url: null as string | null,
    finished: false
});
const settingsPickerDropdown = ref({
    show: false
});
const theme = computed(() => appStore.theme);
const leftDrawer = computed(() => appStore.leftDrawer);

const lightTheme = () => {
    appStore.changeTheme('Light');
}
const darkTheme = () => {
    appStore.changeTheme('Dark');
}
const updateSettingsPicker = () => {
    settingsPickerDropdown.value.show = !settingsPickerDropdown.value.show;
}
const showDownload = () => {
    downloadModal.value.show = true;
}
const startDownload = () => {
    downloadModal.value.downloading = true;
    debug('Download', 'Initiating download of URL', downloadModal.value.url);
    DownloadUpdate(downloadModal.value.url as string);
    EventsOn('download:total', (total: number) => {
        let time = performance.now()
        EventsOn('download:progress', (percent: number) => {
            downloadModal.value.percent = parseInt((percent / total * 100).toFixed());
            if (downloadModal.value.percent == 100) {
                downloadModal.value.finished = true;
                debug('Download', `Download finished in ${(performance.now() - time).toFixed(3)}ms`)

            }
        })
    });
}
const restartLightable = () => {
    Restart()
}
</script>

<template>
    <SettingsPickerDropdown v-if="settingsPickerDropdown.show" @close="updateSettingsPicker">

    </SettingsPickerDropdown>
    <NModal v-model:show="downloadModal.show" preset="dialog" :closable="!downloadModal.finished">
        <template #header>
            {{ downloadModal.downloading ? 'Downloading exe...' : 'Input EXE URL' }}
        </template>
        <div class="content">
            <NInput placeholder="URL" v-if="!downloadModal.downloading" v-model:value="downloadModal.url" />
            <div class="progress" v-if="downloadModal.downloading">
                <NProgress type="line" :percentage="downloadModal.percent" />
            </div>
        </div>
        <template #action>
            <NButton type="primary" quaternary @click="startDownload" :loading="downloadModal.downloading" v-if="!downloadModal.finished">Download</NButton>
            <NButton type="warning" quaternary v-else @click="restartLightable">Restart</NButton>
        </template>
    </NModal>
    <div class="titlebar" style="--wails-draggable: drag" :with-drawer="leftDrawer.show">
        <div class="titlebar-left" data-wails-no-drag v-if="!leftDrawer.show">
            <div class="item" id="go-back">
                <NButton text type="success" @click="$router.back()" :disabled="$router.currentRoute.value.name == 'home'">
                    <template #icon>
                        <NIcon>
                            <ChevronBack />
                        </NIcon>
                    </template>
                </NButton>
            </div>
        </div>
        <!-- <div class="titlebar-right" data-wails-no-drag>
            <div class="item">
                <NButton title="Download" text style="padding: 0" @click="showDownload">
                    <template #icon>
                        <NIcon>
                            <Download />
                        </NIcon>
                    </template>
                </NButton>
            </div>
            <div class="item">
                <NButton title="Settings" text style="padding: 0" type="info" @click="updateSettingsPicker">
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
                        <NIcon :size="16">
                            <Square />
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
        </div> -->
    </div>
</template>


<style lang="scss" scoped>
.titlebar {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    flex-direction: row;
    height: 32px;
    z-index: 2;
    position: absolute;
    top: 0;
    width: 100vw;
    
    &[with-drawer='true'] {
        margin-left: 250px;
    }

    .titlebar-left {
        margin-right: auto;

        #go-back {
            padding: 0 8px;
        }
    }

    .titlebar-right,
    .titlebar-left {
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