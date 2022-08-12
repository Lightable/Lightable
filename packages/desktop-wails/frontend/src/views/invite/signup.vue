<script lang="ts" setup>
import { onBeforeMount, ref } from 'vue';
import { useRouter } from 'vue-router';
import { NForm, NFormItem, NInput, NButton, NIcon, FormInst, useMessage } from 'naive-ui';
import { PersonAdd } from '@vicons/ionicons5';
import { useConfettiStore } from '../../stores/ConfettiStore';
import { RegisterEmail } from '../../../wailsjs/go/client/HttpClient';
import { debug } from '../../composable/Logger';
import { useAppStore } from '../../stores/AppStore';

const appStore = useAppStore();
const confetti = useConfettiStore();
const message = useMessage();
const loading = ref(false);
const formValue = ref({ email: '' });
const router = useRouter();

const formRules = {
    email: {
        required: true,
        message: 'Email is required',
        trigger: 'blur'
    }
}

const requestRef = ref(null) as any;
const formRef = ref<FormInst | null>(null);
const onFormSubmit = (e: MouseEvent) => {
    e.preventDefault();
    formRef.value?.validate(async (errors) => {
        if (!errors) {
            debug('Invite Form', 'Form is valid');
            loading.value = true;
            let data = await RegisterEmail(formValue.value.email)
            if (data.status != 0) {
                requestRef.value = JSON.parse(data.Json);
            } else {
                loading.value = false;
                message.error(data.Err);
                debug('Invite Request Error', data.Err);
                return
            }
            debug('Invite Request', requestRef.value);
            loading.value = false;
            message.success('You\'ll receive a code soon. Please be patient', {
                duration: 5000
            });
            confetti.create({
                particleCount: 200,
                ticks: 50,
                origin: { y: 0.6 },
                spread: 70
            });
            return router.push('/');
        }
    })
}

onBeforeMount(() => appStore.leftDrawer.show = false);
</script>

<template>
    <div class="signup-center">
        <h2 class="title ns">Sign up to get an early access code</h2>
        <NForm class="form" :rules="formRules" :model="formValue" ref="formRef" size="large">
            <NFormItem label="Email" class="item" path="email" :feedback="(requestRef != null) ? requestRef.message : ''" :validation-status="(requestRef != null && requestRef.code == 'REGISTER_INVALID') ? 'error' : 'success'">
                <NInput placeholder="Input Email" type="text" class="input" v-model:value="formValue.email" :loading="loading" />
            </NFormItem>
        </NForm>
        <NButton size="large" tertiary type="primary" style="--n-width: 248px" round @click="onFormSubmit" :loading="loading">
            <template #icon>
                <NIcon>
                    <PersonAdd />
                </NIcon>
            </template>
            Sign-up for an invite
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
    gap: 40px;

    .title {
        color: var(--text-color-1);
    }

    .form {
        width: 340px;

        .item {
            .input {
                text-align: left;
            }
        }
    }
}
</style>