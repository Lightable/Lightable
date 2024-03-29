<script setup lang="ts">
import ZPaneWrapVue from './ZPaneWrap.vue';
import ZRightPane from './ZRightPane.vue';
import { NSkeleton, NAvatar, NIcon, NInput, NInputGroup, NButton, NSpin, NButtonGroup, NInputNumber, NDatePicker } from 'naive-ui';
import { CheckmarkCircle, CloseCircle } from '@vicons/ionicons5';
import ZOverlay from '../ZOverlay.vue';
import { computed, Ref, ref, defineEmits } from 'vue';
import { useClientStore } from '../../stores/ClientStore';
import { UserSearchType } from '../../lib/LiteClient';
const emit = defineEmits(['searchEnabled', 'stopSearchEnabled', 'searchDisabled', 'stopSearchDisabled'])
const props = defineProps({
     enabledUsersLoading: Boolean,
     disabledUsersLoading: Boolean,
     invitedUsersLoading: Boolean
});
let clientStore = useClientStore();
const currentSearchType = ref("NAME") as Ref<UserSearchType>

const currentEnabledSearchName = ref("");
const currentEnabledSearchID = ref("");
const currentEnabledSearchDate = ref("");
const currentDisabledSearchName = ref("");
const currentDisabledSearchID = ref("");
const currentDisabledSearchDate = ref("");

const enabledUsers = computed(() => Array.from(clientStore.enabledUsers.values()));
const disabledUsers = computed(() => Array.from(clientStore.disabledUsers.values()));
const invitedUsersWithCode = computed(() => clientStore.invites.filter(i => i.code != null || i.code != undefined));
const invitedUsersWithoutCode = computed(() => clientStore.invites.filter(i => i.code == null || i.code == undefined));
const lite = computed(() => clientStore.lite);

const searchEnabledUsers = (type: UserSearchType) => {
     emit('searchEnabled');
     clientStore.enabledUsers.clear();
     switch (type) {
          case 'NAME': {
               lite.value.getEnabledUsers(type, currentEnabledSearchName.value).then(c => {
                    emit('stopSearchEnabled');
               });
               break;
          }
          case 'ID': {
               lite.value.getEnabledUsers(type, currentEnabledSearchID.value).then(c => {
                    emit('stopSearchEnabled');
               });
               break;
          }
          case 'CREATED': {
               lite.value.getEnabledUsers(type, currentEnabledSearchDate.value).then(c => {
                    emit('stopSearchEnabled');
               });
               break;
          }
     }
}
const updateEnabledUserSearch = (type: UserSearchType, value: string) => {
     switch (type) {
          case 'NAME': {
               currentEnabledSearchName.value = value;
               break;
          }
          case 'ID': {
               currentEnabledSearchName.value = value;
               break;
          }
          case 'CREATED': {
               currentEnabledSearchDate.value = value;
               break;
          }
     }
     console.log('Search with type ', type, ' and value ', value);
}
const updateDisabledUserSearch = (type: UserSearchType, value: string) => {
     switch (type) {
          case 'NAME': {
               currentDisabledSearchName.value = value;
               break;
          }
          case 'ID': {
               currentDisabledSearchName.value = value;
               break;
          }
          case 'CREATED': {
               currentDisabledSearchDate.value = value;
               break;
          }
     }
     console.log('Search with type ', type, ' and value ', value);
}
const searchDisabledUsers = (type: UserSearchType) => {
     emit('searchDisabled');
     clientStore.disabledUsers.clear();
     switch (type) {
          case 'NAME': {
               lite.value.getDisabledUsers(type, currentDisabledSearchName.value).then(c => {
                    emit('stopSearchDisabled');
               });
               break;
          }
          case 'ID': {
               lite.value.getDisabledUsers(type, currentDisabledSearchID.value).then(c => {
                    emit('stopSearchDisabled');
               });
               break;
          }
          case 'CREATED': {
               lite.value.getDisabledUsers(type, currentDisabledSearchDate.value).then(c => {
                    emit('stopSearchDisabled');
               });
               break;
          }
     }
}
</script>

