<script setup lang="ts">
import { NIcon } from 'naive-ui';

const props = defineProps({
    icon: Object,
    text: String,
    cb: Function,
    disabled: Boolean
});

const clickEvent = () => {
    // @ts-ignore
    props?.cb();
}
</script>

<template>
    <div class="drawer-component" :disabled="Boolean(props.disabled)" @click="clickEvent">
        <NIcon :size="18">
            <component :is="props.icon" />
        </NIcon>
        <div class="content">
            {{ props.text }}
        </div>
    </div>
</template>


<style lang="scss" scoped>
.drawer-component {
    border-radius: 4px;
    display: flex;
    height: 16px;
    flex-direction: row;
    align-items: center;
    justify-content: flex-start;
    width: 80%;
    transition: all 150ms ease;
    padding-left: 0.5rem;
    padding-right: 0.5rem;
    padding-top: 0.50rem;
    padding-bottom: 0.50rem;
    margin-top: 2px;
    gap: 16px;
    cursor: pointer;

    &[disabled='true'] {
        opacity: var(--opacity-disabled);
    }

    &:hover {
        &[disabled='false'] {
            background-color: #2080f0;

            .content {
                filter: rgb(44, 44, 44);
            }
        }
    }
}
</style>
