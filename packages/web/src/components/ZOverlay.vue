<template>
    <div class="overlay">
        <div class="content">
            <slot />
        </div>
        <div class="container">
            <slot name="hover" />
        </div>
    </div>
</template>

<style lang="scss" scoped>
.overlay {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    .container {
        display: none;
    }
    &:hover {
        position: relative;

        .container {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translateX(-50%) translateY(-50%);
        }

        .content {
            position: relative;
            opacity: 0.5;
            transition: opacity .2s ease-in-out;
            
        }
    }

    
}
</style>