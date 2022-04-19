<template>
  <tippy content="Avatars are not yet supported ⚠️" v-tippy="{placement: 'top'}" v-if="displaywarn">
    <div class="avatar">
      <Missing v-if="true" :height="size" :width="size" color="var(--error)" />

      <!-- <img :src="client?.generateFileURL(src.animated, false, {id: src.img, animated: src.animated})!!" :height="size" :width="size" v-else/> -->
    </div>
  </tippy>
  <div class="else-warn" v-else>
     <div class="avatar">
      <Missing v-if="true" :height="size" :width="size" color="var(--error)" />

      <!-- <img :src="client?.generateFileURL(src.animated, false, {id: src.img, animated: src.animated})!!" :height="size" :width="size" v-else/> -->
    </div>
  </div>
  <slot/>
</template>

<script lang="ts">
import {Attachment} from '@/lib/structures/Attachment';
import {defineComponent, computed} from 'vue';
import {ClientStore} from '../../../../stores/ClientStore';
import {Filter} from './AvatarFilter';
import Missing from '@/components/global/Icons/Missing.vue';

export default defineComponent({
  name: 'Avatar',
  props: {
    size: {
      type: String,
      required: false,
      default: () => '32px',
    },
    filter: {
      type: String,
      required: false,
      default: () => 'None' as Filter,
    },
    src: {
      type: Object,
      required: false,
      default: () => null,
    },
    animate: Boolean,
    displaywarn: {
      type: Boolean,
      required: false,
      default: () => true
    }
  },
  setup(props) {
    let client = ClientStore();
    console.log(props);
    return {
      client: computed(() => client.client),
    };
  },
  components: {Missing},
});
</script>
