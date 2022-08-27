<script setup lang="ts">
import { ref } from 'vue';
import { NButton, NIcon } from 'naive-ui';
import { ChevronDown, ChevronUp } from '@vicons/ionicons5';
const props = defineProps({
    title: String
})

const collapsed = ref(false)
</script>

<template>
    <div class="section ns" aria-role="section">
        <span class="section-header" @click="collapsed = !collapsed">
            {{ title }}
            <NButton class="collapse-button" text type="primary" v-if="collapsed">
                <template #icon>
                    <NIcon>
                        <ChevronDown />
                    </NIcon>
                </template>
            </NButton>
            <NButton class="collapse-button" text type="error" v-if="!collapsed">
                <template #icon>
                    <NIcon>
                        <ChevronUp />
                    </NIcon>
                </template>
            </NButton>
        </span>
        <div class="divider" />
        <div class="body" v-if="!collapsed">
            <slot />
        </div>

    </div>
</template>

<style lang="scss" scoped>
.section {
    display: flex;
    flex-direction: column;
    gap: 4px;
    width: 100%;
    margin: 15px;

    .section-header {
        color: var(--text-color-3);
        font-family: 'Sarabun';
        font-weight: 600;
        font-size: 12px;
        font-kerning: 0;
        letter-spacing: .5px;
        margin-left: 2px;
        display: flex;
        flex-direction: row;

        .collapse-button {
            margin-left: auto;
            margin-right: 20px;
        }
    }

    .divider {
        height: 2px;
        width: 100%;
        background-color: #1D1D23;
        border-radius: .25rem;
        margin-top: 2px;
    }

    .body {
        display: flex;
        flex-direction: column;
        gap: 8px;
        margin-top: 20px;
        margin-bottom: 35px;
    }


}
</style>