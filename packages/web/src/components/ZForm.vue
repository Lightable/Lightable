<script setup lang="ts">
import { ref } from 'vue';
import { NButton, NIcon, NForm, NFormItem, NInput, FormInst, useDialog } from 'naive-ui';
import { LogInOutline } from '@vicons/ionicons5';
import { useAppStore } from '../stores/AppStore';
import { useClientStore } from '../stores/ClientStore';
import { useRouter } from 'vue-router';
let appStore = useAppStore();
let clientStore = useClientStore();
let dialog = useDialog();
let loginStatus = ref({
    loading: false,
    failed: false,
});
let formValue = ref({
    user: {
        email: '',
        password: ''
    }
})
let formRules = {
    user: {
        email: {
            required: true,
            message: 'Email is required',
            trigger: 'blur'
        },
        password: {
            required: true,
            message: 'Password is required',
            trigger: 'blur'
        }
    }
}
const formRef = ref<FormInst | null>(null);
let router = useRouter();
let formSubmit = (e: MouseEvent) => {
    e.preventDefault();
    formRef.value?.validate(async (errors) => {
        if (!errors) {
            loginStatus.value.loading = true;
            let response = await clientStore.login(formValue.value.user.email, formValue.value.user.password);
            if (response != undefined) {
                if (!response.id) {
                    
                    loginStatus.value.loading = false;
                    return
                }
                if (!response.enabled) {
                    dialog.create(
                        {
                            type: 'error',
                            title: 'Account Isn\'t enabled',
                            content: 'Your account isn\'t enabled.. Be more patient.. bastard',
                            positiveText: 'Ok, shut up'
                        }
                    );
                    router.push('/');
                    return
                }

                router.push('settings');
            } {
                loginStatus.value.failed = true;
                loginStatus.value.loading = false;
            }
        }
    })
} 
</script>


<template>
    <NElement>
        <NForm :rules="formRules" :label-width="80" :model="formValue" size="large" ref="formRef" class="login-form">
            <NFormItem label="Email" path="user.email" class="item">
                <NInput v-model:value="formValue.user.email" placeholder="Input Email" type="text" class="input" />
            </NFormItem>
            <NFormItem label="Password" path="user.password" class="item">
                <NInput v-model:value="formValue.user.password" placeholder="Input Password" type="password" class="input" />
            </NFormItem>
            <NFormItem>
                <div class="lower">
                    <NButton large :loading="loginStatus.loading" type="primary" @click="formSubmit">
                        <template #icon>
                            <NIcon>
                                <LogInOutline />
                            </NIcon>
                        </template>
                        Login
                    </NButton>
                </div>
            </NFormItem>
        </NForm>
    </NElement>
</template>


<style lang="scss">

</style>