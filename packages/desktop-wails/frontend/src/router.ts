import { createRouter, createWebHashHistory } from 'vue-router';
import { OnRouteChange } from '../wailsjs/go/app/App';
import { debug } from './composable/Logger';

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
            path: '/concepts/soon',
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
                        return import('./views/concepts/ui/component/Message.vue');
                    }
                },
                {
                    path: 'chat-input',
                    name: 'concepts-ui-chat-input',
                    component() {
                        return import('./views/concepts/ui/component/ChatInput.vue');
                    }
                },
                {
                    path: 'loading',
                    name: 'concepts-ui-loading',
                    component() {
                        return import('./views/concepts/ui/component/Loading.vue');
                    }
                }
            ],
            component() {
                return import('./views/concepts/ui/index.vue');
            }
        },
        {
            path: '/app',
            name: 'app',
            children: [
                {
                    path: 'home',
                    name: 'app-home',
                    component() {
                        return import('./views/app/channels/@me/home.vue')
                    }
                },
                {
                    path: 'channel/dm/:id',
                    name: 'channel-dm',
                    component() {
                        return import('./views/app/channels/[dm].vue')
                    }
                }
            ],
            component() {
                return import('./views/app/AppMain.vue');
            }
        }
    ]
});
router.beforeEach((to, from) => {
    debug('Router', `Route changed from ${from.fullPath} to ${to.fullPath}`);
    OnRouteChange(from.fullPath, to.fullPath);
});

export default router;