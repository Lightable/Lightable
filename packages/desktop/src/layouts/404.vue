
<script lang="ts">
import {defineComponent, getCurrentInstance} from 'vue';
import {AppStore} from '@/stores/AppStore';
export default defineComponent({
  name: 'NotFound',
  setup() {
    let store = AppStore();
    store.showNotFoundModal(true);
  },
  beforeCreate() {
      this.$router.back();
  }
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
