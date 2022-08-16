<script setup lang="ts">
import { NInput, NSelect } from 'naive-ui';
import ChatInput from '../../../../components/chat/ChatInput.vue';
import { ChatInputState } from '../../../../composable/ChatInput';
import { ref } from 'vue';

const message = ref('This is a message');
const statefulValue = ref(ChatInputState.LOADING);

</script>


<template>
    <div class="preview">
        <h2 :style="{ marginLeft: '20px' }">Chat Input preview</h2>
        <div class="card input">
            <h3>Controls</h3>
            <NInput placeholder="Stateful Message" type="text" v-model:value="message" />
            <NSelect :options="[{ label: 'Loading', value: ChatInputState.LOADING }, { label: 'Failed', value: ChatInputState.FAILED }, { label: 'Warning', value: ChatInputState.WARNING }]" v-model:value="statefulValue" :default-value="statefulValue" />
        </div>
        <ChatInput :state="statefulValue" :message="message"/>
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
    .card {
        padding: 8px;
    }
}
</style>