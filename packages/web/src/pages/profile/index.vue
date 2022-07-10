<script setup lang="ts">
import { NResult } from 'naive-ui';
</script>

<template>
    <div class="profile">
        <div class="unavalible">
            <NResult status="error">
                <template #default>
                    <span class="result-status">This profile is not available</span>
                </template>
                <template #footer>
                    Cope
                </template>
            </NResult>
        </div>
    </div>
</template>

<style lang="scss" scoped>
.profile {
    display: flex;
    justify-content: center;
    align-items: center;
    max-height: 100%;
    min-height: calc(100vh - 46.4px);

    .unavalible {
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        .result-status {
            font-size: 24px;
        }
    }
}
</style>