<template>
  <MoveDown>
    <div
      class="item"
      :type="pending ? false : `button`"
      @click="pending ? () => {} : $router.push(`/app/channels/@me/${user._id}`)"
      @contextmenu="openProfileContext"
    >
      <div class="inner">
        <Avatar :circle="true" :user="user" />
        <div class="detail-wrap">
          <span class="name">
            <b>{{ user.name }}</b>
          </span>
          <div class="last-message" v-if="false">
            <span class="sent"> {{ last.user.name }} </span>
            <span class="sep"> • </span>
            <span class="message"> {{ trun(last.content, 19) }} </span>
          </div>
        </div>
        <div class="actions" v-if="pending">
          <SuccessIcon class="action" type="button" @click="user.acceptFriend()" />
          <ErrorIcon class="action" type="button" @click="user.denyFriend()" />
        </div>
      </div>
      <span class="time">{{ last.readableTime }}</span>
    </div>
  </MoveDown>
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import Avatar from '@/components/app/User/Avatar/Avatar.vue';
import {User} from '@/lib/structures/Users';
import trunacate from '@/lib/trunacate';
import SuccessIcon from '../Icons/Status/SuccessIcon.vue';
import ErrorIcon from '../Icons/Status/ErrorIcon.vue';
import MoveDown from './MoveDown.vue';
import { AppStore } from '@/stores/AppStore';
export default defineComponent({
  name: 'SideItemFriend',
  components: {Avatar, SuccessIcon, ErrorIcon, MoveDown},
  setup(props) {
    return {
      trun: trunacate,
    };
  },
  mounted() {
    // setInterval(() => {
    //   this.last.revaluateDate();
    // }, 10000);
  },
  methods: {
    openProfileContext(e: MouseEvent) {
      let appStore = AppStore();
      e.preventDefault();
      this.$contextmenu({
        x: e.x,
        y: e.y,
        items: [
          {
            label: 'Profile',
            divided: false,
            onClick: () => {
              appStore.setCurrentlyActiveProfile(this.user);
            }
          }
        ]
      })
    }
  },
  props: {
    user: {
      type: Object,
      required: true,
    },
    last: {
      type: Object || Boolean,
      required: false,
      default: () => ({
        sent: 'NA',
        message: 'Be the first to message',
        time: 'NA',
      }),
    },
    pending: {
      type: Boolean,
      required: false,
      default: () => false,
    },
  },
});
</script>

<style lang="scss" scoped>
.item {
  width: 96%;
  display: flex;
  padding: 5px;
  max-height: 95px;
  .time {
    margin-left: auto;
    margin-right: 9px;
    color: var(--gray);
    font-size: calc(var(--font-size) + 5px);
  }
  .inner {
    display: flex;
    flex-direction: row;
    color: var(--white);
    justify-content: center;
    align-items: center;
    .detail-wrap {
      display: flex;
      flex-direction: column;
      align-items: flex-start;
      .last-message {
        margin-left: 5px;
        font-size: calc(var(--font-size) + 6px);
        color: var(--gray);
        display: flex;
        flex-wrap: wrap;

        .sep {
          margin-left: 5px;
          margin-right: 5px;
        }
      }
      .name {
        margin-left: 5px;
        font-size: calc(var(--font-size) + 6px);
      }
      .message {
        word-wrap: break-word;
      }
    }
  }
  transition: all 150ms ease;
  &:hover {
    background-color: var(--sb);
  }
}
.actions {
  margin-left: 70px;
  .action {
    height: 32px;
    transition: all 250ms ease;
    &:hover {
      filter: brightness(80%);
    }
  }
}
</style>
