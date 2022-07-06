<script setup lang="ts">
import { ref } from 'vue';

const clicked = ref(false);

const props = defineProps({
    overlayClass: String,
    disabled: Boolean
})
</script>



<template>
    <div class="overlay-wrap">
        <div class="top">
            <slot name="header" />
        </div>
        <div :class="`overlay ${overlayClass}`" @click="() => {(!disabled)  ? () => {clicked = true; $emit('overlay-click')} : () => {} }" :disabled="disabled">

            <div class="content">
                <slot />
            </div>
            <div class="container" v-if="!clicked && !disabled">
                <slot name="hover" />
            </div>
            <div class="container" v-if="disabled"/>
            <div class="container" v-else-if="clicked && !disabled">
                <slot name="loading" />
            </div>
        </div>
    </div>
</template>

<style lang="scss" scoped>
.overlay-wrap {
    .top {
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
    }

    .overlay {
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        cursor: pointer;

        .container {
            display: none;
        }


        &[disabled="true"] {
              position: relative;
            .content {
                position: relative;
                opacity: 0.4;
                transition: opacity .2s ease-in-out;
            }
        }
        &:hover,
        &:focus,
        &:active {

            position: relative;

            .container {
                display: flex;
                flex-direction: column;
                justify-content: center;
                align-items: center;
                position: absolute;
                top: 50%;
                left: 50%;
                transform: translateX(-50%) translateY(-50%);
            }

            .content {
                position: relative;
                opacity: 0.4;
                transition: opacity .2s ease-in-out;

            }

        }


    }
}
</style>