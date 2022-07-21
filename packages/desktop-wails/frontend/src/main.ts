import { createPinia } from 'pinia';
import { createApp } from 'vue'
import App from './App.vue'
import router from './router';

createApp(App).use(router).use(createPinia()).mount('#app')
