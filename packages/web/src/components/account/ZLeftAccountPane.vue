<script setup lang="ts">
import { computed, defineProps, PropType } from 'vue';
import { NElement, NAvatar, NButton, NSkeleton } from 'naive-ui';
import { Account } from '../../User';
import { useClientStore } from '../../stores/ClientStore';
const props = defineProps({
    user: Object as PropType<Account>,
    loading: Boolean
});
let lite = computed(() => useClientStore().lite)
let viewPort = computed(() => window.innerWidth)
</script>

<template>
    <NElement class="profile-left">
        <div class="avatar-wrap">
            <NSkeleton round :height="128" :width="128" v-if="loading"/>
            <NAvatar :size="128" :src="(user?.avatar) ? lite.$getSelfAvatar()!! : undefined" round v-else alt="Your Profile Picture"/>
        </div>
        <div class="lower basic-info">
            <NSkeleton :width="150" :height="32" round v-if="loading"/>
            <span class="name" v-else>
                {{ props.user?.name }}
            </span>
            <NSkeleton style="margin-top: 8px;" :width="150" :height="16" round v-if="loading"/>
            <span class="id" style="font-size: 16px;" v-else>
                {{ props.user?.id }}
            </span>
        </div>
        <div class="lower tabs">
            <NButton text style="font-size: 24px;" disabled>
                Public
            </NButton>
        </div>
    </NElement>
</template>

<style lang="scss" scoped>
.profile-left {
    display: flex;
    flex-direction: column;

    .avatar-wrap {
        padding: 8px;
        width: fit-content;
        margin-bottom: 40px;
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

        &.lower {
            margin-top: 45px;
        }
    }
}
@media only screen and(max-width: 852px) { 
    .profile-left {
        justify-content: center;
        align-items: center;
        .avatar-wrap {
            margin-bottom: 0;
        }
        .basic-info {
            justify-content: center;
            align-items: center;
        }
    }
}
</style>