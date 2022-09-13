<script setup lang="ts">
import {PhCheckCircle, PhXCircle, PhDesktopTower} from '@dnlsndr/vue-phosphor-icons';
import {NAvatar} from 'naive-ui';
import {WindowSetSize, WindowSetMaxSize, WindowCenter} from '../../wailsjs/runtime/';
import { GetLocation } from '../../wailsjs/go/client/Client';
WindowSetSize(940, 450)
WindowSetMaxSize(940, 450)
WindowCenter()

console.log(GetLocation())
</script>

<template>
  <div class="margin">
    <h2>Select Your Account Below</h2>
    <div class="inner">
      <div class="accounts">
        <div class="account">
          <NAvatar :size="48" round class="avatar"/>
          <div class="details">
            <span class="name">Brys</span>
          </div>
          <PhCheckCircle :size="48" weight="duotone" color="var(--success-color-pressed)" class="icon"/>
        </div>
        <div class="account">
          <NAvatar :size="48" round class="avatar"/>
          <div class="details">
            <span class="name">Brys Alt</span>
          </div>
          <PhXCircle :size="48" weight="duotone" color="var(--error-color)" class="icon"/>
        </div>
        <div class="account">
          <NAvatar :size="48" round class="avatar"/>
          <div class="details box">
            <span class="name">Se</span>
          </div>
        </div>
      </div>
      <div class="account-detail">
        <div class="part">
          <NAvatar :size="72"/>
          <span class="name">Se</span>
        </div>
        <div class="part ns">
          <span class="detail">Account last accessed</span>
          <span>21st Of July</span>
        </div>
        <div class="part device">
          <PhDesktopTower :size="72" weight="duotone" color="var(--info-color-pressed)"/>
          <div class="details box">
            <span class="last-seen">
              Last seen at: <b class="date">1:27 AM</b>
            </span>
            <span class="last-seen">
              Near: <b>Green Bay WI</b>
            </span>
          </div>
        </div>
        <div class="part lower ns">
          <PhXCircle :size="32" weight="duotone" color="var(--error-color)" class="icon"/>
          <span class="details" state="error">
            Re-login required
          </span>
        </div>
      </div>
    </div>
  </div>
</template>


<style lang="scss" scoped>
.margin {
  margin: 20px;

  .inner {
    display: flex;
    flex-direction: row;

    .accounts {
      display: flex;
      flex-direction: column;
      gap: 16px;

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
      width: 350px;
      border-radius: .25rem;
      padding: 8px;
      display: flex;
      height: 250px;
      flex-direction: column;
      gap: 16px;
      .part {
        display: flex;
        flex-direction: row;
        justify-content: flex-start;
        gap: 32px;
        align-items: center;

        &.lower {
          margin-top: auto;
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