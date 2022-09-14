<script setup lang="ts">
import { ref, Ref, onMounted } from 'vue';
import { PhCheckCircle, PhDesktopTower, PhDeviceMobileCamera, PhUserCircleMinus } from '@dnlsndr/vue-phosphor-icons';
import {NAvatar,  NTooltip, NButton, NIcon }  from 'naive-ui';
import { GetSelfAvatar, GetUser, GetUsers, GetAvatarRaw, ChangeUser, RemoveUser } from '../../wailsjs/go/client/Client';
import { GetDevices } from '../../wailsjs/go/client/HttpClient';
import { mocks } from '../../wailsjs/go/models';
import { useAppStore } from '../stores/AppStore';
import { useRouter } from 'vue-router';

const router = useRouter();
const appStore = useAppStore();
const defaultUser = ref() as Ref<undefined | mocks.PrivateUser>
const users = ref([]) as Ref<mocks.PrivateUser[]>
const hoveredUser = ref() as Ref<UserWithDevice | undefined>
const avatars = ref(new Map()) as Ref<Map<string, string>>

appStore.leftDrawer.show = false;

GetUser().then(u => {
  if (!u) return
  defaultUser.value = u!!
  GetSelfAvatar(64).then(a => {
    avatars.value.set(defaultUser.value!!.id, a)
    GetDevices(u!!.token.token).then((devices) => {
      hoveredUser.value = {
        user: u,
        device: devices[devices.length - 1],
        access: new Date(devices[devices.length - 1].lastAccess * 1000).toLocaleString()
      }
    })
  })
})



GetUsers().then(async u => {
  if (u.length == 0) {
    router.push('/login');
  }
  const us = users.value
  for (let i = 0; u.length > i; i++) {
    let user = u[i]
    if (user.id === defaultUser.value!!.id) return
    us.push(user)
    if (user.avatar) {
      try {
        const avatar = await GetAvatarRaw(user.id, user.avatar.id, 64)
        avatars.value.set(user.id, avatar)
      } catch(e) {
        console.log('Error occurred in get avatar')
      }
    }
  }
})

const onMouseOver = async (u: mocks.PrivateUser) => {
  try {
    const devices = await GetDevices(u.token.token)
    hoveredUser.value = {
      user: u,
      device: devices[devices.length - 1],
      access: new Date(devices[devices.length - 1].lastAccess * 1000).toLocaleString()
    }
  } catch (err) {
    console.log('Error occurred in get devices')
  }
}

const removeAccount = async (u: mocks.PrivateUser) => {
  if (u.id === defaultUser.value!!.id) {
    defaultUser.value = undefined;
    appStore.user = null;
  } else {
    users.value = destroy(users.value, u)
  }
  if (hoveredUser.value) {
    hoveredUser.value = undefined;
  }
  await RemoveUser(u)
  if (!defaultUser.value) return router.push('/login');
}

const changeUser = async (u: mocks.PrivateUser) => {
  await ChangeUser(u)
  router.push('/app/home')
}


const goHome = () => {
  appStore.overrideExistingUser = true;
  router.push('/')
}

interface UserWithDevice {
  user: mocks.PrivateUser,
  device: any,
  access: string
}

function destroy(arr: Array<any>, val: any) {
  for (let i = 0; i < arr.length; i++) if (arr[i] === val) arr.splice(i, 1);
  return arr;
}
</script>

