<template>
    <div class="ext-tag">
        <File :size="16"
            color="var(--purple)" />
        <div class="text open-on-hover"
            @click="openFile">
            {{ text }}<b class="ext">.{{ ext }}</b>
        </div>
        <Close :size="24"
            color="var(--error)"
            class="isButton"
            @click="$emit('close', fullPath)" />
    </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { File, Slash as Close } from 'lucide-vue-next';
import { open } from "@tauri-apps/api/shell";
export default defineComponent({
    name: 'ExtensionTag',
    components: { File, Close },
    props: {
        text: {
            type: String,
            required: false,
            default: () => {
                return 'File'
            }
        },
        ext: {
            type: String,
            required: false,
            default: () => {
                return 'js'
            }
        },
        fullPath: String
    },
    methods: {
        async openFile() {
            await open(this.fullPath!!);
        }
    }
})
</script>

<style lang="scss" scoped>
.ext-tag {
    display: flex;
    flex-direction: row;
    gap: 4px;
    align-items: center;
    padding: 8px;
    animation: turnOpacity 250ms ease;

    .open-on-hover {
        cursor: pointer;

        &:hover {
            color: var(--orange);

        }
    }

    .text {
        color: var(--gray);

        .ext {
            color: var(--purple);
        }
    }
}

.isButton {
    cursor: pointer;
    margin-left: auto;
    &:hover {
        opacity: 0.5;
    }
}

@keyframes turnOpacity {
    to {
        transform: trnaslateY(0);
    }

    from {
        transform: translateY(-120px);
    }
}
</style>