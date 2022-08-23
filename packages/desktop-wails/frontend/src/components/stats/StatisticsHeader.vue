<script setup lang="ts">
import AppHeader from '../header/AppHeader.vue';
import { NIcon } from 'naive-ui';
import { QueryStatsFilled } from '@vicons/material';
</script>


<template>
    <AppHeader title="Statistics">
        <template #icon>
            <NIcon :size="32">
                <QueryStatsFilled/>
            </NIcon>
        </template>
    </AppHeader>
</template>