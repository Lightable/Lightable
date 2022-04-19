<template>
  <div class="alert" :type="type" :clickable="clickable" @click="clicked">
    <div class="header">
      <div class="icon">
        <SuccessIcon v-if="type == 'success'" />
        <WarningIcon v-if="type == 'warning'" />
        <ErrorIcon v-if="type == 'error'" />
      </div>
     
    </div>
    <div class="body">
         <span class="title">{{ title }}</span>
        <span>{{body}}</span>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent, PropType} from 'vue';
import SuccessIcon from '@/components/global/Icons/Status/SuccessIcon.vue';
import WarningIcon from '@/components/global/Icons/Status/WarningIcon.vue';
import ErrorIcon from '@/components/global/Icons/Status/ErrorIcon.vue';
import {AlertTypes} from './AlertTypes';
export default defineComponent({
  props: {
    type: String as PropType<AlertTypes>,
    title: String,
    body: String,
    clickable: Boolean
  },
  methods: {
      clicked() {
          this.$emit('clicked')
      }
  },
  components: {SuccessIcon, WarningIcon, ErrorIcon},
});
</script>
<style lang="scss" scoped>
$default: rgba(255, 255, 255, 0.1);
$info: rgba(56, 137, 197, 0.25);
$success: rgba(42, 148, 125, 0.25);
$warning: rgba(240, 138, 0, 0.25);
$error: rgba(208, 58, 82, 0.25);
.alert {
  display: flex;
  flex-direction: row;
  line-height: 1.6;
  border-radius: 3px;
  font-size: 14px;
  color: var(--white);
  padding: 15px;
  transition: all 150ms ease;
  &:hover {
      filter: brightness(110%);
  }
  &[clickable] {
      cursor: pointer;
  }
  &[type='success'] {
      background: $success;
       border: 1px solid var(--green);
  }
  &[type='warning'] {
      background: $warning;
       border: 1px solid var(--yellow);
  }
  &[type='error'] {
      background: $error;
      border: 1px solid var(--error);
  }
  &[type='default'] {
      background: $default;
       border: 1px solid var(--gray);
  }
  .header {
      .icon {
          height: 26px;
          width: 26px;
          line-height: 1em;
      }
  }
  .body {
    display: inline-flex;
    flex-direction: column;
    align-items: flex-start;
    justify-content: center;
    align-content: flex-start;
    margin-left: 10px;
       .title {
         font-size: 16px;
         font-weight: 550;
      }
      .body {
              margin-top: 7px;
      }

  }
}
</style>
