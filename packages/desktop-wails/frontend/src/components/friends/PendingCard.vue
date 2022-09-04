<script setup lang="ts">
import { ref, PropType } from 'vue';
import { NIcon, NButton, NSpin } from 'naive-ui';
import { Close } from '@vicons/ionicons5';
import { CheckRound as Check } from '@vicons/material';
import { mocks } from '../../../wailsjs/go/models';
import { AcceptFriend, GetRelations } from '../../../wailsjs/go/client/RelationshipManager';
import {useAppStore} from '../../stores/AppStore'
import RelationshipCard from './RelationshipCard.vue';

const appStore = useAppStore();
const props = defineProps({
    user: Object as PropType<mocks.PublicUser>
})

const pendingState = ref({
    loading: false,

})


const deny = () => {
    pendingState.value.loading = true;
}

const accept = () => {
    pendingState.value.loading = true;
    AcceptFriend(props.user?.name!!).then(_ => {
        GetRelations().then(r => {
            console.log(r)
            appStore.relationships = r;
        })
    }).catch(e => {
        console.log(e);
    })
}
</script>
    
<template>
    <div class="outer">
        <RelationshipCard :friend="user">
            <div class="actions">
                <button class="btn" @click="deny" v-if="!pendingState.loading">
                    <NIcon :size="48" color="var(--error-color)" class="icon">
                        <Close />
                    </NIcon>
                </button>
                <button class="btn" @click="accept" v-if="!pendingState.loading">
                    <NIcon :size="48" color="var(--success-color)" class="icon">
                        <Check />
                    </NIcon>
                </button>
                <NSpin :size="42" v-if="pendingState.loading" class="spinner" />
            </div>
        </RelationshipCard>
    </div>
</template>
    
    
<style lang="scss" scoped>
.outer {
    max-width: 95%;
    min-width: 10%;
}

.actions {
    display: flex;
    flex-direction: row;
    gap: 32px;
    margin-right: 20px;
    justify-content: center;
    align-items: center;

    .btn {
        all: unset;
        display: flex;
        padding: 2px;
        height: 48px;
        width: 48px;
        border-radius: 50%;
        justify-content: center;
        align-items: center;
        cursor: pointer;
        background-color: #121217;
        transition: all .3s ease-in-out;

        &:hover {
            background-color: #101014;
            transform: scale(1.15);
        }
    }
}

.spinner {
    animation: rainbow 5s linear infinite;
}

</style>