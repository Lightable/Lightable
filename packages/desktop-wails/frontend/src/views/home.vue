<script setup lang="ts">
import { NButton } from 'naive-ui';
import { computed, onBeforeMount } from 'vue';
import { HasUser } from '../../wailsjs/go/app/App';
import { useAppStore } from '../stores/AppStore';

const appStore = useAppStore();
const hasUser = computed(() => appStore.hasUser);
const setHasUser = (bool: boolean) => {
    HasUser(bool);
    appStore.load();
}

onBeforeMount(() => appStore.leftDrawer.show = false);

appStore.setTitle('Home');
</script>


<template>
    <div class="home" v-if="!hasUser">
        <h2 class="h ns">Sign-up for closed beta below</h2>
        <NButton size="large" tertiary type="primary" style="--n-width: 248px" round @click="$router.push('/invite/signup')">
            Sign-up
        </NButton>
        <h2 class="h ns">I have an invite code</h2>
        <NButton size="large" tertiary type="info" style="--n-width: 248px" round @click="$router.push('/signup')">
            Enter invite code
        </NButton>
        <NButton class="subtext" text @click="setHasUser(true)">
            Bring me back to login
        </NButton>
    </div>
    <div class="home" v-else>
        <h2 class="h ns">Please Log In</h2>
        <NButton size="large" tertiary type="info" style="--n-width: 248px" id="login-btn" round @click="$router.push('/login')">
            Log In
        </NButton>
        <NButton class="subtext" text @click="setHasUser(false)">
            I want to register another user
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
    .subtext {
        color: var(--text-color-3);
        font-weight: normal;
        &:hover {
            color: var(--text-color-2);
        }
    }
}
</style>