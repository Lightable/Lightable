<script setup lang="ts">
import { NAvatar, NImage, NImageGroup } from 'naive-ui';
import { PropType } from 'vue';
import { mocks } from '../../../wailsjs/go/models';
import ImageAttachment from './ImageAttachment.vue';
const props = defineProps({
    // @ts-ignore
    author: Object as PropType<mocks.PrivateUser | mocks.PublicUser>,
    content: String,
    attachments: Array,

    // Debug
    isPreview: Boolean
});
</script>

<template>
    <!-- Preview Debug -->
    <div class="message" v-if="isPreview">
        <div class="inner-message">
            <div class="avatar">
                <NAvatar round :size="48" src="http://192.168.50.111:8096/Users/4c03beee9f14424a97b2c2f8f1fecbfa/Images/Primary?tag=40936718fb72aa506a79c70af63e1c90&quality=90" class="message-avatar" />
            </div>
            <div class="container">
                <div class="name ns">
                    You
                </div>
                <div class="content">
                    Enjoying Lightable? Let us know at <span class="link">lightable.app/review</span> !<br>
                    Something not working how it should? Press <kbd>F11</kbd> + <kbd>.</kbd>
                </div>
            </div>

        </div>
        <div class="attachments">
            <NImageGroup>
                <NImage :height="72" src="https://www.svgbackgrounds.com/wp-content/uploads/2021/05/flat-mountains-warm-colors-bg.jpg" class="attachment"/>
                <NImage :height="72" src="https://cdn.dribbble.com/users/247458/screenshots/2503828/pattern.jpg" class="attachment"/>
                <NImage :height="72" src="https://images.vexels.com/media/users/3/148164/isolated/preview/3185943f270935da54942bad878dcefc-abstract-circular-elements-background.png" class="attachment"/>
            </NImageGroup>
        </div>
    </div>
    <!-- Message -->
    <div class="message" v-else>
        <div class="inner-message">
            <div class="avatar">
                <NAvatar round :size="48" :src="author.getAvatar()" class="message-avatar" />
            </div>
            <div class="container">
                <div class="name ns">
                    {{ author.self ? 'You' : author.name }}
                </div>
                <div class="content">
                    {{ content }}
                </div>
            </div>

        </div>
        <div class="attachments">
            <!-- To be continued -->
        </div>
    </div>
</template>

<style lang="scss" scoped>
kbd {
    background-color: var(--modal-color);
    border-radius: 3px;
    border: 1px solid var(--body-color);
    color: var(--text-color-3);
    display: inline-block;
    font-size: .85em;
    font-weight: 700;
    line-height: 1;
    padding: 2px 4px;
    white-space: nowrap;
}

.link {
    color: var(--info-color);
    cursor: pointer;
    transition: all .3s ease-in-out;

    &:hover {
        color: var(--info-color-hover);
    }
}

.message {
    max-width: 40%;
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    align-items: flex-start;

    .attachments {
        margin-left: 75px;
        margin-top: 20px;
        display: flex;
        flex-direction: row;
        gap: 16px;

        .attachment {
            transition: all 250ms ease;
            cursor: pointer;

            &:hover {
                transform: scale(1.15);
                filter: saturate(110%);
            }
        }
    }

    .inner-message {
        display: inline-flex;
        flex-direction: row;
        position: relative;
        align-items: flex-end;

        .avatar {
            margin-bottom: -9px;

            .message-avatar {
                border: rgba(255, 255, 255, 0) 2px solid;

                &:hover {
                    border: var(--info-color) 2px solid;
                }
            }
        }

        .container {
            display: flex;
            flex-direction: column;
            margin-left: 8px;
            padding-top: 16px;
            padding-left: 16px;
            padding-right: 16px;
            padding-bottom: 8px;
            background: var(--tab-color);
            border-top-left-radius: 8px;
            border-top-right-radius: 8px;
            border-bottom-right-radius: 8px;
            min-width: 250px;
            max-width: 100%;
            gap: 4px;

            .name {
                font-weight: 500;
                font-family: 'Rubik';
                font-size: 18px;
                color: var(--info-color-hover);
            }

            .content {
                font-family: "Gill Sans";
                color: var(--text-color-2);
                font-size: 16px;
                word-break: break-word;
            }
        }
    }
}
</style>