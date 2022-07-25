<script setup lang="ts">
import { NButton, NIcon, NForm, NFormItem, NInput } from 'naive-ui';
import { LogInOutline } from '@vicons/ionicons5';
</script>


<template>
    <div class="login-center" :label-width="80">
        <NForm class="form">
            <NFormItem label="Email" class="item">
                <NInput placeholder="Input Email" type="text" class="input" />
            </NFormItem>
            <NFormItem label="Password" class="item">
                <NInput placeholder="Input Password" type="password" class="input" />
            </NFormItem>
        </NForm>
        <NButton size="large" tertiary type="primary" style="--n-width: 248px" round>
            <template #icon>
                <NIcon>
                    <LogInOutline />
                </NIcon>
            </template>
            Login
        </NButton>
    </div>
</template>


<style lang="scss" scoped>
.login-center {
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
    height: inherit;

    .form {
        width: 30%;
        .item {
            .input {
                text-align: left;
            }
        }
    }
}
</style>