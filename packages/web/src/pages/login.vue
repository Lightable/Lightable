<script setup lang="ts">
import ZForm from '../components/ZForm.vue';
</script>


<template>
    <div class="login-page">
        <div class="form">
            <h2>Log In To Your Account</h2>
            <ZForm />
        </div>
    </div>
</template>


<style lang="scss" scoped>
.login-page {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    height: calc(100vh - 80px);

    .form {
        width: 40%;
    }
}
</style>