import { createRouter, createWebHashHistory } from 'vue-router';
import { useAppStore } from './stores/AppStore';

let routes = [
    {
        path: '/',
        name: 'home',
        component() {
            // @ts-ignore
            return import('./pages/index.vue');
        }
    },
    {
        path: '/login',
        name: 'login',
        component() {
            // @ts-ignore
            return import('./pages/login.vue');
        }
    },
    {
        path: '/settings',
        name: 'settings',
        component() {
            // @ts-ignore
            return import('./pages/settings.vue');
        }
    },
    {
        path: '/logout',
        name: 'logout',
        component() {
            // @ts-ignore
            return import('./pages/logout.vue');
        }
    }
]

const router = createRouter({
    history: createWebHashHistory(),
    routes
});

router.beforeEach(async (to) => {
    const publicPages = ['/login','/','/logout'];
    const authRequired = !publicPages.includes(to.path);
    const auth = useAppStore();

    if (authRequired && !auth.account) {
        return '/login';
    }
})
export default router;