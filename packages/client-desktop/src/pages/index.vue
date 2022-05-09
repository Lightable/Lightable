<script lang="ts">
import {Client} from '@/lib/Client';
import {AuthenticationStore} from '@/stores/AuthenticationStore';
import {ClientStore} from '@/stores/ClientStore';
import {invoke} from '@tauri-apps/api';
import {defineComponent} from 'vue';
export default defineComponent({
  name: 'index',
  beforeCreate() {
    let authStore = AuthenticationStore();
    setTimeout(() => {
        invoke('close_splashscreen');
    }, 300)
    if (authStore.accounts.size === 1) {
      return this.$router.push('/app/channels/@me');
    } else if (authStore.accounts.size <= 0) {
      return this.$router.push('login');
    } else {
      return this.$router.push('accounts');
    }
    //
    // if (authentication.accounts.entries.length < 1)
    // this.$router.push('/app');
    // return this.$router.push('/login');
  },
});
</script>
