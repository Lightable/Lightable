<script setup lang="ts">
import { PropType } from 'vue';
import { NIcon, NBadge, NTooltip } from 'naive-ui';
import { LightableDrawerComponentPair } from '../../stores/AppStore';
import { useRouter } from 'vue-router';

const props = defineProps({
    pair: Object as PropType<LightableDrawerComponentPair>
});

const router = useRouter();

const clickEvent = () => {
    const route = router.currentRoute.value.fullPath;
    const pair = props.pair!!;
    switch (pair.t) {
        case "Route": {
            if (pair.path!! === route) return
            if (router.currentRoute.value.fullPath === pair.path!!) return
            router.push(pair.path!!);
            break;
        }
        case "Function": {
            if (pair.cb) pair.cb();
            break;
        }
        default: {
            // @ts-ignore
            props?.cb();
            break
        }
    }
}

</script>

<template>
    <NTooltip v-if="pair && pair.tooltip" trigger="hover" placement="bottom">
        <template #trigger>
            <div :class="`drawer-component ${(pair.t !== `Function` && $router.currentRoute.value.fullPath === pair.path) ? 'active-path' : ''}`" :disabled="(pair.t !== `Function` && router.currentRoute.value.fullPath === pair.path || pair.path === '' || router.currentRoute.value.name === pair.name) ? true : Boolean(false)" @click="clickEvent" v-if="pair" :path="pair.path">
                <NBadge :dot="pair?.badge?.show" :type="pair?.badge?.type" :processing="pair.badge.processing" v-if="pair.badge">
                    <template #value>
                        <component :is="pair.badge.icon" weight="duotone" color="var(--text-color-2)" v-if="pair.badge.icon" />
                        {{ pair.badge.value }}
                    </template>
                    <component :is="pair.icon" weight="duotone" :size="24" color="var(--text-color-2)" />
                </NBadge>
                <component :is="pair.icon" weight="duotone" :size="24" color="var(--text-color-2)" v-if="!pair.badge" />
                <slot name="icon" v-if="!pair.icon" />

                <div class="content">
                    {{ pair.text }}
                </div>
            </div>
        </template>
        {{ pair.tooltip }}
    </NTooltip>
    <div :class="`drawer-component ${(pair.t !== `Function` && $router.currentRoute.value.fullPath === pair.path) ? 'active-path' : ''}`" :disabled="(pair.t !== `Function` && router.currentRoute.value.fullPath === pair.path || pair.path === '' || router.currentRoute.value.name === pair.name) ? true : Boolean(false)" @click="clickEvent" v-if="pair && !pair.tooltip" :path="pair.path">
        <NBadge :dot="pair?.badge?.show" :type="pair?.badge?.type" :processing="pair.badge.processing" v-if="pair.badge">
            <template #value>
                <component :is="pair.badge.icon" weight="duotone" color="var(--text-color-2)" v-if="pair.badge.icon" />
                {{ pair.badge.value }}
            </template>
            <component :is="pair.icon" weight="duotone" :size="24" color="var(--text-color-2)" />
        </NBadge>
        <component :is="pair.icon" weight="duotone" :size="24" color="var(--text-color-2)" v-if="!pair.badge" />
        <slot name="icon" v-if="!pair.icon" />

        <div class="content">
            {{ pair.text }}
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
        cursor: not-allowed;
    }

    &.active-path {
        background-color: rgba(255, 255, 255, 0.082)
    }

    &:hover {
        &[disabled='false'] {
            background-color: var(--windows-accent-colour);

            .content {
                filter: rgb(44, 44, 44);
            }
        }
    }
}
</style>
