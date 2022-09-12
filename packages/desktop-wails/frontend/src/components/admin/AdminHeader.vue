<script setup lang="ts">
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
        <AppHeader title="Admin">
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
            </template>
        </AppHeader>
    </template>