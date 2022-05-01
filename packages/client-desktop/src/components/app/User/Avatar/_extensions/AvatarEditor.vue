<template>
  <BaseModal>
    <template v-slot:icon>
      <Edit height="32" width="32" color="var(--ac)" />
    </template>
    <template v-slot:name>
      <span class="title">Edit Your Avatar</span>
    </template>
    <template v-slot:body>
      <div class="corner">
        <span class="file-size">24.4MB</span>
      </div>
      <div class="image-editor">
        <Cropper
          class="cropper"
          src="https://images.unsplash.com/photo-1600984575359-310ae7b6bdf2?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=700&q=80"
          :stencil-component="$options.components.CircleStencil"
        />
      </div>
    </template>
  </BaseModal>
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import BaseModal from '@/components/app/Modal/BaseModal.vue';
import Edit from '@/components/Icons/Edit.vue';
import {CircleStencil, Cropper} from 'vue-advanced-cropper';
import 'vue-advanced-cropper/dist/style.css';
export default defineComponent({
  name: 'AvatarEditor',
  components: {
    BaseModal,
    Edit,
    Cropper,
    CircleStencil,
  },
});
</script>

<style lang="scss" scoped>
.title {
  font-size: calc(var(--font-size) + 12px);
  margin-left: 8px;
  color: var(--white);
  font-weight: bold;
}
.cropper {
  height: 600px;
  width: 600px;
  background: #ddd;
}

</style>