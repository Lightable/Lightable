<script setup lang="ts">
import { computed, ref } from 'vue';
import { useAppStore } from '../../../stores/AppStore';
import { NAvatar, NModal, NButton, NProgress } from 'naive-ui';
import { GetSelfAvatar, OpenAvatarPickDialog } from '../../../../wailsjs/go/client/Client';
import AppSection from '../../../components/app/AppSection.vue';
import ProfileCard from '../../../components/settings/ProfileCard.vue';
import { PreviewRound } from '@vicons/material';
import { UploadAvatar } from '../../../../wailsjs/go/client/HttpClient';
import { EventsOff, EventsOn } from '../../../../wailsjs/runtime/runtime';
const appStore = useAppStore();
let copy: any;
const user = computed(() => appStore.user);
const avatar = computed(() => appStore.avatar);

const previewModal = ref({
    show: false,
    closable: true,
    uploadable: true,
    uploading: false,
    uploadedPercent: 0,
    error: false as boolean | string,
    waiting: false,
    file: ''
})
appStore.setTitle('Settings • Profile');

if (user.value?.avatar) {
    GetSelfAvatar(64).then(a => {
        copy = a
        appStore.avatar = a;
    })
}

const selectAvatar = async () => {
    let preview = previewModal.value;
    const path = await OpenAvatarPickDialog()
    if (path === '.' || path === '' || !path) return
    console.log('Select avatar', path)
    appStore.avatar = path;
    preview.file = path;
    preview.show = true;
    preview.uploadable = true;
    preview.error = false;
}

const closeAvatarSelectModal = () => {
    if (!previewModal.value.closable) return
    previewModal.value.show = false;
    appStore.avatar = copy;
}


const uploadAvatar = async () => {
    let preview = previewModal.value;
    preview.closable = false;
    preview.uploading = true;
    EventsOn('upload:progress', (d: number) => {
        preview.uploadedPercent = roundOf(d, 0);
    });
    EventsOn('upload:finished', (d) => {
        preview.uploading = false;
        preview.waiting = true;
        EventsOff('upload:progress');
        EventsOff('upload:finished');
    });
    try {
        await UploadAvatar(preview.file);
        appStore.avatar = await GetSelfAvatar(64);
        preview.show = false;
        preview.closable = true;
        preview.waiting = false;
    } catch (e: Error) {
        preview.closable = true;
        preview.waiting = false;
        preview.uploadable = false;
        preview.error = 'This avatar can\'t be uploaded';
        appStore.avatar = null;
    }
    // var interval = setInterval(() => {
    //     if (preview.uploadedPercent === 100) {
    //         preview.uploading = false;
    //         preview.waiting = true;
    //         return clearInterval(interval)
    //     }
    //     preview.uploadedPercent += 1
    // }, 20)
}
function roundOf(n: number, p: number) {
    const n1 = n * Math.pow(10, p + 1);
    const n2 = Math.floor(n1 / 10);
    if (n1 >= (n2 * 10 + 5)) {
        return (n2 + 1) / Math.pow(10, p);
    }
    return n2 / Math.pow(10, p);
}

const enableFlag = (flag: string) => {
    appStore.user!!.profileOptions[flag]= true;
}
const disableFlag = (flag: string) => { 
    appStore.user!!.profileOptions[flag] = false;
    console.log(appStore.user);
}
</script>

<template>
    <NModal preset="dialog" :show="previewModal.show" title="Avatar Preview" @close="closeAvatarSelectModal" @mask-click="closeAvatarSelectModal" :closable="previewModal.closable">
        <div class="preview">
            <NAvatar :size="222" round :src="(avatar as any)" class="avatar" :style="(previewModal.uploading) ? `opacity: ${previewModal.uploadedPercent / 100};` : 'opacity: 1;'" />
        </div>
        <template #action>
            <div class="body">
                <NProgress :show-indicator="false" :percentage="previewModal.uploadedPercent" v-if="previewModal.uploading || previewModal.waiting" :processing="previewModal.waiting" />
                <span class="ns details" v-if="previewModal.uploading || previewModal.waiting || previewModal.error">
                    <span v-if="previewModal.uploading">Uploading {{ previewModal.uploadedPercent }}% complete</span>
                    <span v-if="previewModal.waiting">Waiting for server</span>
                    <span v-if="previewModal.error">{{previewModal.error}}</span>
                </span>
                <div class="space">
                    <NButton type="error" secondary @click="closeAvatarSelectModal" :disabled="previewModal.uploading || previewModal.waiting">Cancel</NButton>
                    <NButton type="primary" secondary @click="uploadAvatar" :loading="previewModal.uploading || previewModal.waiting" :disabled="!previewModal.uploadable">Upload</NButton>
                </div>
            </div>
        </template>
    </NModal>
    <AppSection title="PROFILE">
        <div class="outer">
            <ProfileCard title="Avatar" @clicked="selectAvatar">
                <NAvatar :size="42" round :src="(avatar as any)" />
            </ProfileCard>
            <ProfileCard title="Name">
                {{ user?.name }}
            </ProfileCard>

        </div>
    </AppSection>
    <AppSection title="CONFIGURATION">
        <div class="outer">
            <ProfileCard title="Public Profile" togglable @enable="enableFlag('IsPublic')" @disable="disableFlag('IsPublic')" :toggle="user?.profileOptions['IsPublic']"/>
            <ProfileCard title="Show Profile Status" togglable @enable="enableFlag('ShowStatus')" @disable="disableFlag('ShowStatus')" :toggle="user?.profileOptions['ShowStatus']"/>
        </div>
    </AppSection>
</template>


<style lang="scss" scoped>
.outer {
    max-width: 65%;
    min-width: 10%;

}

.preview {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;

    .avatar {
        object-fit: cover;
    }
}

.body {
    display: flex;
    flex-direction: column;
    justify-content: center;
    width: 100%;
    gap: 16px;

    .details {
        text-align: center;
    }

    .space {
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        width: 100%;
    }
}
</style>