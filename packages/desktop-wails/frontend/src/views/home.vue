<script setup lang="ts">
import { NButton } from 'naive-ui';
import { SetAPI } from '../../wailsjs/go/client/HttpClient';

SetAPI('localhost:8080')
</script>


<template>
    <div class="home">
        <h2 class="h ns">Sign-up for closed beta below</h2>
        <NButton size="large" tertiary type="primary" style="--n-width: 248px" round @click="$router.push('/signup')">
            Sign-up
        </NButton>
        <h2 class="h ns">I have an invite code</h2>
        <NButton size="large" tertiary type="info" style="--n-width: 248px" round>
            Enter invite code
        </NButton>
    </div>
</template>


<style lang="scss" scoped>
.home {
    display: flex;
    height: inherit;
    width: 100%;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    gap: 32px;
    .h {
        color: var(--text-color-1);
    }
}
</style>