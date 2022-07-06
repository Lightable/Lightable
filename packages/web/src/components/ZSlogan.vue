<script setup lang="ts">
    import { defineAsyncComponent } from 'vue';

    const Element = defineAsyncComponent({
        loader: () => import('naive-ui/lib/element/src/Element')
    });
    const Ellipsis = defineAsyncComponent({
        loader: () => import('naive-ui/lib/ellipsis/src/Ellipsis')
    });
    const titleOptions = ['Talk to your loser friends', 'Chat with ease', 'Make it yours', 'Chat for all time', 'All I want for Christmas is youuuuuuu', '195 Countries, One ZenSpace'];
    const option = titleOptions.sort(function () { return 0.5 - Math.random() })[0];
</script>


<template>
    <Element type="div" class="slog">
        <Ellipsis style="max-width: 85vw"><h2 class="option">{{ option }}</h2></Ellipsis>
        <h3 style="color: var(--text-color-2);">Open source, maybe secure</h3>
        <h2 style="color: var(--text-color-3)">Definitely stupid.</h2>
    </Element>
</template>


<style lang="scss">
h2 {
    margin: 10px 0  !important;
    font-weight: 700 !important;
    font-size: 40px;
}
h3 {
    margin: 2px;
    font-size: 24px;
}
.slog {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    .option {
        font-weight: 700;
        font-family: 'Inter';
        font-size: 3.75rem;
        color: var(--info-color);
    }
}
@media only screen and (max-width: 1147px) {
    .slog {
        .option {
            font-weight: 500;
            font-size: 1.4rem;
        }
    h2 {
        font-size: 22px;
    }
    h3 {
        font-size: 18px;
        margin: 0;
    }
}
}
</style>