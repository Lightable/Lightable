import { createApp } from 'vue';
// @ts-ignore
import App from './App.vue';
import { createPinia } from 'pinia';
import router from './router';
import ContextMenu from 'vue3-context-menu';
import VueTippy from 'vue-tippy';
import 'tippy.js/dist/tippy.css';
import { devtools } from './devtools';
import { ClipboardPlugin } from './plugins/Clipboard';
import { MotionPlugin } from '@vueuse/motion';
import '@vueform/toggle/themes/default.css'
const invokedApp = createApp(App)
    .use(router)
    .use(createPinia())
    .use(ContextMenu)
    .use(VueTippy, {
        directive: 'tippy', // => v-tippy
        component: 'tippy', // => <tippy/>
        componentSingleton: 'tippy-singleton', // => <tippy-singleton/>,
        defaultProps: {
            placement: 'auto-end',
            allowHTML: true,
        }, // => Global default options * see all props
    })
    .use(devtools)
    .use(ClipboardPlugin)
    .use(MotionPlugin);
invokedApp.mount('#app');