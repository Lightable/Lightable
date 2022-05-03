<template>
  <BaseModal @clicked="modalVis(false)">
    <template v-slot:icon>
      <Admin color="var(--purple)" height="45px" width="45px" />
    </template>
    <template v-slot:name>
      <span class="title">New Release</span>
    </template>
    <template v-slot:body>
      <div class="input-tree">
        <input type="text" class="input" placeholder="Version" v-model="release.version" />
        <input type="text" class="input" placeholder="Title" v-model="release.title" />
        <input type="text" class="input" placeholder="GitHub Release URL" v-model="release.url" />
        <textarea
          plaintext
          placeholder="Description"
          class="input descript"
          v-model="release.notes"
        />
        <input type="text" class="input" placeholder="Signature" v-model="release.signature" />
      </div>
    </template>
    <template v-slot:actions>
      <div class="row">
        <button class="db" dbt="success" @click="save(release)">Save</button>
        <button class="db" dbt="danger" @click="modalVis(false)">Nevermind..</button>
      </div>
    </template>
  </BaseModal>
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import BaseModal from '../BaseModal.vue';
import Admin from '@/components/Icons/Admin.vue';
import {Release} from '@/lib/structures/Release';
import {AppStore} from '@/stores/AppStore';
import {ClientStore} from '@/stores/ClientStore';
export default defineComponent({
  name: 'NewReleaseModal',
  components: {BaseModal, Admin},
  setup() {
    let appStore = AppStore();
    return {
      client: ClientStore().client,
      modalVis: appStore.setNewReleaseModalVis,
    };
  },
  mounted() {},
  data() {
    return {
      release: {
        version: '',
        title: '',
        url: '',
        notes: '',
        signature: '',
      } as unknown as Release,
    };
  },
  methods: {
    async save(release: Release) {
      let req = await this.client?.saveRelease(release);
      console.log(req);
      this.modalVis(false);
    },
  },
});
</script>

<style lang="scss" scoped>
@import '@/assets/sass/ui/input.scss';
@import '@/assets/sass/ui/input.scss';
.title {
  font-size: calc(var(--font-size) + 12px);
  margin-left: 8px;
  color: var(--white);
  font-weight: bold;
}
.row {
  display: flex;
  justify-content: space-between;
}
.input-tree {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  .descript {
    resize: none;
    width: 76%;
    height: 300px;
    font-family: Inter;
    font-size: calc(var(--font-size) + 8px);
    padding: 4px;
  }
  .input {
    @include input;
    width: 70%;
    margin-top: 8px;
  }
}
</style>
