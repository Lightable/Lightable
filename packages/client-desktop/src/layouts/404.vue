<template>
  <div class="error">
      <div class="face">
          <span>:</span>
          <span>|</span>
      </div>
      <div class="descript">
          <Alert :title="'404'" :body="'That page doesn\'t exist!'" :type="'error'" v-on:clicked="$router.push('/app')" :clickable="true"/>
      </div>
  </div>
</template>

<script lang="ts">
import {SnackStore} from '@/stores/SnackStore';
import {defineComponent, computed} from 'vue';
import {useRouter} from 'vue-router';
import Alert from '@/components/global/Alert/Alert.vue';

export default defineComponent({
    name: "NotFound",
    setup() {
        const currentRoute = computed(() => {
            return useRouter().currentRoute.value.fullPath;
        });
        return { currentRoute };
    },
    mounted() {
        let snackBar = SnackStore();
        snackBar.create(`error`, `Failed to navigate to "${String(this.currentRoute)}"`, true, undefined);
    },
    components: { Alert }
});
</script>

<style lang="scss" scoped>
.error {
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
    height: 100vh;
    .face {
        font-size: 10em;
        animation: spin forwards 1s ease;
        color: var(--error);
        margin-left: 20px;
    }
}
@keyframes spin {
    0% {
        transform: rotate(0deg);
    }
    100% {
        transform: rotate(90deg);
    }
}
</style>
