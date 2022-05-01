<template>
  <div class="theme-card">
    <span class="title">Select A Theme</span>
    <div class="theme-selector">
      <div class="color" @click="$refs.picker.click()" type="button" style="background-color: var(--ac)">
        <input type="color" class="picker" ref="picker" @input="e => changeAccentColor(e, 'ac')"/>
      </div>
      <CustomSelect
        :options="['dark', 'fruit', 'liberty', 'moon', 'new', 'ocean']"
        :default="theme"
        class="theme-select"
        @input="(selected) => setTheme(selected as string)"
      />
    </div>
    <div class="theme-customizer">
      <span class="title">Customize Theme</span>
      <div class="theme-range-pick">
        <div class="color" @click="$refs.bgpicker.click()" type="button"  style="background-color: var(--bg)">
          <input
            type="color"
            class="picker"
            ref="bgpicker"
            @input="e => changeAccentColor(e, 'bg')"
          />
        </div>
        <span class="theme-color-name">Background</span>
      </div>
    </div>
     <div class="theme-range-pick">
        <div class="color" @click="$refs.fgpicker.click()" type="button"  style="background-color: var(--fg)">
          <input
            type="color"
            class="picker"
            ref="fgpicker"
            @input="e => changeAccentColor(e, 'fg')"
          />
        </div>
        <span class="theme-color-name">Foreground</span>
      </div>
       <div class="theme-range-pick">
        <div class="color" @click="$refs.textpicker.click()" type="button"  style="background-color: var(--text)">
          <input
            type="color"
            class="picker"
            ref="textpicker"
            @input="e => changeAccentColor(e, 'text')"
          />
        </div>
        <span class="theme-color-name">Text</span>
      </div>
       <div class="theme-range-pick">
        <div class="color" @click="$refs.sbpicker.click()" type="button"  style="background-color: var(--sb)">
          <input
            type="color"
            class="picker"
            ref="sbpicker"
            @input="e => changeAccentColor(e, 'sb')"
          />
        </div>
        <span class="theme-color-name">Selection Background</span>
      </div>
       <div class="theme-range-pick">
        <div class="color" @click="$refs.sfpicker.click()" type="button"  style="background-color: var(--sf)">
          <input
            type="color"
            class="picker"
            ref="sfpicker"
            @input="e => changeAccentColor(e, 'sf')"
          />
        </div>
        <span class="theme-color-name">Selection Foreground</span>
      </div>
       <div class="theme-range-pick">
        <div class="color" @click="$refs.scpicker.click()" type="button"  style="background-color: var(--sc-bg)">
          <input
            type="color"
            class="picker"
            ref="scpicker"
            @input="e => changeAccentColor(e, 'sc-bg')"
          />
        </div>
        <span class="theme-color-name">Secondary Background</span>
      </div>
       <div class="theme-range-pick">
        <div class="color" @click="$refs.highlightpicker.click()" type="button"  style="background-color: var(--highlight)">
          <input
            type="color"
            class="picker"
            ref="highlightpicker"
            @input="e => changeAccentColor(e, 'highlight')"
          />
        </div>
        <span class="theme-color-name">Highlight</span>
      </div>
       <div class="theme-range-pick">
        <div class="color" @click="$refs.notificationpicker.click()" type="button"  style="background-color: var(--notification)">
          <input
            type="color"
            class="picker"
            ref="notificationpicker"
            @input="e => changeAccentColor(e, 'notification')"
          />
        </div>
        <span class="theme-color-name">Notification</span>
      </div>
        <div class="theme-range-pick">
        <div class="color" @click="$refs.greenpicker.click()" type="button"  style="background-color: var(--green)">
          <input
            type="color"
            class="picker"
            ref="greenpicker"
            @input="e => changeAccentColor(e, 'green')"
          />
        </div>
        <span class="theme-color-name">Green</span>
      </div>
        <div class="theme-range-pick">
        <div class="color" @click="$refs.yellowpicker.click()" type="button"  style="background-color: var(--yellow)">
          <input
            type="color"
            class="picker"
            ref="yellowpicker"
            @input="e => changeAccentColor(e, 'Yellow')"
          />
        </div>
        <span class="theme-color-name">Yellow</span>
      </div>
        <div class="theme-range-pick">
        <div class="color" @click="$refs.bluepicker.click()" type="button"  style="background-color: var(--blue)">
          <input
            type="color"
            class="picker"
            ref="bluepicker"
            @input="e => changeAccentColor(e, 'blue')"
          />
        </div>
        <span class="theme-color-name">Blue</span>
      </div>
        <div class="theme-range-pick">
        <div class="color" @click="$refs.redpicker.click()" type="button"  style="background-color: var(--red)">
          <input
            type="color"
            class="picker"
            ref="redpicker"
            @input="e => changeAccentColor(e, 'red')"
          />
        </div>
        <span class="theme-color-name">Red</span>
      </div>
        <div class="theme-range-pick">
        <div class="color" @click="$refs.purplepicker.click()" type="button"  style="background-color: var(--purple)">
          <input
            type="color"
            class="picker"
            ref="purplepicker"
            @input="e => changeAccentColor(e, 'purple')"
          />
        </div>
        <span class="theme-color-name">Purple</span>
      </div>
        <div class="theme-range-pick">
        <div class="color" @click="$refs.orangepicker.click()" type="button"  style="background-color: var(--orange)">
          <input
            type="color"
            class="picker"
            ref="orangepicker"
            @input="e => changeAccentColor(e, 'orange')"
          />
        </div>
        <span class="theme-color-name">Orange</span>
      </div>
        <div class="theme-range-pick">
        <div class="color" @click="$refs.cyanpicker.click()" type="button"  style="background-color: var(--cyan)">
          <input
            type="color"
            class="picker"
            ref="cyanpicker"
            @input="e => changeAccentColor(e, 'cyan')"
          />
        </div>
        <span class="theme-color-name">Cyan</span>
      </div>
       <div class="theme-range-pick">
        <div class="color" @click="$refs.graypicker.click()" type="button"  style="background-color: var(--gray)">
          <input
            type="color"
            class="picker"
            ref="graypicker"
            @input="e => changeAccentColor(e, 'gray')"
          />
        </div>
        <span class="theme-color-name">Gray</span>
      </div>
       <div class="theme-range-pick">
        <div class="color" @click="$refs.errorpicker.click()" type="button"  style="background-color: var(--error);">
          <input
            type="color"
            class="picker"
            ref="errorpicker"
            @input="e => changeAccentColor(e, 'error')"
            value="var(--ac)"
          />
        </div>
        <span class="theme-color-name">Error</span>
      </div>
  </div>
