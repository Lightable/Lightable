<script setup lang="ts">
import { defineAsyncComponent } from 'vue';
const Element = defineAsyncComponent({
    loader: () => import('naive-ui/lib/element/src/Element')
});
const Tooltip = defineAsyncComponent({
    loader: () => import('naive-ui/lib/tooltip/src/Tooltip')
});
</script>

<template>
    <Element tag="div" class="admin-display">
        <div class="header">
            <h3 class="header-text">Meet the
                <Tooltip trigger="hover">
                    <template #trigger>
                        <s class="the-truth">social rejects</s>
                    </template>
                    It's the <b>honest</b> truth
                </Tooltip>
                lovely people who run this dump
            </h3>
        </div>
    </Element>
</template>

<style lang="scss" scoped>
.admin-display {
    .header {
        color: var(--text-color-2);

        .header-text {
            .the-truth {
                color: var(--text-color-3);
            }
        }
    }
}
</style>