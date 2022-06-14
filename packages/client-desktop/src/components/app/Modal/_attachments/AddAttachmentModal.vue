<template>
    <BaseModal @clicked="$emit('upload-close')">
        <template #name>
            <span class="title">Add Attachments ({{total.uploaded}}/{{total.yetToUpload}})</span>
        </template>
        <template #icon>
            <FilePlus :size="24"
                color="var(--white)" />
        </template>
        <template #body>
            <div class="files">
            <ExtensionTag v-for="(file, index) in files"
                v-bind:key="index"
                :text="file.name"
                :ext="file.extension"
                :full-path="file.fullPath!!"
                @close="$emit('remove-file', file)" />
                </div>
        </template>
        <template #actions
            v-if="showConfirm">
            <div class="upload-actions">
                <button class="db upload-btn cb" :dbt="(total.percent >= 1) ? 'default' : 'success'" @click="uploadFiles()" :disable="total.percent >= 1">
                    <Upload size="16" color="var(--white)"/> Upload {{total.percent}}%
                </button>
                <button class="db cancel-btn cb" @click="$emit('upload-close')" dbt="danger">
                    <Cancel size="16" color="var(--white)" /> Cancel
                </button>
            </div>
        </template>
    </BaseModal>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";
import BaseModal from "../BaseModal.vue";
import SourceIcon from "@/components/Icons/SourceIcon.vue";
import Add from "@/components/Icons/Add.vue";
import ExtensionTag from "./_extensions/ExtensionTag.vue";
import { FilePlus, Upload, Slash as Cancel } from 'lucide-vue-next';
import { IAttachment } from "@/lib/structures/Messages";

export default defineComponent({
    name: 'AddAttachmentModal',
    components: { BaseModal, SourceIcon, Add, ExtensionTag, FilePlus, Upload, Cancel },
    props: {
        files: Array as PropType<IAttachment[]>,
        showConfirm: Boolean
    },
    data() {
        return {
            total: {
                uploaded: 0,
                yetToUpload: this.files!!.length,
                percent: 0
            }
        }
    },
    methods: {
        uploadFiles() {
            console.log('Uploading Files...');
            setInterval(() => {
                if (this.total.percent == 100) return
                this.total.percent =+ this.total.percent+1;
            }, 50)
        }
    }
})
</script>

<style lang="scss" scoped>
.title {
    font-size: calc(var(--font-size) + 12px);
    margin-left: 8px;
    color: var(--white);
    font-weight: bold;
}

.attachment-text {
    display: flex;
    flex-direction: row;
    align-items: center;
}

.cb {
    display: flex;
    flex-direction: row;
    gap: 4px;
}

.upload-actions {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
}
.files {
    max-height: 50vh;
    overflow-y: auto;
}
</style>