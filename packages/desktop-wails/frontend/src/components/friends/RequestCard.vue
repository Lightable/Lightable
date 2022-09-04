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
            <RelationshipCard :friend="user"/>
        </div>
    </template>
    
    
<style lang="scss" scoped>
  .outer {
    max-width: 95%;
    min-width: 10%;
}
</style>