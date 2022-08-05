<script setup lang="ts">
import { ref } from 'vue';
import { NButton, NIcon, NForm, NFormItem, NInput, FormInst, NModal, useMessage } from 'naive-ui';
import { debug } from '../composable/Logger';
import { LogInOutline } from '@vicons/ionicons5';
import { RegisterUser } from '../../wailsjs/go/client/HttpClient';
import { HasUser } from '../../wailsjs/go/app/App';
import { useRouter } from 'vue-router';
const message = useMessage();

const inputCodeModal = ref({
    show: false,
    code: '',
});

const router = useRouter();
const loading = ref(false);
const formValue = ref({ username: '', email: '', password: '' });
const formRules = {
    username: {
        required: true,
        message: 'Username is required',
        trigger: 'blur'
    },
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
const requestRef = ref(null) as any;
const formRef = ref<FormInst | null>(null);

const onFormSubmit = (e: MouseEvent) => {
    e.preventDefault();
    formRef.value?.validate(async (errors) => {
        if (!errors) {
            debug('Signup Form', 'Form is valid');
            inputCodeModal.value.show = true;
        }
    });
}

const onCodeSubmit = async () => {
    let formValues = formValue.value;
    let userRegister = await RegisterUser(formValues.username, formValues.email, formValues.password, inputCodeModal.value.code);
    if (userRegister.status == 0) {
        message.error(userRegister.Err);
        return
    } else {
        let json = JSON.parse(userRegister.Json);
        if (userRegister.status == 403) {
            message.error(json.bad);
            return
        } else if (userRegister.status == 201) {
            HasUser(true)
            router.push('/');
        }
    }
}

</script>


<template>
    <NModal v-model:show="inputCodeModal.show" preset="dialog">
        <template #header>
            Input Invite Code
        </template>
        <div class="content" :style="{ display: 'flex', flexDirection: 'column', gap: '4px', marginTop: '20px' }">
            <NInput placeholder="Invite Code" v-model:value="inputCodeModal.code" :loading="loading" status="warning" />
        </div>
        <template #action>
            <NButton quaternary type="warning" @click="onCodeSubmit" :loading="loading">
                Submit
            </NButton>
        </template>
    </NModal>
    <div class="signup-center">
        <h2 class="title ns">Sign Up</h2>
        <NForm class="form" :rules="formRules" :model="formValue" ref="formRef" size="large">
            <NFormItem label="Username" class="item" required path="username">
                <NInput placeholder="Username" type="text" class="input" v-model:value="formValue.username" />
            </NFormItem>
            <NFormItem label="Email" class="item" required path="email">
                <NInput placeholder="Input Email" type="text" class="input" v-model:value="formValue.email" />
            </NFormItem>
            <NFormItem label="Password" class="item" required path="password">
                <NInput placeholder="Input Password" type="password" class="input" v-model:value="formValue.password" />
            </NFormItem>
        </NForm>
        <NButton size="large" tertiary type="warning" style="--n-width: 248px" round @click="onFormSubmit">
            <template #icon>
                <NIcon>
                    <LogInOutline />
                </NIcon>
            </template>
            Sign-up
        </NButton>
    </div>
</template>

<style lang="scss" scoped>
.signup-center {
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