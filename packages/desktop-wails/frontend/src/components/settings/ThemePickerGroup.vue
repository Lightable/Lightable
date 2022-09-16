<script setup lang="ts">

</script>

<template>
  <div class="theme-group">
    <div class="theme-picker-group">
      <div class="upper">
        <span class="title">Primary</span>
        <div class="color-picker">
          <span class="hex">#fff</span>
          <div class="underline-color"/>
        </div>
      </div>
      <div class="color">
        <span>Hover <span class="hex">#fff</span></span>
        <div class="color-picker">
          <input type="color"/>
          <div class="underline-color"/>
        </div>
      </div>
    </div>
  </div>
</template>


<style lang="scss" scoped>
.theme-picker-group {
  display: flex;
  flex-direction: column;
  padding: 8px;
  width: 100px;
  height: 100px;
  border-radius: .25rem;
  align-items: center;

.upper {
  padding: 12px;
  width: fit-content;
  border-radius: .25rem;
  display: flex;
  flex-direction: row;
  gap: 8px;
}
}

.color {
  display: flex;
  flex-direction: column;
}

.color-picker {
  width: 100%;

.underline-color {
  height: 4px;
  width: 100%;
  background-color: #fff;
  border-radius: .15rem;
}
}
</style>