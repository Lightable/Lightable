<script setup lang="ts">
import AppHeader from '../header/AppHeader.vue';
import { NIcon, NButton } from 'naive-ui';
import { Cog } from '@vicons/ionicons5';
import { ColorPalette as ColorPalette } from '@vicons/ionicons5';
import { Person28Filled as Person, Bug24Filled as Debug } from '@vicons/fluent';
import { useRouter } from 'vue-router';

const router = useRouter();
const navigate = (path: string) => {
    router.push({name: path});
}
</script>


<template>
    <AppHeader title="Settings">
        <template #icon>
            <NIcon :size="32">
                <Cog />
            </NIcon>
        </template>
        <template #actions>
            <NButton quaternary @click="navigate('settings-profile')" :disabled="$router.currentRoute.value.name === 'settings-profile'">
                <template #icon>
                    <NIcon>
                        <Person/>
                    </NIcon>
                </template>
                Profile
            </NButton>
            <NButton quaternary @click="navigate('settings-customize')" :disabled="$router.currentRoute.value.name === 'settings-customize'">
                <template #icon>
                    <NIcon>
                        <ColorPalette/>
                    </NIcon>
                </template>
                Customize
            </NButton>
            <NButton quaternary @click="navigate('settings-debug')" :disabled="$router.currentRoute.value.name === 'settings-debug'">
                <template #icon>
                    <NIcon>
                        <Debug/>
                    </NIcon>
                </template>
                Debug
            </NButton>
        </template>
    </AppHeader>
</template>