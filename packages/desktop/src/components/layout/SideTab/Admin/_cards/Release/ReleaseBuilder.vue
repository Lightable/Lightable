<template>
  <div class="release">
    <div class="main">
      <span class="__tag"><slot name="tag"/></span> - <span class="__title"><slot name="title"/></span>
    </div>
    <div class="details">
      <div class="__descript" :contenteditable="edit" @input="(e) => $emit('inputtext', e.srcElement.innerText)"><slot name="description"/></div>
    </div>
    <div class="sig">
        <slot name="sig"/>
    </div>
    <div class="actions">
        <slot name="actions"/>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import Download from '../../../../../Icons/Download.vue';
import Edit from '@/components/Icons/Edit.vue';
import Close from '@/components/Icons/Close.vue';
export default defineComponent({
  components: {Download, Edit, Close},
  name: 'ReleaseBuilder',
  props: {
      edit: Boolean
  }
});
</script>

<style lang="scss" scoped>
.release {
  color: var(--text);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 8px;
  .main {
    color: var(--white);
    .__tag {
      background-color: var(--blue);
      padding: 3px;
      border-radius: 8px;
    }
  }
  .details {
    margin-top: 8px;
    background-color: var(--sb);
    min-height: 120px;
    max-height: 140px;
    max-width: 250px;
    border-radius: 10px;
    word-wrap: break-word;
    .__descript {
      padding: 4px;
      height: 100%;
      width: 100%;
       &:focus {
            border: none;
            outline: none;
        }
    }

  }
  .actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-direction: row;
    width: 100%;
    margin-top: 5px;
    margin-left: auto;
  }
}
</style>
