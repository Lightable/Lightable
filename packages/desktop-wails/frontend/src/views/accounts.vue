<script setup lang="ts">
import {PhCheckCircle, PhXCircle} from '@dnlsndr/vue-phosphor-icons';
import {NAvatar} from 'naive-ui';
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
          <PhCheckCircle :size="48" weight="duotone" color="var(--success-color)" class="icon"/>
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
          <div class="details">
            <span class="name">Se</span>
          </div>
        </div>
      </div>
      <div class="account-detail">

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

       .details {
         display: flex;
         flex-direction: column;
         justify-content: center;
         align-items: center;
         height: 100%;

         .name {
           font-size: 24px;
         }
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
     margin-left: 250px;
     width: 500px;
     height: 450px;
     background-color: var(--card-color);
     border-radius: .25rem;
     padding: 8px;
   }
 }
}
</style>