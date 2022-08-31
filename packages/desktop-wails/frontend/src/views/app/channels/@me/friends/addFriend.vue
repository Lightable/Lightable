<script setup lang="ts">
import { ref } from 'vue';
import { useAppStore } from '../../../../../stores/AppStore';
import { NButton } from 'naive-ui';
import AppSection from '../../../../../components/app/AppSection.vue';
const appStore = useAppStore();

appStore.setTitle('Friends • Requests');


</script>

<template>
    <div class="header ns">
        <h3>Add Friend</h3>
        <span class="subtitle">Add a friend with thier username (ex: Brys0)</span>
        <div class="outer-input">
            <input class="input" placeholder="Add friend by username" />
            <NButton secondary type="info">Add Friend</NButton>
        </div>
    </div>
    <AppSection title="REQUESTS">
        <h3 style="color: var(--text-color-2); text-align: center;" class="center">Could not find any requests...<br>At least you're not left on pending 😎</h3>
    </AppSection>
</template>


<style lang="scss" scoped>
.header {
    margin: 20px;

    .subtitle {
        color: var(--text-color-3);
    }

    .outer-input {
        display: flex;
        flex-direction: row;
        padding: 5px;
        background-color: #1f1f22;
        border-radius: 3px;

        .input {
            all: unset;
            padding: 4px;
            background-color: transparent;
            border: none;
            color: var(--text-color-2);
            width: 100%;
            caret-color: var(--info-color);

            &:hover {
                background-color: transparent;
                border: none;
            }

            &:focus {
                border: none;
            }
        }
    }
}
</style>