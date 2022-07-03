<template>
    <NElement type="div" class="slog">
        <NEllipsis style="max-width: 85vw"><h2 class="option">{{ option }}</h2></NEllipsis>
        <h3 style="color: var(--text-color-2);">Open source, maybe secure</h3>
        <h2 style="color: var(--text-color-3)">Definitely stupid.</h2>
    </NElement>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { NElement, NEllipsis } from 'naive-ui';
export default defineComponent({
    setup() {
        let titleOptions = ['Talk to your loser friends', 'Chat with ease', 'Make it yours', 'Chat for all time', 'All I want for Christmas is youuuuuuu', '195 Countries, One ZenSpace'];
        let option = titleOptions.sort(function () { return 0.5 - Math.random() })[0];
        return { option }
    },
    components: {NElement, NEllipsis}
})

</script>

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