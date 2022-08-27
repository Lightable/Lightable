import { createPinia } from 'pinia';
import { createApp } from 'vue'
import App from './App.vue'
import router from './router';
import clickoutside from './composable/useclickoutside';
import mousemoveoutside from './composable/movemouseoutside';

const app = createApp(App).use(router).use(createPinia()).directive('click-outside', clickoutside).directive('mouse-move-outside', mousemoveoutside)


app.mount('#app')
