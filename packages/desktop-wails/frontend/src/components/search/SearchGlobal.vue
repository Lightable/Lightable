<script setup lang="ts">
import { ref, computed, Ref, onMounted } from 'vue';
import { NIcon, NTooltip, NAvatar, NEllipsis } from 'naive-ui';
import { ChevronRightFilled } from '@vicons/material';
import { useAppStore } from '../../stores/AppStore';
import ProfileCard from '../ProfileCard.vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const searchi = ref(null) as Ref<HTMLElement | null>
const appStore = useAppStore();
const search = ref({
    searching: false,
    term: '',
    find: appStore.users as any,
    onTerm: false
});
const users = computed(() => appStore.users);
const searchApp = computed(() => appStore.search);
const onInputSearch = () => {
    search.value.searching = true;
    search.value.onTerm = true;
    searchForFriend(search.value.term)
    setTimeout(() => {
        search.value.onTerm = false;
    }, 350)
}
const searchForFriend = (name: string) => {
    search.value.find = [];
    users.value.forEach((i) => {
        if (i.name.toLowerCase().includes(name.toLowerCase())) {
            search.value.find.push(i)
        }
    });
}

document.onkeydown = (evt) => {
    evt = evt || window.event;
    if (evt.key === 'Escape') {
        appStore.search.show = false;
    }
}
onMounted(() => {
    searchi!!.value?.focus();
});
router.afterEach(() => {
    if (searchApp.value.show) {
        searchApp.value.show = false
    }
})

appStore.setTitle('Search');
</script>

<template>
    <div class="search-global">
        <div class="query-error ns">
            <div class="kbd"><span>Tap <kbd>Escape</kbd> to leave</span></div>
        </div>
        <div class="header ns" :searching="search.searching" :input-term="search.onTerm" :not-found="search.find.length === 0 && search.term.length !== 0">
            <NTooltip trigger="hover" placement="left">
                <template #trigger>
                    <span class="modifier">
                        <NIcon :size="24" color="var(--text-color-3)">
                            <ChevronRightFilled />
                        </NIcon>
                    </span>
                </template>
                Searching Friends
            </NTooltip>
            <input class="search-input" v-model="search.term" placeholder="Search Friends" @input="onInputSearch" ref="searchi"/>
            <span class="amount">{{ search.find.length }}</span>
        </div>
        <div class="contents">
            <TransitionGroup name="list" tag="div" class="contents">
                <ProfileCard v-for="(item, index) in search.find" v-bind:key="index" :friend="item">
                    {{ search.term }}
                </ProfileCard>
            </TransitionGroup>
        </div>
    </div>
</template>

<style lang="scss" scoped>
.search-global {
    height: 100vh;
    justify-content: center;
    margin: 10px;
    .header {
        display: flex;
        flex-direction: row;
        align-items: center;
        background-color: rgba(255, 255, 255, 0.089);
        width: 50vw;
        border-radius: .25rem;
        border: 1px solid transparent;
        transition: all 150ms ease;
        position: relative;
        &[input-term='true'] {
            border: 1px solid rgb(106, 144, 250);
        }
        &[not-found='true'] {
            border: 1px solid var(--error-color);
        }
        .modifier {
            display: flex;
        }

        .search-input {
            all: unset;
            width: 90%;
            height: 30px;
        }

        .amount {
            color: var(--text-color-3);
            position: absolute;
            right: 15px;
        }
    }

    .contents {
        display: flex;
        flex-direction: column;
        gap: 8px;
        max-height: 78%;
        overflow-y: auto;
        margin: 15px;
        &::-webkit-scrollbar {
            display: none;
        }
        .friend-search-card {
            display: flex;
            flex-direction: row;
            align-items: center;
            justify-content: flex-start;
            gap: 8px;
        }
    }

    .query-error {
        height: 90px;
        margin-top: 20px;
        .kbd {
            color: var(--text-color-3); 
            position: absolute; 
            left: 20px; 
            top: 20px; 
            background-color: #383838;
            border: 1px solid var(--info-color);
            padding: 8px;
            border-radius: .25rem;
            z-index: 999;
        }
        .inner {
            display: flex;
            flex-direction: row;
            align-items: center;
            gap: 8px;
            color: var(--text-color-2);
            font-family: 'Jetbrains Mono';
            margin-top: 20px;
            .query-result {
                font-size: 20px;

                .term {
                    color: var(--info-color);
                }
            }
        }
    }
}

.list-enter-active,
.list-leave-active {
    transition: all 0.5s ease;
}

.list-enter-from,
.list-leave-to {
    opacity: 0;
    transform: translateX(30px);
}

.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.5s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}
</style>