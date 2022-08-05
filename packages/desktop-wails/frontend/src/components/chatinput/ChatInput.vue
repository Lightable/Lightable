<script setup lang="ts">
import { ref } from 'vue';
import { NIcon, NTooltip } from 'naive-ui';
import { AddCircleOutline as AddCircle, Heart } from '@vicons/ionicons5';
import 'vfonts/Lato.css';

const attribution = ref(true);

const removeAttribution = () => {
    attribution.value = false;
}
</script>


<template>
    <div class="outer-chat">
        <div class="chat-input">
            <div class="attachments">
            </div>
            <div class="inner">
                <div class="actions">
                    <!-- TODO: Add attachment support web/desktop/server -->
                    <NTooltip trigger="hover" v-if="attribution">
                        <template #trigger>
                            <NIcon color="var(--error-color)" :size="24" @click="removeAttribution">
                                <Heart />
                            </NIcon>
                        </template>
                        Thanks for trying this out!
                    </NTooltip>
                </div>
                <div class="input" contenteditable data-placeholder="Chat with ______" />
            </div>
        </div>
    </div>
</template>

<style lang="scss" scoped>
.outer-chat {
    width: calc(100vw - 284px);
    padding-bottom: 16px;
    padding-top: 16px;
    padding-right: 16px;
    padding-left: 16px;
    background-color: #0c0b0b;
    border-top-right-radius: 8px;
    border-top-left-radius: 8px;
    // min-height: 40px;
    // max-height: 400px;

    .chat-input {
        width: 100%;
        display: flex;
        // min-height: 40px;
        // max-height: 400px;

        .attachments {}

        .inner {
            display: inline-flex;
            flex-direction: row;
            justify-content: center;
            align-items: center;
            gap: 8px;
            width: 100%;
            // min-height: 40px;
            // max-height: 400px;

            .input {
                display: flex;
                flex-direction: column;
                justify-content: center;
                align-items: flex-start;
                width: 100%;
                // max-height: 50px;
                transition: all 250ms ease;
                border: none;
                outline: none;
                line-height: 1.5;
                background-color: #111111;
                min-height: 40px;
                padding-left: 14px;
                padding-right: 14px;
                font-size: 18px;
                font-weight: 380;
                caret-color: var(--info-color);
                font-family: v-sans, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol";
                color: #949494;
                border-radius: 4px;

                &:hover {
                    border: none;
                }

                &:empty:before {
                    content: attr(data-placeholder);
                }
            }

            .actions {
                display: flex;
                flex-direction: row;
                justify-content: center;
                align-items: center;
            }
        }
    }
}
</style>