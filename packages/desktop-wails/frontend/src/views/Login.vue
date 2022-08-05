<script setup lang="ts">
import { ref } from 'vue';
import { NButton, NIcon, NForm, NFormItem, NInput, FormInst, useMessage } from 'naive-ui';
import { LogInOutline } from '@vicons/ionicons5';
import { debug } from '../composable/Logger';
import { LoginWithEmailAndPassword } from '../../wailsjs/go/client/HttpClient';
import { SetUser } from '../../wailsjs/go/app/App';
import { useRouter } from 'vue-router';

const message = useMessage();

const router = useRouter();

const formValue = ref({
    email: '',
    password: ''
});

const formRules = {
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
};

const formRef = ref<FormInst | null>(null);
const loading = ref(false);

const onFormSubmit = async (e: MouseEvent) => {
    e.preventDefault();
    formRef.value?.validate(async (errors) => {
        if (!errors) {
            debug('Login Form', 'Form is valid');
            let formData = formValue.value;
            try {
                loading.value = true;
                let request = await LoginWithEmailAndPassword(formData.email, formData.password);
                SetUser(request.id, request);
                router.push('/concepts/soon');
            } catch (e: any) {
                loading.value = false;
                message.error(e, { duration: 5000 });
                debug('Login Request (FAIL)', e);
            }
        }
    });
}

</script>


<template>
    <div class="login-center" :label-width="80">
        <h2 class="title ns">Sign In</h2>
        <NForm class="form" :rules="formRules" :model="formValue" ref="formRef" size="large">
            <NFormItem label="Email" class="item" required size="large" path="email">
                <NInput placeholder="Input Email" type="text" class="input" v-model:value="formValue.email" :loading="loading" />
            </NFormItem>
            <NFormItem label="Password" class="item" required size="large" path="password">
                <NInput placeholder="Input Password" type="password" class="input" v-model:value="formValue.password" :loading="loading" />
            </NFormItem>
        </NForm>
        <NButton size="large" tertiary type="primary" style="--n-width: 248px" round @click="onFormSubmit" :loading="loading">
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