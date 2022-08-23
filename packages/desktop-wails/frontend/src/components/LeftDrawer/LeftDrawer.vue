<script setup lang="ts">
import { computed, ref } from 'vue';
import { NButton, NIcon, NInput } from 'naive-ui';
import { Light } from '@vicons/carbon';
import { Search24Regular } from '@vicons/fluent';
import { useAppStore } from '../../stores/AppStore';
import { useClickOutside } from '../../composable/click-outside'
import SearchGlobal from '../search/SearchGlobal.vue'
const appStore = useAppStore();

const version = computed(() => appStore.version);
const search = ref({
    searching: false,
    term: ''
});
const container = ref(null);

const onSearchClick = () => {
    appStore.search.show = true;
}

useClickOutside(container, () => {
    search.value.searching = false;
})

document.addEventListener('keydown', (ev) => {
    if (ev.key === 'f' && ev.ctrlKey && !appStore.search.show) {
        ev.preventDefault();
        appStore.search.show = true;
        return
    }
})
</script>


<template>
    <div class="drawer-container">
        <div class="header">
            <div class="icon">
                <Light id="logo" color="var(--lightable-red)" />
            </div>
            <span class="title">
                Lightable
            </span>
            <div class="search-container" @click="onSearchClick" ref="container">
                <input type="text" placeholder="Search..." class="search-input" :searching="search.searching" />
                <NButton text type="info" size="large" class="search-btn" v-if="!search.searching">
                    <template #icon>
                        <NIcon>
                            <Search24Regular />
                        </NIcon>
                    </template>
                </NButton>
            </div>
        </div>
        <div class="actions">
            <div class="top-level">
                <slot name="top" />
            </div>
            <div class="groups">
                <slot name="groups" />
            </div>
        </div>
        <div class="lower ns">
            <span>{{ version }}</span>
        </div>
    </div>
</template>


<style lang="scss" scoped>
.drawer-container {
    height: 100vh;
    display: flex;
    flex-direction: column;

    .header {
        display: flex;
        flex-direction: column;
        justify-content: flex-start;
        gap: 8px;
        align-items: center;
        flex-direction: row;
        height: 45px;
        padding-top: 1px;
        padding-left: 8px;

        .icon {
            height: 26px;
            width: 26px;
            font-size: 18px;
        }

        .title {
            color: #fff;
            font-size: 1rem;
        }

        .search-container {
            height: 25px;
            width: 120px;
            margin-left: auto;
            margin-right: 12px;
            background-color: rgba(41, 41, 41, 0.411);
            border-radius: .5rem;
            display: flex;
            justify-content: space-evenly;
            align-items: center;

            .search-input {
                all: unset;
                font-size: 12px;
                line-height: inherit;
                height: 20px;
                width: 90px;
                padding-left: 5px;
                color: var(--text-color-2);

                &[searching='true'] {
                    width: 111px;
                }
            }

            .search-btn {}

            // .search-icon {
            //     height: 24px;
            //     width: 24px;
            //     font-size: 18px;
            // }
        }

    }

    .actions {
        display: flex;
        flex-direction: column;
        gap: 8px;
        justify-content: center;
        align-items: center;

        .top-level {
            flex-direction: column;
            display: flex;
            width: 100%;
            justify-content: center;
            align-items: center;
        }

        .groups {
            flex-direction: column;
            display: flex;
            width: 100%;
            justify-content: center;
            align-items: center;
        }

    }

    .lower {
        margin-top: auto;
        margin-left: 5px;
        color: var(--text-color-3);

        span {
            font-family: "Jetbrains Mono";
            font-weight: lighter;
        }
    }
}
</style>


