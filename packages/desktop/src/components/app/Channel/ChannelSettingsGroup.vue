<template>
    <div class="settings-group">
        <div class="title">
            <h2 class="__text"><slot name="title"/></h2>
        </div>
        <div class="group-data">
            <slot name="body"/>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from "vue"
export default defineComponent({
    name: 'ChannelSettingsGroup',
});
</script>

<style lang="scss" scoped>
.settings-group {
    .title {
        padding: 8px;
        .__text {
            font-size: calc(var(--font-size) + 12px);
            color: var(--purple);
        }
    }
    .group-data {
         padding: 10px;
    }
}
</style>