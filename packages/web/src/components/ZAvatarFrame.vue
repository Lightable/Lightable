
<template>
    <div class="avatar-frame">
        <div class="avatar-wrap">
            <slot name="avatar"/>
        </div>
        <div class="avatar-action">
            <slot/>
        </div>
    </div>
</template>

<style lang="scss" scoped>
.avatar-frame {
    display: flex;
    position: relative;
    .avatar-action {
        position: absolute;
        right: 8px;
        bottom: -2px;
    }
}
</style>