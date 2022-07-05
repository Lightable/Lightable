<script lang="ts" setup>
</script>

<template>
        <div class="pane-right">
            <slot name="default"/>
        </div>
</template>

<style lang="scss" scoped>
    .pane-right {
        transition: 0.3s;
        border-radius: 8px;
        width: 100%;
        margin-top: 10px;
        padding: 8px;
    }

@media only screen and(max-width: 852px) {
        .pane-right {
            justify-content: center;
            align-items: center;
            width: 93%;
        }
    }
</style>