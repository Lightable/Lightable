import { createRouter, createWebHashHistory } from 'vue-router';

const router = createRouter({
    history: createWebHashHistory(),
    routes: [
        {
            path: '/login',
            name: 'login',
            component() {
                return import('./views/login.vue');
            }
        },
        {
            path: '/signup',
            name: 'signup',
            component() {
                return import('./views/signup.vue');
            }
        },
        {
            path: '/',
            name: 'home',
            component() {
                return import('./views/home.vue');
            }
        },
        {
            path: '/signup',
            name: 'invitesignup',
            component() {
                return import('./views/invite/signup.vue')
            }
        }
    ]
});

export default router;