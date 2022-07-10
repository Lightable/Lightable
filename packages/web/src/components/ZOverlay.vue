<script setup lang="ts">
import { ref, defineEmits } from 'vue';

const emit = defineEmits(['overlay-click']);
const clicked = ref(false);

const props = defineProps({
    overlayClass: String,
    disabled: Boolean
});

const overlayClicked = () => {
    if (!props.disabled) {
        clicked.value = true;
        emit('overlay-click');
    }
}
</script>



<template>
    <div class="overlay-wrap">
        <div class="top">
            <slot name="header" />
        </div>
        <div :class="`overlay ${overlayClass}`" @click="overlayClicked" :disabled="disabled" :load-state="clicked">

            <div class="content">
                <slot />
            </div>
            <div class="container default" v-if="!clicked && !disabled">
                <slot name="hover" />
            </div>
            <div class="container disabled-pass-through" v-if="disabled" />
            <div class="container load-pass-through" v-if="clicked">
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

        &[load-state='true'] {
            position: relative;

            .load-pass-through {
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