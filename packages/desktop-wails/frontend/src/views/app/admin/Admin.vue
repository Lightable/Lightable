<script setup lang="ts">
import { useAppStore } from '../../../stores/AppStore';
import AdminHeader from '../../../components/admin/AdminHeader.vue';
const appStore = useAppStore();
appStore.setTitle('Admin');
</script>
    
    
    <template>
    <AdminHeader />
    <div class="admin-page">
        <router-view />
    </div>
</template>
    
    
    <style lang="scss" scoped>
    .admin-page {
        width: 100%;
        height: 100%;
        overflow-y: auto;
    
        &::-webkit-scrollbar {
            display: none;
        }
    }
    </style>