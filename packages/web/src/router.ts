import { createRouter, createWebHashHistory } from 'vue-router';
import { useAppStore } from './stores/AppStore';


const router = createRouter({
    history: createWebHashHistory(),
    routes: [
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
            children: [
                {
                    path: 'public',
                    name: 'public',
                    component() {
                        return import('./pages/settings/Public.vue');
                    }
                },
                {
                    path: 'admin',
                    name: 'admin',
                    component() {
                        return import('./pages/settings/Admin.vue');
                    }
                }
            ],
            component() {
                // @ts-ignore
                return import('./pages/settings/index.vue');
            }
        },
        {
            path: '/logout',
            name: 'logout',
            component() {
                // @ts-ignore
                return import('./pages/logout.vue');
            },
        }
    ]
});

router.beforeEach(async (to) => {
    const publicPages = ['/login', '/', '/logout'];
    const adminPages = ['/settings/admin'];
    const authRequired = !publicPages.includes(to.path);
    const adminRequired = adminPages.includes(to.path);
    const auth = useAppStore();

    if (authRequired && !auth.account) {
        return '/login';
    } else if (adminRequired && !auth?.account?.admin) {
        return '/settings/public';
    }
})
export default router;