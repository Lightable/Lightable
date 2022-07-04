<script lang="ts" setup>
</script>

<template>
        <div class="pane-right">
            <slot name="default"/>
        </div>
</template>

<style lang="scss" scoped>
    .pane-right {
        box-shadow: rgba(0, 0, 0, 0.19) 0px 10px 20px, rgba(0, 0, 0, 0.23) 0px 6px 6px;
        transition: 0.3s;
        border-radius: 8px;
        width: 100%;
        margin-top: 50px;
        padding: 8px;

        &:hover {
            box-shadow: rgba(0, 0, 0, 0.25) 0px 14px 28px, rgba(0, 0, 0, 0.22) 0px 10px 10px;
        }
        
    }

@media only screen and(max-width: 852px) {
        .pane-right {
            justify-content: center;
            align-items: center;
            width: 80%;
        }
    }
</style>