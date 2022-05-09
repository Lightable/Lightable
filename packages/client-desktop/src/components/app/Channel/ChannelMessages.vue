<template>
  <div class="channel-messages">
    <Notice class="notice" v-if="!release"> Messages are not yet released </Notice>
    <div class="classic __messages" v-if="UIType == 'Classic'"></div>
    <div class="sms __messages" v-if="UIType == 'SMS'">
      <SMSMessage
        v-for="(message, index) in messages.values()"
        :message="message"
        :prevchain="messages.$hasChain(message._id)"
        :self="message.author_id == client.self?._id"
        v-bind:key="index"
        v-if="release"
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
      v-if="release"
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
      v-if="release"
    >
      Create Self Messages
    </button>
  </div>
</template>

<script lang="ts">
import {defineComponent, PropType} from 'vue';
import Messages, {UIMessageType} from '@/lib/structures/Messages';
import {ClientStore} from '@/stores/ClientStore';
import {Client} from '@/lib/Client';
import SMSMessage from './_message/SMSMessage.vue';
import Notice from './Notice.vue';
export default defineComponent({
  name: 'ChannelMessages',
  setup() {
    let clientStore = ClientStore();
    return {
      client: clientStore.client!! as Client,
    };
  },
  props: {
    messages: {
      type: Object as PropType<Messages>,
      required: true,
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
      this.client.users.get('45667265216368640')?.messages?.$makeRandom(amount);
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
  data() {
    return {
      release: false,
    };
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
