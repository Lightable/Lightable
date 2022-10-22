<script setup lang="ts">
import {computed, onMounted, PropType, Ref, ref} from 'vue';
import ChatHeader from './ChatHeader.vue';
import ChatInput from './ChatInput.vue';
import {NAvatar, NScrollbar, NAlert, NSpin} from 'naive-ui';
import Message from '../message/Message.vue';
import {useAppStore} from '../../stores/AppStore';
import {mocks} from '../../../wailsjs/go/models';
import {EventsOn, EventsOff} from '../../../wailsjs/runtime';
import {GetAvatar, SendTyping} from '../../../wailsjs/go/client/Client';
import {RelationshipStatus} from '../../stores/AppStore'
import {GetMessagesFromDM} from '../../../wailsjs/go/client/ChannelManager';

const appStore = useAppStore();

const props = defineProps({
  channel: Object as PropType<mocks.Channel>
});

const avatar = ref() as Ref<string | undefined>
const getUser = (id: string | undefined): mocks.PublicUser | mocks.PrivateUser | undefined => {
  const initialFind = appStore.users.find(u => u.id === id);
  if (!initialFind && appStore.user!!.id == id) return appStore.user!!;
  return initialFind
}
const chatContainer = ref() as Ref<HTMLElement | undefined>;
const atBottom = ref(true);
const scroll = ref({
  offsetHeight: 0,
  scrollTop: 0,
  scrollHeight: 0
})
const typing = ref([]) as Ref<Array<string>>
const user = computed(() => getUser(props.channel?.id))
const status = computed(() => appStore.getUserTypeRelation(props.channel?.id))

onMounted(async () => {
  if (props.channel) {
    props.channel.messages = await GetMessagesFromDM(props.channel!!.id, null, null);
    console.log(props.channel.messages);
    avatar.value = await GetAvatar(props.channel?.id, 64)
    chatContainer.value!!.scrollTo({
      top: 20000
    })
  }
})
const sendMessage = (cnt: string) => {
  props.channel?.messages.push({
    content: cnt,
    type: 0,
    // @ts-ignore
    channel: props.channel.id,
    // @ts-ignore
    author: appStore.user!!.id,
    created: Date.now(),
    system: false,
  })
  setTimeout(() => {
    chatContainer.value!!.scrollTo({
      top: (scroll.value.scrollHeight - scroll.value.offsetHeight) + 500
    })
  }, 5)
}

function destroy(arr: Array<any>, val: any) {
  for (let i = 0; i < arr.length; i++) if (arr[i] === val) arr.splice(i, 1);
  return arr;
}

const startTyping = (e: string) => {
  typing.value.push(e)
  SendTyping(props.channel.id!!);
}

EventsOn('ws:read:server|dm-message', ({channel, message}) => {
  props?.channel?.messages.push(message)
  setTimeout(() => {
    if (atBottom.value) {
      chatContainer.value!!.scrollTo({
        top: (scroll.value.scrollHeight - scroll.value.offsetHeight) + 500
      })
    }
  }, 5)
})

EventsOn("ws:read:channel|typing", (ut: mocks.PublicUser) => {
  if (typing.value.indexOf(ut.name) != -1) return
  typing.value.push(ut.name)
  setTimeout(() => {
    typing.value = destroy(typing.value, ut.name)
  }, 2000)
})

const chatOnScroll = (d: any) => {
  const src = d.srcElement;
  const {offsetHeight, scrollTop, scrollHeight} = src;
  scroll.value = {offsetHeight, scrollTop, scrollHeight}
  atBottom.value = offsetHeight + scrollTop >= scrollHeight;
}
</script>

<template>
  <div class="chat">
    <ChatHeader :title="(user) ? user?.name : 'Unknown'">
      <template #icon>
        <NAvatar round :src="avatar" @click="appStore.openUserProfile(user.id)" class="avatar"/>
      </template>
    </ChatHeader>
    <NScrollbar style="height: 100%; width: 100%; padding-bottom: 5px; padding-top: 5px;" trigger="hover"
                ref="chatContainer" @scroll="chatOnScroll">
      <div class="pad">
        <div class="infinite-load ns">
          <NAlert>
            <span style="text-align: center; display: block;">Messages are loading</span>
          </NAlert>
        </div>
        <div class="errors ns">
          <NAlert id="request" title="Can't send messages to this user" type="error"
                  v-if="!status || typeof status === 'number' && status != RelationshipStatus.FRIEND">
            <span class="error" v-if="status === RelationshipStatus.REQUEST">This user has yet to accept your friend request</span>
            <span class="error" v-if="status === RelationshipStatus.PENDING">You still need to accept this user's friend request!</span>
            <span class="error" v-if="status === RelationshipStatus.UNKNOWN">We're not sure who, or what you're trying to message :/</span>
            <span class="error" v-else-if="!status">How did you get here? How did we allow you to get here? We'll never know 🤫</span>
          </NAlert>
        </div>

        <Message v-for="(item, _) in channel.messages" v-if="channel?.messages" :author="getUser(item?.author as any)"
                 :content="item.content" :system="item.system"/>
      </div>
    </NScrollbar>
    <ChatInput @message="sendMessage" :self="appStore.user as any" :recipient="user" @typing="(e) => startTyping(e!!)"
               @typing-end="(e: string) => { typing = destroy(typing, e) }" :typing-users="typing"/>
  </div>
</template>

<style lang="scss" scoped>
.chat {
  height: 100vh;
  width: 100%;
  top: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;

  .pad {
    padding: 8px;
    margin-top: 50px;

    .infinite-load,
    .errors {
      color: var(--text-color-3);
      text-align: center;
    }
  }
}

.avatar {
  cursor: pointer;
}
</style>