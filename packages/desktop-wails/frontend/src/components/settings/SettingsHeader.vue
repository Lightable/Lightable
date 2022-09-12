<script setup lang="ts">
import {computed} from 'vue';
import { useUpdateStore } from '../../stores/UpdateStore';
import AppHeader from '../header/AppHeader.vue';
import { NIcon, NButton } from 'naive-ui';
import { Cog } from '@vicons/ionicons5';
import { ColorPalette as ColorPalette } from '@vicons/ionicons5';
import { Person28Filled as Person, Bug24Filled as Debug } from '@vicons/fluent';
import { PhDownload, PhUser, PhPalette, PhBug } from '@dnlsndr/vue-phosphor-icons';
import { useRouter } from 'vue-router';

const updateStore = useUpdateStore();

const update = computed(() => updateStore.currentUpdate);
const router = useRouter();
const navigate = (path: string) => {
    router.push({name: path});
}
</script>


<template>
    <AppHeader title="Settings">
        {{update}}
        <template #icon>
            <NIcon :size="32">
                <Cog />
            </NIcon>
        </template>
        <template #actions>
            <NButton quaternary @click="navigate('settings-update')" :disabled="$router.currentRoute.value.name === 'settings-update'" v-if="update.available">
                <template #icon>
                    <NIcon>
                        <PhDownload weight="duotone" color="var(--success-color)"/>
                    </NIcon>
                </template>
                Update
            </NButton>
            <NButton quaternary @click="navigate('settings-profile')" :disabled="$router.currentRoute.value.name === 'settings-profile'">
                <template #icon>
                    <NIcon>
                        <PhUser weight="duotone"/>
                    </NIcon>
                </template>
                Profile
            </NButton>
            <NButton quaternary @click="navigate('settings-customize')" :disabled="$router.currentRoute.value.name === 'settings-customize'">
                <template #icon>
                    <NIcon>
                        <PhPalette weight="duotone"/>
                    </NIcon>
                </template>
                Customize
            </NButton>
            <NButton quaternary @click="navigate('settings-debug')" :disabled="$router.currentRoute.value.name === 'settings-debug'">
                <template #icon>
                    <NIcon>
                        <PhBug weight="duotone"/>
                    </NIcon>
                </template>
                Debug
            </NButton>
        </template>
    </AppHeader>
</template>