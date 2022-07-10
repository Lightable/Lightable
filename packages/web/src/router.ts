import { createRouter, createWebHistory } from 'vue-router';
import { useAppStore } from './stores/AppStore';


const router = createRouter({
    history: createWebHistory(),
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
                    path: 'profile',
                    name: 'settings-profile',
                    component() {
                        return import('./pages/settings/Profile.vue');
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
        },
        {
            path: '/profiles',
            name: 'profiles',
            component() {
                return import('./pages/profiles.vue')
            }
        },
        {
            path: '/profile/:id',
            name: 'profile',
            component() {
                return import('./pages/profile/index.vue')
            }
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