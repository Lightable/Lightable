<script setup lang="ts">
import { onMounted, PropType, ref } from 'vue';
import { mocks } from '../../../wailsjs/go/models';
import { NAvatar } from 'naive-ui';
import { GetAvatar } from '../../../wailsjs/go/client/Client';
import { useAppStore } from '../../stores/AppStore';

const appStore = useAppStore();
const avatar = ref();
const props = defineProps({
    friend: Object as PropType<mocks.PublicUser>
});

onMounted(async () => {
    if (props.friend) {
        avatar.value = await GetAvatar(props.friend.id, 64)
    }
})
appStore.events.on('user|update', async (u: mocks.PublicUser) => {
  if (u.avatar) {
    avatar.value = await GetAvatar(props.friend!!.id, 64)
  }
})
</script>

<template>
    <div class="friend-card">
        <div class="card-left">
            <NAvatar :src="avatar" :size="48" round style="width: 48px; height: 48px;"/>
            <span class="name">{{friend?.name}}</span>
        </div>
        <div class="card-right">
            <slot/>
        </div>
    </div>
</template>


<style lang="scss" scoped>
.friend-card {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    background-color: #141419;
    padding: 12px;
    border-radius: .5rem;
    max-width: 100%;
    min-width: 10%;
    .card-left {
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 16px;
        font-size: 1.8em;
        font-weight: bold;
        
    }
    .card-right {
        display: flex;
        flex-direction: row;
    }
}
</style>