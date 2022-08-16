<script setup lang="ts">
import { onMounted, PropType, ref } from 'vue';
import { NIcon, NTooltip, NSpin } from 'naive-ui';
import { AddCircleOutline as AddCircle, Heart, CloseOutline as Close, WarningOutline as Warning } from '@vicons/ionicons5';
import { ChatInputState } from '../../composable/ChatInput';
import 'vfonts/Lato.css';
import { mocks } from '../../../wailsjs/go/models.js';
const attribution = ref(true);
const contentToSend = ref('');
let src: HTMLElement;
const removeAttribution = () => {
    attribution.value = false;
}
onMounted(() => {
    src = document.getElementById('chat-input')!!
});
const onKey = (e: KeyboardEvent) => {
    let shiftMod = e.getModifierState('Shift');
    if (e.key == 'Enter' && shiftMod) {
        src!!.style.height = src.scrollHeight + 24 + 'px';
    } else if (e.key == 'Enter' && !shiftMod) {
        e.preventDefault();
        src!!.style.height = src!!.style.minHeight;
        contentToSend.value = '';
    }
}
const backspaceTrigger = (e: KeyboardEvent) => {
    let lines = contentToSend.value.split('\n');
    let last = lines[lines.length - 1];
    if (lines[0] == '') {
        src.style.height = `30px`;
    }
    if (e.key == 'Backspace' && last == '') {
        src!!.style.height = src.scrollHeight - 24 + 'px';
    }
}
const backspaceKeyUP = (e: KeyboardEvent) => {
    if (e.key == 'Backspace' && contentToSend.value == '') {
        src!!.style.height = '30px';
    }
}
const onPaste = () => {
    // Wait for value to filled
    window.setTimeout(() => {
        src!!.style.height = src.scrollHeight + 'px';
    }, 1)
}
let props = defineProps({
    state: Number as PropType<ChatInputState>,
    message: String,
    typingUsers: Array as PropType<Array<String>>,
    recipient: Object as PropType<mocks.PrivateUser>,
    isPreview: Boolean
});
console.log(props);
</script>


<template>
    <!-- Debug -->
    <!-- {{ props }} -->
    <!-- Debug -->
    <div class="outer-chat">
        <div class="states ns" v-if="isPreview || state">
            <div class="state typing" v-if="isPreview || typingUsers !== undefined">
                <span class="user" v-for="(user, index) in typingUsers" v-bind:key="index">{{ user + (typingUsers!!.length > 1 ? ',' : '') }}</span>
                is typing...
            </div>
            <div class="state sending" v-if="isPreview || state === ChatInputState.LOADING">
                <NSpin :size="24" />
                {{ message }}
            </div>
            <div class="state failed" v-if="isPreview || state === ChatInputState.FAILED">
                <NIcon :size="24" color="var(--error-color)">
                    <Close />
                </NIcon>
                {{ message }}
            </div>
            <div class="state warning" v-if="isPreview || state === ChatInputState.WARNING">
                <NIcon :size="24" color="var(--warning-color)">
                    <Warning />
                </NIcon>
                {{ message }}
            </div>
        </div>
        <div class="chat-input">
            <div class="attachments">
            </div>
            <div class="inner">
                <div class="actions">
                    <!-- TODO: Add attachment support web/desktop/server -->
                    <NTooltip trigger="hover" v-if="attribution">
                        <template #trigger>
                            <NIcon color="var(--error-color-suppl)" :size="24" @click="removeAttribution">
                                <Heart />
                            </NIcon>
                        </template>
                        Thanks for trying this out!
                    </NTooltip>
                </div>
                <textarea class="input" :placeholder="(isPreview || recipient == undefined) ? 'Chat with ______' : `Chat with ${recipient?.name}`" @keypress="onKey" id="chat-input" @keydown="backspaceTrigger" @keyup="backspaceKeyUP" v-model="contentToSend" @paste="onPaste" />
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
    display: flex;
    flex-direction: column;
    transition: all 150ms ease;
    .states {
        display: flex;
        flex-direction: column;
        justify-content: flex-start;
        align-items: flex-start;
        margin-bottom: 16px;
        gap: 8px;
        .state {
            display: flex;
            flex-direction: row;
            justify-content: flex-start;
            align-items: center;
            gap: 18px;
            color: var(--text-color-3);
        }
    }
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
                transition: all 250ms ease;
                // max-height: 50px;
                border: none;
                outline: none;
                line-height: 1.5;
                background-color: #111111;
                min-height: 34px;
                max-height: 45vh;
                height: 34px;
                padding-left: 14px;
                padding-right: 14px;
                font-size: 18px;
                caret-color: var(--info-color);
                font-family: "Open Sans Light";
                color: var(--text-color-2);
                border-radius: 4px;
                resize: none;
                letter-spacing: 1px;
                box-sizing: border-box;
                &::-webkit-scrollbar {
                    display: none;
                }
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