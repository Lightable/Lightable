<script setup lang="ts">
import ZAccount from '../components/account/ZAccount.vue';
</script>

<template>
    <div class="settings-page">
        <ZAccount />
    </div>
</template>



<style lang="scss" scoped>
.settings-page {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    height: calc(100vh - 80px);
}

.bottom {
    margin-top: auto;
}
</style>