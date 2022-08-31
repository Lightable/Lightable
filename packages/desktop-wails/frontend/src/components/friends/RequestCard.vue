<script setup lang="ts">
    import { PropType } from 'vue';
    import { mocks } from '../../../wailsjs/go/models';
    import RelationshipCard from './RelationshipCard.vue';
    const props = defineProps({
        user: Object as PropType<mocks.PublicUser>
    })
    </script>
    
    <template>
        <div class="outer">
            <RelationshipCard :friend="user">
               
            </RelationshipCard>
        </div>
    </template>
    
    
<style lang="scss" scoped>
    .outer {
        width: 90%;
    }
</style>