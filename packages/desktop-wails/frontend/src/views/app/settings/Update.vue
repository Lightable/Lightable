<script setup lang="ts">
import { ref, computed } from 'vue';
import { NSelect, NButton } from 'naive-ui';
import { useAppStore } from '../../../stores/AppStore';
import { useUpdateStore } from '../../../stores/UpdateStore';
import { useRouter } from 'vue-router';

const router = useRouter();
const appStore = useAppStore();
const updateStore = useUpdateStore();

const update = computed(() => updateStore.currentUpdate.data);
const self = computed(() => appStore.user!!);
const buildOptions = [
    {
        label: 'Canary (Latest, Buggiest)',
        value: 3,
    },
    {
        label: 'Beta (Tested, Incomplete)',
        disabled: true,
        value: 2
    },
    {
        label: 'Stabe (Tested, LTS)',
        disabled: true,
        value: 1
    }
]

const buildOption = ref(3);
</script>

<template>
    <div class="body">
        <h3 class="ns">Hey {{self.name}} we've got an update for you...</h3>
        <div class="content ns">
            <div class="section">
                <span>Change build type</span>
                <NSelect v-model:value="buildOption" :options="buildOptions" :fallback-option="(buildOptions[0] as any)" status="error" />
            </div>
            <div class="section">
                <span class="title">{{update?.version}} - <span class="normal">{{update?.title}}</span><span class="normal tiny">?</span></span>
            </div>
            <div class="section">
                <span class="title">Release Notes</span>
                <div class="notes">
                    {{update?.notes}}
                </div>
                <div class="signature">
                    <span>Published by: {{update?.signature}}</span>
                </div>
            </div>
            <div class="section push">
                <NButton size="large" secondary type="primary">
                    <template #icon>

                    </template>
                    Download
                </NButton>
            </div>

        </div>
    </div>
</template>


<style lang="scss" scoped>
.body {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;

    .content {
        display: flex;
        flex-direction: column;
        width: 70%;

        .section {
            display: flex;
            flex-direction: column;
            gap: 1px;
            margin: 5px;

            .title {
                font-size: 18px;
                font-weight: bold;

                .normal {
                    font-weight: normal;
                    &.tiny {
                        font-size: 14px;
                        color: var(--text-color-3);
                        margin-left: 8px;
                    }
                }

            }

            .notes {
                background-color: var(--modal-color);
                padding: 8px;
                border-radius: .2rem;
                min-height: 15vh;
                max-height: 25vh;
                word-wrap: normal;
                white-space: pre-line;
            }

            .signature {
                margin: 8px;
                color: var(--text-color-3);
            }
            &.push {
                margin-top: 20px;
            }
        }
    }
}
</style>