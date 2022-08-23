<script setup lang="ts">
import { PropType } from 'vue';
import { mocks } from '../../wailsjs/go/models';
const props = defineProps({
    user: Object as PropType<mocks.PublicUser>
})
</script>


<template>
    <div class="avatar-w-indicator" v-if="user">
        <div class="avatar">
            <slot />
        </div>
        <div class="indicator" :status="user.state" v-if="user.state !== 0" />
    </div>
</template>


<style lang="scss" scoped>
.avatar-w-indicator {
    position: relative;
    height: fit-content;
    width: fit-content;

    .avatar {
        display: flex;
        position: relative;
        width: auto;
        height: auto;
    }

    .indicator {
        width: 16px;
        height: 16px;
        border-radius: 50%;

        border: rgb(16, 16, 20) 3px solid;
        bottom: -3px;
        right: -10%;
        position: absolute;

        &[status='1'] {
            background-color: var(--success-color);
        }
        &[status='2'] {
            background-color: var(--warning-color);
        }
        &[status='3'] {
            background-color: var(--error-color);
        }
    }
}
</style>