<script setup lang="ts">
import { ref } from 'vue';
import ZAdminPane from '../../components/account/ZAdminPane.vue';
import { useClientStore } from '../../stores/ClientStore';

let clientStore = useClientStore();
let enabledUsersLoading = ref(true);
let disabledUsersLoading = ref(true);
let invitedUsersLoading = ref(true);
clientStore.lite.getEnabledUsers(null, null).then(() => {
    enabledUsersLoading.value = false
    clientStore.lite.getDisabledUsers(null, null).then(() => {
        disabledUsersLoading.value = false;
        clientStore.lite.getInvitedUsers().then(() => {
            invitedUsersLoading.value = false;
        });
    });
});
</script>

<template>
    <div class="root">
        <ZAdminPane :enabled-users-loading="enabledUsersLoading" @search-enabled="enabledUsersLoading = true" @stop-search-enabled="enabledUsersLoading = false" @search-disabled="disabledUsersLoading = true" @stop-search-disabled="disabledUsersLoading = false" :disabled-users-loading="disabledUsersLoading" :invited-users-loading="invitedUsersLoading"/>
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