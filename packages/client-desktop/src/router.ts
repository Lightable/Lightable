import { createRouter, createWebHashHistory } from "vue-router"

let routes = [
    {
        path: '/:pathMatch(.*)*',
        component() {
            // @ts-ignore
            return import('./layouts/404.vue');
        }
    },
    {
        path: '/',
        component() {
            // @ts-ignore
            return import('./pages/index.vue');
        }
    },
    {
        path: '/login',
        component() {
            // @ts-ignore
            return import('./pages/login/login.vue');
        }
    },
    {
        path: '/app',
        component() {
            // @ts-ignore
            return import('./pages/app/app.vue');
        },
        children: [
            {
                path: '/app/channels/@me',
                component() {
                    // @ts-ignore
                    return import('./pages/app/channels/@me/index.vue');
                }
            }
        ]
    }
]
const router = createRouter({
    history: createWebHashHistory(),
    routes
})
export default router;