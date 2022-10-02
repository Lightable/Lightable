<script setup lang="ts">
import {ref} from 'vue';
import {NButton, NIcon} from 'naive-ui';
import {EditRound as Edit, CheckRound as Check, CloseRound as Close} from '@vicons/material';


const emit = defineEmits(['enable', 'disable', 'clicked']);
const props = defineProps({
  title: String,
  togglable: Boolean,
  toggle: Boolean,
})


const toggleClick = (ty: boolean) => {
  console.log(ty);
  switch (ty) {
    case false: {
      emit('disable');
      break;
    }
    case true: {
      emit('enable');
      break;
    }
  }
}
</script>

<template>
  <div class="profile-card">
    <div class="card-left">
      <span class="name">{{ title }}</span>
      <div class="value" v-if="!togglable">
        <slot/>
      </div>
    </div>
    <button class="edit" @click="$emit('clicked')" v-if="!togglable">
      <Edit class="icon"/>
    </button>
    <div class="toggle" v-if="togglable">
      <button class="toggle-btn toggle-off" @click="toggleClick(false)" v-if="togglable" :pressed="!toggle">
        <Close class="icon toggle-off"/>
      </button>
      <button class="toggle-btn toggle-on" @click="toggleClick(true)" v-if="togglable" :pressed="toggle">
        <Check class="icon toggle-on"/>
      </button>
    </div>

  </div>
</template>

<style lang="scss" scoped>
.profile-card {
  display: flex;
  background-color: var(--lightable-card-color);
  height: 4rem;
  width: 100%;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  max-width: 100%;
  min-width: 10%;

  .card-left {
    display: flex;
    flex-direction: row;
    align-items: center;

    .name {
      font-size: 20px;
      font-weight: bold;
      margin-left: 20px;
      width: 190px;
      color: var(--text-color-1);
    }

    .value {
      margin-left: 5rem;
      display: flex;
      justify-content: center;
      align-items: center;
      color: var(--text-color-2);
    }
  }

  .toggle {
    margin-right: 20px;
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center;
    border: rgba(39, 39, 39, 0.562) 1px solid;
    border-radius: .5rem;

    &.toggle-on {
      background-color: var(--success-color-suppl);

    }

    &.toggle-off {
      background-color: var(--error-color-suppl);
    }

    .toggle-btn {
      all: unset;
      display: flex;
      flex-direction: row;
      justify-content: center;
      align-items: center;
      height: 32px;
      width: 50px;
      cursor: pointer;
      background-color: var(--modal-color);
      transition: all 250ms ease;

      &.toggle-on {
        border-top-right-radius: .5rem;
        border-bottom-right-radius: .5rem;
      }

      &.toggle-off {
        border-top-left-radius: .5rem;
        border-bottom-left-radius: .5rem;
      }

      &[pressed='true'] {
        opacity: 0.6;
      }

      &:hover {
        opacity: 0.8;
      }

      .icon {
        height: 32px;
        width: 32px;
        padding: 8px;
        transition: all 250ms ease;

        &.toggle-on {
          color: var(--success-color-suppl);
        }

        &.toggle-off {
          color: var(--error-color-suppl);
        }
      }
    }
  }

  .edit {
    all: unset;
    height: 40px;
    width: 40px;
    cursor: pointer;
    background-color: var(--windows-accent-colour);
    border-radius: 50%;
    transition: all 250ms ease;
    margin-right: 20px;


    .icon {
      height: 24px;
      width: 24px;
      padding: 8px;

    }


    &:hover {
      background-color: rgba(14, 14, 14, 0.641);

      .icon {
        color: var(--windows-accent-colour);

      }
    }
  }
}
</style>