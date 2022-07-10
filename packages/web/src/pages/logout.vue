<script lang="ts" setup>
import { useAppStore } from '../stores/AppStore';
import {useMessage} from 'naive-ui';
import { useRouter } from 'vue-router';
let appStore = useAppStore();
let router = useRouter();
let message = useMessage();
appStore.account = null;
localStorage.clear();
message.info('Goodbye, dumbass. U can\'t remove this LLLLL cope fr 💀', {closable: false, duration: 0})
router.push('/');
</script>