<template>
     <ZPaneWrapVue>
          <ZRightPane>
               <div class="card-header">
                    <h2 style="color: var(--error-color);" class="header">Disabled Accounts</h2>
                    <!-- <NSkeleton :width="180" :height="30" round v-if="loading" /> -->
                    <span :style="{ color: 'var(--text-color-2)' }">Search By: </span>
                    <NButtonGroup>
                         <NButton quaternary :disabled="currentSearchType == 'NAME'" @click="currentSearchType = 'NAME'">
                              Name
                         </NButton>
                         <NButton quaternary :disabled="currentSearchType == 'ID'" @click="currentSearchType = 'ID'">
                              ID
                         </NButton>
                         <NButton quaternary :disabled="currentSearchType == 'CREATED'" @click="currentSearchType = 'CREATED'">
                              Created
                         </NButton>
                    </NButtonGroup>
                    <NInputGroup class="any-search">
                         <NInput placeholder="Search Name" @input="(e: any) => updateDisabledUserSearch('NAME', e as unknown as string)" v-if="currentSearchType == 'NAME'" style="width: 100%;" />
                         <NInputNumber placeholder="User ID" @input="(e: any) => updateDisabledUserSearch('ID', e as unknown as string)" clearable v-if="currentSearchType == 'ID'" style="width: 100%;" />
                         <NDatePicker placeholder="User created date" @update:value="(e: any) => updateDisabledUserSearch('CREATED', new Date(e as unknown as number).toISOString())" v-if="currentSearchType == 'CREATED'" style="width: 100%;" />
                         <NButton tertiary @click="searchDisabledUsers(currentSearchType)">
                              Search
                         </NButton>
                    </NInputGroup>
               </div>
               <div class="card-body">
                    <div class="disabled inner">
                         <NSkeleton class="friend" :height="64" :width="64" round v-if="disabledUsersLoading" v-for="(_, index) in [0, 0, 0]" v-bind:key="index" />
                         <ZOverlay overlay-class="treat-as-button disabled" v-for="(user, index) in disabledUsers" v-bind:key="user.id" v-on:overlay-click="lite.enableUser(user.id)" :disabled="user.admin || user.id == lite?.user?.id">
                              <template #header>
                                   <span style="color: var(--text-color-3); font-size: 18px; font-family: 'strawfordregular'">{{ user.name }}</span>
                              </template>

                              <NAvatar round :size="64" :src="(user.avatar) ? lite.$getExternalAvatar(user.id, user.avatar.id) : undefined" :alt="`${user.name} Profile Picture`" />
                              <template #hover>
                                   <NIcon color="var(--success-color)" :size="18">
                                        <CheckmarkCircle />
                                   </NIcon>
                                   <span style="color: var(--success-color)">Enable</span>
                              </template>
                              <template #loading>
                                   <NSpin />
                              </template>
                         </ZOverlay>

                    </div>

               </div>
          </ZRightPane>
          <ZRightPane>
               <div class="card-header">
                    <h2 style="color: var(--success-color)" class="header">Enabled Accounts</h2>
                    <span :style="{ color: 'var(--text-color-2)' }">Search By: </span>
                    <NButtonGroup>
                         <NButton quaternary :disabled="currentSearchType == 'NAME'" @click="currentSearchType = 'NAME'">
                              Name
                         </NButton>
                         <NButton quaternary :disabled="currentSearchType == 'ID'" @click="currentSearchType = 'ID'">
                              ID
                         </NButton>
                         <NButton quaternary :disabled="currentSearchType == 'CREATED'" @click="currentSearchType = 'CREATED'">
                              Created
                         </NButton>
                    </NButtonGroup>
                    <NInputGroup class="any-search">
                         <NInput placeholder="Search Name" @input="(e: any) => updateEnabledUserSearch('NAME', e as unknown as string)" v-if="currentSearchType == 'NAME'" style="width: 100%;" />
                         <NInputNumber placeholder="User ID" @input="(e: any) => updateEnabledUserSearch('ID', e as unknown as string)" clearable v-if="currentSearchType == 'ID'" style="width: 100%;" />
                         <NDatePicker placeholder="User created date" @update:value="(e: any) => updateEnabledUserSearch('CREATED', new Date(e as unknown as number).toISOString())" v-if="currentSearchType == 'CREATED'" style="width: 100%;" />
                         <NButton tertiary @click="searchEnabledUsers(currentSearchType)">
                              Search
                         </NButton>
                    </NInputGroup>

               </div>
               <!-- <NSkeleton :width="180" :height="30" round v-if="loading" /> -->
               <div class="card-body">
                    <div class="enabled inner">
                         <NSkeleton class="friend" :height="64" :width="64" round v-if="enabledUsersLoading" v-for="(_, index) in [0, 0, 0]" v-bind:key="index" />
                         <ZOverlay overlay-class="treat-as-button enabled" v-for="(user, index) in enabledUsers" v-bind:key="user.id" v-on:overlay-click="lite.disableUser(user.id)" :disabled="user.admin || user.id == lite?.user?.id">
                              <template #header>
                                   <span style="color: var(--text-color-3); font-size: 18px; font-family: 'strawfordregular';">{{ user.name }}</span>
                              </template>
                              <NAvatar round :size="64" :src="(user.avatar) ? lite.$getExternalAvatar(user.id, user.avatar.id) : undefined" :alt="`${user.name} Profile Picture`" />
                              <template #hover>
                                   <NIcon color="var(--error-color)" :size="18">
                                        <CloseCircle />
                                   </NIcon>
                                   <span style="color: var(--error-color)">Disable</span>
                              </template>
                              <template #loading>
                                   <NSpin />
                              </template>
                         </ZOverlay>
                    </div>
               </div>
          </ZRightPane>
          <ZRightPane>
               <div class="card-header">
                    <h2 style="color: var(--info-color);" class="header">Invites</h2>
               </div>
               <div class="card-body">
                    <div class="inner invites-inner">
                         <div class="invite-requests">
                              <div class="header">
                                   <h3 class="title">Requests</h3>
                                   <div class="underline" />
                              </div>
                              <div class="invites-data">
                                   <div class="entry" v-if="invitedUsersLoading">
                                        <NSkeleton :width="200" :height="150" style="margin-top: 10px" />
                                   </div>
                                   <div class="entry" v-if="!invitedUsersLoading" v-for="(item, index) in invitedUsersWithoutCode" v-bind:key="index">
                                        <span class="email">{{ item.email }}</span>
                                        <div class="action">
                                             <NButton text type="primary" @click="lite.acceptPendingInviteUser(item.email)">Accept</NButton>
                                        </div>
                                   </div>
                              </div>
                         </div>
                         <div class="invite-requests">
                              <div class="header">
                                   <h3 class="title">Accepted</h3>
                                   <div class="underline" />
                              </div>
                              <div class="invites-data">
                                   <div class="entry" v-if="invitedUsersLoading">
                                        <NSkeleton :width="200" :height="150" style="margin-top: 10px" />
                                   </div>
                                   <div class="entry" v-if="!invitedUsersLoading" v-for="(item, index) in invitedUsersWithCode" v-bind:key="index">
                                        <span class="email">{{ item.email }} </span>
                                        <span class="code">{{ item.code }}</span>
                                        <div class="action">

                                             <NButton text type="error" @click="lite.rollbackInvitedUser(item.email)">Rollback</NButton>
                                        </div>
                                   </div>
                              </div>
                         </div>
                    </div>
               </div>
          </ZRightPane>
     </ZPaneWrapVue>
