<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { NSteps, NStep, NButton, NInput } from 'naive-ui';
import { TestWailsInstall, TestGCCInstall, GetWorkDir, ChangeWorkDir, PNPMBuild, WailsBuild } from '../../../../wailsjs/go/client/DeveloperClient';
import { EventsOn, EventsOff } from '../../../../wailsjs/runtime/runtime';
import Convert from 'ansi-to-html';

const convert = new Convert({
    colors: {
        1: 'var(--error-color-pressed)',
        2: 'var(--success-color-pressed)',
        3: 'var(--warning-color)',
        4: 'var(--info-color-pressed)',
        5: '#c96afc',
        9: 'var(--error-color)',
        10: 'var(--success-color)'
    }
});
const verificationSteps = ref({
    current: 0,
    status: 'process' as 'process' | 'wait' | 'error' | 'finish',
    complete: false,
    dir: '',
})

const consoleLogs = ref({
    currentPostProcess: 'None' as 'Frontend' | 'Backend' | 'None',
    frontendBuild: [] as Array<string>,
    frontendBuildTime: 0,
    wailsBuild: [] as Array<string>,
    wailsBuildTime: 0,
})

const verifyDir = async () => {
    verificationSteps.value.complete = true;
}

const changeWorkDir = async () => {
    verificationSteps.value.dir = await ChangeWorkDir();
}

onMounted(async () => {
    let steps = verificationSteps.value;
    if (import.meta.env.DEV) {
        steps.current += 2;
        try {
            await TestWailsInstall();
            steps.current += 1;
            await TestGCCInstall();
            steps.current += 1;
            steps.dir = await GetWorkDir();
        } catch (e) {
            steps.status = 'error';
        }
    } else {
        steps.current = 1;
        steps.status = 'error';
    }
})


const startBuild = async () => {
    let console = consoleLogs.value;
    console.currentPostProcess = 'Frontend';
    let frontendBuilder = setInterval(() => {
        console.frontendBuildTime += 1;
    }, 1000)
    await PNPMBuild();
    clearInterval(frontendBuilder);
    console.currentPostProcess = 'Backend';
    let wailsBuilder = setInterval(() => {
        console.wailsBuildTime =+ 1
    });
    await WailsBuild();
    clearInterval(wailsBuilder);
}


EventsOn('dev:frontend|build', (e: string) => {
    console.log('PNPM build', e);
    consoleLogs.value.frontendBuild.push(e);
})
EventsOn('dev:backend|build', (e: string) => {
    console.log('Wails build', e);
    consoleLogs.value.wailsBuild.push(e);
})

onUnmounted(() => {
    Promise.all([EventsOff('dev:frontend|build'), EventsOff('dev:frontend|build')])
});
</script>

<template>
    <div class="body">
        <h2>Create Release</h2>
        <TransitionGroup name="list">
            <div class="space" v-if="!verificationSteps.complete">
                <span class="details">Release Wizard | Booting🪄</span>
                <div class="inner">
                    <NSteps vertical :current="verificationSteps.current" :status="verificationSteps.status" v-if="!verificationSteps.complete">
                        <NStep title="Checking for DEV mode" />
                        <NStep title="Verifying Wails install" />
                        <NStep title="Checking for gcc" />
                        <NStep title="Verifying Work Directory" />
                    </NSteps>
                    <div class="step" v-if="verificationSteps.current == 4">
                        <code class="dir">{{ verificationSteps.dir }}</code>
                        <NButton secondary type="warning" @click="changeWorkDir">Change Directory</NButton>
                        <NButton secondary type="success" @click="verifyDir">Verify Directory</NButton>
                    </div>
                </div>
            </div>
        <div class="space" v-if="verificationSteps.complete && consoleLogs.currentPostProcess === 'None'">
            <span class="details">Release Wizard | Create 🌍</span>
            <div class="inner">
                <div class="header">
                    <NInput placeholder="Version" class="version" />
                    <div class="divider" />
                    <NInput placeholder="Title" />
                </div>
                <div class="release-body">
                    <NInput type="textarea" class="notes" :resizable="false" />
                </div>
                <div class="footer">
                    <NButton secondary type="error">Cancel</NButton>
                    <NButton secondary type="info" @click="startBuild">Start Building</NButton>
                </div>
            </div>
        </div>
            <div class="space" v-if="consoleLogs.currentPostProcess === 'Frontend'">
                <span class="details">Release Wizard | Building Frontend 🏗️ <span class="time">{{consoleLogs.frontendBuildTime}}s</span></span>
                <div class="logs">
                    <p class="log" v-for="(item, index) in consoleLogs.frontendBuild" v-bind:key="index" v-html="convert.toHtml(item)" />
                </div>
            </div>
            <div class="space" v-if="consoleLogs.currentPostProcess === 'Backend'">
                <span class="details">Release Wizard | Building Backend 🏗️ <span class="time">{{consoleLogs.wailsBuildTime}}s</span></span>
                <div class="logs">
                    <p class="log" v-for="(item, index) in consoleLogs.wailsBuild" v-bind:key="index" v-html="convert.toHtml(item)" />
                </div>
            </div>
        </TransitionGroup>
    </div>
</template>


<style lang="scss" scoped>
.body {
    margin: 20px;
    height: 90%;
    margin-bottom: 35px;

    .space {
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        height: 80%;
        .details {
            margin: 8px;
        }
        .time {
            color: var(--text-color-3);
            font-size: 12px;
        }
        .inner {
            display: flex;
            flex-direction: column;
            align-items: center;
            background-color: var(--card-color);
            padding: 16px;
            border-radius: .5rem;
            min-width: 25vw;
            max-width: 50vw;

            .header {
                display: flex;
                flex-direction: row;
                gap: 8px;
                align-items: flex-start;
                width: 100%;

                .version {
                    width: 120px;
                }
            }

            .release-body {
                width: 100%;
                margin-top: 20px;

                .notes {
                    height: 250px;
                }
            }

            .footer {
                margin-top: 10px;
                display: flex;
                flex-direction: row;
                width: 100%;
                justify-content: space-between;
            }
        }

        .logs {
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            background-color: var(--card-color);
            padding: 16px;
            border-radius: .5rem;
            min-width: 45vw;
            max-width: 85vw;
            max-height: 50%;
            overflow-y: auto;

            p {
                all: unset;
                color: var(--text-color-3);
                font-family: "JetBrains Mono";
            }

            &::-webkit-scrollbar {
                display: none;
            }
        }
    }


}

.step {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    width: 20%;
    margin: 10px;
    gap: 10px;

    .dir {
        margin: 10px;
    }
}

.divider {
    height: 30px;
    width: 5px;
    border-radius: .5rem;
    background-color: var(--body-color);
}

.list-enter-active,
.list-leave-active {
    transition: all 0.5s ease;
}

.list-enter-from,
.list-leave-to {
    opacity: 0;
    transform: translateX(30px);
}
</style>