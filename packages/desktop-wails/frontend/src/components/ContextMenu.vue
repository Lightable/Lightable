<script setup lang="ts">
const emit = defineEmits<{
    (e: 'close', value: boolean): void
}>();
const handleClickOutside = () => {
    emit('close', false);
}

const props = defineProps({
    x: Number,
    y: Number,
    show: Boolean
})

</script>

<template>
    <Transition name="fade">
        <div class="context-menu" :style="{ left: `${x}px`, top: `${y}px` }" ref="contextMenu" v-if="show" v-click-outside="handleClickOutside">
                <slot />
        </div>
    </Transition>
</template>

<style lang="scss" scoped>
.context-menu {
    display: flex;
    position: absolute;
    background-color: #0c0c0f;
    width: 150px;
    height: fit-content;
    border-radius: 5px;
    padding: 8px;
    align-items: center;
    gap: 8px;
    flex-direction: column;
}
</style>
