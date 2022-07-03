<template>
  <div class="notice">
      <WarningIcon height="42px" width="42px" /><span class="__text"><slot/></span>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue"
import WarningIcon from "@/components/Icons/Status/WarningIcon.vue"
export default defineComponent({
    name: "ChannelNotice",
    components: { WarningIcon }
})
</script>

<style lang="scss" scoped>
.notice {
    display: flex;
    height: 100%;
    width: 100%;
    flex-direction: row;
    align-items: center;
    justify-content: center;
    .__text {
        font-size: calc(var(--font-size) + 22px);
        font-weight: bold;
        color: var(--yellow);
        margin-left: 5px;
    }
}
</style>
