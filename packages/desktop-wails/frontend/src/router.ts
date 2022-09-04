import { createRouter, createWebHashHistory } from 'vue-router';
import { useLoadingBar } from 'naive-ui';
import { OnRouteChange } from '../wailsjs/go/app/App';
import { WindowSetTitle } from '../wailsjs/runtime/runtime';
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
                },
                {
                    path: 'search',
                    name: 'concepts-ui-search',
                    component() {
                        return import('./views/concepts/ui/component/Search.vue');
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
                    path: 'friends',
                    name: 'app-friends',
                    children: [
                        {
                            path: '',
                            name: 'friends-home',
                            component() {
                                return import('./views/app/channels/@me/friends/all.vue')
                            }
                        },
                        {
                            path: '/pending',
                            name: 'friends-pending',
                            component() {
                                return import('./views/app/channels/@me/friends/pending.vue')
                            }
                        },
                        {
                            path: '/requests',
                            name: 'friends-add',
                            component() {
                                return import('./views/app/channels/@me/friends/addFriend.vue')
                            }
                        }
                    ],
                    component() {
                        return import('./views/app/channels/@me/friends/friends.vue')
                    }
                },
                {
                    path: 'settings',
                    name: 'app-settings',
                    children: [
                        {
                            path: '',
                            name: 'settings-profile',
                            component() {
                                return import('./views/app/settings/Profile.vue')
                            }
                        },
                        {
                            path: 'debug',
                            name: 'settings-debug',
                            component() {
                                return import('./views/app/settings/Debug.vue')
                            }
                        },
                        {
                            path: 'update',
                            name: 'settings-update',
                            component() {
                                return import('./views/app/settings/Update.vue')
                            }
                        }
                    ],
                    component() {
                        return import('./views/app/settings/Settings.vue')
                    }
                },
                {
                    path: 'admin',
                    name: 'app-admin',
                    children: [
                        {
                            path: 'release',
                            name: 'admin-release',
                            component() {
                                return import('./views/app/admin/AdminRelease.vue');
                            }
                        }
                    ],
                    component() {
                        return import('./views/app/admin/Admin.vue');
                    }
                },
                {
                    path: 'channel/dm/:id',
                    name: 'channel-dm',
                    component() {
                        return import('./views/app/channels/dm.vue')
                    }
                }
            ],
            component() {
                return import('./views/app/AppMain.vue');
            }
        }
    ]
});


  
export default router;