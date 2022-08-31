<script setup lang="ts">
import { ref, computed } from 'vue';
import { useAppStore } from '../../../../../stores/AppStore';
import { NButton, useMessage } from 'naive-ui';
import { RequestFriend, GetRelations } from '../../../../../../wailsjs/go/client/RelationshipManager';
import AppSection from '../../../../../components/app/AppSection.vue';
import RequestCard from '../../../../../components/friends/RequestCard.vue';
const appStore = useAppStore();
const toast = useMessage();
appStore.setTitle('Friends • Requests');


const requests = computed(() => appStore.relationships.requests);
// const addFriendModal = ref({
//     show: false,
//     friend: '' as string,
//     loading: false as boolean,
//     cb: async () => {
//         let modal = addFriendModal.value;
//         modal.loading = true;
//         try {
//             let req = await RequestFriend(modal.friend);
//             modal.loading = false;
//             toast.success(`Got it! We'll let ${req.name} know about your potential friendship ASAP`);
//             GetRelations().then(r => {
//                 appStore.relationships = r;
//             });
//             return
//         } catch (e) {
//             modal.loading = false;
//             toast.warning(e as string);
//         }
//     }
// });
const addFriendState = ref({
    loading: false as boolean,
    friend: '' as string,
    cb: async () => {
        let state = addFriendState.value;
        state.loading = true;
        try {
            let req = await RequestFriend(state.friend);
            state.loading = false;
            toast.success(`Got it! We'll let ${req.name} know about your potentional friendship ASAP`);
            let r = await GetRelations();
            appStore.relationships = r;
            return
        } catch (e) {
            state.loading = false;
            toast.warning(e as string);
            throw e;
        }
    }
})

const onKeyEnterPressed = (e: KeyboardEvent) => {
    if (e.key == "Enter") {
        addFriendState.value.cb();
    }
}
</script>

<template>
    <div class="header ns">
        <h3>Add Friend</h3>
        <span class="subtitle">Add a friend with thier username (ex: Brys0)</span>
        <div class="outer-input">
            <input class="input" placeholder="Add friend by username" v-model="addFriendState.friend" @keypress="onKeyEnterPressed"/>
            <NButton secondary type="info" @click="addFriendState.cb" :loading="addFriendState.loading">Add Friend</NButton>
        </div>
    </div>
    <AppSection title="REQUESTS">
        <span class="req" v-for="(req, index) in requests" v-bind:key="index" v-if="requests.length !== 0">
            <RequestCard :user="req" />
        </span>
        <h3 style="color: var(--text-color-2); text-align: center;" class="center" v-else>Could not find any requests...<br>At least you're not left on pending 😎</h3>
    </AppSection>
</template>


<style lang="scss" scoped>
.header {
    margin: 20px;

    .subtitle {
        color: var(--text-color-3);
    }

    .outer-input {
        display: flex;
        flex-direction: row;
        padding: 5px;
        background-color: #1f1f22;
        border-radius: 3px;

        .input {
            all: unset;
            padding: 4px;
            background-color: transparent;
            border: none;
            color: var(--text-color-2);
            width: 100%;
            caret-color: var(--info-color);

            &:hover {
                background-color: transparent;
                border: none;
            }

            &:focus {
                border: none;
            }
        }
    }
}
</style>