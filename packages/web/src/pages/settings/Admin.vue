<script setup lang="ts">
import { ref } from 'vue';
import ZAdminPane from '../../components/account/ZAdminPane.vue';
import { useClientStore } from '../../stores/ClientStore';

let clientStore = useClientStore();
let enabledUsersLoading = ref(true);
let disabledUsersLoading = ref(true);
clientStore.lite.getEnabledUsers(null, null, null).then(() => {
    enabledUsersLoading.value = false
    clientStore.lite.getDisabledUsers().then(() => {
        disabledUsersLoading.value = false;
    });
});
</script>

<template>
    <div class="root">
        <ZAdminPane :enabled-users-loading="enabledUsersLoading" :disabled-users-loading="disabledUsersLoading"/>
    </div>
</template>


<script lang="ts">

export default {
    beforeRouteLeave() {
        let clientStore = useClientStore();
        clientStore.enabledUsers.clear();
        clientStore.disabledUsers.clear();
    }
}
</script>