<script setup lang="ts">
import { computed, ref } from 'vue';
import { useAppStore } from '../../stores/AppStore';
import { useClientStore } from '../../stores/ClientStore';
import { NElement, useDialog } from 'naive-ui';
import { useConfettiStore } from '../../stores/ConfettiStore';
import ZLeftAccountPane from '../../components/account/ZLeftAccountPane.vue';
let appStore = useAppStore();
let clientStore = useClientStore();
let account = computed(() => appStore.account ? appStore.account : undefined);
let loading = ref(true);
let confettiStore = useConfettiStore();
let dialog = useDialog();
clientStore.lite.$getSelf().then(() => {
    loading.value = false;
    if (!account.value?.enabled) {
        confettiStore.create(
            {
                particleCount: 200,
                ticks: 50,
                origin: { y: 0.6 },
                spread: 70
            });
        dialog.create(
            {
                type: 'error',
                title: 'That didn\'t work',
                content: 'Congrats fucko your account is disabled',
                positiveText: 'Ok, shut up'
            }
        )
    }

});
</script>

<template>
    <div class="settings-page">
        <NElement type="div" class="account">
            <ZLeftAccountPane :user="account" :loading="loading" />
            <div class="profile-right-pane space">
                <router-view v-slot="{ Component }">
                    <component :is="Component" />
                </router-view>
            </div>
        </NElement>
    </div>
</template>



<style lang="scss" scoped>
.account {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;

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
            margin-top: 0px;
            margin-left: 0;
        }
    }

}

.settings-page {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    max-height: 100%;
    overflow-y: scroll;
    min-height: calc(100vh - 46.4px);
}

.bottom {
    margin-top: auto;
}
</style>