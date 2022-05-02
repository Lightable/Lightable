<template>
  <ReleaseBuilder :edit="canEdit" @inputtext="e => (updatedNotes = e)">
    <template v-slot:tag>
      {{ updatedTag }}
    </template>
    <template v-slot:title>
      {{ updatedTitle }}
    </template>
    <template v-slot:description>
      {{ release.description }}
    </template>
    <template v-slot:sig>
      <span class="raw-sig">{{ trun(updatedSignature, 20) }}</span>
    </template>
    <template v-slot:actions>
      <button class="db" dbt="success">
        <Download color="var(--white)" />
      </button>
      <button class="db" dbt="warning" v-if="!canEdit" @click="canEdit = true">
        <Edit color="var(--white)" />
      </button>
      <button class="db" dbt="warning" v-if="canEdit" @click="canEdit = false">
        <Save color="var(--white)" />
      </button>
      <button class="db" dbt="danger">
        <Close color="var(--white)" />
      </button>
    </template>
  </ReleaseBuilder>
  <div class="more-edit" v-if="canEdit">
    <input type="text" class="input" placeholder="Tag" v-model="updatedTag" />
    <input type="text" class="input" placeholder="Title" v-model="updatedTitle" />
    <input type="text" class="input" placeholder="Signature" v-model="updatedSignature" />
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import Download from '../../../../../Icons/Download.vue';
import Edit from '@/components/Icons/Edit.vue';
import Close from '@/components/Icons/Close.vue';
import ReleaseBuilder from './ReleaseBuilder.vue';
import Save from '../../../../../Icons/Save.vue';
import trunacate from '@/lib/trunacate';
export default defineComponent({
  components: {Download, Edit, Close, ReleaseBuilder, Save},
  name: 'Release',
  props: {
    release: {
      type: Object,
      required: false,
      default: () => {
        return {
          tag: '0.0.0',
          title: 'Release Title',
          notes: 'Release Notes',
          signature: 'Release Signature',
        };
      },
    },
  },
  setup() {
    return {
      trun: trunacate,
    };
  },
  methods: {
    newDescript(e: any) {
      console.log(e);
    },
  },
  data() {
    return {
      updatedTag: this.release.tag,
      updatedTitle: this.release.title,  
      updatedNotes: this.release.description,
      updatedSignature: this.release.signature,
      canEdit: false,
    };
  },
});
</script>

<style lang="scss" scoped>
@import '@/assets/sass/ui/input.scss';
.more-edit {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  .input {
    @include input;
    width: 70%;
    margin-top: 8px;
  }
}
.raw-sig {
  max-width: 20px;
}
</style>
