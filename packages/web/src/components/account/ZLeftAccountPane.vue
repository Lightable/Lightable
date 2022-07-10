<script setup lang="ts">
import { computed, ComputedRef, defineAsyncComponent, defineProps, PropType, ref, Ref } from 'vue';
import { NIcon, useMessage } from 'naive-ui';
import { Link } from '@vicons/ionicons5';
import { Account } from '../../User';
import { useClientStore } from '../../stores/ClientStore';
import useClipboard from '../../composables/useClipboard';
import { LiteClient } from '../../lib/LiteClient';
const props = defineProps({
    user: Object as PropType<Account>,
    loading: Boolean
});
const lite = computed(() => useClientStore().lite) as ComputedRef<LiteClient>;
const fileRef = ref(null) as Ref<HTMLInputElement | null>;
const avatarUpload = ref(null) as Ref<AvatarUpload | null>;
const message = useMessage();
interface AvatarUpload {
    status: AvatarUploadStatus,
    progress?: number
}
enum AvatarUploadStatus {
    UPLOADING,
    FINISHED,
    LEWD,
    ERROR
}
const Element = defineAsyncComponent({
    loader: () => import('naive-ui/lib/element/src/Element')
});
const Avatar = defineAsyncComponent({
    loader: () => import('naive-ui/lib/avatar/src/Avatar')
});
const Button = defineAsyncComponent({
    loader: () => import('naive-ui/lib/button/src/Button')
});
const Skeleton = defineAsyncComponent({
    loader: () => import('naive-ui/lib/skeleton/src/Skeleton')
});
const Progress = defineAsyncComponent({
    loader: () => import('naive-ui/lib/progress/src/Progress')
});
const Shield = defineAsyncComponent({
    loader: () => import('@vicons/ionicons5/ShieldCheckmark')
});
const Image = defineAsyncComponent({
    loader: () => import('@vicons/ionicons5/ImageOutline')
});
const Tooltip = defineAsyncComponent({
    loader: () => import('naive-ui/lib/tooltip/src/Tooltip')
});
const ZAvatarFrame = defineAsyncComponent({
    loader: () => import('../ZAvatarFrame.vue')
});
const CloudUpload = defineAsyncComponent({
    loader: () => import('@vicons/ionicons5/CloudUpload')
});
const getAvatarFile = () => {
    fileRef.value!!.click();
}
const uploadAvatarFile = () => {
    // @ts-ignore
    let file = fileRef.value!!.files[0];
    if (file == null || file == undefined) return
    // @ts-ignore
    if (file.type != 'image/png' && file.type != 'image/jpeg' && file.type != 'image/webp') return
    lite.value.$updateAvatar(file);
    lite.value!!.on('self:avatar:upload:progress', (num) => {
        avatarUpload.value = {
            status: AvatarUploadStatus.UPLOADING,
            progress: num
        };
    });
    lite.value!!.on('self:avatar:upload:finish', () => {
        avatarUpload.value = {
            status: AvatarUploadStatus.FINISHED,
        };
    });
    lite.value!!.on('self:avatar:upload:lewd', () => {
        avatarUpload.value = {
            status: AvatarUploadStatus.LEWD,
        };
    });

}
const { toClipboard } = useClipboard();
const copyLinkToClipboard = () => {
    toClipboard(`https://zenspaceweb.vercel.app/profile/${lite?.value?.user?.name}`);
    message.success('Link copied to clipboard');
}
</script>