</template>

<script lang="ts">
import {defineComponent, computed} from 'vue';
import CustomSelect from '@/components/global/CustomSelect.vue';
import {SettingsStore} from '@/stores/SettingsStore';
export default defineComponent({
  name: 'ThemeCard',
  components: {CustomSelect},
  setup() {
    let settingsStore = SettingsStore();
    return {
      setTheme: settingsStore.setCurrentTheme,
      theme: computed(() => settingsStore.themes.current),
    };
  },
  methods: {
    changeAccentColor(el: any, variable: string) {
      let srcElValue = el.srcElement.value;
      var r = document.querySelector(':root');
      r.style.setProperty(`--${variable}`, srcElValue);
    },
  },
});
</script>

<style lang="scss" scoped>
.theme-card {
  display: flex;
  justify-content: center;
  flex-direction: column;
  .title {
    color: var(--purple);
  }

  .theme-selector {
    display: flex;
    flex-direction: row;
    justify-content: left;
    align-items: center;
    padding: 5px;
    .theme-select {
      width: 80%;
      margin-left: 10px;
    }
  }
  .color {
    height: 32px;
    width: 32px;
    border-radius: 50%;
    .picker {
      display: none;
    }
  }
}
.theme-range-pick {
    display: flex;
    flex-direction: row;
    align-items: center;
    padding: 8px;
    .theme-color-name {
        margin-left: auto;
        margin-right: 20px;
        color: var(--white);
    }
}
</style>
