<script setup lang="ts">
import { computed, ref } from 'vue';
import { useAppStore } from '../../stores/AppStore';
import { useClientStore } from '../../stores/ClientStore';
import ZLeftAccountPane from './ZLeftAccountPane.vue';
import ZRightAccountPane from './ZRightAccountPane.vue';
import {NElement} from 'naive-ui';
let appStore = useAppStore();
let clientStore = useClientStore();
let loading = ref(true);
clientStore.lite.getFriends().then(() => {
    loading.value = false;
});
let account = computed(() => appStore.account ? appStore.account : undefined);

</script>

<template>
    <NElement type="div" class="account">
        <ZLeftAccountPane :user="account" v-if="account?.enabled" :loading="loading"/>
        <ZRightAccountPane :user="account" class="space" v-if="account?.enabled" :loading="loading"/>
        <div class="not-allowed" v-if="!account?.enabled">
            <h2 style="color: var(--error-color)">Account is disabled</h2>
        </div>
    </NElement>
</template>

<style lang="scss" scoped>
.account {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    height: 90vh;
    .space {
        margin-left: 120px;
    }
}

@media only screen and(max-width: 852px) {
    .account {
        flex-direction: column;
        justify-content: center;
        align-items: center;
        .space {
            margin-top: 50px;
            margin-left: 0;
        }
    }
}
</style>