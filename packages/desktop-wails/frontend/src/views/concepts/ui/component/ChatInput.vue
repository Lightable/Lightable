<script setup lang="ts">
import ChatInputVue from '../../../../components/chatinput/ChatInput.vue.js';
import ChatInput from '../../../../components/chatinput/ChatInput.vue';
</script>


<template>
    <div class="preview">
        <h2 :style="{marginLeft: '20px'}">Chat Input preview</h2>
        <ChatInput />
    </div>
</template>

<style lang="scss" scoped>
.preview {
    width: 100%;
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    align-items: flex-start;
    height: inherit;
    gap: 16px;
}
</style>