<template>
  <div class="margin">
    <h2 class="ns">Select Your Account Below</h2>

    <div class="inner">
      <div class="accounts">
        <div class="account" v-if="defaultUser" @mouseenter="onMouseOver(defaultUser as mocks.PrivateUser)" @click="$router.push('/app/home')">
          <NAvatar :size="48" round class="avatar" :src="avatars.get(defaultUser.id)"/>
          <div class="details">
            <span class="name">{{ defaultUser.name }}</span>
          </div>
          <NTooltip trigger="hover" placement="right">
            <template #trigger>
              <PhCheckCircle :size="48" weight="duotone" color="var(--success-color-pressed)" class="icon"/>
            </template>
            Default Account
          </NTooltip>
        </div>
        <div class="account" v-for="(user, index) in users" v-bind:key="index" @mouseenter="onMouseOver(user)" @click="changeUser(user)">
          <NAvatar :size="48" round class="avatar" :src="avatars.get(user.id)"/>
          <div class="details">
            <span class="name">{{user.name}}</span>
          </div>
        </div>
        <NButton quaternary type="primary" style="width: 50%;" @click="goHome">Go Home</NButton>
      </div>

      <div class="account-detail" v-if="hoveredUser">
        <div class="part">
          <NAvatar :size="72" :src="avatars.get(hoveredUser.user.id)" round/>
          <span class="name">{{hoveredUser.user.name}}</span>
        </div>
        <div class="part device">
          <PhDesktopTower :size="72" weight="duotone" color="var(--info-color-pressed)" v-if="hoveredUser.device.os === 'windows' || hoveredUser.device.os === 'darwin' || hoveredUser.device.os ===  'linux'"/>
          <PhDeviceMobileCamera :size="72" weight="duotone" color="var(--info-color-pressed)" v-if="hoveredUser.device.os === 'iOS' || hoveredUser.device.os === 'android'"/>
          <div class="details box">
            <div class="browser">
              {{hoveredUser.device.browser}} on {{hoveredUser.device.os}}
            </div>
            <span class="last-seen">
              Last seen at: <b class="date">{{hoveredUser.access}}</b>
            </span>
            <span class="last-seen">
              Near: <b>{{hoveredUser.device.geo.city}}, {{hoveredUser.device.geo.stateName}}, {{hoveredUser.device.geo.country}}</b>
            </span>
            <div class="part space">
              <NButton type="error" secondary @click="removeAccount(hoveredUser!!.user!! as mocks.PrivateUser)">
                <template #icon>
                  <PhUserCircleMinus type="duotone"/>
                </template>
                Remove Account
              </NButton>
            </div>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>


<style lang="scss" scoped>
.margin {
  margin: 20px;
  height: 100%;
  width: 100%;
  .inner {
    display: flex;
    flex-direction: row;
    height: 100%;
    width: 100%;
    .accounts {
      display: flex;
      flex-direction: column;
      gap: 16px;
      overflow-y: auto;
      height: 80%;
      &::-webkit-scrollbar {
        display: none;
      }
      .account {
        display: flex;
        flex-direction: row;
        justify-content: flex-start;
        align-items: center;
        width: 400px;
        gap: 32px;
        height: 72px;
        padding: 8px 8px 8px 20px;
        background-color: var(--card-color);
        border-radius: .5rem;
        transition: opacity 0.3s ease;
        cursor: pointer;
        user-select: none;
        box-shadow: rgba(0, 0, 0, 0.24) 0px 3px 8px;
        .details {
          display: flex;
          flex-direction: column;
          justify-content: center;
          align-items: center;
          height: 100%;


        }

        .icon {
          margin-left: auto;
          margin-right: 20px;
        }

        &:hover {
          opacity: 0.8;
        }
      }
    }

    .account-detail {
      margin-left: auto;
      margin-right: auto;
      height: 240px;
      width: 350px;
      padding: 32px;
      display: flex;
      flex-direction: column;
      justify-content: space-around;
      border-radius: .5rem;
      background-color: var(--card-color);
      box-shadow: rgba(0, 0, 0, 0.24) 0px 3px 8px;
      gap: 16px;
      .part {
        display: flex;
        flex-direction: row;
        justify-content: flex-start;
        gap: 32px;
        align-items: center;
        &.space {
          margin: 8px;
        }
        .details {
          &.box {
            display: flex;
            flex-direction: column;
          }
          &[state='error'] {
            color: var(--error-color-suppl);
          }
        }
      }
    }
  }
}

.name {
  font-size: 24px;
}
</style>