<script setup lang="ts">
import { computed, ref } from 'vue';
import { useAppStore } from '../../stores/AppStore';
import { useClientStore } from '../../stores/ClientStore';
import ZPublicPane from '../../components/account/ZProfilePane.vue';
let appStore = useAppStore();
let clientStore = useClientStore();
let loading = ref(true);
clientStore.lite.getFriends().then(() => {
    loading.value = false;
});
let account = computed(() => clientStore.lite.user ? clientStore.lite.user : undefined);
console.log(account);
</script>

<template>
    <div class="root">
        <ZPublicPane :user="account" v-if="account?.enabled" :loading="loading" />
        <div class="not-allowed" v-if="!account?.enabled">
            <h2 style="color: var(--error-color)">Account is disabled</h2>
        </div>
    </div>
</template>


<style lang="scss">
</style>