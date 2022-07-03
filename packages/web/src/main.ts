import { createPinia } from 'pinia';
import { createApp } from 'vue'
import App from './App.vue'
import router from './router';
import piniaPersistState from 'pinia-plugin-persistedstate';
import { createHead } from '@vueuse/head';
const pinia = createPinia();
pinia.use(piniaPersistState);
const app = createApp(App);
app.use(createHead());
app.use(pinia);
app.use(router);
app.mount('#app');
