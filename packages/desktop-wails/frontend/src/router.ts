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
            path: '/invite/signup',
            name: 'invitesignup',
            component() {
                return import('./views/invite/signup.vue')
            }
        },
        {
            path: '/concepts',
            name: 'soon',
            component() {
                return import('./views/concepts/ComingSoon.vue')
            }
        },
        {
            path: '/concepts/ui',
            name: 'ui',
            children: [
                {
                    path: 'message',
                    name: 'concepts-ui-message',
                    component() {
                        return import('./views/concepts/ui/component/Message.vue')
                    }
                },
                {
                    path: 'chat-input',
                    name: 'concepts-ui-chat-input',
                    component() {
                        return import('./views/concepts/ui/component/ChatInput.vue')
                    }
                }
            ],
            component() {
                return import('./views/concepts/ui/index.vue')
            }
        }
    ]
});

export default router;