</template>


<style lang="scss" scoped>
.treat-as-button {
     cursor: pointer;
}

.card-header {
     margin-left: 4px;
     user-select: none;
     font-family: 'Titillium Web';
     padding: 8px;

     h2 {
          font-weight: 100;
          padding: 0;
          margin: 0;
          font-size: 24px;
     }

     &.flex {
          display: flex;
          flex-direction: column;
          justify-content: center;
          align-items: center;
          gap: 8px;
     }
}

.invites-inner {
     color: var(--text-color-2);
     font-family: v-sans, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol";

     .invite-requests {
          max-width: 50%;
          justify-content: center;
          align-items: center;
          flex-direction: row;

          .header {
               display: flex;
               flex-direction: column;

               .title {
                    font-weight: normal;
                    margin-top: 8px;
                    margin-bottom: 8px;
               }

               .underline {
                    background-color: gray;
                    height: 2px;
                    width: 100%;
                    border-radius: 20px;
               }
          }

          .invites-data {
               .entry {
                    display: flex;
                    flex-direction: row;
                    gap: 8px;

                    .code {
                         color: var(--text-color-3);
                         opacity: .8;
                    }
               }
          }
     }
}

.card-body {
     min-width: 300px;
     max-width: 55vw;

     .inner {
          display: flex;
          flex-direction: row;
          padding: 6px;
          justify-content: space-between;
          flex-wrap: wrap;
          gap: 16px;
     }

}

@media only screen and(max-width: 852px) {
     .card-header {
          display: flex;
          flex-direction: column;
          justify-content: center;
          align-items: center;
     }

     .invites-inner {
          flex-direction: column !important;
          justify-content: center !important;
          align-items: center !important;

          .invite-requests {
               display: flex !important;
               flex-direction: column !important;

               .invites-data {
                    .entry {
                         flex-direction: column !important;
                         justify-content: center !important;
                         align-items: center !important;
                    }
               }
          }
     }
}
</style>