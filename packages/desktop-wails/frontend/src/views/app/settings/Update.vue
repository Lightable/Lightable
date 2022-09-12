<script setup lang="ts">
import {ref, computed} from 'vue';
import {NSelect, NButton, NProgress} from 'naive-ui';
import {useAppStore} from '../../../stores/AppStore';
import {useUpdateStore} from '../../../stores/UpdateStore';
import {DownloadUpdate, Restart} from '../../../../wailsjs/go/app/App';
import {EventsOn, EventsOff} from '../../../../wailsjs/runtime/runtime';
import {useRouter} from 'vue-router';
import {debug} from "../../../composable/Logger";

const router = useRouter();
const appStore = useAppStore();
const updateStore = useUpdateStore();

const update = computed(() => updateStore.currentUpdate.data);
const downloading = computed(() => updateStore.currentUpdate.downloading);
const percent = computed(() => updateStore.currentUpdate.percentage);
const finished = computed(() => updateStore.currentUpdate.finished);

const self = computed(() => appStore.user!!);
const buildOptions = [
  {
    label: 'Canary (Latest, Buggiest)',
    value: 3,
  },
  {
    label: 'Beta (Tested, Incomplete)',
    disabled: true,
    value: 2
  },
  {
    label: 'Stable (Tested, LTS)',
    disabled: true,
    value: 1
  }
]

const buildOption = ref(3);

const startDownload = async () => {
  let currentUpdate = updateStore.currentUpdate;
  currentUpdate.downloading = true;
  debug('Settings | Update Manager', 'Starting Update', updateStore);
  EventsOn('download:total', (total: number) => {
    debug('Settings | Update Manager', `Starting update with size ${total}`)
    EventsOn('download:progress', (percent: number) => {
      currentUpdate.percentage = parseInt((percent / total * 100).toFixed());
    })
  });
  await DownloadUpdate(currentUpdate.data!!.url);
  currentUpdate.finished = true;
  console.log('Finished Update');
  Promise.all([EventsOff('download:progress'), EventsOff('download:total')]);
}
const restart = () => {
  Restart()
}
</script>

<template>
  <div class="upload-bg">
    <ul class="circles" v-if="downloading">
      <li></li>
      <li></li>
      <li></li>
      <li></li>
      <li></li>
      <li></li>
      <li></li>
      <li></li>
      <li></li>
      <li></li>
    </ul>
    <div class="body" v-if="!downloading">
      <h3 class="ns">Hey {{ self.name }} we've got an update for you...</h3>
      <div class="content ns">
        <div class="section">
          <span>Change build type</span>
          <NSelect v-model:value="buildOption" :options="buildOptions" :fallback-option="(buildOptions[0] as any)"
                   status="error"/>
        </div>
        <div class="section">
        <span class="title">{{ update?.version }} - <span class="normal">{{ update?.title }}</span><span
            class="normal tiny">?</span></span>
        </div>
        <div class="section">
          <span class="title">Release Notes</span>
          <div class="notes">
            {{ update?.notes }}
          </div>
          <div class="signature">
            <span>Published by: {{ update?.signature }}</span>
          </div>
        </div>
        <div class="section push">
          <NButton size="large" secondary type="primary" @click="startDownload">
            Download
          </NButton>
        </div>
      </div>
    </div>
    <div class="download" v-if="downloading">
      <div class="inner">
        Downloading update... (18.5MB)
        <NProgress :percentage="percent" border-radius="12px 0 12px 0" fill-border-radius="12px 0 12px 0" type="line"
                   :height="24" status="success" indicator-placement="inside"/>
        <div class="actions">
          <NButton type="info" dashed style="max-width: 80%; min-width: 350px;" :disabled="!finished" @click="restart">
            Restart
          </NButton>
        </div>
      </div>
    </div>
  </div>

</template>


<style lang="scss" scoped>
.upload-bg {
  position: relative;
  height: 100%;
  width: 100%;
}

.body, .download {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  &.download {
    height: 90%;
    width: 100%;

    .inner {
      min-width: 500px;
      max-width: 90%;

      .actions {
        display: flex;
        margin: 20px;
        justify-content: center;
        align-items: center;
      }
    }
  }

  .content {
    display: flex;
    flex-direction: column;
    width: 70%;

    .section {
      display: flex;
      flex-direction: column;
      gap: 1px;
      margin: 5px;

      .title {
        font-size: 18px;
        font-weight: bold;

        .normal {
          font-weight: normal;

          &.tiny {
            font-size: 14px;
            color: var(--text-color-3);
            margin-left: 8px;
          }
        }

      }

      .notes {
        background-color: var(--modal-color);
        padding: 8px;
        border-radius: .2rem;
        min-height: 15vh;
        max-height: 25vh;
        word-wrap: normal;
        white-space: pre-line;
      }

      .signature {
        margin: 8px;
        color: var(--text-color-3);
      }

      &.push {
        margin-top: 20px;
      }
    }
  }
}

.circles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  margin: 0;
}

.circles li {
  position: absolute;
  display: block;
  list-style: none;
  width: 20px;
  height: 20px;
  background: rgba(250, 153, 255, 0.2);
  animation: animate 25s linear infinite;
  bottom: -150px;

}

.circles li:nth-child(1) {
  left: 25%;
  width: 80px;
  height: 80px;
  animation-delay: 0s;
}


.circles li:nth-child(2) {
  left: 10%;
  width: 20px;
  height: 20px;
  animation-delay: 2s;
  animation-duration: 12s;
}

.circles li:nth-child(3) {
  left: 70%;
  width: 20px;
  height: 20px;
  animation-delay: 4s;
}

.circles li:nth-child(4) {
  left: 40%;
  width: 60px;
  height: 60px;
  animation-delay: 0s;
  animation-duration: 18s;
}

.circles li:nth-child(5) {
  left: 65%;
  width: 20px;
  height: 20px;
  animation-delay: 0s;
}

.circles li:nth-child(6) {
  left: 75%;
  width: 110px;
  height: 110px;
  animation-delay: 3s;
}

.circles li:nth-child(7) {
  left: 35%;
  width: 150px;
  height: 150px;
  animation-delay: 7s;
}

.circles li:nth-child(8) {
  left: 50%;
  width: 25px;
  height: 25px;
  animation-delay: 15s;
  animation-duration: 45s;
}

.circles li:nth-child(9) {
  left: 20%;
  width: 15px;
  height: 15px;
  animation-delay: 2s;
  animation-duration: 35s;
}

.circles li:nth-child(10) {
  left: 85%;
  width: 150px;
  height: 150px;
  animation-delay: 0s;
  animation-duration: 11s;
}


@keyframes animate {

  0% {
    transform: translateY(0) rotate(0deg);
    opacity: 1;
    border-radius: 0;
  }

  100% {
    transform: translateY(-1000px) rotate(720deg);
    opacity: 0;
    border-radius: 50%;
  }

}
</style>