<template>
    <Element class="profile-left">
        <div class="avatar-wrap">
            <Skeleton round :height="128" :width="128" v-if="loading" />
            <ZAvatarFrame v-else>
                <template #avatar>
                    <Progress :percentage="avatarUpload.progress" type="circle" style="margin: 0 8px 12px 0;" :height="130" :circle-gap="128" v-if="avatarUpload != null && avatarUpload.status != AvatarUploadStatus.FINISHED" :color="(avatarUpload.status != AvatarUploadStatus.LEWD && avatarUpload.status != AvatarUploadStatus.ERROR) ? '#aaff96' : 'var(--error-color)'">
                        <span class="avatar-upload-percentage inner-progress" v-if="avatarUpload.status == AvatarUploadStatus.UPLOADING">
                            {{ avatarUpload.progress }}%
                        </span>
                        <span class="avatar-lewd inner-progress" v-if="avatarUpload.status == AvatarUploadStatus.LEWD">
                            Can't upload provocative avatar
                        </span>
                        <span class="avatar-fail inner-progress" v-if="avatarUpload.status == AvatarUploadStatus.ERROR">
                            Something went wrong
                        </span>
                    </Progress>
                    <Avatar :size="128" :src="(user?.avatar) ? lite.$getSelfAvatar()!! : undefined" round alt="Your Profile Picture" v-else />
                </template>
                <div class="action" @click="getAvatarFile">
                    <CloudUpload color="var(--text-color-3)" class="upload-svg" />
                </div>
            </ZAvatarFrame>
            <div class="badges">
                <div class="admin" v-if="user?.admin" @click="message.info(`${user?.name} is marked as an Administrator for ZenSpace`, { closable: true })">
                    <Tooltip trigger="hover">
                        <template #trigger>
                            <NIcon :size="24" color="var(--info-color)">
                                <Shield />
                            </NIcon>
                        </template>
                        <b>{{ user.name }}</b> is marked as an Administrator for ZenSpace
                    </Tooltip>
                </div>
            </div>
        </div>
        <div class="lower basic-info">
            <Skeleton :width="150" :height="32" round v-if="loading" />
            <span class="name" v-else>
                {{ props.user?.name }}
            </span>
            <Skeleton style="margin-top: 8px;" :width="150" :height="16" round v-if="loading" />
            <span class="id" style="font-size: 16px;" v-else>
                {{ props.user?.id }}
            </span>
        </div>
        <div class="lower tabs">
            <Button text style="font-size: 18px;" :disabled="($router.currentRoute.value.name == 'settings-profile')" @click="$router.push('/settings/profile')">
                Profile
            </Button>
            <Button text style="font-size: 18px;" :disabled="($router.currentRoute.value.name == 'admin')" @click="$router.push('/settings/admin')" v-if="user?.admin">
                Admin
            </Button>
        </div>
        <div class="tabs lower actions">
            <Tooltip trigger="hover">
                <template #trigger>
                    <Button quaternary type="info" size="small" @click="copyLinkToClipboard">
                        <template #icon>
                            <NIcon>
                                <Link />
                            </NIcon>
                        </template>
                        Profile Link
                    </Button>
                </template>
                Copy link for others to view your profile
            </Tooltip>
            <Tooltip trigger="hover">
                <template #trigger>
                    <Button quaternary type="info" size="small" disabled>
                        <template #icon>
                            <NIcon>
                                <Image />
                            </NIcon>
                        </template>
                        Image Link
                    </Button>
                </template>
                I really can't be asked creating another image generator man
            </Tooltip>
        </div>
        <input type="file" accept=".png,.jpg,.jpeg" ref="fileRef" :style="{ display: 'none' }" @input="uploadAvatarFile" />
    </Element>
</template>

<style lang="scss" scoped>
.inner-progress {
    font-size: large;
    color: var(--text-color-2);

    &.avatar-lewd {
        text-align: center;
        font-size: 12px;
    }
}

.action {
    padding: 4px;
    border-radius: 50%;
    height: 32px;
    width: 32px;
    background-color: var(--body-color);
    display: flex;
    justify-content: center;
    align-items: center;

    .upload-svg {
        transition: .2s ease-in-out;
        cursor: pointer;
        height: 24px;
        width: 24px;

        &:hover {
            color: var(--text-color-2);
        }
    }
}

.profile-left {
    display: flex;
    flex-direction: column;

    .avatar-wrap {
        padding: 8px;
        width: fit-content;
        margin-bottom: 40px;
    }

    .badges {
        margin-top: 5px;
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        align-items: center;
        gap: 8px;
    }

    .basic-info {
        display: flex;
        flex-direction: column;
        font-size: 24px;
        color: var(--text-color-1);

        .name {
            font-weight: bold;
        }

        .id {
            color: var(--text-color-3);
        }
    }

    .tabs {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;

        &.lower {
            margin-top: 35px;
        }
    }

    .actions {
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        align-items: center;
    }
}

@media only screen and(max-width: 852px) {
    .profile-left {
        justify-content: center;
        align-items: center;

        .badges {
            justify-content: center;
        }

        .avatar-wrap {
            margin-bottom: 0;
        }

        .basic-info {
            justify-content: center;
            align-items: center;
        }

        .actions {
            flex-direction: column;
            justify-content: center;
            align-items: center;
        }
    }
}
</style>