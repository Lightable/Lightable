<template>
    <div class="sidebar-item" @click="(path) ? $router.push(path!!) : ''">
        <slot name="icon"></slot>
        <span class="item-text"><slot name="text"></slot></span>
    </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
    props: {
        path: String
    },
});
</script>


<style lang="scss" scoped>
.sidebar-item {
    width: 85%;
    background-color: var(--sb);
    flex-direction: row;
    justify-content: left;
    align-items: center;
    vertical-align: middle;
    display: flex;
    padding: 8px;
    border-radius: 8px;
    color: var(--white);
    cursor: pointer;
    transition: 150ms ease;
    margin-top: 8px;
    .item-text {
       margin-left: 6px;
    }
    &:hover {
        filter: brightness(120%);
    }

}
</style>