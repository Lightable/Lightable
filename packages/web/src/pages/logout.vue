<script lang="ts" setup>
import { useAppStore } from '../stores/AppStore';
let appStore = useAppStore();
appStore.account = null;
localStorage.clear();
location.href = '/';
</script>