<template>
    <div class="progress" :style="`height: ${height}; width: ${width}`">
        <div class="inner-progress" :completed="percentComplete" :style="`width: ${percentComplete}%; height: ${height};`">
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
name: 'ProgressBar',
    props: {
        height: String,
        width: String,
        completed: Boolean,
        percentComplete: Number
    }
});
</script>

<style scoped lang="scss">
.progress {
    border-radius: 20px;
    background: var(--bg);
    
    .inner-progress {
         border-radius: 20px;
        background: var(--purple);
        transition: all 150ms ease;
        &[completed='100'] {
            background: var(--green);
        }
    }
}
</style>