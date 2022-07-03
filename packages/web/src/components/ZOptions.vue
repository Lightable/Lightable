<script setup lang="ts">
import { computed } from 'vue';
import { NButton, NIcon, NTooltip } from 'naive-ui';
import { LogoWindows, BrowsersOutline, CogOutline, LogOutOutline } from '@vicons/ionicons5';
import { useAppStore } from '../stores/AppStore';
let appStore = useAppStore();
let userExists = computed(() => appStore.account != null)
console.log(userExists)
</script>
<template>
    <div class="options">
        <!-- <img src="../assets/RightHere.svg" height="148" class="img-l"/> -->
        <div class="wrap">
            <NTooltip trigger="hover">
                <template #trigger>
                    <NButton size="large" class="download-btn btn" type="primary" disabled>
                        <template #icon>
                            <NIcon>
                                <LogoWindows />
                            </NIcon>
                        </template>
                        Download For Windows
                    </NButton>
                </template>
                You don't want to see that anyways...
            </NTooltip>
            <NTooltip trigger="hover">
                <template #trigger>
                    <NButton size="large" class="web-btn space btn" type="info" tertiary disabled>
                        <template #icon>
                            <NIcon>
                                <BrowsersOutline />
                            </NIcon>
                        </template>
                        Open Web Client
                    </NButton>
                </template>
                I haven't even created the project yet 💀
                <!-- <img src="../assets/RightHereHFlip.svg" height="148" class="img-r"/> -->
            </NTooltip>
        </div>
        <NButton text type="info" size="large" @click="$router.push('settings')">
            <template #icon>
                <CogOutline />
            </template>
            Account Settings
        </NButton>
        <NButton text type="error" size="large" @click="$router.push('logout')" v-if="userExists">
            <template #icon>
                <LogOutOutline />
            </template>
            Logout
        </NButton>
    </div>
</template>

<style lang="scss" scoped>
.options {
    display: flex;
    flex-direction: column;

    .wrap {
        display: flex;
        flex-direction: row;
        align-items: center;
        margin: auto;
        justify-content: space-between;
        margin-top: 25px;

        .btn {
            margin: 20px;
        }

        img {
            position: absolute;

            &.img-l {
                margin-left: -148px;
                margin-top: 95px;
            }

            &.img-r {
                margin-left: 220px;
                margin-top: 95px;
            }
        }
    }
}
@media only screen and (max-width: 1147px) {
  .options {
    .wrap {
        flex-direction: column;
    }
  }
}
</style>