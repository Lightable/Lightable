import { createRouter, createWebHashHistory } from 'vue-router';

const router = createRouter({
    history: createWebHashHistory(),
    routes: [
        {
            path: '/login',
            name: 'login',
            component() {
                return import('./views/Login.vue');
            }
        }
    ]
});

export default router;