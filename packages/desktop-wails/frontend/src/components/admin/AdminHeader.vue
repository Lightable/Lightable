<script setup lang="ts">
    import {computed} from 'vue';
    import { useUpdateStore } from '../../stores/UpdateStore';
    import AppHeader from '../header/AppHeader.vue';
    import { NIcon, NButton } from 'naive-ui';

    import { PhUpload, PhShieldStar } from '@dnlsndr/vue-phosphor-icons';
    import { useRouter } from 'vue-router';
    
    const updateStore = useUpdateStore();

    const router = useRouter();
    const navigate = (path: string) => {
        router.push({name: path});
    }
    </script>
    
    
    <template>
        <AppHeader title="Settings">
            <template #icon>
                <NIcon :size="32">
                    <PhShieldStar />
                </NIcon>
            </template>
            <template #actions>
                <NButton quaternary @click="navigate('admin-release')" :disabled="$router.currentRoute.value.name === 'admin-release'">
                    <template #icon>
                        <NIcon>
                            <PhUpload weight="duotone" color="var(--success-color)"/>
                        </NIcon>
                    </template>
                    Create Release
                </NButton>
                <!-- <NButton quaternary @click="navigate('settings-profile')" :disabled="$router.currentRoute.value.name === 'settings-profile'">
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
                </NButton> -->
            </template>
        </AppHeader>
    </template>