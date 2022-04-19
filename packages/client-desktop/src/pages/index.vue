<script lang="ts">
import { Client } from "@/lib/Client";
import { AuthenticationStore } from "@/stores/AuthenticationStore";
import { ClientStore } from "@/stores/ClientStore";
import { defineComponent } from "vue";
   export default defineComponent({
        name: 'index',
        beforeCreate() {
            // let authentication = AuthenticationStore();
            // if (authentication.accounts.entries.length < 1) 
            // this.$router.push('/app');
            return this.$router.push('/login');
        }
   });
</script>