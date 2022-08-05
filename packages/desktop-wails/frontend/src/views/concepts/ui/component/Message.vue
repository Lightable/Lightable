<script setup lang="ts">
import Message from '../../../../components/message/Message.vue';

</script>

<template>
    <div class="preview">
        <h3>Message Preview</h3>
        <Message :is-preview="true"/>
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