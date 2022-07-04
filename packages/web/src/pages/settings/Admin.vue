<script setup lang="ts">
import { ref } from 'vue';
import ZAdminPane from '../../components/account/ZAdminPane.vue';
import { useClientStore } from '../../stores/ClientStore';

let clientStore = useClientStore();

let enabledUsersLoading = ref(true);
let disabledUsersLoading = ref(true);
clientStore.lite.getEnabledUsers().then(() => {
    enabledUsersLoading.value = false
    clientStore.lite.getDisabledUsers().then(() => {
        disabledUsersLoading.value = false;
    });
})
</script>

<template>
    <div class="root">
        <ZAdminPane :loading="true"/>
    </div>
</template>