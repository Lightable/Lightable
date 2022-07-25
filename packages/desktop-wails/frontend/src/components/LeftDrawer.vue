<script setup lang="ts">
import { Light } from '@vicons/carbon';
import { Search24Regular } from '@vicons/fluent';
import { NButton, NIcon } from 'naive-ui';
</script>


<template>
    <div class="drawer-container">
        <div class="header">
            <div class="icon">
                <Light id="logo" color="var(--lightable-red)"/>
            </div>
            <span class="title">
                Lightable
            </span>
            <div class="search-container">
                <NButton text type="info" size="large">
                    <template #icon>
                        <NIcon>
                            <Search24Regular />
                        </NIcon>
                    </template>
                </NButton>
            </div>
        </div>
    </div>
</template>


<style lang="scss" scoped>
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
    font-family: Inter;
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
        height: 24px;
        width: 24px;
        margin-left: auto;
        margin-right: 12px;
        background-color: rgba(41, 41, 41, 0.548);
        border-radius: 50%;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 4px;
        .search-icon {
            height: 24px;
            width: 24px;
            font-size: 18px;
        }
    }

}
</style>


