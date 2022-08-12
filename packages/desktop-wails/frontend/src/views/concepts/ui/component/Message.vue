<script setup lang="ts">
import Message from '../../../../components/message/Message.vue';
import { NInputGroup, NInput } from 'naive-ui';
import { ref } from 'vue';

let content = ref('Welcome to lightable');

let user = ref({
    name: 'You',
    avatar: undefined as undefined | string
});
</script>

<template>
    <div class="preview">
        <h3>Message Preview</h3>
        <Message :content="content" :author="user"/>
        <div class="controls">
            <h3>Controls</h3>
            <div class="card content">
                <h4>Content</h4>
                <NInput placeholder="Content" type="text" @update-value="(v: string) => content = v" :default-value="content"/>
            </div>
            <div class="card user">
                <h4>User</h4>
                <NInputGroup>
                    <NInput placeholder="Avatar" type="text" @update-value="(v: string) => user.avatar = v" :default-value="user.avatar"/>
                    <NInput placeholder="Name" type="text" @update-value="(v: string) => user.name = v" :default-value="user.name"/>
                </NInputGroup>
            </div>
        </div>
    </div>
</template>


<style lang="scss" scoped>
.preview {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    height: inherit;
    width: 100%;
    gap: 16px;
}
</style>