<template>
        <div class="overlay" @click="$emit('overlay-click')">
            <slot />
        </div>
</template>


<style lang="scss" scoped>
.overlay {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    position: fixed;
    z-index: 9999;
    backdrop-filter: blur(5px);
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(22, 22, 22, 0.589);
}



</style>