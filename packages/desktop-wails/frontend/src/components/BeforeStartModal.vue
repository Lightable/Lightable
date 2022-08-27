<script setup lang="ts">
import { computed, ref } from 'vue';
import { useAppStore } from '../stores/AppStore';
import { NModal, NInput, NSelect, NButton } from 'naive-ui';
import { CreateResponder } from '../../wailsjs/go/app/App';
import { SetSocket } from '../../wailsjs/go/client/Client';
import { SetAPI, SetSecure } from '../../wailsjs/go/client/HttpClient';
const appStore = useAppStore();
const displayModal = computed(() => appStore.shouldShowOnboardModal);

const secureOptions = [
    {
        label: `Yes, It's secure`,
        value: true,
    },
    {
        label: `No, It's not secure`,
        value: false
    }
] as any

const defaultOptions = {
    api: 'api.lightable.app',
    gateway: 'api.lightable.app',
    secure: true
}

const onboardModalValues = ref({
    api: '',
    gateway: '',
    secure: false as any,
    loading: false
})

const useDefaults = () => {
    const modalRef = onboardModalValues.value
    modalRef.api = defaultOptions.api;
    modalRef.gateway = defaultOptions.gateway;
    modalRef.secure = defaultOptions.secure;
}

const saveResponder = async () => {
    const modalRef = onboardModalValues.value;
    modalRef.loading = true;
    await CreateResponder();
    await Promise.all([SetSocket(modalRef.gateway), SetSecure(modalRef.secure), SetAPI(modalRef.api)]);
    modalRef.loading = false;
    appStore.shouldShowOnboardModal = false;
}
</script>

<template>
    <NModal title="Before you start..." :closable="false" preset="dialog" :show="displayModal">
        Define your server URL, or use the default one included
        <NInput placeholder="API URL (http://x.x.x.x)" v-model:value="onboardModalValues.api" class="onboard-input" :loading="onboardModalValues.loading" />
        <NInput placeholder="Gateway URL (ws://x.x.x.x)" v-model:value="onboardModalValues.gateway" class="onboard-input" :loading="onboardModalValues.loading" />
        <NSelect placeholder="Is this server secure? (https/wss)" :options="secureOptions" v-model:value="onboardModalValues.secure" class="onboard-input" :loading="onboardModalValues.loading" />
        <div class="onboard-modal-actions">
            <NButton tertiary class="onboard-input" type="info" @click="useDefaults" :loading="onboardModalValues.loading">
                Default
            </NButton>
            <NButton tertiary class="onboard-input" type="primary" :loading="onboardModalValues.loading" @click="saveResponder">
                I'm okay with these
            </NButton>
        </div>

    </NModal>
</template>


<style lang="scss" scoped>
.onboard-input {
  margin-top: 8px;
}

.onboard-modal-actions {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
}
</style>