<script setup lang="ts">
import { PropType } from 'vue';
import { LoadingStates } from '../../composable/LoadingData';
import { CheckmarkDone, Warning } from '@vicons/ionicons5';
import { ErrorFilled } from '@vicons/carbon';
import { NIcon } from 'naive-ui';
interface LoadingStep {
    name: string,
    state: LoadingStates,
    finished: boolean
}

let props = defineProps({
    steps: Array as PropType<LoadingStep[]>
});

</script>


<template>
    <div class="loading full">
        <div class="header ns">
            <h1>Lightable is loading</h1>
        </div>
        <div class="body loading-body ns">
            <div class="outer-loading">
                <div class="loading-bar">
                    <div class="inner-bar" :style="{ width: (steps!!.filter(v => v.finished == true)?.length / steps!!.length) * 100 + '%' }" />
                </div>
                <span class="steps">{{ steps?.filter(v => v.finished == true).length }} out of {{ steps?.length }} completed
                    <div class="entry-container completed">
                        <span class="entry completed-entries" v-for="(item, index) in steps?.filter(v => v.state == LoadingStates.SUCCESS && v.finished == true)" v-bind:key="index">
                            <NIcon :size="20">
                                <CheckmarkDone />
                            </NIcon>
                            {{ item.name }}
                        </span>
                    </div>
                    <div class="entry-container warned">
                        <span class="entry warned-entries" v-for="(item, index) in steps?.filter(v => v.state == LoadingStates.WARNING && v.finished == true)" v-bind:key="index">
                           <NIcon :size="20">
                                <Warning />
                            </NIcon>
                           {{ item.name }}
                        </span>
                    </div>
                    <div class="entry-container errored">
                        <span class="entry errored-entries" v-for="(item, index) in steps?.filter(v => v.state == LoadingStates.FAILED && v.finished == true)" v-bind:key="index">
                             <NIcon :size="20">
                                <ErrorFilled />
                            </NIcon>
                            {{ item.name }}
                        </span>
                    </div>
                    <div class="entry-container incompleted">
                        <span class="entry incompleted-entries" v-for="(item, index) in steps?.filter(v => v.finished == false)" v-bind:key="index">
                            {{ item.name }}
                        </span>
                    </div>
                </span>
                <span class="left" :finished="(steps!!.length - steps!!.filter(v => v.finished == true).length) === 0">
                    {{ steps?.filter(v => v.finished == false).length }} task{{ steps?.filter(v => v.finished == false).length == 1 ? '' : 's' }} left
                </span>
            </div>

        </div>
    </div>
</template>


<style lang="scss" scoped>
.loading {
    display: flex;
    min-width: 40%;
    max-width: 90%;
    flex-direction: column;

    .body {
        display: flex;
        flex-direction: column;

        .outer-loading {
            display: flex;
            flex-direction: column;
            gap: 8px;

            .steps {
                display: flex;
                flex-direction: row;
                gap: 8px;

                .entry-container {
                    display: flex;
                    flex-direction: row;
                    gap: 8px;
                    &.completed {
                        color: var(--success-color);
                    }

                    &.warned {
                        color: var(--warning-color);
                    }

                    &.errored {
                        color: var(--error-color);
                    }

                    &.incompleted {
                        color: var(--text-color-3);
                    }

                    .entry {
                        display: flex;
                        flex-direction: row;
                        gap: 4px;
                        // justify-content: center;
                        // align-items: center;
                    }
                }


            }

            .loading-bar {
                height: 20px;
                background-color: rgb(25, 25, 25);
                width: 100%;
                border-radius: 20px;

                .inner-bar {
                    transition: all 150ms ease;
                    // width: 50%;
                    height: 20px;
                    background-color: var(--info-color);
                    border-radius: 20px;
                }
            }

        }

        .left {
            color: var(--text-color-1);
            &[finished='true'] {
                color: var(--text-color-3);
            }
        }
    }
}
</style>