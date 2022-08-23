<script setup lang="ts">
import SearchGlobal from '../../../../components/search/SearchGlobal.vue';

</script>

<template>
    <div class="preview">
        <SearchGlobal/>
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