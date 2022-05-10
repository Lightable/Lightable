<template>
  <div class="channel-messages">
    <Notice class="notice" v-if="!messageExperiment"> Messages are not yet released </Notice>
    <div class="classic __messages" v-if="UIType == 'Classic'"></div>
    <div class="sms __messages" v-if="UIType == 'SMS'">
      <SMSMessage
        v-for="(message, index) in messages.values()"
        :message="message"
        :prevchain="messages.$hasChain(message._id)"
        :self="message.author_id == client.self?._id"
        v-bind:key="index"
        v-if="messageExperiment"
      />
    </div>
    <button
      class="db"
      dbt="success"
      @click="
        () => {
          createMessage(10);
        }
      "
      v-if="messageExperiment"
    >
      Create Messages (10)
    </button>
    <button
      class="db"
      dbt="success"
      @click="
        () => {
          createSelfMessages();
        }
      "
      v-if="messageExperiment"
    >
      Create Self Messages
    </button>
  </div>
</template>

<script lang="ts">
import {defineComponent, PropType, computed} from 'vue';
import Messages, {UIMessageType} from '@/lib/structures/Messages';
import {ClientStore} from '@/stores/ClientStore';
import {Client} from '@/lib/Client';
import SMSMessage from './_message/SMSMessage.vue';
import Notice from './Notice.vue';
import { AppStore } from '@/stores/AppStore';
import { User } from '@/lib/structures/Users';
export default defineComponent({
  name: 'ChannelMessages',
  setup() {
    let clientStore = ClientStore();
    let appStore = AppStore();
    return {
      client: clientStore.client!! as Client,
      messageExperiment: computed(() => appStore.experiments.get('messages'))
    };
  },
  props: {
    messages: {
      type: Object as PropType<Messages>,
      required: true,
    },
    externaluser: {
      type: Object as PropType<User>,
      required: true
    },
    UIType: {
      type: String as PropType<UIMessageType>,
      required: false,
      default: () => {
        return 'SMS';
      },
    },
  },
  components: {SMSMessage, Notice},
  methods: {
    createMessage(amount: number = 1) {
      this.client.users.get(this.externaluser._id)?.messages?.$makeRandom(amount);
    },
    createSelfMessages() {
      this.messages.create({
        content: 'Test message self',
        created: {
          sec: 11111,
          milli: 92,
        },
        id: this.messages.randomString(10),
        attachments: null,
        mentions: null,
        edited: null,
        user: this.client.self?.asIUser()!!,
      });
    },
  },
});
</script>

<style lang="scss" scoped>
.channel-messages {
  height: calc(100vh - 70px);
  .__messages {
    overflow-y: scroll;
    height: calc(100% - 155px);
  }
}
</style>
